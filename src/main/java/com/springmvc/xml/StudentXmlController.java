package com.springmvc.xml;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * SpringMVC Controller —— XML 方式（springmvc 模块）
 *
 * ==========================================
 * 【与注解方式的对比】（注解版见 com.springmvc.controller.StudentController）
 * ==========================================
 *
 * | 维度         | 注解方式                        | XML 方式（本类）                          |
 * |--------------|--------------------------------|------------------------------------------|
 * | URL 定义位置 | 类/方法上的 @RequestMapping     | XML 中 <bean name="/student/list"/>      |
 * | 实现要求     | 普通类 + 注解                   | 实现 org.springframework.web.servlet.mvc.Controller 接口 |
 * | 返回值       | @ResponseBody 直接写响应体      | ModelAndView（逻辑视图名 + 数据）         |
 * | 视图渲染     | 无（RESTful 风格）              | InternalResourceViewResolver → JSP       |
 * | HandlerMapping| RequestMappingHandlerMapping   | BeanNameUrlHandlerMapping（bean 名即 URL）|
 *
 * XML 方式三要素（对应 resources/springmvc/xml/springmvc-servlet.xml）：
 *   ① BeanNameUrlHandlerMapping —— 用 bean 的 name 当访问路径
 *   ② 本类注册为 <bean name="/student/list"/>
 *   ③ InternalResourceViewResolver —— 逻辑视图名拼接 prefix/suffix 找到 JSP
 *
 */
public class StudentXmlController implements Controller {
    //Controller 接口的方法，默认返回值为 ModelAndView : 这是SpringMVC的特有的返回值 表示的是数据和视图的组合

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        // Mock 数据（Service/Mapper 层重建后替换为数据库查询）
        List<String> students = new ArrayList<>();
        students.add("zhangsan / CS / 90.0");
        students.add("lisi / Math / 85.5");
        students.add("wangwu / CS / 78.0");

        // "studentList" 是逻辑视图名
        // → InternalResourceViewResolver 拼成 /WEB-INF/views/studentList.jsp

       // ModelAndView mv = new ModelAndView("studentList.jsp");
        //mv.addObject("students", students);

        // 第二、三个参数：把 students 集合放入模型，JSP 中用 ${students} 取出
        return new ModelAndView("studentList", "students", students);

        //没有设定视图解析器，所以返回的逻辑视图名是 /WEB-INF/views/studentList.jsp

        //此种方式的SpringMVC的请求的局限性 : 一个请求 需要对应一个Controller实现类
    }
}
