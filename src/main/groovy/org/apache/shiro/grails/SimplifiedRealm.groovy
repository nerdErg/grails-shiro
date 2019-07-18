package org.apache.shiro.grails

import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authz.Permission

/**
 * User: pmcneil
 * Date: 1/07/19
 *
 */
interface SimplifiedRealm {

    AuthenticationInfo authenticate(AuthenticationToken authenticationToken) throws AuthenticationException

    boolean hasRole(Object principal, String roleName)
    boolean hasAllRoles(Object principal, Collection<String> roles)
    boolean isPermitted(Object principal, Permission requiredPermission)

}