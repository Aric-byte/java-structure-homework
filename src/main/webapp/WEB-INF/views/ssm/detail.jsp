<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>学生详情 - SSM 整合</title>
</head>
<body>

<%-- ${student} 单个实体：路径变量 id → Service → Mapper selectById --%>
<h2>学生详情</h2>
<table border="1" cellpadding="6">
    <tr><th>id</th><td>${student.id}</td></tr>
    <tr><th>姓名</th><td>${student.name}</td></tr>
    <tr><th>年龄</th><td>${student.age}</td></tr>
    <tr><th>邮箱</th><td>${student.email}</td></tr>
    <tr><th>专业</th><td>${student.major}</td></tr>
    <tr><th>分数</th><td>${student.score}</td></tr>
</table>

<p>
    <a href="${pageContext.request.contextPath}/ssm/student/list">返回列表</a>
</p>

</body>
</html>
