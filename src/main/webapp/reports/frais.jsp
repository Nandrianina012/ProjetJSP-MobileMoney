<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <jsp:include page="/includes/app-head.jsp">
        <jsp:param name="title" value="Frais"/>
    </jsp:include>
</head>
<body class="app-page">
<div class="app-wrap">
    <jsp:include page="/includes/app-header.jsp">
        <jsp:param name="headerTitle" value="Frais"/>
        <jsp:param name="headerSubtitle" value="Définissez les tranches de frais d'envoi et de retrait selon les montants."/>
    </jsp:include>
    <jsp:include page="/includes/app-nav.jsp">
        <jsp:param name="active" value="frais"/>
    </jsp:include>

    <c:if test="${param.ok != null}">
        <div class="alert alert-success">${fn:escapeXml(param.msg)}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error">${fn:escapeXml(error)}</div>
    </c:if>

    <section class="section-block">
        <p class="muted" style="margin-top:0;margin-bottom:0.75rem;">
            Définissez des tranches non chevauchantes. Exemple : 0-50 000, 50 001-100 000, etc.
        </p>
        <div class="actions-row">
            <button id="toggleSendFeeFormBtn" type="button" class="btn btn-warm" onclick="toggleFeeForm('send')">
                Ajouter un nouveau frais d'envoi
            </button>
            <button id="toggleReceiveFeeFormBtn" type="button" class="btn btn-warm" onclick="toggleFeeForm('receive')">
                Ajouter un nouveau frais de retrait
            </button>
        </div>
        <div id="sendFeeFormPanel" class="form-panel">
            <h2 class="card-title">Nouvelle tranche — envoi</h2>
            <form method="post" action="${pageContext.request.contextPath}/frais">
                <input type="hidden" name="action" value="createSend">
                <div class="form-grid">
                    <div class="field">
                        <label for="m1s">Montant min</label>
                        <input id="m1s" type="number" name="montant1" required min="0">
                    </div>
                    <div class="field">
                        <label for="m2s">Montant max</label>
                        <input id="m2s" type="number" name="montant2" required min="0">
                    </div>
                    <div class="field">
                        <label for="fs">Frais (Ar)</label>
                        <input id="fs" type="number" name="frais" required min="0">
                    </div>
                    <div class="field" style="align-self:end;">
                        <button type="submit" class="btn btn-primary">Ajouter</button>
                    </div>
                </div>
            </form>
        </div>
        <div id="receiveFeeFormPanel" class="form-panel">
            <h2 class="card-title">Nouvelle tranche — retrait</h2>
            <form method="post" action="${pageContext.request.contextPath}/frais">
                <input type="hidden" name="action" value="createReceive">
                <div class="form-grid">
                    <div class="field">
                        <label for="m1r">Montant min</label>
                        <input id="m1r" type="number" name="montant1" required min="0">
                    </div>
                    <div class="field">
                        <label for="m2r">Montant max</label>
                        <input id="m2r" type="number" name="montant2" required min="0">
                    </div>
                    <div class="field">
                        <label for="fr">Frais (Ar)</label>
                        <input id="fr" type="number" name="frais" required min="0">
                    </div>
                    <div class="field" style="align-self:end;">
                        <button type="submit" class="btn btn-primary">Ajouter</button>
                    </div>
                </div>
            </form>
        </div>
    </section>

    <section class="section-block">
        <h2 class="card-title">Frais envoi</h2>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Min</th>
                    <th>Max</th>
                    <th>Frais</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${sendFees}" var="f">
                    <c:set var="fid" value="${fn:replace(fn:replace(f.id,' ','_'),'.','_')}"/>
                    <tr>
                        <td><strong>${f.id}</strong></td>
                        <td>
                            <input form="form-send-${fid}" type="number" name="montant1" value="${f.montant1}" required>
                        </td>
                        <td>
                            <input form="form-send-${fid}" type="number" name="montant2" value="${f.montant2}" required>
                        </td>
                        <td>
                            <input form="form-send-${fid}" type="number" name="frais" value="${f.frais}" required>
                        </td>
                        <td class="cell-actions">
                            <form id="form-send-${fid}" method="post" action="${pageContext.request.contextPath}/frais" style="display:inline;">
                                <input type="hidden" name="action" value="updateSend">
                                <input type="hidden" name="id" value="${f.id}">
                                <button type="submit" class="btn btn-edit">Enregistrer</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/frais" style="display:inline;" data-confirm="Confirmer la suppression de cette tranche de frais d'envoi ?">
                                <input type="hidden" name="action" value="deleteSend">
                                <input type="hidden" name="id" value="${f.id}">
                                <button type="submit" class="btn btn-danger">Supprimer</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>

    <section class="section-block">
        <h2 class="card-title">Frais retrait</h2>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Min</th>
                    <th>Max</th>
                    <th>Frais</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${receiveFees}" var="f">
                    <c:set var="fid" value="${fn:replace(fn:replace(f.id,' ','_'),'.','_')}"/>
                    <tr>
                        <td><strong>${f.id}</strong></td>
                        <td>
                            <input form="form-rec-${fid}" type="number" name="montant1" value="${f.montant1}" required>
                        </td>
                        <td>
                            <input form="form-rec-${fid}" type="number" name="montant2" value="${f.montant2}" required>
                        </td>
                        <td>
                            <input form="form-rec-${fid}" type="number" name="frais" value="${f.frais}" required>
                        </td>
                        <td class="cell-actions">
                            <form id="form-rec-${fid}" method="post" action="${pageContext.request.contextPath}/frais" style="display:inline;">
                                <input type="hidden" name="action" value="updateReceive">
                                <input type="hidden" name="id" value="${f.id}">
                                <button type="submit" class="btn btn-edit">Enregistrer</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/frais" style="display:inline;" data-confirm="Confirmer la suppression de cette tranche de frais de retrait ?">
                                <input type="hidden" name="action" value="deleteReceive">
                                <input type="hidden" name="id" value="${f.id}">
                                <button type="submit" class="btn btn-danger">Supprimer</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
</div>
<script>
    function toggleFeeForm(type) {
        var panelId = type === "send" ? "sendFeeFormPanel" : "receiveFeeFormPanel";
        var btnId = type === "send" ? "toggleSendFeeFormBtn" : "toggleReceiveFeeFormBtn";
        var openLabel = type === "send" ? "Ajouter un nouveau frais d'envoi" : "Ajouter un nouveau frais de retrait";
        var panel = document.getElementById(panelId);
        var btn = document.getElementById(btnId);
        var otherPanel = document.getElementById(type === "send" ? "receiveFeeFormPanel" : "sendFeeFormPanel");
        var otherBtn = document.getElementById(type === "send" ? "toggleReceiveFeeFormBtn" : "toggleSendFeeFormBtn");
        var otherLabel = type === "send" ? "Ajouter un nouveau frais de retrait" : "Ajouter un nouveau frais d'envoi";
        var willOpen = !panel.classList.contains("is-open");
        otherPanel.classList.remove("is-open");
        otherBtn.innerText = otherLabel;
        panel.classList.toggle("is-open", willOpen);
        btn.innerText = willOpen ? "Masquer le formulaire" : openLabel;
    }
</script>
<script src="${pageContext.request.contextPath}/js/theme.js?v=2"></script>
</body>
</html>
