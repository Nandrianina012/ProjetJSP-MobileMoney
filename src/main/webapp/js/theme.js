(function () {
    var key = 'mm-app-theme';

    function sync(btn) {
        if (!btn) return;
        var dark = document.documentElement.getAttribute('data-theme') === 'dark';
        btn.textContent = dark ? 'Mode clair' : 'Mode sombre';
        btn.setAttribute('aria-pressed', dark ? 'true' : 'false');
    }

    document.addEventListener('DOMContentLoaded', function () {
        var btn = document.getElementById('themeToggle');
        if (btn) {
            btn.addEventListener('click', function () {
                var next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-theme', next);
                localStorage.setItem(key, next);
                sync(btn);
            });
            sync(btn);
        }

        // Auto-hide only success feedback messages after 4 seconds.
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function (alert) {
            if (!alert.classList.contains('alert-success')) return;
            setTimeout(function () {
                alert.style.transition = 'opacity 0.25s ease';
                alert.style.opacity = '0';
                setTimeout(function () {
                    alert.remove();
                }, 250);
            }, 4000);
        });
    });
})();
