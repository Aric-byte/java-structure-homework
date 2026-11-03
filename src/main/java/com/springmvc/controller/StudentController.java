package com.springmvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringMVC Controller 示例 —— springmvc 模块（重建中）
 *
 * ==========================================
 * 【核心知识点】
 * ==========================================
 *
 * 1. @RestController = @Controller + @ResponseBody
 *    方法返回值直接写入响应体（JSON/字符串），不再走视图解析器
 *
 * 2. @RequestMapping("/student")
 *    类级别路径前缀：本类所有方法的 URL 都以 /student 开头
 *
 * 3. 常用参数绑定注解：
 *    @RequestParam  → ?name=xxx 查询参数
 *    @PathVariable   → /detail/{id} 路径变量
 *    （实体绑定 → POST 表单/JSON，待 Service 层重建后补充）
 *
 * 4. 当前为纯内存 Mock 数据，Service/Mapper 层重建后替换为数据库查询
 */
@Controller //响应以Json的形式
//@RestController
@RequestMapping("/student") //请求的前缀
public class StudentController {  //使用注解的方式实现Controller

    /**
     * GET /student/hello —— 最简单的入门接口
     */
    @GetMapping("/hello")
   // @ResponseBody //将方法的返回值直接写入响应体（JSON/字符串），不再走视图解析器
    //将一个控制类中的方法 映射成为一个http的请求

    public String hello() {

        System.out.println("SpringMVC Controller is running");
        return "/display";
    }

    /**
     * GET /student/list?major=CS —— @RequestParam 查询参数绑定
     * major 可不传（required=false，默认 null 返回全部）
     */
    @GetMapping("/list")
    @ResponseBody
    public List<String> list(@RequestParam(required = false) String major) {
        List<String> all = new ArrayList<>();
        all.add("zhangsan / CS / 90.0");
        all.add("lisi / Math / 85.5");
        all.add("wangwu / CS / 78.0");
        if (major == null || major.isEmpty()) {
            return all;
        }
        List<String> filtered = new ArrayList<>();
        for (String s : all) {
            if (s.contains(major)) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    /**
     * GET /student/detail/{id} —— @PathVariable 路径变量绑定
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id) {
        return "student id = " + id + "（Service 层重建后返回完整学生信息）";
    }
}
