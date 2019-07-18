<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>Login</title>
</head>

<body>
<div id="auth-login" class="container" role="main">
  <g:if test="\${flash.message || msg}">
    <div class="alert alert-info">
      \${flash.message ?: msg}
      <g:if test="\${targetUri}">
        <span class="text-secondary">You need to log in to access the page at \${targetUri}.</span>
      </g:if>
    </div>
  </g:if>
  <g:elseif test="\${targetUri}">
    <div class="alert alert-info">
      You need to log in to access the page at \${targetUri}.
    </div>
  </g:elseif>


  <shiro:isLoggedIn>
    <h2>Logged in</h2>

    <p>You are currently logged in as <shiro:principal property="userName"/>.</p>
    <a href="\${createLink(action: 'signOut')}" title="log out">Logout</a>
  </shiro:isLoggedIn>

  <shiro:isNotLoggedIn>

    <g:form action="signIn">
      <fieldset class="form">
        <input type="hidden" name="targetUri" value="\${targetUri}"/>
        <label for="username">Username:</label>
        <input type="text" id="username" name="username" value="\${username}" class="form-control"/>
        <label for="password">Password:</label>
        <input type="password" id="password" name="password" value="" class="form-control"/>
        <label for="rememberMe">Remember me?:</label>
        <g:checkBox id="rememberMe" name="rememberMe" value="\${rememberMe}"/>
      </fieldset>
      <fieldset class="buttons">
        <input type="submit" value="Sign in" class="btn btn-primary"/>
      </fieldset>
    </g:form>
  </shiro:isNotLoggedIn>
</div>
</body>
</html>
