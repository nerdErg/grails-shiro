package org.apache.shiro.grails

import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.InvalidPermissionStringException
import org.apache.shiro.authz.permission.PermissionResolver
import org.apache.shiro.subject.PrincipalCollection
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

/**
 * This tests the old RealmAdaptor part of the GrailsShiroRealm trait
 * The methods we're testing convert vararg string argument conversions
 */
class RealmAdapterSpec extends Specification implements GrailsUnitTest {

    def setup() {
    }

    def cleanup() {
    }

    void "test checkPermission"() {
        given:
        TestRealm r = new TestRealm()
        PermissionResolver pr = Mock()
        PrincipalCollection principal = Mock()
        Permission mockPermission = Mock()
        r.setPermissionResolver(pr)

        when: "I check a permission using a string"
        r.checkPermission(principal, 'blah:bing:wap')

        then: "it works and no exception is thrown"
        1 * pr.resolvePermission('blah:bing:wap') >> mockPermission

        when: "permission is not given"
        r.permissionGiven = false
        r.checkPermission(principal, 'blah:bing:wap')

        then: "An Authroization exception is thrown"
        1 * pr.resolvePermission('blah:bing:wap') >> mockPermission
        AuthorizationException ae1 = thrown()
        ae1.message == "Bugger."

        when: "The resolver throws InvalidPermissionStringException"
        r.checkPermission(principal, 'blah:bing:wap')

        then:
        1 * pr.resolvePermission('blah:bing:wap') >> { String perm ->
            throw new InvalidPermissionStringException("Flip", "blah:bing:wap")
        }
        AuthorizationException ae2 = thrown()
        ae2.message.contains("Invalid Permission String: blah:bing:wap")
        ae2.message.contains("Flip")
    }

