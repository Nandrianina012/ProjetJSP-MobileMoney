<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <jsp:include page="/includes/app-head.jsp">
        <jsp:param name="title" value="Clients — Mobile Money"/>
    </jsp:include>
</head>
<body class="app-page">
<div class="app-wrap">
    <jsp:include page="/includes/app-header.jsp">
        <jsp:param name="headerTitle" value="Clients"/>
        <jsp:param name="headerSubtitle" value="Gérez les clients, retrouvez un compte et mettez à jour leurs informations."/>
    </jsp:include>
    <jsp:include page="/includes/app-nav.jsp">
        <jsp:param name="active" value="clients"/>
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-error">${fn:escapeXml(error)}</div>
    </c:if>
    <c:if test="${param.ok != null}">
        <div class="alert alert-success">${fn:escapeXml(param.msg)}</div>
    </c:if>

    <section class="section-block">
        <h2 class="card-title">Recherche</h2>
        <div class="client-search-actions">
            <form class="search-bar" method="get" action="${pageContext.request.contextPath}/clients">
                <input type="text" name="q" value="${fn:escapeXml(param.q)}" placeholder="Nom ou numéro">
                <button type="submit" class="btn btn-primary">Rechercher</button>
            </form>
            <button id="toggleClientFormBtn" type="button" class="btn btn-warm" onclick="toggleClientForm()">
                Ajouter un nouveau client
            </button>
        </div>
    </section>

    <div class="section-block-panel">
        <div id="clientFormPanel" class="form-panel" style="margin-top:0.75rem;">
            <h2 class="card-title" id="clientFormTitle">Nouveau client</h2>
            <form id="clientForm" method="post" action="${pageContext.request.contextPath}/clients">
                <input type="hidden" id="clientAction" name="action" value="create">
                <input type="hidden" id="originalNumtel" name="originalNumtel" value="">
                <div class="form-grid form-grid-two-cols">
                    <div class="field">
                        <label for="numtel">Téléphone</label>
                        <div class="phone-field">
                            <select id="numtelCountry" class="phone-country" aria-label="Indicatif pays">
                                <option value="+261" selected>🇲🇬 +261</option>
                                <option value="+33">🇫🇷 +33</option>
                                <option value="+1">🇺🇸 +1</option>
                                <option value="+32">🇧🇪 +32</option>
                                <option value="+242">🇨🇬 +242</option>
                                <option value="+243">🇨🇩 +243</option>
                                <option value="+225">🇨🇮 +225</option>
                                <option value="+221">🇸🇳 +221</option>
                            </select>
                            <input id="numtel" name="numtel" required autocomplete="tel" inputmode="numeric">
                        </div>
                    </div>
                    <div class="field">
                        <label for="nom">Nom</label>
                        <input id="nom" name="nom" placeholder="Nom complet" required autocomplete="name">
                    </div>
                    <div class="field">
                        <label for="sexe">Sexe</label>
                        <select id="sexe" name="sexe" required>
                            <option value="">Choisir</option>
                            <option value="M">M</option>
                            <option value="F">F</option>
                        </select>
                    </div>
                    <div class="field">
                        <label for="age">Âge</label>
                        <input id="age" type="number" name="age" min="1" max="120" required>
                    </div>
                    <div class="field">
                        <label for="solde">Solde (Ar)</label>
                        <input id="solde" type="number" name="solde" min="0" required>
                    </div>
                    <div class="field" style="grid-column: span 2;">
                        <label for="mail">E-mail</label>
                        <input id="mail" type="email" name="mail" placeholder="exemple@mail.com" autocomplete="email">
                    </div>
                </div>
                <div class="form-actions">
                    <button id="submitBtn" type="submit" class="btn btn-primary">Enregistrer</button>
                    <button type="button" class="btn btn-ghost" onclick="resetClientForm()">Réinitialiser</button>
                </div>
            </form>
        </div>
    </div>

    <div class="section-block-panel">
        <div id="statementFormPanel" class="form-panel" style="margin-top:0.75rem;">
            <h2 class="card-title">Relevé mensuel d'un client</h2>
            <form id="statementForm" class="form-grid form-grid-two-cols" method="get" action="${pageContext.request.contextPath}/reports/statement-pdf" data-no-loading style="max-width:36rem;">
                <div class="field">
                    <label for="statement-numtel">Téléphone client</label>
                    <input id="statement-numtel" name="numtel" readonly required>
                </div>
                <div class="field">
                    <label for="statement-year">Année</label>
                    <input id="statement-year" type="number" name="year" required min="2000" max="2100" value="2026">
                </div>
                <div class="field">
                    <label for="statement-month">Mois</label>
                    <input id="statement-month" type="number" name="month" required min="1" max="12" placeholder="1–12">
                </div>
                <div class="field" style="align-self:end;">
                    <div class="form-actions" style="margin-top:0;">
                        <button type="submit" class="btn btn-primary">Télécharger PDF</button>
                        <button type="button" class="btn btn-ghost" onclick="closeStatementForm()">Fermer</button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <section class="section-block">
        <h2 class="card-title">Liste des clients</h2>
        <div class="table-wrap">
            <table class="data-table data-table--clients">
                <colgroup>
                    <col class="col-nom">
                    <col class="col-tel">
                    <col class="col-sexe">
                    <col class="col-age">
                    <col class="col-solde">
                    <col class="col-mail">
                    <col class="col-actions">
                </colgroup>
                <thead>
                <tr>
                    <th scope="col" class="th-nom">Nom</th>
                    <th scope="col" class="th-tel">Téléphone client</th>
                    <th scope="col" class="th-sexe">Sexe</th>
                    <th scope="col" class="th-age">Âge</th>
                    <th scope="col" class="th-solde">Solde (Ar)</th>
                    <th scope="col" class="th-mail">E-mail</th>
                    <th scope="col" class="th-actions">Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${clients}" var="c">
                    <tr>
                        <td class="td-nom"><strong>${fn:escapeXml(c.nom)}</strong></td>
                        <td class="td-tel"><span class="numcell js-phone">${fn:escapeXml(c.numtel)}</span></td>
                        <td class="td-sexe">
                            <span class="badge-sexe ${c.sexe == 'M' ? 'm' : 'f'}">${fn:escapeXml(c.sexe)}</span>
                        </td>
                        <td class="td-age">${c.age}</td>
                        <td class="td-solde">${c.solde} <span class="muted">Ar</span></td>
                        <td class="td-mail">
                            <c:choose>
                                <c:when test="${empty c.mail}">
                                    <span class="muted">—</span>
                                </c:when>
                                <c:otherwise>${fn:escapeXml(c.mail)}</c:otherwise>
                            </c:choose>
                        </td>
                        <td class="td-actions">
                            <div class="client-actions-menu-wrap">
                                <button type="button" class="btn btn-menu client-actions-toggle" onclick="toggleClientActions(this)" aria-label="Actions client">...</button>
                                <div class="client-actions-menu" hidden>
                                    <button type="button" class="btn btn-edit"
                                            onclick="editFromButton(this)"
                                            data-numtel="${fn:escapeXml(c.numtel)}"
                                            data-nom="${fn:escapeXml(c.nom)}"
                                            data-sexe="${fn:escapeXml(c.sexe)}"
                                            data-age="${c.age}"
                                            data-solde="${c.solde}"
                                            data-mail="${fn:escapeXml(c.mail)}">
                                        Modifier
                                    </button>
                                    <form method="post" action="${pageContext.request.contextPath}/clients" class="cell-actions-form" data-confirm="Confirmer la suppression de ce client ?">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="numtel" value="${fn:escapeXml(c.numtel)}">
                                        <button type="submit" class="btn btn-danger">Supprimer</button>
                                    </form>
                                    <button type="button" class="btn btn-ghost" onclick="openStatementFromButton(this)" data-numtel="${fn:escapeXml(c.numtel)}">
                                        Relever
                                    </button>
                                </div>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <c:if test="${empty clients}">
            <p class="muted" style="margin-top:1rem;">Aucun client.</p>
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

    function formatStoredPhone(raw) {
        var text = (raw || "").trim();
        if (!text) return "";
        if (text.charAt(0) === "+") return text;

        var digits = text.replace(/\D/g, "");
        if (digits.startsWith("261") && digits.length >= 12) {
            return "+261 " + formatNationalPhone(digits.slice(3));
        }
        if (digits.length === 10 && digits.charAt(0) === "0") {
            return "+261 " + formatNationalPhone(digits.slice(1));
        }
        return text;
    }

    function formatPhoneInputValue(input) {
        if (!input) return;
        input.value = formatNationalPhone(input.value);
    }

    function formatPhonesInList() {
        var phones = document.querySelectorAll(".js-phone");
        phones.forEach(function (cell) {
            cell.textContent = formatStoredPhone(cell.textContent);
        });
    }

    function toggleClientForm(forceOpen) {
        var panel = document.getElementById("clientFormPanel");
        var btn = document.getElementById("toggleClientFormBtn");
        var isOpen = panel.classList.contains("is-open");
        if (forceOpen === true) {
            panel.classList.add("is-open");
            btn.innerText = "Masquer le formulaire";
            return;
        }
        if (forceOpen === false) {
            panel.classList.remove("is-open");
            btn.innerText = "Ajouter un nouveau client";
            return;
        }
        if (!isOpen) {
            panel.classList.add("is-open");
            btn.innerText = "Masquer le formulaire";
        } else {
            panel.classList.remove("is-open");
            btn.innerText = "Ajouter un nouveau client";
        }
    }

    function editFromButton(btn) {
        var d = btn.dataset;
        toggleClientForm(true);
        document.getElementById("clientFormTitle").innerText = "Modifier le client";
        document.getElementById("clientAction").value = "update";
        document.getElementById("originalNumtel").value = d.numtel || "";
        document.getElementById("numtel").value = formatNationalPhone((d.numtel || "").replace(/^261/, ""));
        document.getElementById("numtel").readOnly = true;
        document.getElementById("numtelCountry").disabled = true;
        document.getElementById("nom").value = d.nom || "";
        document.getElementById("sexe").value = d.sexe || "";
        document.getElementById("age").value = d.age || "";
        document.getElementById("solde").value = d.solde || "";
        document.getElementById("mail").value = d.mail || "";
        document.getElementById("submitBtn").innerText = "Mettre à jour";
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function resetClientForm() {
        document.getElementById("clientFormTitle").innerText = "Nouveau client";
        document.getElementById("clientAction").value = "create";
        document.getElementById("originalNumtel").value = "";
        document.getElementById("clientForm").reset();
        document.getElementById("numtel").readOnly = false;
        document.getElementById("numtelCountry").disabled = false;
        document.getElementById("numtelCountry").value = "+261";
        document.getElementById("submitBtn").innerText = "Enregistrer";
        toggleClientForm(false);
    }

    function closeAllClientActionMenus() {
        document.querySelectorAll(".client-actions-menu-wrap.is-open").forEach(function (el) {
            el.classList.remove("is-open");
        });
        document.querySelectorAll(".client-actions-menu-wrap .client-actions-menu").forEach(function (menu) {
            menu.hidden = true;
        });
    }

    function toggleClientActions(btn) {
        var wrap = btn.closest(".client-actions-menu-wrap");
        if (!wrap) return;
        var menu = wrap.querySelector(".client-actions-menu");
        if (!menu) return;
        var willOpen = !wrap.classList.contains("is-open");
        closeAllClientActionMenus();
        wrap.classList.toggle("is-open", willOpen);
        menu.hidden = !willOpen;
    }

    function openStatementFromButton(btn) {
        var numtel = btn.dataset.numtel || "";
        var panel = document.getElementById("statementFormPanel");
        var input = document.getElementById("statement-numtel");
        var monthInput = document.getElementById("statement-month");
        var yearInput = document.getElementById("statement-year");
        if (!panel || !input) return;
        input.value = formatStoredPhone(numtel);
        var now = new Date();
        if (monthInput && !monthInput.value) monthInput.value = String(now.getMonth() + 1);
        if (yearInput && !yearInput.value) yearInput.value = String(now.getFullYear());
        panel.classList.add("is-open");
        closeAllClientActionMenus();
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function closeStatementForm() {
        var panel = document.getElementById("statementFormPanel");
        if (!panel) return;
        panel.classList.remove("is-open");
    }

    document.addEventListener("DOMContentLoaded", function () {
        var form = document.getElementById("clientForm");
        var numtelInput = document.getElementById("numtel");
        var countrySelect = document.getElementById("numtelCountry");
        if (numtelInput) {
            numtelInput.addEventListener("input", function () {
                formatPhoneInputValue(numtelInput);
            });
            formatPhoneInputValue(numtelInput);
        }
        if (form && numtelInput && countrySelect) {
            form.addEventListener("submit", function () {
                numtelInput.value = buildInternationalPhone(countrySelect.value, numtelInput.value);
            });
        }
        var statementForm = document.getElementById("statementForm");
        if (statementForm) {
            statementForm.addEventListener("submit", function () {
                var btn = statementForm.querySelector("button[type='submit']");
                if (!btn) return;
                var label = btn.textContent;
                setTimeout(function () {
                    btn.disabled = false;
                    btn.classList.remove("is-loading");
                    btn.textContent = label;
                }, 1200);
            });
        }
        document.addEventListener("click", function (event) {
            if (!event.target.closest(".client-actions-menu-wrap")) {
                closeAllClientActionMenus();
            }
        });
        formatPhonesInList();
    });

    <c:if test="${not empty error}">
    toggleClientForm(true);
    </c:if>
</script>
<script src="${pageContext.request.contextPath}/js/theme.js?v=2"></script>
</body>
</html>
