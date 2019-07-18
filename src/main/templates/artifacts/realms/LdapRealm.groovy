package ${packageName}

import grails.core.GrailsApplication
import org.apache.shiro.authc.*
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermissionResolver
import org.apache.shiro.grails.GrailsShiroRealm
import org.apache.shiro.grails.LdapServer
import org.apache.shiro.grails.LdapUser
import org.apache.shiro.grails.SimplifiedRealm

/**
 * Simple realm that authenticates users against an LDAP server.
 */
class ${className} implements GrailsShiroRealm, SimplifiedRealm {
    static authTokenClass = UsernamePasswordToken

    def grailsApplication
    private LdapServer theLdapServer

    ${className}() {
        setTokenClass(UsernamePasswordToken)
        setPermissionResolver(new WildcardPermissionResolver())
    }

    @Override
    AuthenticationInfo authenticate(AuthenticationToken authToken) throws AuthenticationException {
        if (authToken instanceof UsernamePasswordToken) {
            log.info "Attempting to authenticate \${authToken.username} in LDAP realm..."
            String username = authToken.username
            String password = new String(authToken.password)

            // No username is invalid
            if (!username) {
                throw new AccountException("Null usernames are not allowed by this realm.")
            }

            // No password is invalid
            if (!password) {
                throw new CredentialsException("Null password are not allowed by this realm.")
            }

            LdapUser ldapPrincipal = new LdapUser(getLdapServer(), username)
            SimpleAuthenticationInfo account = new SimpleAuthenticationInfo(ldapPrincipal,
                    password, "${className}")
            if (!ldapServer.doCredentialsMatch(authToken, account)) {
                log.info "Invalid password (${className})"
                throw new IncorrectCredentialsException("Invalid password for user \${username}")
            }
            return account
        }
        throw new AccountException("\${authToken.class.name} tokens are not accepted by this realm \${getName()}.")
    }

    @Override
    boolean hasAllRoles(Object principal, Collection<String> roles) {
        if (principal instanceof LdapUser) {
            LdapUser user = principal
            return user.roles.containsAll(roles)
        }
        return false
    }

    @Override
    boolean hasRole(Object principal, String roleName) {
        if (principal instanceof LdapUser) {
            LdapUser user = principal
            return user.roles.contains(roleName)
        }
        return false
    }

    @Override
    boolean isPermitted(Object principal, Permission requiredPermission) {
        if (principal instanceof LdapUser) {
            LdapUser ph = (LdapUser) principal
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

    LdapServer getLdapServer() {
        if (!this.theLdapServer) {
            if (grailsApplication) {
                setupServer()
            } else {
                throw new Exception("no grailsApplication to setup LDAP server.")
            }
        }
        return theLdapServer
    }

    private setupServer() {
        theLdapServer = new LdapServer()
        ldapServer.searchBase = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.base', String, '')
        ldapServer.usernameAttribute = grailsApplication.config.getProperty('security.shiro.realm.ldap.username.attribute', String, "uid")
        ldapServer.searchUser = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.user', String, "")
        ldapServer.searchPass = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.pass', String, "")
        ldapServer.ldapUrls = grailsApplication.config.getProperty('security.shiro.realm.ldap.server.urls', String, "ldap://localhost:389/")
                                                      .split(',').collect { it.trim() }
        //Group or role config
        ldapServer.groupOu = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.group.name', String, '')
        ldapServer.groupMemberElement = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.group.member.element', String, '')
        ldapServer.groupMemberPrefix = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.group.member.prefix', String, '')
        ldapServer.groupMemberPostfix = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.group.member.postfix', String, '')
        //Permission setup. Permissions exist under roles and users as a particular sub directory
        ldapServer.permSubCn = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.permission.commonName', String, 'cn=permissions')
        ldapServer.permMemberElement = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.permission.member.element', String, 'uniqueMember')
        ldapServer.permMemberPrefix = grailsApplication.config.getProperty('security.shiro.realm.ldap.search.permission.member.prefix', String, 'uid=')
    }
}
