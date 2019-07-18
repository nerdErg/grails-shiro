package org.apache.shiro.grails

import org.apache.shiro.authc.UsernamePasswordToken
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import javax.naming.directory.InitialDirContext

/**
 * User: pmcneil
 * Date: 15/07/19
 *
 */
class LdapServerSpec extends Specification implements GrailsUnitTest {

    LdapServer ldapServer

    def setup() {
        ldapServer = new LdapServer()
        //configure the server 
        ldapServer.searchBase = 'ou=users,dc=example,dc=com'
        ldapServer.usernameAttribute = 'uid'
        ldapServer.searchUser = 'uid=admin,ou=system'
        ldapServer.searchPass = 'secret'
        ldapServer.ldapUrls = 'ldap://localhost:10389'.split(',').collect { it.trim() }
        //Group or role config
        ldapServer.groupOu = 'ou=groups,dc=example,dc=com'
        ldapServer.groupMemberElement = 'uniqueMember'
        ldapServer.groupMemberPrefix = 'uid='
        ldapServer.groupMemberPostfix = ''
        //Permission setup. Permissions exist under roles and users as a particular sub directory
        ldapServer.permSubCn = 'cn=permissions'
        ldapServer.permMemberElement = 'uniqueMember'
        ldapServer.permMemberPrefix = 'uid='
    }

    def cleanup() {
    }

    @Unroll
    void "test we can connect to the ldap server as #user"() {
        when: "I attempt an LDAP search"
        InitialDirContext context = null
        String url = null
        ldapServer.ldapSearch { InitialDirContext ctx, String ldapUrl ->
            context = ctx
            url = ldapUrl
        }

        then: "we get the right URL choosen and we have a context"
        context != null
        url == 'ldap://localhost:10389'

        when: "I check some credentials"
        UsernamePasswordToken token = new UsernamePasswordToken(user, password)
        boolean yes = ldapServer.doCredentialsMatch(token, null)

        then: "They do"
        yes

        when: "I give it the wrong password it fails"
        token = new UsernamePasswordToken(user, '12345')
        yes = ldapServer.doCredentialsMatch(token, null)

        then: "They don't"
        !yes

        where:
        user      | password
        'pmcneil' | 'idunno'
        'fbloggs' | 'password'
    }

    @Unroll
    void "test we can get roles for #user"() {
        when: "we check the connection"
        boolean connect = ldapServer.checkConnection()

        then: "we can connected"
        connect

        when: "we get the roles for pmcneil"
        List<String> roles = ldapServer.roles(user)

        then: "We get a list of roles"
        roles.size() == expectedRoles.size()
        roles.containsAll(expectedRoles)

        where:
        user      | expectedRoles
        'pmcneil' | ['admin', 'editor', 'user']
        'fbloggs' | ['user']
    }

    @Unroll
    void "test we can get permissions for role #role"() {
        when: "we check the connection"
        boolean connect = ldapServer.checkConnection()

        then: "we can connected"
        connect

        when: "we get the perms for #role"
        List<String> perms = ldapServer.rolePermissions(role)

        then: "We get a list of roles"
        perms.size() == expectedPerms.size()
        perms.containsAll(expectedPerms)

        where:
        role     | expectedPerms
        'admin'  | ['book:*', 'user:*']
        'editor' | ['book:read,write']
        'user'   | ['book:index,list', 'book:show:*']
    }

    @Unroll
    void "test we can get user (#user) permissions"() {
        when: "we check the connection"
        boolean connect = ldapServer.checkConnection()

        then: "we can connected"
        connect

        when: "we get the roles for pmcneil's cn"
        List<String> perms = ldapServer.userPermissions(user)

        then: "We get a list of roles"
        perms.size() == expectedPerms.size()
        perms.containsAll(expectedPerms)

        where:
        user           | expectedPerms
        'Peter McNeil' | ['book:write']
        'Fred Bloggs'  | ['cart:*']
    }

    @Unroll
    void "test we can get attributes"() {
        when: "we check the connection"
        boolean connect = ldapServer.checkConnection()

        then: "we can connected"
        connect

        when: "we get the attributes for #user"
        Map attribs = ldapServer.getUserAttributes(user)
        println attribs

        then: "We get cn= #cn"
        attribs.cn == cn

        where:
        user      | cn
        'pmcneil' | 'Peter McNeil'
        'fbloggs' | 'Fred Bloggs'
    }

    @Unroll
    void "test LdapUser class gets all permissions"() {
        when: "we check the connection"
        boolean connect = ldapServer.checkConnection()

        then: "we can connected"
        connect

        when: "we make and LdapUser"
        LdapUser ldapUser = new LdapUser(ldapServer, user)

        then:
        ldapUser
        ldapUser.fullName == cn

        when: "we get all the permissions"
        Set<String> perms = ldapUser.permissions
        println perms

        then:
        perms.size() == expectedPerms.size()
        perms.containsAll(expectedPerms)

        where:
        user      | cn             | expectedPerms
        'pmcneil' | 'Peter McNeil' | ['book:*', 'user:*', 'book:read,write', 'book:show:*', 'book:index,list', 'book:write']
        'fbloggs' | 'Fred Bloggs'  | ['book:show:*', 'book:index,list', 'cart:*']
    }

}
