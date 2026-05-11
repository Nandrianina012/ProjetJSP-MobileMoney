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
        <jsp:param name="headerSubtitle" value="Enregistrez un envoi puis consultez l'historique des transactions effectuées."/>
    </jsp:include>
    <jsp:include page="/includes/app-nav.jsp">
        <jsp:param name="active" value="envois"/>
    </jsp:include>

    <c:if test="${param.ok != null}">
        <div class="alert alert-success">${fn:escapeXml(param.msg)}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-error">${fn:escapeXml(error)}</div>
    </c:if>

    <section class="section-block">
        <h2 class="card-title">Nouvel envoi</h2>
        <form class="stack-form" method="post" action="${pageContext.request.contextPath}/envois">
            <div class="form-grid form-grid-two-cols">
                <div class="field">
                    <label for="numEnvoyeur">Téléphone expéditeur</label>
                    <div class="phone-field">
                        <select id="numEnvoyeurCountry" class="phone-country" aria-label="Indicatif pays expéditeur">
                            <option value="+261" selected>🇲🇬 +261</option>
                            <option value="+33">🇫🇷 +33</option>
                            <option value="+1">🇺🇸 +1</option>
                            <option value="+32">🇧🇪 +32</option>
                            <option value="+242">🇨🇬 +242</option>
                            <option value="+243">🇨🇩 +243</option>
                            <option value="+225">🇨🇮 +225</option>
                            <option value="+221">🇸🇳 +221</option>
                        </select>
                        <input id="numEnvoyeur" name="numEnvoyeur" required autocomplete="tel" inputmode="numeric">
                    </div>
                </div>
                <div class="field">
                    <label for="numRecepteur">Téléphone destinataire</label>
                    <div class="phone-field">
                        <select id="numRecepteurCountry" class="phone-country" aria-label="Indicatif pays destinataire">
                            <option value="+261" selected>🇲🇬 +261</option>
                            <option value="+33">🇫🇷 +33</option>
                            <option value="+1">🇺🇸 +1</option>
                            <option value="+32">🇧🇪 +32</option>
                            <option value="+242">🇨🇬 +242</option>
                            <option value="+243">🇨🇩 +243</option>
                            <option value="+225">🇨🇮 +225</option>
                            <option value="+221">🇸🇳 +221</option>
                        </select>
                        <input id="numRecepteur" name="numRecepteur" required autocomplete="tel" inputmode="numeric">
                    </div>
                </div>
                <div class="field">
                    <label for="montant">Montant (Ar)</label>
                    <input id="montant" type="number" name="montant" required min="1" placeholder="Montant">
                </div>
                <div class="field">
                    <label for="raison">Motif <span class="muted">(optionnel)</span></label>
                    <input id="raison" name="raison" placeholder="Ex. Trosa, facture…">
                </div>
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

    <section class="section-block">
        <h2 class="card-title">Historique des envois</h2>
        <p class="muted" style="margin-top:-0.75rem;margin-bottom:1rem;">Les operations passees ne sont pas modifiables pour garantir la coherence des soldes.</p>
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Expéditeur</th>
                    <th>Destinataire</th>
                    <th>Montant (Ar)</th>
                    <th>Frais retrait payés par expéditeur</th>
                    <th>Motif</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${envois}" var="e">
                    <tr>
                        <td><fmt:formatDate value="${e.dateEnvoi}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td>${fn:escapeXml(e.numEnvoyeur)}</td>
                        <td>${fn:escapeXml(e.numRecepteur)}</td>
                        <td>${e.montant}</td>
                        <td style="text-align:center;">
                            <c:out value="${e.payerFraisRetrait ? 'Oui' : 'Non'}"/>
                        </td>
                        <td>${fn:escapeXml(e.raison)}</td>
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
<script>
    function normalizeNationalDigits(raw) {
        var digits = (raw || "").replace(/\D/g, "");
        if (digits.length === 10 && digits.charAt(0) === "0") {
            digits = digits.slice(1);
        }
        if (digits.length > 9) {
            digits = digits.slice(0, 9);
        }
        return digits;
    }

    function formatNationalPhone(raw) {
        var digits = normalizeNationalDigits(raw);
        if (!digits) return "";
        if (digits.length <= 2) return digits;
        if (digits.length <= 4) return digits.slice(0, 2) + " " + digits.slice(2);
        if (digits.length <= 7) return digits.slice(0, 2) + " " + digits.slice(2, 4) + " " + digits.slice(4);
        return digits.slice(0, 2) + " " + digits.slice(2, 4) + " " + digits.slice(4, 7) + " " + digits.slice(7);
    }

    function buildInternationalPhone(countryCode, nationalRaw) {
        var formattedNational = formatNationalPhone(nationalRaw);
        if (!formattedNational) return "";
        return countryCode + " " + formattedNational;
    }

    document.addEventListener("DOMContentLoaded", function () {
        var form = document.querySelector("form.stack-form");
        ["numEnvoyeur", "numRecepteur"].forEach(function (id) {
            var input = document.getElementById(id);
            if (!input) return;
            input.addEventListener("input", function () {
                input.value = formatNationalPhone(input.value);
            });
            input.value = formatNationalPhone(input.value);
        });

        if (form) {
            form.addEventListener("submit", function () {
                var numEnvoyeur = document.getElementById("numEnvoyeur");
                var numRecepteur = document.getElementById("numRecepteur");
                var countryEnvoyeur = document.getElementById("numEnvoyeurCountry");
                var countryRecepteur = document.getElementById("numRecepteurCountry");
                if (numEnvoyeur && countryEnvoyeur) {
                    numEnvoyeur.value = buildInternationalPhone(countryEnvoyeur.value, numEnvoyeur.value);
                }
                if (numRecepteur && countryRecepteur) {
                    numRecepteur.value = buildInternationalPhone(countryRecepteur.value, numRecepteur.value);
                }
            });
        }

        document.querySelectorAll("tbody td:nth-child(2), tbody td:nth-child(3)").forEach(function (cell) {
            var text = (cell.textContent || "").trim();
            if (!text) return;
            if (/^\+/.test(text)) return;
            var digits = text.replace(/\D/g, "");
            if (digits.startsWith("261") && digits.length >= 12) {
                cell.textContent = "+261 " + formatNationalPhone(digits.slice(3));
            } else if (digits.length === 10 && digits.charAt(0) === "0") {
                cell.textContent = "+261 " + formatNationalPhone(digits.slice(1));
            }
        });
    });
</script>
<script src="${pageContext.request.contextPath}/js/theme.js?v=2"></script>
</body>
</html>
