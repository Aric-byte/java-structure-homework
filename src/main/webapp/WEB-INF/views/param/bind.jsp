<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <title>SpringMVC 参数绑定综合演示</title>
    <style>
        body { font-family: sans-serif; background: #fafafa; max-width: 960px; margin: 20px auto; padding: 0 16px; }
        h1 { border-left: 5px solid #369; padding-left: 10px; }
        h2 { border-bottom: 2px solid #369; padding-bottom: 4px; margin-top: 36px; }
        .form-box { background: #fff; border: 1px solid #ddd; border-radius: 6px; padding: 16px 20px; margin: 12px 0; }
        label { display: inline-block; width: 120px; font-weight: bold; margin-bottom: 8px; }
        input[type=text], input[type=number], input[type=email] { padding: 4px 8px; border: 1px solid #ccc; border-radius: 4px; width: 200px; }
        input[type=submit] { padding: 6px 20px; background: #369; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
        input[type=submit]:hover { background: #258; }
        .hint { color: #888; font-size: 12px; margin: 4px 0 12px; }
        .row { margin-bottom: 8px; }
        .indent { margin-left: 24px; }
    </style>
</head>
<body>

<h1>SpringMVC 参数绑定综合演示</h1>
<p>本页面演示 SpringMVC 从页面表单接收数据的 4 种核心方式。提交后返回的是 Controller 方法返回的纯文本结果。</p>

<!-- ================================================================ -->
<h2>1. 接收单值（基本类型 + String）</h2>
<div class="form-box">
    <p class="hint">
        表单 field name 与 Controller 方法参数名一致即可自动绑定。<br>
        基本类型建议用包装类（Integer/Double）避免参数缺失时报 400。
    </p>
    <form action="<%= ctx %>/annotation/param/single" method="post">
        <div class="row">
            <label>用户名 (String):</label>
            <input type="text" name="username" value="张三">
        </div>
        <div class="row">
            <label>年龄 (Integer):</label>
            <input type="number" name="age" value="20">
        </div>
        <div class="row">
            <label>分数 (Double):</label>
            <input type="number" step="0.1" name="score" value="95.5">
        </div>
        <div class="row">
            <label>邮箱 (可空):</label>
            <input type="email" name="email" value="">
        </div>
        <div class="row">
            <input type="submit" value="提交单值">
        </div>
    </form>
</div>

<!-- ================================================================ -->
<!-- ================================================================ -->
<h3 style="margin-top: 24px; color:#555;">1.2 接收单值（用 User 对象封装多个参数 → /param/single2）</h3>
<div class="form-box">
    <p class="hint">
        上面 single 方法是<b>每个字段用一个参数</b>接收；这里 single2 是<b>用一个 User 对象封装所有字段</b>。<br>
        两者效果等价，但对象封装更适合字段多的场景（参数列表不会太长）。<br>
        表单 field name 必须与 User 实体的属性名完全同名（<code>username / password / email</code>）。
    </p>
    <form action="<%= ctx %>/annotation/param/single2" method="post">
        <div class="row">
            <label>ID (Integer):</label>
            <input type="number" name="user.id" value="1001">
        </div>
        <div class="row">
            <label>用户名 (username):</label>
            <input type="text" name="user.username" value="admin">
        </div>
        <div class="row">
            <label>密码 (password):</label>
            <input type="text" name="user.password" value="123456">
        </div>
        <div class="row">
            <label>邮箱 (email):</label>
            <input type="email" name="user.email" value="admin@test.com">
        </div>
        <div class="row">
            <input type="submit" value="提交 single2 (User 对象)">
        </div>
    </form>
</div>

<!-- ================================================================ -->
<h2>2. 接收对象数据（实体自动绑定）</h2>
<div class="form-box">
    <p class="hint">
        SpringMVC 自动调用 setter 注入，字段名必须与实体属性同名。<br>
        支持类型自动转换：String → Integer / Double 等。
    </p>
    <form action="<%= ctx %>/annotation/param/object" method="post">
        <div class="row">
            <label>学号 (id):</label>
            <input type="number" name="id" value="1">
        </div>
        <div class="row">
            <label>姓名 (name):</label>
            <input type="text" name="name" value="李四">
        </div>
        <div class="row">
            <label>年龄 (age):</label>
            <input type="number" name="age" value="21">
        </div>
        <div class="row">
            <label>专业 (major):</label>
            <input type="text" name="major" value="计算机">
        </div>
        <div class="row">
            <label>分数 (score):</label>
            <input type="number" step="0.1" name="score" value="88.0">
        </div>
        <div class="row">
            <input type="submit" value="提交对象">
        </div>
    </form>
</div>

<!-- ================================================================ -->
<h2>3. 接收集合（List&lt;对象&gt; 索引绑定）</h2>
<div class="form-box">
    <p class="hint">
        <b>关键：</b>表单字段名必须使用 <code>集合名[索引].属性名</code> 格式。<br>
        SpringMVC 会自动创建 List 并填充每个对象。
    </p>
    <form action="<%= ctx %>/annotation/param/collection" method="post">
        <div class="row">
            <label>地址1-省份:</label>
            <input type="text" name="addresses[0].province" value="广东">
            <label>城市:</label>
            <input type="text" name="addresses[0].city" value="深圳">
            <label>街道:</label>
            <input type="text" name="addresses[0].street" value="科技园路1号">
        </div>
        <div class="row">
            <label>地址2-省份:</label>
            <input type="text" name="addresses[1].province" value="北京">
            <label>城市:</label>
            <input type="text" name="addresses[1].city" value="北京">
            <label>街道:</label>
            <input type="text" name="addresses[1].street" value="中关村大街">
        </div>
        <div class="row">
            <input type="submit" value="提交集合">
        </div>
    </form>
</div>

<!-- ================================================================ -->
<h2>4. 接收嵌套对象（对象包含对象）</h2>
<div class="form-box">
    <p class="hint">
        <b>关键：</b>表单字段名使用 <code>对象.嵌套对象.属性</code> 格式（点号分隔层级）。<br>
        SpringMVC 会自动实例化嵌套对象并注入属性（需要无参构造器）。
    </p>
    <form action="<%= ctx %>/annotation/param/nested" method="post">
        <h4 style="margin: 10px 0 6px;">—— 学生基本信息 ——</h4>
        <div class="row">
            <label>学号 (id):</label>
            <input type="number" name="id" value="2">
        </div>
        <div class="row">
            <label>姓名 (name):</label>
            <input type="text" name="name" value="王五">
        </div>
        <div class="row">
            <label>专业 (major):</label>
            <input type="text" name="major" value="软件工程">
        </div>

        <h4 style="margin: 14px 0 6px;">—— 家庭住址（嵌套对象 address） ——</h4>
        <div class="row">
            <label>省份:</label>
            <input type="text" name="address.province" value="广东">
            <label>城市:</label>
            <input type="text" name="address.city" value="广州">
            <label>街道:</label>
            <input type="text" name="address.street" value="天河路385号">
        </div>

        <h4 style="margin: 14px 0 6px;">—— 其他地址（嵌套集合 addresses） ——</h4>
        <div class="row">
            <label>备用地址1-省份:</label>
            <input type="text" name="addresses[0].province" value="上海">
            <label>城市:</label>
            <input type="text" name="addresses[0].city" value="上海">
        </div>
        <div class="row">
            <label>备用地址2-省份:</label>
            <input type="text" name="addresses[1].province" value="浙江">
            <label>城市:</label>
            <input type="text" name="addresses[1].city" value="杭州">
        </div>

        <div class="row" style="margin-top: 14px;">
            <input type="submit" value="提交嵌套对象">
        </div>
    </form>
</div>

<p style="color:#666; margin-top: 30px;">
    <a href="<%= ctx %>/index.jsp">← 返回首页</a>
</p>

</body>
</html>