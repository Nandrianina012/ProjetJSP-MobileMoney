<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="here" value="${param.active}"/>
<nav class="app-nav" aria-label="Navigation principale">
    <details class="menu-dropdown">
        <summary class="btn menu-summary btn-menu">Menu</summary>
        <div class="menu-items">
            <a class="app-nav-link ${here == 'dashboard' ? 'is-active' : ''}" href="${ctx}/">Tableau de bord</a>
            <a class="app-nav-link ${here == 'clients' ? 'is-active' : ''}" href="${ctx}/clients">Clients</a>
            <a class="app-nav-link ${here == 'frais' ? 'is-active' : ''}" href="${ctx}/frais">Frais</a>
            <a class="app-nav-link ${here == 'envois' ? 'is-active' : ''}" href="${ctx}/envois">Envoyer de l'argent</a>
            <a class="app-nav-link ${here == 'retraits' ? 'is-active' : ''}" href="${ctx}/retraits">Retirer de l'argent</a>
        </div>
    </details>
</nav>
