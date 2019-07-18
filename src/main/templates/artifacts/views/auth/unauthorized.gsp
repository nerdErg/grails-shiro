<!doctype html>
<html>
<head>
    <title>Unauthorized</title>
    <meta name="layout" content="main">
</head>

<body>
<div class="container">
    <h1>Not Authorized (403)</h1>
    <div class="alert alert-info">
        <p>
            You are Not Authorized
            <g:if test="\${targetUri}">to access the page at \${targetUri}</g:if>
            <g:else>to access the resource.</g:else>
        </p>
        <g:if test="\${msg}">
            <p>
                The error message was: \${msg}
            </p>
        </g:if>
    </div>

    <shiro:isLoggedIn>
        <p>You are currently logged in as <shiro:principal property="userName"/>.</p>
        <a href="\${createLink(action: 'signOut')}" title="log out">Logout</a>
    </shiro:isLoggedIn>
    <shiro:isNotLoggedIn>
        <p>You are currently not logged in.</p>
        <a href="\${createLink(action: 'login')}" title="log in">Login</a>
    </shiro:isNotLoggedIn>
</div>
</body>
</html>
