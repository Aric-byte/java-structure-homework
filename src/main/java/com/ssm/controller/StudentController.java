package com.ssm.controller;

import com.ssm.entity.Student;
import com.ssm.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 表现层 —— SSM 整合的入口：Controller 只做"参数 ↔ Service"的中转 + 视图跳转
 *
 * 完整链路（一次请求）：
 *   浏览器 → DispatcherServlet(/ssm/*) → StudentController
 *          → StudentService(@Transactional) → StudentMapper 代理 → SQL → DB
 *          → ModelAndView → studentList.jsp → 浏览器
 *
 * 【注意】本 Servlet 映射 /ssm/*，redirect 的路径必须带上 /ssm 前缀！
 *   "redirect:/ssm/student/list" —— redirect 是【上下文相对】而非 Servlet 相对，
 *   只写 /student/list 会跳到 /上下文/student/list（404）。
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** 学生列表（Ssm 整合首页） */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("students", studentService.findAll());
        return "ssm/studentList";
    }

    /** 详情页数据（演示路径变量 → Service → 单条查询） */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        return "ssm/detail";
    }

    /** 表单新增：表单参数 → 实体绑定 → Service 事务方法 → 落库 → 重定向回列表 */
    @PostMapping("/add")
    public String add(Student student, RedirectAttributes redirectAttributes) {
        studentService.addStudent(student);
        redirectAttributes.addFlashAttribute("message", "新增成功：" + student.getName());
        return "redirect:/ssm/student/list";
    }

    /**
     * 事务回滚演示：插入后 Service 抛异常 → 事务回滚 → 数据库没有这条数据。
     * Controller 捕获异常（事务已在 Service 代理处回滚）转成提示
     */
    @GetMapping("/add-fail")
    public String addFail(Student student, RedirectAttributes redirectAttributes) {
        try {
            studentService.addThenFail(student);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("message",
                    "事务已回滚（查列表确认无此数据）：" + e.getMessage());
        }
        return "redirect:/ssm/student/list";
    }

    /** 删除 → 事务方法 → 重定向回列表 */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        studentService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "已删除 id=" + id);
        return "redirect:/ssm/student/list";
    }
}
