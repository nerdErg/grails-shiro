package ${packageName}

import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.credential.CredentialsMatcher
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermissionResolver
import org.apache.shiro.grails.GrailsShiroRealm
import org.apache.shiro.grails.SimplifiedRealm
import org.apache.shiro.grails.PrincipalHolder

class ${className} implements GrailsShiroRealm, SimplifiedRealm {

    CredentialsMatcher credentialMatcher

    ${className}() {
        setTokenClass(UsernamePasswordToken)
        setPermissionResolver(new WildcardPermissionResolver())
    }

    AuthenticationInfo authenticate(AuthenticationToken authToken) {
        if (authToken instanceof UsernamePasswordToken) {
            log.info "Attempting to authenticate \${authToken.username} in DB realm..."
            String username = authToken.username

            // Null username is invalid
            if (username == null) {
                throw new AccountException("Null usernames are not allowed by this realm \${getName()}.")
            }

            ${userClassName} user = ${userClassName}.findByUsername(username)
            if (!user) {
                throw new UnknownAccountException("No account found for user [\${username}]")
            }

            log.info "Found user \${user.username} in DB"

            // Now check the user's password against the hashed value stored in the database.
            // First we create a SimpleAccount to hold our Prinicipal Object which in this case is utility PrincipalHolder
            // defined below
            SimpleAuthenticationInfo account = new SimpleAuthenticationInfo(new ${principalHolderClassName}(user), user.passwordHash, "${className}")
            if (!credentialMatcher.doCredentialsMatch(authToken, account)) {
                log.info "Invalid password (${className})"
                throw new IncorrectCredentialsException("Invalid password for user \${username}")
            }
            return account
        }
        throw new AccountException("\${authToken.class.name} tokens are not accepted by this realm \${getName()}.")
    }

    // Implements SimpleRealm
    boolean hasRole(Object principal, String roleName) {
        if (principal instanceof ${principalHolderClassName}) {
            ${principalHolderClassName} ph = (${principalHolderClassName}) principal
            return ph.roles.find { it == roleName} != null
        }
        return false
    }

    // Implements SimpleRealm
    boolean hasAllRoles(Object principal, Collection<String> roles) {
        if (principal instanceof ${principalHolderClassName}) {
            ${principalHolderClassName} ph = (${principalHolderClassName}) principal
            return ph.roles.containsAll(roles)
        }
        return false
    }

    // Implements SimpleRealm
    boolean isPermitted(Object principal, Permission requiredPermission) {
        if (principal instanceof ${principalHolderClassName}) {
            ${principalHolderClassName} ph = (${principalHolderClassName}) principal
            return anyImplied(requiredPermission, ph.permissions)
        }
        return false
    }

    private boolean anyImplied(Permission requiredPermission, Collection<String> permStrings) {
        permStrings.find { String permString ->
            getPermissionResolver()
                    .resolvePermission(permString)
                    .implies(requiredPermission)
        } != null
    }
}

class ${principalHolderClassName} implements Serializable, PrincipalHolder {

    Long id
    String userName

    ${principalHolderClassName}(${userClassName} user) {
        id = user.id
        userName = user.username
    }

    Set<String> getRoles() {
        ${userClassName}.get(id)?.roles?.collect { it.name } ?: []
    }

    Set<String> getPermissions() {
        ${userClassName} user = ${userClassName}.get(id)
        Set<String> permissions = []
        if (user) {
            permissions.addAll(user.permissions)
            user.roles.each {
                ${roleClassName} role ->
                permissions.addAll(role.permissions)
            }
            return permissions
        }
        return []
    }

    String toString() {
        return userName
    }
}