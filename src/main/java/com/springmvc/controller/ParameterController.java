package com.springmvc.controller;

import com.mybatis03.entity.User;
import com.springmvc.entity.Address;
import com.springmvc.entity.Student;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * SpringMVC 接收参数的 N 种方式 —— springmvc 模块（注解方式）
 *
 * ============================================================
 * 【参数绑定知识点总览】
 * ============================================================
 * | # | 方式                | 典型请求                                   |
 * |---|---------------------|--------------------------------------------|
 * | 1 | @RequestParam       | /param/query?name=x&age=18                 |
 * | 2 | @PathVariable       | /param/path/1/zhangsan                     |
 * | 3 | 数组参数             | /param/array?hobby=a&hobby=b               |
 * | 4 | List 参数           | /param/list?ids=1,2,3                      |
 * | 5 | 实体自动绑定         | POST 表单 → 同名 setter 注入                |
 * | 6 | @RequestBody        | POST JSON → 实体                           |
 * | 7 | @RequestHeader      | 请求头 User-Agent                          |
 * | 8 | @CookieValue        | Cookie JSESSIONID                          |
 * | 9 | 原生 Servlet API     | HttpServletRequest / HttpSession           |
 * | 10| Map 接收             | /param/map?k1=v1&k2=v2（k1..k2..a 任意的键） |
 *
 * ============================================================
 * 【页面演示五大场景 —— 从简单到复杂】
 * ============================================================
 * | ① 路径传参       | @PathVariable      | URL 占位符 → 方法参数
 * | ② 单值表单接收   | @RequestParam      | 表单 field name → 基本类型 + String
 * | ③ 对象数据接收   | 实体自动绑定        | 表单 field name → 实体属性 setter 注入
 * | ④ 集合接收       | 实体 + 索引字段     | addresses[0].province → List<对象>
 * | ⑤ 嵌套对象接收   | 实体 + 点号字段     | address.province → 对象.嵌套对象.属性
 *
 * 【绑定规则】参数名 == 属性名 → 自动注入；类型自动转换（String → Integer/Double...）
 */
@RestController
@RequestMapping("/param")
public class ParameterController {

    /**
     * 1. @RequestParam —— 查询参数
     * - name 必传（缺了报 400）
     * - age 有 defaultValue，不传也不报错
     * - major 可空
     */
    @GetMapping("/query")
    public String query(@RequestParam String name,
                        @RequestParam(defaultValue = "18") Integer age,
                        @RequestParam(required = false) String major) {
        return "1) @RequestParam -> name=" + name + ", age=" + age + ", major=" + major;
    }

    /**
     * 【页面场景①】@PathVariable —— 路径变量（REST 风格 URL）
     * {} 占位符名字必须和方法参数名一致
     * 示例：GET /param/path/1001/zhangsan → id=1001, name=zhangsan
     */
    @GetMapping("/path/{id}/{name}")
    public String path(@PathVariable Integer id, @PathVariable String name) {
        return "① 路径传参 @PathVariable -> id=" + id + ", name=" + name;
    }

    /**
     * 3. 数组参数 —— 同名参数出现多次自动绑成数组
     */
    @GetMapping("/array")
    public String array(@RequestParam String[] hobby) {
        return "3) 数组参数 -> hobby=" + Arrays.toString(hobby);
    }

    /**
     * 4. List 参数 —— 逗号分隔自动拆分（@RequestParam 不可省，否则按实体绑定处理）
     */
    @GetMapping("/list")
    public String list(@RequestParam List<Integer> ids) {
        return "4) List 参数 -> ids=" + ids;
    }

    /**
     * 5. 实体自动绑定 —— 不写任何注解
     * POST 表单（application/x-www-form-urlencoded）字段名与属性同名 → setter 注入
     */
    @PostMapping("/form")
    public String form(Student student) {
        return "5) 实体绑定 -> " + student;
    }

    /**
     * 6. @RequestBody —— 请求体 JSON → 实体
     * 需要 Jackson 依赖（mvc:annotation-driven 自动注册 MappingJackson2HttpMessageConverter）
     * Content-Type 必须是 application/json
     */
    @PostMapping("/json")
    public String json(@RequestBody Student student) {
        return "6) @RequestBody -> " + student;
    }

    /**
     * 7. @RequestHeader + 8. @CookieValue —— 请求头 / Cookie
     * JSESSIONID 设 required=false：无 Cookie 访问不报错
     */
    @GetMapping("/header")
    public String header(@RequestHeader("User-Agent") String userAgent,
                         @CookieValue(value = "JSESSIONID", required = false) String jsessionid) {
        return "7) User-Agent=" + userAgent.substring(0, Math.min(40, userAgent.length()))
                + "\n8) JSESSIONID=" + jsessionid;
    }

