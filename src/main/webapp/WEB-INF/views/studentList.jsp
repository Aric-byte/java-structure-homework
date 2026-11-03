<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>学生列表 - SpringMVC XML 方式</title>
</head>
<body>
<h2>学生列表（XML 方式：Controller 接口 + BeanNameUrlHandlerMapping + JSP 视图）</h2>

<%-- EL 表达式取出 ModelAndView 放入模型的数据：${students} --%>
<ul>
    <li>${students}</li>
</ul>

<hr/>
<p>
    对比注解方式（REST 风格，返回 JSON/字符串）：
    <a href="${pageContext.request.contextPath}/annotation/student/hello">/annotation/student/hello</a>
</p>
</body>
</html>
