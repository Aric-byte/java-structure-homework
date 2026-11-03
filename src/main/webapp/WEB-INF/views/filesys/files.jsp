<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的提交 · 资料上传下载系统</title>
    <link rel="stylesheet" href="<c:url value='/filesys/static/filesys.css'/>">
</head>
<body>
<header>
    <h1>资料上传下载系统</h1>
    <span class="user">
        <c:out value="${sessionScope.realName}"/>（<c:out value="${sessionScope.username}"/>）
        · <a href="<c:url value='/demos.jsp'/>">其他模块演示</a>
        · <a href="<c:url value='/filesys/logout'/>">退出</a>
    </span>
</header>

<main>
    <%-- 上传结果提示（flash 属性，重定向后只显示一次） --%>
    <c:if test="${not empty success}">
        <div class="msg ok"><c:out value="${success}"/></div>
    </c:if>
    <c:if test="${not empty errors}">
        <div class="msg err">
            <ul><c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach></ul>
        </div>
    </c:if>

    <div class="card">
        <h2>提交作业</h2>
        <%-- accept 只是选择文件时的便利筛选，真正的类型与大小校验在服务端 FileService --%>
        <form method="post" action="<c:url value='/filesys/files/upload'/>" enctype="multipart/form-data" class="row">
            <input type="file" name="files" multiple accept="${allowedExt}">
            <button class="primary" type="submit">上传</button>
        </form>
        <p class="hint">可一次选择多个文件。支持 ${allowedExt}，单个文件不超过 ${maxSizeMb}MB。</p>
    </div>

    <div class="card">
        <h2>我的提交</h2>
        <div class="tabs">
            <c:url var="allUrl" value="/filesys/files"/>
            <a href="${allUrl}" class="${empty category ? 'on' : ''}">全部</a>
            <c:forEach var="cat" items="${['文档', '图片', '压缩包']}">
                <c:url var="catUrl" value="/filesys/files"><c:param name="category" value="${cat}"/></c:url>
                <a href="${catUrl}" class="${category == cat ? 'on' : ''}">${cat}</a>
            </c:forEach>
        </div>

        <c:choose>
            <c:when test="${empty files}">
                <div class="empty">
                    <c:choose>
                        <c:when test="${empty category}">还没有提交任何文件，在上方选择文件上传吧</c:when>
                        <c:otherwise>「<c:out value="${category}"/>」分类下暂无文件</c:otherwise>
                    </c:choose>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th>原始文件名</th><th>分类</th><th>大小</th><th>上传时间</th>
                            <th>状态</th><th>下载次数</th><th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="f" items="${files}">
                            <tr>
                                <td class="name"><c:out value="${f.originName}"/></td>
                                <td><span class="tag">${f.category}</span></td>
                                <td class="num">${f.sizeText}</td>
                                <td class="num"><fmt:formatDate value="${f.createTime}" pattern="yyyy-MM-dd HH:mm"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${f.frozen}"><span class="tag warn">已冻结</span></c:when>
                                        <c:otherwise><span class="tag ok">正常</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="num">${f.downloadCount}</td>
                                <td>
                                    <%-- 下载只传 fileId，磁盘路径由服务端从数据库取 --%>
                                    <a href="<c:url value='/filesys/files/${f.id}/download'/>">下载</a>
                                    <c:choose>
                                        <c:when test="${f.frozen}">
                                            <form method="post" action="<c:url value='/filesys/files/${f.id}/unfreeze'/>">
                                                <button class="link" type="submit">解冻</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form method="post" action="<c:url value='/filesys/files/${f.id}/freeze'/>">
                                                <button class="link" type="submit">冻结</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                    <form method="post" action="<c:url value='/filesys/files/${f.id}/delete'/>"
                                          onsubmit="return confirm('确定删除该文件？')">
                                        <button class="link danger" type="submit">删除</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>
</body>
</html>
