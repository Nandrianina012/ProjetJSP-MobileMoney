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
        <h2 class="card-title dashboard-quick-heading">Vue rapide</h2>
        <div class="dashboard-quick-scroll">
            <div class="dashboard-quick-row">
                <div class="card dashboard-quick-card">
                    <p class="muted dashboard-quick-label">Recette journalière de l'opérateur</p>
                    <p class="stat-inline dashboard-quick-value">${recetteJour} <span class="unit">Ar</span></p>
                    <p class="muted dashboard-quick-hint">Somme des frais d'envoi et de retrait (jour sélectionné)</p>
                </div>
                <div class="card dashboard-quick-card">
                    <p class="muted dashboard-quick-label">Opérations (${date})</p>
                    <p class="stat-inline dashboard-quick-value">${opsCount}</p>
                </div>
                <div class="card dashboard-quick-card">
                    <p class="muted dashboard-quick-label">Envois</p>
                    <p class="stat-inline dashboard-quick-value">${envoisCount}</p>
                    <p class="muted dashboard-quick-hint">${envoisMontant} Ar</p>
                </div>
                <div class="card dashboard-quick-card">
                    <p class="muted dashboard-quick-label">Retraits</p>
                    <p class="stat-inline dashboard-quick-value">${retraitsCount}</p>
                    <p class="muted dashboard-quick-hint">${retraitsMontant} Ar</p>
                </div>
                <div class="dashboard-quick-action">
                    <button type="button" class="btn btn-ghost dashboard-alltime-open" id="open-alltime-stats" aria-haspopup="dialog" aria-controls="stats-alltime-dialog">
                        Statistiques de tous les temps
                    </button>
                </div>
            </div>
        </div>
    </section>

    <dialog class="stats-dialog" id="stats-alltime-dialog" aria-labelledby="stats-alltime-title">
        <div class="stats-dialog-inner">
            <header class="stats-dialog-header">
                <h2 id="stats-alltime-title" class="stats-dialog-title">Statistiques de tous les temps</h2>
            </header>
            <div class="stats-dialog-scroll">
                <div class="stats-dialog-grid">
                    <div class="card stats-dialog-card">
                        <p class="muted stats-dialog-label">Recette totale de l'opérateur</p>
                        <p class="stat-inline stats-dialog-value">${recette} <span class="unit">Ar</span></p>
                        <p class="muted stats-dialog-hint">Somme des frais d'envoi et de retrait</p>
                    </div>
                    <div class="card stats-dialog-card">
                        <p class="muted stats-dialog-label">Opérations (tous les temps)</p>
                        <p class="stat-inline stats-dialog-value">${allTimeOpsCount}</p>
                        <p class="muted stats-dialog-hint stats-dialog-hint--empty" aria-hidden="true">&nbsp;</p>
                    </div>
                    <div class="card stats-dialog-card">
                        <p class="muted stats-dialog-label">Envois</p>
                        <p class="stat-inline stats-dialog-value">${allTimeEnvoisCount}</p>
                        <p class="muted stats-dialog-hint">${allTimeEnvoisMontant} Ar</p>
                    </div>
                    <div class="card stats-dialog-card">
                        <p class="muted stats-dialog-label">Retraits</p>
                        <p class="stat-inline stats-dialog-value">${allTimeRetraitsCount}</p>
                        <p class="muted stats-dialog-hint">${allTimeRetraitsMontant} Ar</p>
                    </div>
                </div>
            </div>
            <footer class="stats-dialog-footer">
                <button type="button" class="btn btn-primary" id="close-alltime-stats">Fermer</button>
            </footer>
        </div>
    </dialog>

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
<script>
(function () {
    var dlg = document.getElementById('stats-alltime-dialog');
    var openBtn = document.getElementById('open-alltime-stats');
    var closeBtn = document.getElementById('close-alltime-stats');
    if (!dlg || !openBtn || typeof dlg.showModal !== 'function') return;
    openBtn.addEventListener('click', function () { dlg.showModal(); });
    closeBtn.addEventListener('click', function () { dlg.close(); });
    dlg.addEventListener('click', function (e) {
        var r = dlg.getBoundingClientRect();
        if (e.clientX < r.left || e.clientX > r.right || e.clientY < r.top || e.clientY > r.bottom) {
            dlg.close();
        }
    });
})();
</script>
</body>
</html>
