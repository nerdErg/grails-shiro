/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.shiro.grails

import groovy.transform.CompileStatic
import org.apache.shiro.authc.*
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.Authorizer
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.UnauthorizedException
import org.apache.shiro.authz.permission.InvalidPermissionStringException
import org.apache.shiro.authz.permission.PermissionResolver
import org.apache.shiro.authz.permission.PermissionResolverAware
import org.apache.shiro.realm.Realm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Simple implementation of the Shiro Realm that wraps a 'Realm' artifact. It is basically an adapter between
 * the Grails world and the Shiro world.
 *
 * @author Peter McNeil
 */
//TODO removed static compilation from class and placed on methods because of groovy compiler error.
//"Couldn't find trait helper classes on compile classpath!" see Traits.java line 159
//This doesn't happen if you remove the implements clause from the trait. Needs further investigation.
//@CompileStatic
trait GrailsShiroRealm implements Realm, Authorizer, PermissionResolverAware, LogoutAware {

    private PermissionResolver permResolver = null
    final static Logger LOG = LoggerFactory.getLogger(GrailsShiroRealm.class)
    Class tokenClass

    @SuppressWarnings("unused")
    @CompileStatic
    void setTokenClass(Class clazz) {
        this.tokenClass = clazz
    }

    /**
     * This is a placeholder method for the target Realm. If you don't implement authenticate then you'll get
     * a NotImplementedException
     *
     * @param authenticationToken AuthenticationToken to authenticate
     * @return AuthenticationInfo* @throws AuthenticationException if not authenticated.
     */
    @CompileStatic
    AuthenticationInfo authenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
        throw new NotImplementedException("authenticate has not been implemented in the realm")
    }

    /**
     * A helper method to create a SimpleAuthenticationInfo from a collection of principal objects. This does not set any
     * credentials.
     * @param principals
     * @return SimpleAuthenticationInfo without any credentials
     */
    @SuppressWarnings("unused")
    @CompileStatic
    SimpleAuthenticationInfo makeSimpleAuthInfoSansCredentials(Collection principals) {
        return new SimpleAuthenticationInfo(new SimplePrincipalCollection(principals, getName()), null)
    }

    /**
     * A helper method to create a SimpleAuthenticationInfo from a principal object. This does not set any
     * credentials.
     * @param principals
     * @return SimpleAuthenticationInfo without any credentials
     */
    @SuppressWarnings("unused")
    @CompileStatic
    SimpleAuthenticationInfo makeSimpleAuthInfoSansCredentials(Object principal) {
        return new SimpleAuthenticationInfo(new SimplePrincipalCollection([principal], getName()), null)
    }

    /**
     * Set the permission resolver. This is initially set to WildcardPermissionResolver unless replaced via spring
     * @param pr
     */
    @CompileStatic
    void setPermissionResolver(PermissionResolver pr) {
        this.permResolver = pr
    }

    @CompileStatic
    PermissionResolver getPermissionResolver() {
        permResolver
    }

    /**
     * Wraps the authenticate method logging unable to authenticate exceptions
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @CompileStatic
    AuthenticationInfo getAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // If the target realm has an 'authenticate' method, we use that.
        try {
            authenticate(authenticationToken)
        }
        catch (Exception ex) {
            ((Logger) LOG).info "Unable to authenticate with ${getName()} - ${ex.message}"
            throw ex
        }
    }

    /* (non-Javadoc)
     * @see org.apache.shiro.realm.Realm#getName()
     */

    @CompileStatic
    String getName() {
        return this.class.name
    }

    /**
     * Default implementation, checks the tokenClass
     *
     * @see org.apache.shiro.realm.Realm#supports(org.apache.shiro.authc.AuthenticationToken)
     */
    @CompileStatic
    boolean supports(AuthenticationToken authenticationToken) {
        if (this.tokenClass) {
            return this.tokenClass.isAssignableFrom(authenticationToken.getClass())
        } else {
            return false
        }
    }

    /**
     * Placeholder do nothing on logout override as required
     *
     * @see org.apache.shiro.authc.LogoutAware#onLogout(PrincipalCollection)
     */
    @CompileStatic
    void onLogout(PrincipalCollection principal) {
    }

    /**
     * Check for a permission
     * @see org.apache.shiro.authz.Authorizer#checkPermission(org.apache.shiro.subject.PrincipalCollection, org.apache.shiro.authz.Permission)
     * @param principal
     * @param permission
     * @throws AuthorizationException
     */
    @CompileStatic
    void checkPermission(PrincipalCollection principal, Permission permission) throws AuthorizationException {
        if (!isPermitted(principal, permission)) {
            throw new UnauthorizedException("User '${principal.getPrimaryPrincipal()}' does not have permission '${permission}'")
        }
    }

    /**
     * Check for a set of permissions, the user must have all permissions.
     * @see org.apache.shiro.authz.Authorizer#checkPermissions(org.apache.shiro.subject.PrincipalCollection, Collection < Permission >)
     * @param principal
     * @param permissions
     * @throws AuthorizationException
     */
    @CompileStatic
    void checkPermissions(PrincipalCollection principal, Collection<Permission> permissions) throws AuthorizationException {
        if (!isPermittedAll(principal, permissions)) {
            throw new UnauthorizedException("User '${principal.getPrimaryPrincipal()}' does not have the required permissions.")
        }
    }

    /**
     * Check user has a named role
     * @see org.apache.shiro.authz.Authorizer#checkRole(PrincipalCollection, java.lang.String)
     * @param principal
     * @param role
     * @throws AuthorizationException
     */
    @CompileStatic
    void checkRole(PrincipalCollection principal, String role) throws AuthorizationException {
        if (!hasRole(principal, role)) {
            throw new UnauthorizedException("User '${principal.getPrimaryPrincipal()}' does not have role '${role}'")
        }
    }

    /**
     * Check a user has a set of named roles. The user must have all the roles.
     * @see org.apache.shiro.authz.Authorizer#checkRoles(PrincipalCollection, Collection < String >)
     * @param principal
     * @param roles
     * @throws AuthorizationException
     */
    @CompileStatic
    void checkRoles(PrincipalCollection principal, Collection<String> roles) throws AuthorizationException {
        if (!hasAllRoles(principal, roles)) {
            throw new UnauthorizedException("User '${principal.getPrimaryPrincipal()}' does not have all these roles: ${roles}")
        }
    }

    /**
     * This should be implemented in your realm either as the simplified version in SimplifiedRealm
     * or a just override this.
     *
     * @see org.apache.shiro.authz.Authorizer#hasAllRoles(PrincipalCollection, Collection < String >)
     * @param principalCollection
     * @param roles
     * @return true if they have all the roles
     */
    @CompileStatic
    boolean hasAllRoles(PrincipalCollection principalCollection, Collection<String> roles) {
        hasAllRoles(principalCollection.primaryPrincipal, roles)
    }

    @CompileStatic
    boolean hasAllRoles(Object principal, Collection<String> roles) {
        LOG.error("hasAllRoles has not been implemented in the Realm ${getName()}")
        return false
    }

    /**
     * This should be implemented in your realm either as the simplified version in SimplifiedRealm
     * or a just override this.
     *
     * @see org.apache.shiro.authz.Authorizer#hasRole(PrincipalCollection, String)
     * @param principalCollection
     * @param role
     * @return true if they have this role
     */
    @CompileStatic
    boolean hasRole(PrincipalCollection principalCollection, String role) {
        return hasRole(principalCollection.primaryPrincipal, role)
    }

    @CompileStatic
    boolean hasRole(Object principal, String roleName) {
        LOG.error("hasRole has not been implemented in the Realm ${getName()}")
        return false
    }

    /**
     * Default implementation using hasRole in a loop override if you can do better.
     *
     * @see org.apache.shiro.authz.Authorizer#hasRoles(PrincipalCollection, List < String >)
     * @param principalCollection
     * @param roles
     * @return array of boolean mapped to roles in list
     */
    @CompileStatic
    boolean[] hasRoles(PrincipalCollection principalCollection, List<String> roles) {

        boolean[] retval = new boolean[roles.size()]
        for (int i in 0..<roles.size()) {
            retval[i] = hasRole(principalCollection, roles[i])
        }

        return retval
    }

    /**
     * This should be implemented in your realm either as the simplified version in SimplifiedRealm
     * or a just override this.
     *
     * @see org.apache.shiro.authz.Authorizer#isPermitted(PrincipalCollection, org.apache.shiro.authz.Permission)
     * @param principalCollection
     * @param permission
     * @return true if the principal has this permission
     */
    @CompileStatic
    boolean isPermitted(PrincipalCollection principalCollection, Permission permission) {
        return isPermitted((Object) principalCollection.primaryPrincipal, permission)
    }

    @CompileStatic
    boolean isPermitted(Object principal, Permission requiredPermission) {
        LOG.error("isPermitted has not been implemented in the Realm ${getName()}")
        return false
    }

    /**
     * Default implementation of @see org.apache.shiro.authz.Authorizer#isPermitted(Object, java.util.List) calling
     * isPermitted in a loop. Override if you want any different.
     *
     * @param principalCollection
     * @param permissions
     * @return Array of booleans indicating the permissions the user has.
     */
    @CompileStatic
    boolean[] isPermitted(PrincipalCollection principalCollection, List<Permission> permissions) {
        boolean[] retval = new boolean[permissions.size()]

        for (int i in 0..<retval.length) {
            retval[i] = isPermitted(principalCollection, permissions[i])
        }
        return retval
    }

    /**
     * Default implementation of @see org.apache.shiro.authz.Authorizer#isPermittedAll(Object, java.util.Collection) using
     * isPermitted.
     *
     * @param principalCollection
     * @param permissions
     * @return true if all user has all permissions
     */
    @CompileStatic
    boolean isPermittedAll(PrincipalCollection principalCollection, Collection<Permission> permissions) {
        permissions.each { permission ->
            if (!isPermitted(principalCollection, permission)) return false
        }
        return true
    }

    /**
     * Ensures the corresponding Subject/user implies the specified permission String.
     *
     * @param principal a PrincipalCollection
     * @param s the permission String to use
     * @throws AuthorizationException if the user does not have the permission, or the permission string
     *                                is Invalid
     */
    @CompileStatic
    void checkPermission(PrincipalCollection principal, String s) throws AuthorizationException {
        checkPermission(principal, toPermission(s))
    }

    /**
     * Ensures the corresponding Subject/user
     * {@link Permission#implies(Permission) implies} all of the
     * specified permission strings.
     *
     * @param principal a PrincipalCollection
     * @param strings the permission Strings to use
     * @throws AuthorizationException if the user does not have the permission, or the permission string
     *                                is Invalid
     */
    @CompileStatic
    void checkPermissions(PrincipalCollection principal, String... strings) throws AuthorizationException {
        checkPermissions(principal, toPermissionList(strings))
    }

    /**
     * Convert the permission string to a Permission and then check it. The Authorizer interface
     * does not specify that it can throw an Authorization exception which is sensible for an boolean
     * method. So if the PermissionResolver.resolvePermission() throws an InvalidPermissionStringException
     * we return false
     *
     * @param principal a PrincipalCollection
     * @param s the permission String to use
     * @return true if permitted
     * @see #isPermitted(PrincipalCollection principals, String permission)
     */
    @CompileStatic
    boolean isPermitted(PrincipalCollection principal, String s) {
        try {
            return isPermitted(principal, toPermission(s))
        } catch (AuthorizationException aex) {
            // if the permission isn't valid then it's *not* permitted
            System.out.println(aex.getMessage())
            return false
        }
    }

    /**
     * Checks if the corresponding Subject implies the given permission strings and returns a boolean array
     * indicating which permissions are implied.
     * <p>
     * The Authorizer interface does not specify that it can throw an Authorization exception which is
     * sensible for an boolean method. So if the PermissionResolver.resolvePermission() throws an
     * InvalidPermissionStringException we return boolean[0]
     *
     * @param principal a PrincipalCollection
     * @param strings the permission Strings to use
     * @return boolean array true if permitted
     */
    @CompileStatic
    boolean[] isPermitted(PrincipalCollection principal, String... strings) {
        try {
            return isPermitted(principal, toPermissionList(strings))
        } catch (AuthorizationException aex) {
            // if the permission isn't valid then it's *not* permitted
            System.out.println(aex.getMessage())
            return new boolean[0]
        }
    }

    /**
     * Returns <tt>true</tt> if the corresponding Subject/user implies all of the specified permission strings,
     * <tt>false</tt> otherwise.
     *
     * The Authorizer interface does not specify that it can throw an Authorization exception which is
     * sensible for an boolean method. So if the PermissionResolver.resolvePermission() throws an
     * InvalidPermissionStringException we return boolean[0]
     *
     * @param principal a PrincipalCollection
     * @param strings the permission Strings to use
     * @return boolean true if permitted
     */
    @CompileStatic
    boolean isPermittedAll(PrincipalCollection principal, String... strings) {
        try {
            return isPermittedAll(principal, toPermissionList(strings))
        } catch (AuthorizationException aex) {
            // if the permission isn't valid then it's *not* permitted
            System.out.println(aex.getMessage())
            return false
        }
    }

    /**
     * Same as {@link #checkRoles(org.apache.shiro.subject.PrincipalCollection, java.util.Collection)
     * checkRoles(PrincipalCollection subjectPrincipal, Collection&lt;String&gt; roleIdentifiers)} but doesn't require a collection
     * as an argument.
     *
     * @param principal a PrincipalCollection
     * @param roles role name strings
     * @throws AuthorizationException if the user does not have all of the specified roles.
     */
    @CompileStatic
    void checkRoles(PrincipalCollection principal, String... roles) throws AuthorizationException {
        checkRoles(principal, Arrays.asList(roles))
    }

    /**
     * Converts a single permission string into a Permission instances.
     */
    @CompileStatic
    private Permission toPermission(String s) throws AuthorizationException {
        if (permissionResolver == null) return null
        try {
            return permissionResolver.resolvePermission(s)
        } catch (InvalidPermissionStringException ex) {
            //most resolvers don't seem to throw this, but it doesn't mean they can't so pass it on.
            throw new AuthorizationException("Invalid Permission String: " + s +
                    "\nCannot authorize a subject with an Invalid Permission String. Error: " + ex.getMessage())
        }
    }

    /**
     * Converts an array of string permissions into a list of
     * {@link org.apache.shiro.authz.permission.WildcardPermission} instances.
     */
    @CompileStatic
    private List<Permission> toPermissionList(String[] strings) throws AuthorizationException {
        List<Permission> permissions = new ArrayList<>(strings.length)
        for (String string : strings) {
            permissions.add(toPermission(string))
        }
        return permissions
    }
}
