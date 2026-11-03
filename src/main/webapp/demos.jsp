<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Spring + SpringMVC + MyBatis 学习项目</title>
    <style>
        body { font-family: sans-serif; background: #fafafa; max-width: 960px; margin: 20px auto; }
        table { border-collapse: collapse; width: 100%; margin: 12px 0 28px; background: #fff; }
        th, td { border: 1px solid #ccc; padding: 8px 14px; text-align: left; }
        th { background: #eef; }
        h2 { border-left: 5px solid #369; padding-left: 10px; }
    </style>
</head>
<body>

<h1>Spring + SpringMVC + MyBatis 学习项目</h1>

<h2>SpringMVC XML 方式（/xml/*）</h2>
<table>
    <tr><th>演示</th><th>链接</th></tr>
    <tr><td>Controller 接口 + JSP 视图</td>
        <td><a href="${pageContext.request.contextPath}/xml/student/list">/xml/student/list</a></td></tr>
</table>

<h2>SpringMVC 注解方式（/annotation/*）</h2>
<table>
    <tr><th>演示</th><th>链接</th></tr>
    <tr><td>REST 接口入门</td>
        <td><a href="${pageContext.request.contextPath}/annotation/student/hello">/annotation/student/hello</a>
        <td><a href="${pageContext.request.contextPath}/annotation/student/list?major=CS">/annotation/student/list?major=CS</a>
            （先访问 /annotation/page/session 登录，否则被拦截器拦截）</td></tr>
    <tr><td>页面跳转 + 携带数据</td>
        <td><a href="${pageContext.request.contextPath}/annotation/page/modelAndView">/annotation/page/modelAndView</a></td></tr>
    <tr><td>拦截器登录页</td>
        <td><a href="${pageContext.request.contextPath}/annotation/page/session">/annotation/page/session</a></td></tr>
    <tr><td>文件上传下载</td>
        <td><a href="${pageContext.request.contextPath}/annotation/file/list">/annotation/file/list</a></td></tr>
    <tr><td>补充知识点（乱码/异常/日期绑定）</td>
        <td><a href="${pageContext.request.contextPath}/annotation/extra/static-info">/annotation/extra/static-info</a>、
            <a href="${pageContext.request.contextPath}/annotation/extra/arith/1/0">异常演示</a></td></tr>
    <tr><td>参数绑定（URL 直改参数体验）</td>
        <td><a href="${pageContext.request.contextPath}/annotation/param/query?name=%E5%BC%A0%E4%B8%89&major=CS">/annotation/param/query?name=张三</a></td></tr>
    <tr><td>参数绑定综合演示（路径传参/单值/对象/集合/嵌套对象 五大场景）</td>
        <td><a href="${pageContext.request.contextPath}/annotation/param/bind">/annotation/param/bind</a></td></tr>
</table>

<h2>作业二：资料上传下载系统（com.filesys）</h2>
<table>
    <tr><th>演示</th><th>链接</th></tr>
    <tr><td>登录 → 提交作业 → 列表 → 下载（测试账号 zhangsan / lisi，密码 123456）</td>
        <td><a href="${pageContext.request.contextPath}/filesys/">/filesys/</a></td></tr>
</table>

<h2>学生管理图形界面（com.ssm）</h2>
<table>
    <tr><th>演示</th><th>链接</th></tr>
    <tr><td>增删改查、模糊查询、分页、批量删除 + 功能验证面板（REST 接口 /ssm/api/students）</td>
        <td><a href="${pageContext.request.contextPath}/students.html">/students.html</a></td></tr>
</table>

<h2>SSM 三层整合（com.ssm）</h2>
<table>
    <tr><th>演示</th><th>链接</th></tr>
    <tr><td>学生管理（DB 真实数据）</td>
        <td><a href="${pageContext.request.contextPath}/ssm/student/list">/ssm/student/list</a></td></tr>
    <tr><td>事务回滚演示入口</td>
        <td>列表页下方"事务回滚演示"链接</td></tr>
</table>

<p style="color:#666">数据库初始化脚本见项目根目录 sql/ 目录（User_db.sql、student.sql、dept_emp.sql、mp_student.sql）</p>

</body>
</html>