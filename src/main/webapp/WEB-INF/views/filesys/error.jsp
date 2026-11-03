<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>出错了 · 资料上传下载系统</title>
    <link rel="stylesheet" href="<c:url value='/filesys/static/filesys.css'/>">
</head>
<body>
<div class="card login">
    <h2>出错了</h2>
    <div class="msg err"><c:out value="${message}"/></div>
    <a href="<c:url value='/filesys/files'/>">返回我的提交</a>
</div>
</body>
</html>
