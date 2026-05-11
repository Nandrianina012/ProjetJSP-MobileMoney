(function () {
    function setAlertAccessibility() {
        var alerts = document.querySelectorAll(".alert");
        alerts.forEach(function (alert) {
            var isError = alert.classList.contains("alert-error");
            alert.setAttribute("role", isError ? "alert" : "status");
            alert.setAttribute("aria-live", isError ? "assertive" : "polite");
        });
    }

    function messageForField(field) {
        if (!field) return "Valeur invalide.";
        if (field.validity.valueMissing) return "Ce champ est obligatoire.";
        if (field.validity.typeMismatch && field.type === "email") return "Adresse e-mail invalide.";
        if (field.validity.rangeUnderflow) return "La valeur est trop petite.";
        if (field.validity.rangeOverflow) return "La valeur est trop grande.";
        if (field.validity.badInput) return "Valeur non valide.";
        return "Veuillez verifier ce champ.";
    }

    function clearFieldError(field) {
        field.classList.remove("is-invalid");
        field.removeAttribute("aria-invalid");
        field.setCustomValidity("");
    }

    function applyFieldError(field, message) {
        field.classList.add("is-invalid");
        field.setAttribute("aria-invalid", "true");
        field.setCustomValidity(message);
    }

    function bindFormValidation() {
        var forms = document.querySelectorAll("form");
        forms.forEach(function (form) {
            var confirmMessage = form.getAttribute("data-confirm");
            if (confirmMessage) {
                form.addEventListener("submit", function (e) {
                    if (!window.confirm(confirmMessage)) {
                        e.preventDefault();
                    }
                });
            }

            var submitButton = form.querySelector("button[type='submit']");
            var submitLabel = submitButton ? submitButton.textContent : "";

            form.addEventListener("input", function (e) {
                var target = e.target;
                if (target && (target.matches("input") || target.matches("select") || target.matches("textarea"))) {
                    clearFieldError(target);
                }
            });

            form.addEventListener("submit", function (e) {
                var fields = form.querySelectorAll("input, select, textarea");
                var firstInvalid = null;
                var method = (form.getAttribute("method") || "get").toLowerCase();
                var shouldLockSubmit = method !== "get" && !form.hasAttribute("data-no-loading");

                fields.forEach(function (field) {
                    clearFieldError(field);
                    if (!field.checkValidity()) {
                        var msg = messageForField(field);
                        applyFieldError(field, msg);
                        if (!firstInvalid) firstInvalid = field;
                    }
                });

                if (firstInvalid) {
                    e.preventDefault();
                    firstInvalid.focus();
                    firstInvalid.reportValidity();
                    return;
                }

                if (submitButton && shouldLockSubmit) {
                    submitButton.disabled = true;
                    submitButton.classList.add("is-loading");
                    submitButton.textContent = "Traitement...";
                }
            });

            form.addEventListener("invalid", function (e) {
                e.preventDefault();
                var field = e.target;
                if (field && (field.matches("input") || field.matches("select") || field.matches("textarea"))) {
                    var msg = messageForField(field);
                    applyFieldError(field, msg);
                }
            }, true);

            form.addEventListener("reset", function () {
                var fields = form.querySelectorAll("input, select, textarea");
                fields.forEach(clearFieldError);
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.classList.remove("is-loading");
                    submitButton.textContent = submitLabel;
                }
            });
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        setAlertAccessibility();
        bindFormValidation();
    });
})();
