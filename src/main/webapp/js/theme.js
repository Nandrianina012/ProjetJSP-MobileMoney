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
        if (!btn) return;
        btn.addEventListener('click', function () {
            var next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem(key, next);
            sync(btn);
        });
        sync(btn);
    });
})();
