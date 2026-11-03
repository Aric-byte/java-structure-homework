<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>登录 · 资料上传下载系统</title>
    <link rel="stylesheet" href="<c:url value='/filesys/static/filesys.css'/>">
</head>
<body>
<div class="card login">
    <h2>资料上传下载系统</h2>
    <p class="hint" style="margin-bottom:16px">学员登录后提交结业作业材料</p>

    <c:if test="${not empty error}">
        <div class="msg err"><c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="<c:url value='/filesys/login'/>">
        <label>账号
            <input type="text" name="username" value="<c:out value='${username}'/>" autofocus required>
        </label>
        <label>密码
            <input type="password" name="password" required>
        </label>
        <button class="primary" type="submit">登录</button>
    </form>
</div>
</body>
</html>
