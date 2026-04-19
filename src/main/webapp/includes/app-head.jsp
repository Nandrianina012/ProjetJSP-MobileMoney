<%@ page contentType="text/html;charset=UTF-8" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${param.title}</title>
<script>
    (function () {
        var key = 'mm-app-theme';
        var legacy = 'mm-clients-theme';
        var t = localStorage.getItem(key);
        if (t !== 'light' && t !== 'dark') {
            var old = localStorage.getItem(legacy);
            t = (old === 'light' || old === 'dark') ? old : 'light';
        }
        document.documentElement.setAttribute('data-theme', t);
    })();
</script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css?v=8">
