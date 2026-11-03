<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>回收站 · 资料上传下载系统</title>
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
    <c:if test="${not empty success}">
        <div class="msg ok"><c:out value="${success}"/></div>
    </c:if>
    <c:if test="${not empty errors}">
        <div class="msg err">
            <ul><c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach></ul>
        </div>
    </c:if>

    <div class="card">
        <div class="row" style="justify-content:space-between;margin-bottom:12px">
            <h2 style="margin:0">回收站</h2>
            <a href="<c:url value='/filesys/files'/>">← 返回我的提交</a>
        </div>
        <p class="hint" style="margin:0 0 12px">
            这里是已删除（逻辑删除，status = 0）的文件：记录和磁盘文件都还在，可以恢复。
            「彻底删除」会同时删除数据库记录和磁盘文件，<strong>不可恢复</strong>。
        </p>

        <c:choose>
            <c:when test="${empty files}">
                <div class="empty">回收站是空的</div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead>
                        <tr><th>原始文件名</th><th>分类</th><th>大小</th><th>上传时间</th><th>下载次数</th><th>操作</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="f" items="${files}">
                            <tr>
                                <td class="name"><c:out value="${f.originName}"/></td>
                                <td><span class="tag">${f.category}</span></td>
                                <td class="num">${f.sizeText}</td>
                                <td class="num"><fmt:formatDate value="${f.createTime}" pattern="yyyy-MM-dd HH:mm"/></td>
                                <td class="num">${f.downloadCount}</td>
                                <td>
                                    <form method="post" action="<c:url value='/filesys/files/${f.id}/restore'/>">
                                        <button class="link" type="submit">恢复</button>
                                    </form>
                                    <form method="post" action="<c:url value='/filesys/files/${f.id}/purge'/>"
                                          onsubmit="return confirm('彻底删除后无法恢复（数据库记录和磁盘文件都会删除），确定吗？')">
                                        <button class="link danger" type="submit">彻底删除</button>
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
