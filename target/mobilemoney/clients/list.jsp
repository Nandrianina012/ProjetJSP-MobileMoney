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
        <jsp:param name="headerSubtitle" value="Comptes — recherche par nom ou numéro"/>
    </jsp:include>
    <jsp:include page="/includes/app-nav.jsp">
        <jsp:param name="active" value="clients"/>
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-error">${fn:escapeXml(error)}</div>
    </c:if>
    <c:if test="${param.ok != null}">
        <div class="alert alert-success">Opération enregistrée.</div>
    </c:if>

    <section class="card">
        <h2 class="card-title">Recherche</h2>
        <form class="search-bar" method="get" action="${pageContext.request.contextPath}/clients">
            <input type="text" name="q" value="${fn:escapeXml(param.q)}" placeholder="Nom ou numéro">
            <button type="submit" class="btn btn-primary">Rechercher</button>
        </form>
    </section>

    <section class="card">
        <h2 class="card-title" id="clientFormTitle">Nouveau client</h2>
        <form id="clientForm" method="post" action="${pageContext.request.contextPath}/clients">
            <input type="hidden" id="clientAction" name="action" value="create">
            <div class="form-grid">
                <div class="field">
                    <label for="numtel">Téléphone</label>
                    <input id="numtel" name="numtel" placeholder="032…" required autocomplete="tel">
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
                    <label for="mail">E-mail <span class="muted">(optionnel)</span></label>
                    <input id="mail" type="email" name="mail" placeholder="exemple@mail.com" autocomplete="email">
                </div>
            </div>
            <div class="form-actions">
                <button id="submitBtn" type="submit" class="btn btn-primary">Enregistrer</button>
                <button type="button" class="btn btn-ghost" onclick="resetClientForm()">Réinitialiser</button>
            </div>
        </form>
    </section>

    <section class="card">
        <h2 class="card-title">Liste</h2>
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
                    <th scope="col" class="th-tel">Téléphone</th>
                    <th scope="col" class="th-sexe">Sexe</th>
                    <th scope="col" class="th-age">Âge</th>
                    <th scope="col" class="th-solde">Solde</th>
                    <th scope="col" class="th-mail">E-mail</th>
                    <th scope="col" class="th-actions">Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${clients}" var="c">
                    <tr>
                        <td class="td-nom"><strong>${fn:escapeXml(c.nom)}</strong></td>
                        <td class="td-tel"><span class="numcell">${fn:escapeXml(c.numtel)}</span></td>
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
                            <div class="cell-actions">
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
                                <form method="post" action="${pageContext.request.contextPath}/clients" class="cell-actions-form">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="numtel" value="${fn:escapeXml(c.numtel)}">
                                    <button type="submit" class="btn btn-danger">Supprimer</button>
                                </form>
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
    function editFromButton(btn) {
        var d = btn.dataset;
        document.getElementById("clientFormTitle").innerText = "Modifier le client";
        document.getElementById("clientAction").value = "update";
        document.getElementById("numtel").value = d.numtel || "";
        document.getElementById("numtel").readOnly = true;
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
        document.getElementById("clientForm").reset();
        document.getElementById("numtel").readOnly = false;
        document.getElementById("submitBtn").innerText = "Enregistrer";
    }
</script>
<script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