    /**
     * 9. 原生 Servlet API —— 直接声明即可，SpringMVC 自动传入
     */
    @GetMapping("/servlet")
    public String servlet(HttpServletRequest request, HttpSession session) {
        String name = request.getParameter("name");
        session.setAttribute("lastUser", name);
        return "9) 原生 API -> name=" + name
                + ", session.lastUser=" + session.getAttribute("lastUser");
    }

    /**
     * 10. Map 接收 —— 一次性拿到所有请求参数（适合参数不确定的场景）
     * 注意：Map 前必须加 @RequestParam，否则会被当作 Model（数据模型）
     */
    @PostMapping("/map")
    public String map(@RequestParam Map<String, String> params) {
        return "10) Map 参数 -> " + params;
    }

    // ================================================================
    // 以下为页面演示五大场景 —— 从简单到复杂
    // ================================================================

    /**
     * 【页面场景②】接收单值 —— 基本类型 + String（页面表单提交）
     * 表单 field name 与方法参数名一致 → 自动绑定
     * 基本类型 int/long 等若参数缺失会抛 400，建议用包装类 Integer/Long 或 required=false
     */
    @PostMapping("/single")
    public String singleValue(@RequestParam String username,
                              @RequestParam Integer age,
                              @RequestParam Double score,
                              @RequestParam(required = false) String email) {
       //可以使用对象接收多个值，也可以用多个参数接收 --模型封装
        StringBuilder sb = new StringBuilder();
        sb.append("② 单值表单接收 -> ")
                .append("username=").append(username)
                .append(", age=").append(age)
                .append(", score=").append(score)
                .append(", email=").append(email);
        return sb.toString();
    }

    @PostMapping("/single2")
    public String singleValue(User user) {
        //可以使用对象接收多个值，也可以用多个参数接收 --模型封装

        return user.toString();
    }

    /**
     * 【页面场景③】接收对象数据 —— 表单字段自动绑定到实体属性
     * 无需任何注解，SpringMVC 按"字段名 == 属性名"调用 setter 注入
     * 支持类型自动转换（String → Integer/Double 等）
     */
    @PostMapping("/object")
    public String objectData(Student student) {
        return "③ 对象数据接收 -> " + student;
    }

    /**
     * 【页面场景④】接收集合 —— 表单多个对象数据绑定到 List
     * 【关键】表单字段名必须用"索引方式"：
     *   addresses[0].province=广东
     *   addresses[0].city=深圳
     *   addresses[1].province=北京
     *   addresses[1].city=北京
     * SpringMVC 会自动创建 List 并填充对象
     *
     * 集合放在实体（Student.addresses）中是最安全的绑定方式
     * （若用 JSON 数组直接传集合则用 @RequestBody List<Address>）
     */
    @PostMapping("/collection")
    public String collectionData(Student student) {
        List<Address> addresses = student.getAddresses();
        StringBuilder sb = new StringBuilder();
        sb.append("④ 集合接收 -> addresses.size=")
                .append(addresses == null ? 0 : addresses.size())
                .append(", data=").append(addresses);
        return sb.toString();
    }

    /**
     * 【页面场景⑤】接收嵌套对象 —— 对象内部包含另一个对象
     * 【关键】表单字段名使用"点号."分隔层级：
     *   name=张三&address.province=广东&address.city=深圳&address.street=科技园
     * SpringMVC 会自动 new Address() 并调用 setProvince 等 setter
     *
     * 如果嵌套对象为 null，SpringMVC 会自动实例化（需要无参构造器）
     */
    @PostMapping("/nested")
    public String nestedObject(Student student) {
        StringBuilder sb = new StringBuilder();
        sb.append("⑤ 嵌套对象接收 -> ")
                .append("name=").append(student.getName())
                .append(", major=").append(student.getMajor());
        if (student.getAddress() != null) {
            sb.append(", address.province=").append(student.getAddress().getProvince())
                    .append(", address.city=").append(student.getAddress().getCity())
                    .append(", address.street=").append(student.getAddress().getStreet());
        }
        if (student.getAddresses() != null) {
            sb.append(", addresses.size=").append(student.getAddresses().size());
        }
        return sb.toString();
    }

    /**
     * 页面跳转 —— 返回 ModelAndView 让视图解析器渲染 JSP
     * （@RestController 下返回 ModelAndView 会被正常解析为视图跳转）
     */
    @GetMapping("/bind")
    public ModelAndView bindPage() {
        return new ModelAndView("param/bind");
    }

    // 补充：日期参数绑定 @DateTimeFormat 见 AdvanceController（四大补充知识点统一演示类）
}