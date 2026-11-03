<%-- 首页：直接进入作业二「资料上传下载系统」（未登录时由拦截器转到登录页） --%>
<% response.sendRedirect(request.getContextPath() + "/filesys/"); %>
