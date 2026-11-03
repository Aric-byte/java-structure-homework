<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>出错了 - SpringMVC 全局异常处理</title>
</head>
<body>

<%-- 本页由 GlobalExceptionHandler 统一跳转而来，用户看到友好提示而不是 500 白页 --%>
<h2>抱歉，处理您的请求时出现异常</h2>

<table border="1" cellpadding="6">
    <tr><th>异常类型</th><td>${exClass}</td></tr>
    <tr><th>异常信息</th><td>${exMessage}</td></tr>
</table>

<hr/>
<p>
    返回
    <a href="${pageContext.request.contextPath}/annotation/file/list">文件管理页</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/modelAndView">页面跳转演示</a>
</p>

</body>
</html>
