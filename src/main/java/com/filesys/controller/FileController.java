package com.filesys.controller;

import com.filesys.entity.FileInfo;
import com.filesys.interceptor.LoginInterceptor;
import com.filesys.service.FileAccessException;
import com.filesys.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 作业提交：列表 / 上传 / 下载 / 冻结 / 删除（逻辑删除进回收站）/ 回收站恢复与彻底删除。
 * 当前用户一律从 session 取（@SessionAttribute），不信任请求参数里的用户信息。
 */
@Controller
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /** 提交列表：只显示当前用户自己的记录，可按分类筛选 */
    @GetMapping
    public String list(@SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId,
                       @RequestParam(required = false) String category, Model model) {
        model.addAttribute("files", fileService.listMine(userId, category));
        model.addAttribute("category", category);
        model.addAttribute("allowedExt", String.join(",", fileService.getAllowedExt().stream()
                .sorted().map(e -> "." + e).toList()));
        model.addAttribute("maxSizeMb", fileService.getMaxSize() / 1024 / 1024);
        return "files";
    }

    /** 上传：支持一次多个文件，逐个校验，成功和失败分别提示 */
    @PostMapping("/upload")
    public String upload(@RequestParam(value = "files", required = false) MultipartFile[] files,
                         @SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId,
                         @SessionAttribute(LoginInterceptor.SESSION_USERNAME) String username,
                         RedirectAttributes redirect) {
        List<String> ok = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        if (files == null || files.length == 0 || (files.length == 1 && files[0].isEmpty())) {
            errors.add("请选择要上传的文件");
        } else {
            for (MultipartFile file : files) {
                try {
                    ok.add(fileService.upload(file, userId, username).getOriginName());
                } catch (FileAccessException e) {
                    errors.add(e.getMessage());
                }
            }
        }
        if (!ok.isEmpty()) {
            redirect.addFlashAttribute("success", "提交成功：" + String.join("、", ok));
        }
        if (!errors.isEmpty()) {
            redirect.addFlashAttribute("errors", errors);
        }
        return "redirect:/filesys/files";
    }

    /**
     * 下载：只接收 fileId，磁盘路径从数据库取，不拼接任何用户输入。
     * 中文文件名使用 RFC 5987：filename*=UTF-8''<百分号编码>
     * 校验失败时重定向回列表并提示，不打印异常堆栈。
     */
    @GetMapping("/{id}/download")
    public String download(@PathVariable Integer id,
                           @SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId,
                           HttpServletResponse response, RedirectAttributes redirect) throws IOException {
        FileService.DownloadTarget target;
        try {
            target = fileService.prepareDownload(id, userId);
        } catch (FileAccessException e) {
            redirect.addFlashAttribute("errors", List.of(e.getMessage()));
            return "redirect:/filesys/files";
        }
        FileInfo info = target.getInfo();
        String encoded = URLEncoder.encode(info.getOriginName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(Files.size(target.getPath()));
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        try (OutputStream out = response.getOutputStream()) {
            Files.copy(target.getPath(), out);
        }
        return null;
    }

    @PostMapping("/{id}/freeze")
    public String freeze(@PathVariable Integer id,
                         @SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId,
                         RedirectAttributes redirect) {
        return changeState(redirect, () -> "已冻结：" + fileService.setFrozen(id, userId, true).getOriginName()
                + "（冻结期间不可下载）");
    }

    @PostMapping("/{id}/unfreeze")
    public String unfreeze(@PathVariable Integer id,
                           @SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId,
                           RedirectAttributes redirect) {
        return changeState(redirect, () -> "已解冻：" + fileService.setFrozen(id, userId, false).getOriginName());
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id,
                         @SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId,
                         RedirectAttributes redirect) {
        return changeState(redirect, () -> "已移入回收站：" + fileService.delete(id, userId).getOriginName()
                + "（可在回收站恢复或彻底删除）");
    }

    /** 回收站：当前用户逻辑删除的文件 */
    @GetMapping("/trash")
    public String trash(@SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId, Model model) {
        model.addAttribute("files", fileService.listTrash(userId));
        return "trash";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Integer id,
                          @SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId,
                          RedirectAttributes redirect) {
        return changeState(redirect, "redirect:/filesys/files/trash",
                () -> "已恢复：" + fileService.restore(id, userId).getOriginName());
    }

    /** 彻底删除：数据库记录与磁盘文件一起删除，不可恢复 */
    @PostMapping("/{id}/purge")
    public String purge(@PathVariable Integer id,
                        @SessionAttribute(LoginInterceptor.SESSION_USER_ID) Integer userId,
                        RedirectAttributes redirect) {
        return changeState(redirect, "redirect:/filesys/files/trash",
                () -> "已彻底删除：" + fileService.purge(id, userId).getOriginName() + "（记录与磁盘文件均已删除）");
    }

    private String changeState(RedirectAttributes redirect, java.util.function.Supplier<String> action) {
        return changeState(redirect, "redirect:/filesys/files", action);
    }

    private String changeState(RedirectAttributes redirect, String view, java.util.function.Supplier<String> action) {
        try {
            redirect.addFlashAttribute("success", action.get());
        } catch (FileAccessException e) {
            redirect.addFlashAttribute("errors", List.of(e.getMessage()));
        }
        return view;
    }
}
