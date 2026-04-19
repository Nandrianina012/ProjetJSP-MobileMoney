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
        <jsp:param name="headerSubtitle" value="Tableau de bord — recette opérateur et opérations du jour"/>
    </jsp:include>
    <jsp:include page="/includes/app-nav.jsp">
        <jsp:param name="active" value="dashboard"/>
    </jsp:include>

    <section class="card">
        <h2 class="card-title">Recette totale (frais envoi + retrait)</h2>
        <p class="stat-inline">${recette} <span class="unit">Ar</span></p>
    </section>

    <section class="card">
        <h2 class="card-title">Opérations à une date</h2>
        <form class="search-bar" method="get" action="${pageContext.request.contextPath}/dashboard">
            <input type="date" name="date" value="${date}" title="Date">
            <button type="submit" class="btn btn-primary">Afficher</button>
        </form>
    </section>

    <section class="card">
        <h2 class="card-title">Résultat</h2>
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

    <section class="card">
        <h2 class="card-title">Relevé PDF (un mois)</h2>
        <form class="form-grid" method="get" action="${pageContext.request.contextPath}/reports/statement-pdf" style="max-width:36rem;">
            <div class="field">
                <label for="pdf-tel">Téléphone client</label>
                <input id="pdf-tel" name="numtel" required placeholder="032…">
            </div>
            <div class="field">
                <label for="pdf-year">Année</label>
                <input id="pdf-year" type="number" name="year" required value="2026" min="2000" max="2100">
            </div>
            <div class="field">
                <label for="pdf-month">Mois</label>
                <input id="pdf-month" type="number" name="month" required min="1" max="12" placeholder="1–12">
            </div>
            <div class="field" style="align-self:end;">
                <button type="submit" class="btn btn-primary">Télécharger PDF</button>
            </div>
        </form>
    </section>
</div>
<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
