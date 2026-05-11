<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header class="app-header">
    <div>
        <h1>${param.headerTitle}</h1>
        <c:if test="${not empty param.headerSubtitle}">
            <p class="para-header">${param.headerSubtitle}</p>
        </c:if>
    </div>
    <div class="header-actions">
        <button type="button" class="btn-theme" id="themeToggle" aria-label="Changer le thème"></button>
    </div>
</header>
