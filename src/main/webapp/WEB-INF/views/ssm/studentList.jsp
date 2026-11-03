<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>学生管理 - SSM 整合</title>
    <style>
        table { border-collapse: collapse; width: 90%; margin-top: 10px; }
        th, td { border: 1px solid #999; padding: 6px 12px; text-align: left; }
        th { background: #f0f0f0; }
        .msg { color: #070; margin: 8px 0; }
    </style>
</head>
<body>

<h2>学生管理（SSM：Spring + SpringMVC + MyBatis）</h2>
<p class="msg">${message}</p>

<%-- 数据来自 DB：Controller → Service(@Transactional) → Mapper 代理 → SELECT --%>
<table>
    <tr>
        <th>id</th><th>姓名</th><th>年龄</th><th>邮箱</th><th>专业</th><th>分数</th><th>操作</th>
    </tr>
    <%
        @SuppressWarnings("unchecked")
        java.util.List<com.ssm.entity.Student> students =
                (java.util.List<com.ssm.entity.Student>) request.getAttribute("students");
        if (students == null || students.isEmpty()) {
            out.println("<tr><td colspan='7'>暂无数据，用下面的表单新增一条</td></tr>");
        } else {
            String ctx = request.getContextPath();
            for (com.ssm.entity.Student s : students) {
                out.println("<tr>"
                        + "<td>" + s.getId() + "</td>"
                        + "<td><a href='" + ctx + "/ssm/student/detail/" + s.getId() + "'>"
                        + s.getName() + "</a></td>"
                        + "<td>" + s.getAge() + "</td>"
                        + "<td>" + s.getEmail() + "</td>"
                        + "<td>" + s.getMajor() + "</td>"
                        + "<td>" + s.getScore() + "</td>"
                        + "<td><a href='" + ctx + "/ssm/student/delete/" + s.getId()
                        + "' onclick=\"return confirm('确定删除？')\">删除</a></td>"
                        + "</tr>");
            }
        }
    %>
</table>

<%-- 新增表单：字段名与 Student 属性同名 → 实体自动绑定 --%>
<h3>新增学生（POST → 事务方法 insert）</h3>
<form action="${pageContext.request.contextPath}/ssm/student/add" method="post">
    姓名 <input type="text" name="name" required>
    年龄 <input type="number" name="age" value="20">
    邮箱 <input type="text" name="email" value="test@test.com">
    专业 <input type="text" name="major" value="CS">
    分数 <input type="number" step="0.5" name="score" value="80">
    <button type="submit">提交</button>
</form>

<%-- 事务回滚演示：插入会执行但最终回滚，刷新列表确认数据没有增加 --%>
<p>
    <a href="${pageContext.request.contextPath}/ssm/student/add-fail?name=回滚测试&age=20&email=rb@test.com&major=CS&score=60">
        事务回滚演示（add-fail：插入后抛异常 → 数据库不会有这条）
    </a>
</p>

</body>
</html>
