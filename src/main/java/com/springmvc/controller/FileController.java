package com.springmvc.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * SpringMVC 文件上传 / 下载 —— springmvc 模块（注解方式）
 *
 * ============================================================
 * 【上传知识点】
 * ============================================================
 * 1. 表单必须声明 enctype="multipart/form-data" + method="post"
 * 2. 前提：容器里注册了名为 multipartResolver 的 MultipartResolver
 *    （见 springmvc-servlet.xml，CommonsMultipartResolver + commons-fileupload）
 * 3. 控制器用 MultipartFile 接收，常用 API：
 *    - getOriginalFilename() 原始文件名   - getSize() 字节数
 *    - isEmpty() 是否为空                 - transferTo(File/File) 保存到磁盘
 * 4. 保存文件名加时间戳前缀，防止同名覆盖；中文文件名乱码靠 defaultEncoding=UTF-8
 *
 * 【下载知识点】
 * 1. 响应头 Content-Disposition: attachment; filename*=UTF-8''xxx → 浏览器触发"另存为"
 * 2. 中文文件名必须 URL 编码（URLEncoder + 空格 %20）
 * 3. ResponseEntity<byte[]>：响应头 + 响应体一起返回
 * 4. 安全：resolve 后 normalize 并校验必须仍在目录内，防 ../路径穿越攻击
 */
@Controller
@RequestMapping("/file")
public class FileController {

    /** 上传文件保存目录：用户主目录/springmvc-upload（与应用目录解耦，重复部署不丢文件） */
    private static final Path STORAGE =
            Paths.get(System.getProperty("user.home"), "springmvc-upload");

    static {
        try {
            Files.createDirectories(STORAGE);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * 上传页 + 已上传文件列表
     */
    @GetMapping("/list")
    public String list(Model model) throws IOException {
        List<String> names = new ArrayList<>();
        if (Files.isDirectory(STORAGE)) {
            try (Stream<Path> files = Files.list(STORAGE)) {
                files.forEach(p -> names.add(p.getFileName().toString()));
            }
        }
        model.addAttribute("files", names);
        return "file/upload";
    }

    /**
     * 上传 —— @RequestParam("file") 对应表单 <input type="file" name="file">
     * 文件较大时 MultipartFile 也可直接放方法参数列表（name 与 input 的 name 一致时可省注解）
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         RedirectAttributes redirectAttributes) throws IOException {
        // 上传后用 redirect 展示列表（flash 提示跨重定向，见 PageController 方式⑦）
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "上传失败：请先选择文件");
            return "redirect:/annotation/file/list";
        }

        String originalName = file.getOriginalFilename();
        String savedName = System.currentTimeMillis() + "_" + originalName; // 时间戳防同名覆盖
        file.transferTo(STORAGE.resolve(savedName).toFile());

        redirectAttributes.addFlashAttribute("message",
                "上传成功：" + originalName + " → " + savedName
                        + "（" + file.getSize() + " 字节）");
        return "redirect:/annotation/file/list";
    }

    /**
     * 下载 —— ?name=xxx 返回 ResponseEntity<byte[]>
     * Content-Disposition 的 attachment 通知浏览器下载而不是打开
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String name) throws IOException {
        // 防"路径穿越"：../ 把请求引到存储目录之外
        Path path = STORAGE.resolve(name).normalize();
        if (!path.startsWith(STORAGE) || !Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        byte[] body = Files.readAllBytes(path);

        // 中文文件名 URL 编码；空格要换成 %20（URLEncoder 把空格转成了 +）
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encoded);
        headers.setContentLength(body.length);

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}