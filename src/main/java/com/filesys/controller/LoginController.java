package com.filesys.controller;

import com.filesys.entity.SysUser;
import com.filesys.interceptor.LoginInterceptor;
import com.filesys.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /*
     * 注意：redirect: 后的路径相对于【上下文】而不是 Servlet 映射，
     * 本模块映射在 /filesys/*，所以要写 redirect:/filesys/files，只写 /files 会跳到 404。
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/filesys/files";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute(LoginInterceptor.SESSION_USERNAME) != null) {
            return "redirect:/filesys/files";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(defaultValue = "") String username,
                        @RequestParam(defaultValue = "") String password,
                        HttpServletRequest request, Model model) {
        SysUser user = userService.login(username, password);
        if (user == null) {
            model.addAttribute("error", "账号或密码错误");
            model.addAttribute("username", username);
            return "login";
        }
        // 登录前的 session 作废后重建，防止会话固定攻击
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(LoginInterceptor.SESSION_USER_ID, user.getId());
        session.setAttribute(LoginInterceptor.SESSION_USERNAME, user.getUsername());
        session.setAttribute(LoginInterceptor.SESSION_REAL_NAME, user.getRealName());
        return "redirect:/filesys/files";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/filesys/login";
    }
}
