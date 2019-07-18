package annotation.test

import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.credential.CredentialsMatcher
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermissionResolver
import org.apache.shiro.grails.GrailsShiroRealm
import org.apache.shiro.grails.PrincipalHolder
import org.apache.shiro.grails.SimplifiedRealm
import org.apache.shiro.authc.UsernamePasswordToken

class WildRealm implements GrailsShiroRealm, SimplifiedRealm {

    CredentialsMatcher credentialMatcher
    WildcardPermissionResolver shiroPermissionResolver

    WildRealm() {
        setTokenClass(UsernamePasswordToken)
    }

    AuthenticationInfo authenticate(AuthenticationToken authToken) {
        if (authToken instanceof UsernamePasswordToken) {
            log.info "Attempting to authenticate ${authToken.username} in Wild realm..."
            String username = authToken.username

            // Null username is invalid
            if (username == null) {
                throw new AccountException("Null usernames are not allowed by this realm ${getName()}.")
            }

            SimpleAuthenticationInfo account = new SimpleAuthenticationInfo(new WildPrincipalHolder(), 'user.passwordHash', "WildRealm")
            if (!credentialMatcher.doCredentialsMatch(authToken, account)) {
                log.info "Invalid password (DB realm)"
                throw new IncorrectCredentialsException("Invalid password for user ${username}")
            }
            return account
        }
        throw new AccountException("${authToken.class.name} tokens are not accepted by this realm ${getName()}.")
    }

    boolean hasRole(Object principal, String roleName) {
        WildPrincipalHolder ph = (WildPrincipalHolder) principal
        ph.roles.find { it == roleName } != null
    }

    boolean hasAllRoles(Object principal, Collection<String> roles) {
        WildPrincipalHolder ph = (WildPrincipalHolder) principal
        ph.roles.containsAll(roles)
    }

    boolean isPermitted(Object principal, Permission requiredPermission) {
        WildPrincipalHolder ph = (WildPrincipalHolder) principal
        return anyImplied(requiredPermission, ph.permissions)
    }

    private boolean anyImplied(Permission requiredPermission, Collection<String> permStrings) {
        permStrings.find { String permString ->
            def resolver = shiroPermissionResolver.resolvePermission(permString)
            resolver.implies(requiredPermission)
        } != null
    }
}

class WildPrincipalHolder implements Serializable, PrincipalHolder {

    WildPrincipalHolder() {
    }

    Set<String> getRoles() {
        []
    }

    Set<String> getPermissions() {
        return []
    }
}