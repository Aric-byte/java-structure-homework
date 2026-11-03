<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>文件上传下载 - SpringMVC</title>
    <style>
        table { border-collapse: collapse; width: 80%; margin-top: 12px; }
        th, td { border: 1px solid #999; padding: 6px 12px; text-align: left; }
        th { background: #f0f0f0; }
        .msg { color: #070; margin: 8px 0; }
    </style>
</head>
<body>

<%-- Flash 属性：上传成功后重定向到此页自动显示（见 FileController.upload） --%>
<h2>文件上传 / 下载演示</h2>
<p class="msg">${message}</p>

<%--
    上传表单两个必须项：
    ① method="post"
    ② enctype="multipart/form-data"（缺了只能拿到文件名，拿不到文件内容）
    input 的 name="file" 对应控制器 @RequestParam("file") MultipartFile
--%>
<form action="${pageContext.request.contextPath}/annotation/file/upload"
      method="post" enctype="multipart/form-data">
    <input type="file" name="file" required>
    <button type="submit">上传</button>
    <span style="color:#666">（单文件最大 10MB，见 multipartResolver.maxUploadSize）</span>
</form>

<h3>已上传文件</h3>
<table>
    <tr><th>保存的文件名（时间戳前缀防覆盖）</th><th>操作</th></tr>
    <%
        @SuppressWarnings("unchecked")
        java.util.List<String> files =
                (java.util.List<String>) request.getAttribute("files");
        if (files == null || files.isEmpty()) {
            out.println("<tr><td colspan='2'>暂无文件，先上传一个</td></tr>");
        } else {
            for (String f : files) {
                // 文件名放 URL 参数必须编码（中文/空格），与 FileController.download 的解码逻辑对应
                String enc = java.net.URLEncoder.encode(f, "UTF-8").replace("+", "%20");
                out.println("<tr><td>" + f + "</td>"
                        + "<td><a href='" + request.getContextPath()
                        + "/annotation/file/download?name=" + enc + "'>下载</a></td></tr>");
            }
        }
    %>
</table>

<hr/>
<p style="color:#666">
    上传流程：multipart/form-data 表单 → CommonsMultipartResolver 解析 → MultipartFile 接收 → transferTo 落盘<br/>
    下载流程：Content-Disposition: attachment（attachment 触发"另存为"）→ ResponseEntity&lt;byte[]&gt; 响应体
</p>

</body>
</html>
