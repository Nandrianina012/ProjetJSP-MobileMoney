<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <jsp:include page="/includes/app-head.jsp">
        <jsp:param name="title" value="Retrait"/>
    </jsp:include>
</head>
<body class="app-page">
<div class="app-wrap">
    <jsp:include page="/includes/app-header.jsp">
        <jsp:param name="headerTitle" value="Retrait"/>
        <jsp:param name="headerSubtitle" value="Débit du compte + frais de retrait"/>
    </jsp:include>
    <jsp:include page="/includes/app-nav.jsp">
        <jsp:param name="active" value="retraits"/>
    </jsp:include>

    <c:if test="${param.ok != null}">
        <div class="alert alert-success">Retrait enregistré.</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error">${fn:escapeXml(error)}</div>
    </c:if>

    <section class="card">
        <h2 class="card-title">Nouveau retrait</h2>
        <form class="stack-form" method="post" action="${pageContext.request.contextPath}/retraits">
            <div class="field">
                <label for="numtel">Téléphone client</label>
                <input id="numtel" name="numtel" required autocomplete="tel" placeholder="032…">
            </div>
            <div class="field">
                <label for="montant">Montant (Ar)</label>
                <input id="montant" type="number" name="montant" required min="1" placeholder="Montant">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Valider le retrait</button>
            </div>
        </form>
    </section>

    <section class="card">
        <h2 class="card-title">Historique — modifier / supprimer</h2>
        <p class="muted" style="margin-top:-0.75rem;margin-bottom:1rem;">Les soldes ne sont pas recalculés automatiquement si vous modifiez une ligne ici.</p>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Client</th>
                    <th>Montant</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${retraits}" var="r">
                    <c:set var="rid" value="${fn:replace(fn:replace(r.idrecep,'-','_'),'.','_')}"/>
                    <tr>
                        <td><fmt:formatDate value="${r.daterecep}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td><input form="form-ret-${rid}" name="numtel" value="${fn:escapeXml(r.numtel)}" required></td>
                        <td><input form="form-ret-${rid}" type="number" name="montant" value="${r.montant}" required></td>
                        <td class="cell-actions">
                            <form id="form-ret-${rid}" method="post" action="${pageContext.request.contextPath}/retraits" style="display:inline;">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="idrecep" value="${fn:escapeXml(r.idrecep)}">
                                <button type="submit" class="btn btn-edit">Enregistrer</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/retraits" style="display:inline;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="idrecep" value="${fn:escapeXml(r.idrecep)}">
                                <button type="submit" class="btn btn-danger">Supprimer</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty retraits}">
            <p class="muted" style="margin-top:1rem;">Aucun retrait enregistré.</p>
        </c:if>
    </section>
</div>
<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
