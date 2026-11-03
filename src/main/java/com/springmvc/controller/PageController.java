package com.springmvc.controller;

import com.springmvc.entity.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * SpringMVC 页面跳转 + 携带参数 —— springmvc 模块（注解方式）
 *
 * ============================================================
 * 【知识点总览】跳转的两大类、携带数据的 6 种方式
 * ============================================================
 * 一、跳转方式（返回值字符串的写法）
 *    ① 返回 "逻辑视图名"     → 视图解析器转发到 JSP（默认，一次请求）
 *    ② "forward:/其他路径"   → 转发到另一个 Handler（request 域数据保留）
 *    ③ "redirect:/其他路径"  → 重定向（浏览器二次请求，request 域数据丢失！）
 *
 * 二、携带数据的方式
 *    ① ModelAndView  —— 返回值自带模型
 *    ② Model         —— 方法参数，最常用
 *    ③ ModelMap      —— 同 Model，底层 LinkedHashMap
 *    ④ Map           —— 同 Model（隐式模型）
 *    ⑤ @ModelAttribute —— 方法级：本类所有请求共享的公共参数
 *    ⑥ HttpSession   —— 会话级共享
 *    ⑦ RedirectAttributes —— 重定向专用（URL 参数 / Flash 属性跨请求存活）
 */
@Controller
@RequestMapping("/page")
@SessionAttributes("sessionUser")
public class PageController {

    /**
     * ⑤ @ModelAttribute 方法级用法 —— 公共参数
     * 本 Controller 的【所有】请求进入任何方法前都会先执行它
     * 页面所有跳转结果都能看到 siteName
     */
    @ModelAttribute
    public void commonParams(Model model) {
        model.addAttribute("siteName", "SpringMVC 教学演示站");
    }

    /**
     * ① ModelAndView —— 模型和视图一起返回
     */
    @GetMapping("/modelAndView")
    public ModelAndView modelAndView() { //标准的SpringMVC的跳转
        ModelAndView mv = new ModelAndView("page/show");
        mv.addObject("message", "方式① ModelAndView");
        mv.addObject("name", "zhangsan");
        mv.addObject("age", 20);
        return mv;
    }

    /**
     * ② Model —— 方法参数方式（最常用），返回逻辑视图名
     * via/nm 可选参数：用于演示 forward/redirect 后 URL 参数的存活情况
     */
    @GetMapping("/model")
    public String model(Model model,
                        @RequestParam(required = false) String nm,
                        @RequestParam(required = false) String via) {
        //使用Model参数，将数据放进request域，再转发到一个JSP页面
        //model等于request.setAttribute
        //model.addAttribute等于request.setAttribute
        model.addAttribute("message", "方式② Model（跳转来源: " + via + "）");
        model.addAttribute("name", nm);
        model.addAttribute("age", 22);
        return "page/show"; //转发到一个JSP页面
    }

    /**
     * ③ ModelMap —— 与 Model 等价 ，底层 LinkedHashMap
     * 与 Model 一样，ModelMap 也会放进 request 域
     */
    @GetMapping("/modelMap")
    public String modelMap(ModelMap modelMap) {
        modelMap.addAttribute("message", "方式③ ModelMap");
        modelMap.addAttribute("name", "lisi");
        modelMap.addAttribute("age", 21);
        return "page/show";
    }

    /**
     * ④ Map<String, Object> —— 隐式模型，同样会放进 request 域
     */
    @GetMapping("/map")
    public String map(Map<String, Object> map) {
        //使用Map参数，将数据放进request域，再转发到一个JSP页面
        //map等于request.setAttribute
        //map.put等于request.setAttribute
        map.put("message", "方式④ Map");
        map.put("name", "wangwu");
        map.put("age", 23);
        return "page/show";
    }

    /**
     * 实体 + Model：表单参数绑定到 Student，再放进模型带到页面
     */
    @GetMapping("/entity")
    public String entity(Student student, Model model) {
        model.addAttribute("message", "实体绑定进模型（GET 参数同名注入）");
        model.addAttribute("student", student);
        return "page/show";
    }

    /**
     * ⑥ HttpSession —— 会话级数据，跨多次请求存活
     */
    @GetMapping("/session")
    public String session(HttpSession session, Model model) {
        //使用HttpSession参数，将数据放进request域，再转发到一个JSP页面
        //在SpringMVC的控制器内 直接使用j2ee中的内置对象session  ,但是此方式不推荐
        session.setAttribute("loginUser", "zhangsan");
        model.addAttribute("message", "方式⑥ HttpSession（loginUser 已写入会话）");
        return "page/show";
    }



    /**
     * ② forward —— 转发到另一个 Handler
     * 一次请求内部跳转，URL 不变；?nm=... 作为 URL 参数传给目标方法
     */
    @GetMapping("/forward")
    public String forward() {
        return "forward:/annotation/page/model?nm=forwardUser&via=forward";
    }

    /**
     * ③ redirect —— 重定向
     * 浏览器发起新请求，原 request 域数据全部丢失，只能靠 URL ? 参数传递
     */
    @GetMapping("/redirect")
    public String redirect() {
        return "redirect:/annotation/page/model?nm=redirectUser&via=redirect";
    }

    /**
     * ⑦ RedirectAttributes.addFlashAttribute —— Flash 属性
     * 数据存入 session，redirect 到达目标后【自动】移入模型并从 session 清除
     * → 完美解决"重定向不能带对象"的问题（经典：保存成功提示后跳列表页）
     */
    @GetMapping("/flash")
    public String flash(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "方式⑦ Flash 属性：跨重定向存活");
        redirectAttributes.addFlashAttribute("student",
                new Student(1, "flashStu", 20, "CS", 95.0));
        redirectAttributes.addAttribute("nm", "urlParam");
        return "redirect:/annotation/page/show";
    }

    /**
     * 目标页：仅返回视图，展示各方式放入的数据
     * （Flash 属性到达后自动进入 request 域，页面直接 ${} 取）
     */
    @GetMapping("/show")
    public String show() {
        return "page/show";
    }
}