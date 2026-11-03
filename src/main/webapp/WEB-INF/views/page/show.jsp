<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>参数展示页 - SpringMVC 跳转与参数携带</title>
    <style>
        table { border-collapse: collapse; width: 90%; }
        th, td { border: 1px solid #999; padding: 6px 12px; text-align: left; }
        th { background: #f0f0f0; }
        .hint { color: #666; }
    </style>
</head>
<body>

<%-- ${siteName} 来自 @ModelAttribute 公共方法，本 Controller 所有页面都可见 --%>
<h2>${siteName} —— 参数展示页</h2>
<p class="hint">提示：没被当前方式放入的数据行显示为空，正好用来对照"哪些方式放了什么"</p>

<h3>当前提示：${message}</h3>

<table>
    <tr>
        <th>数据来源</th>
        <th>EL 表达式</th>
        <th>当前值</th>
    </tr>
    <tr>
        <td>@ModelAttribute 公共参数</td>
        <td>${'${'}siteName}</td>
        <td>${siteName}</td>
    </tr>
    <tr>
        <td>模型数据（request 域）</td>
        <td>${'${'}name} / ${'${'}age}</td>
        <td>${name} / ${age}</td>
    </tr>
    <tr>
        <td>实体对象（含 Flash 到达后自动入域）</td>
        <td>${'${'}student}</td>
        <td>${student}</td>
    </tr>
    <tr>
        <td>URL 参数（redirect/forward 携带）</td>
        <td>${'${'}param.nm}</td>
        <td>${param.nm}</td>
    </tr>
    <tr>
        <td>会话数据（HttpSession）</td>
        <td>${'${'}sessionScope.loginUser}</td>
        <td>${sessionScope.loginUser}</td>
    </tr>
</table>

<hr/>
<h3>测试入口</h3>
<p>
    <a href="${pageContext.request.contextPath}/annotation/page/modelAndView">① ModelAndView</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/model">② Model</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/modelMap">③ ModelMap</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/map">④ Map</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/entity?name=表单同名&id=9&major=CS&score=88">实体绑定</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/session">⑥ HttpSession</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/forward">forward 转发</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/redirect">redirect 重定向</a> |
    <a href="${pageContext.request.contextPath}/annotation/page/flash">⑦ Flash 属性</a>
</p>

</body>
</html>
