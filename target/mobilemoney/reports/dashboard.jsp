<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <jsp:include page="/includes/app-head.jsp">
        <jsp:param name="title" value="Mobile Money — Tableau de bord"/>
    </jsp:include>
</head>
<body class="app-page">
<div class="app-wrap">
    <jsp:include page="/includes/app-header.jsp">
        <jsp:param name="headerTitle" value="Mobile Money"/>
        <jsp:param name="headerSubtitle" value="Consultez les opérations du jour et suivez la recette de l'opérateur."/>
    </jsp:include>
    <jsp:include page="/includes/app-nav.jsp">
        <jsp:param name="active" value="dashboard"/>
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
    </c:if>

    <section class="section-block">
        <h2 class="card-title">Vue rapide</h2>
        <div class="form-grid" style="grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); align-items: stretch;">
            <div class="card" style="margin-bottom:0; padding:1rem;">
                <p class="muted" style="margin:0 0 0.35rem;">Recette totale de l'opérateur</p>
                <p class="stat-inline" style="margin:0;">${recette} <span class="unit">Ar</span></p>
                <p class="muted" style="margin:0.2rem 0 0;">Somme des frais d'envoi et de retrait</p>
            </div>
            <div class="card" style="margin-bottom:0; padding:1rem;">
                <p class="muted" style="margin:0 0 0.35rem;">Opérations (${date})</p>
                <p class="stat-inline" style="margin:0;">${opsCount}</p>
            </div>
            <div class="card" style="margin-bottom:0; padding:1rem;">
                <p class="muted" style="margin:0 0 0.35rem;">Envois</p>
                <p class="stat-inline" style="margin:0;">${envoisCount}</p>
                <p class="muted" style="margin:0.2rem 0 0;">${envoisMontant} Ar</p>
            </div>
            <div class="card" style="margin-bottom:0; padding:1rem;">
                <p class="muted" style="margin:0 0 0.35rem;">Retraits</p>
                <p class="stat-inline" style="margin:0;">${retraitsCount}</p>
                <p class="muted" style="margin:0.2rem 0 0;">${retraitsMontant} Ar</p>
            </div>
        </div>
    </section>

    <section class="section-block">
        <h2 class="card-title">Activité par date</h2>
        <form class="search-bar" method="get" action="${pageContext.request.contextPath}/dashboard">
            <input type="date" name="date" value="${date}" title="Date">
            <button type="submit" class="btn btn-primary">Afficher</button>
        </form>
    </section>

    <section class="section-block">
        <h2 class="card-title">Liste des opérations</h2>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Type</th>
                    <th>Détail</th>
                    <th>Montant</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${operations}" var="o">
                    <tr>
                        <td><fmt:formatDate value="${o.date}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${o.type == 'ENVOI'}">
                                    <span class="badge-type envoi">Envoi</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-type retrait">Retrait</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${o.type == 'ENVOI'}">
                                    ${o.principal} → ${o.secondaire}
                                </c:when>
                                <c:otherwise>
                                    ${o.principal}
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>${o.montant} <span class="muted">Ar</span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty operations}">
            <p class="muted" style="margin-top:1rem;">Aucune opération ce jour-là.</p>
        </c:if>
    </section>

</div>
<script src="${pageContext.request.contextPath}/js/theme.js?v=2"></script>
</body>
</html>