    void "test checkPermissions"() {
        given:
        TestRealm r = new TestRealm()
        PermissionResolver pr = Mock()
        PrincipalCollection principal = Mock()
        Permission mockPermission = Mock()
        r.setPermissionResolver(pr)

        when: "I check a permission using a string"
        r.checkPermissions(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then: "it works and no exception is thrown"
        3 * pr.resolvePermission(_) >> mockPermission

        when: "permission is not given"
        r.permissionGiven = false
        r.checkPermissions(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then: "An Authroization exception is thrown"
        3 * pr.resolvePermission(_) >> mockPermission
        AuthorizationException ae1 = thrown()
        ae1.message == "Bugger and Damn."

        when: "The resolver throws InvalidPermissionStringException"
        r.checkPermissions(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then:
        1 * pr.resolvePermission('blah:bing:wap') >> { String perm ->
            throw new InvalidPermissionStringException("Fudge", "blah:bing:wap")
        }
        AuthorizationException ae2 = thrown()
        ae2.message.contains("Invalid Permission String: blah:bing:wap")
        ae2.message.contains("Fudge")
    }

    void "test isPermitted(p, String)"() {
        given:
        TestRealm r = new TestRealm()
        PermissionResolver pr = Mock()
        PrincipalCollection principal = Mock()
        Permission mockPermission = Mock()
        r.setPermissionResolver(pr)

        when: "I check a permission using a string"
        boolean result = r.isPermitted(principal, 'blah:bing:wap')

        then: "it works and no exception is thrown"
        1 * pr.resolvePermission('blah:bing:wap') >> mockPermission
        result

        when: "permission is not given"
        r.permissionGiven = false
        result = r.isPermitted(principal, 'blah:bing:wap')

        then: "it returns false"
        1 * pr.resolvePermission('blah:bing:wap') >> mockPermission
        !result

        when: "The resolver throws InvalidPermissionStringException"
        result = r.isPermitted(principal, 'blah:bing:wap')

        then: "it returns false"
        1 * pr.resolvePermission('blah:bing:wap') >> { String perm ->
            throw new InvalidPermissionStringException("Flip", "blah:bing:wap")
        }
        !result
    }

    void "test isPermitted(p, String...)"() {
        given:
        TestRealm r = new TestRealm()
        PermissionResolver pr = Mock()
        PrincipalCollection principal = Mock()
        Permission mockPermission = Mock()
        r.setPermissionResolver(pr)

        when: "I check a permission using multiple strings"
        boolean[] result = r.isPermitted(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then: "it works and no exception is thrown"
        3 * pr.resolvePermission(_) >> mockPermission
        result[0]

        when: "permission is not given"
        r.permissionGiven = false
        result = r.isPermitted(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then: "An the results will be false"
        3 * pr.resolvePermission(_) >> mockPermission
        !result[0]

        when: "The resolver throws InvalidPermissionStringException"
        result = r.isPermitted(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then:
        1 * pr.resolvePermission('blah:bing:wap') >> { String perm ->
            throw new InvalidPermissionStringException("BangBang", "blah:bing:wap")
        }
        result.size() == 0
    }

    void "test isPermittedAll(p, String...)"() {
        given:
        TestRealm r = new TestRealm()
        PermissionResolver pr = Mock()
        PrincipalCollection principal = Mock()
        Permission mockPermission = Mock()
        r.setPermissionResolver(pr)

        when: "I check a permission using multiple strings"
        boolean result = r.isPermittedAll(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then: "it works and no exception is thrown"
        3 * pr.resolvePermission(_) >> mockPermission
        result

        when: "permission is not given"
        r.permissionGiven = false
        result = r.isPermittedAll(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then: "An the results will be false"
        3 * pr.resolvePermission(_) >> mockPermission
        !result

        when: "The resolver throws InvalidPermissionStringException"
        result = r.isPermittedAll(principal, 'blah:bing:wap', 'bong:foop:dingle', 'fungle')

        then:
        1 * pr.resolvePermission('blah:bing:wap') >> { String perm ->
            throw new InvalidPermissionStringException("BongBong", "blah:bing:wap")
        }
        !result
    }

    void "test checkRoles(p, String...)"() {
        given:
        TestRealm r = new TestRealm()
        PrincipalCollection principal = Mock()

        when: "I check a permission using multiple strings"
        r.checkRoles(principal, 'admin', 'luzer', 'cat', 'magnitron')

        then: "it works and no exception is thrown"
        notThrown(AuthorizationException)

        when: "the role is not ok"
        r.roleOk = false
        r.checkRoles(principal, 'admin', 'luzer', 'cat', 'magnitron')

        then: "An auth exception"
        AuthorizationException ae1 = thrown()
        ae1.message == "Bugger."

    }

}

class TestRealm implements GrailsShiroRealm {

    Boolean permissionGiven = true
    Boolean roleOk = true

    @Override
    boolean isPermitted(PrincipalCollection subjectPrincipal, Permission permission) {
        return permissionGiven
    }

    @Override
    boolean[] isPermitted(PrincipalCollection subjectPrincipal, List<Permission> permissions) {
        boolean[] result = new boolean[permissions.size()]
        int i = 0
        for (Permission p : permissions) {
            result[i++] = permissionGiven
        }
        return result
    }

    @Override
    boolean isPermittedAll(PrincipalCollection subjectPrincipal, Collection<Permission> permissions) {
        return permissionGiven
    }

    @Override
    void checkPermission(PrincipalCollection subjectPrincipal, Permission permission) throws AuthorizationException {
        if (!isPermitted(subjectPrincipal, permission)) {
            throw new AuthorizationException("Bugger.")
        }
    }

    @Override
    void checkPermissions(PrincipalCollection subjectPrincipal, Collection<Permission> permissions) throws AuthorizationException {
        if (!isPermittedAll(subjectPrincipal, permissions)) {
            throw new AuthorizationException("Bugger and Damn.")
        }
    }

    @Override
    boolean hasRole(PrincipalCollection subjectPrincipal, String roleIdentifier) {
        assert false // not expected
        return false
    }

    @Override
    boolean[] hasRoles(PrincipalCollection subjectPrincipal, List<String> roleIdentifiers) {
        assert false // not expected
        return new boolean[0]
    }

    @Override
    boolean hasAllRoles(PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers) {
        assert false // not expected
        return false
    }

    @Override
    void checkRole(PrincipalCollection subjectPrincipal, String roleIdentifier) throws AuthorizationException {
        assert false // not expected
    }

    @Override
    void checkRoles(PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers) throws AuthorizationException {
        if (!roleOk) throw new AuthorizationException("Bugger.")
    }

    @Override
    String getName() {
        return null
    }

    @Override
    boolean supports(AuthenticationToken token) {
        return false
    }

    @Override
    AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return null
    }
}

