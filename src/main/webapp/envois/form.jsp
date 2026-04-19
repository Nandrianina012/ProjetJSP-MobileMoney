<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <jsp:include page="/includes/app-head.jsp">
        <jsp:param name="title" value="Envoi"/>
    </jsp:include>
</head>
<body class="app-page">
<div class="app-wrap">
    <jsp:include page="/includes/app-header.jsp">
        <jsp:param name="headerTitle" value="Envoi d'argent"/>
        <jsp:param name="headerSubtitle" value="Débit expéditeur, crédit destinataire, frais selon barèmes"/>
    </jsp:include>
    <jsp:include page="/includes/app-nav.jsp">
        <jsp:param name="active" value="envois"/>
    </jsp:include>

    <c:if test="${param.ok != null}">
        <div class="alert alert-success">Envoi enregistré.</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error">${fn:escapeXml(error)}</div>
    </c:if>

    <section class="card">
        <h2 class="card-title">Nouvel envoi</h2>
        <form class="stack-form" method="post" action="${pageContext.request.contextPath}/envois">
            <div class="field">
                <label for="numEnvoyeur">Téléphone expéditeur</label>
                <input id="numEnvoyeur" name="numEnvoyeur" required autocomplete="tel" placeholder="032…">
            </div>
            <div class="field">
                <label for="numRecepteur">Téléphone destinataire</label>
                <input id="numRecepteur" name="numRecepteur" required autocomplete="tel" placeholder="032…">
            </div>
            <div class="field">
                <label for="montant">Montant (Ar)</label>
                <input id="montant" type="number" name="montant" required min="1" placeholder="Montant">
            </div>
            <div class="field">
                <label for="raison">Motif <span class="muted">(optionnel)</span></label>
                <input id="raison" name="raison" placeholder="Ex. Trosa, facture…">
            </div>
            <div class="check-field">
                <label>
                    <input type="checkbox" name="payerFraisRetrait" value="on">
                    L'expéditeur paie aussi les frais de retrait du destinataire
                </label>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Valider l'envoi</button>
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
                    <th>Expéditeur</th>
                    <th>Destinataire</th>
                    <th>Montant</th>
                    <th>Frais ret. payés</th>
                    <th>Motif</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${envois}" var="e">
                    <c:set var="eid" value="${fn:replace(fn:replace(e.idEnv,'-','_'),'.','_')}"/>
                    <tr>
                        <td><fmt:formatDate value="${e.dateEnvoi}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td><input form="form-env-${eid}" name="numEnvoyeur" value="${fn:escapeXml(e.numEnvoyeur)}" required></td>
                        <td><input form="form-env-${eid}" name="numRecepteur" value="${fn:escapeXml(e.numRecepteur)}" required></td>
                        <td><input form="form-env-${eid}" type="number" name="montant" value="${e.montant}" required></td>
                        <td style="text-align:center;">
                            <input form="form-env-${eid}" type="checkbox" name="payerFraisRetrait" value="on" ${e.payerFraisRetrait ? 'checked' : ''}>
                        </td>
                        <td><input form="form-env-${eid}" name="raison" value="${fn:escapeXml(e.raison)}"></td>
                        <td class="cell-actions">
                            <form id="form-env-${eid}" method="post" action="${pageContext.request.contextPath}/envois" style="display:inline;">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="idEnv" value="${fn:escapeXml(e.idEnv)}">
                                <button type="submit" class="btn btn-edit">Enregistrer</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/envois" style="display:inline;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="idEnv" value="${fn:escapeXml(e.idEnv)}">
                                <button type="submit" class="btn btn-danger">Supprimer</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty envois}">
            <p class="muted" style="margin-top:1rem;">Aucun envoi enregistré.</p>
        </c:if>
    </section>
</div>
<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
