/*
 * Copyright 2007 Peter Ledbrook.
 *
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
 *
 *
 * Modified 2009 Bradley Beddoes, Intient Pty Ltd, Ported to Apache Ki
 * Modified 2009 Kapil Sachdeva, Gemalto Inc, Ported to Apache Shiro
 */
package org.apache.shiro.grails

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.Permission
import org.apache.shiro.subject.Subject

class ShiroTagLib {
    static namespace = "shiro"

    /**
     * This tag only writes its body to the output if the current user
     * is logged in.
     */
    def isLoggedIn = { attrs, body ->
        if (checkAuthenticated()) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * is not logged in.
     */
    def isNotLoggedIn = { attrs, body ->
        if (!checkAuthenticated()) {
            out << body()
        }
    }

    /**
     * A synonym for 'isLoggedIn'. This is the same name as used by
     * the standard Shiro tag library.
     */
    def authenticated = isLoggedIn

    /**
     * A synonym for 'isNotLoggedIn'. This is the same name as used by
     * the standard Shiro tag library.
     */
    def notAuthenticated = isNotLoggedIn

    /**
     * This tag only writes its body to the output if the current user
     * is either logged in or remembered from a previous session (via
     * the "remember me" cookie).
     */
    def user = { attrs, body ->
        if (SecurityUtils.subject.principal != null) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * is neither logged in nor remembered from a previous session (via
     * the "remember me" cookie).
     */
    def notUser = { attrs, body ->
        if (SecurityUtils.subject.principal == null) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * is remembered from a previous session (via the "remember me"
     * cookie) but not currently logged in.
     */
    def remembered = { attrs, body ->
        if (SecurityUtils.subject.principal != null && !checkAuthenticated()) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * is not remembered from a previous session (via the "remember me"
     * cookie). This is the case if they are a guest user or logged in.
     */
    def notRemembered = { attrs, body ->
        if (SecurityUtils.subject.principal == null || checkAuthenticated()) {
            out << body()
        }
    }

    /**
     * <p>Outputs the string form of the current user's identity. By default
     * this assumes that the subject has only one principal; its string
     * representation is written to the page.</p>
     * Optional attributes:
     * <ul>
     * <li><i>type</i>: Species the type or class of the principal to use</li>
     * <li><i>property</i>: Specifies the name of the property on the
     * principal to use as the string representation, e.g.
     * <code>firstName</code></li>
     * </ul>
     *
     * If the property doesn't exist an exception is thrown
     */
    def principal = { attrs ->
        Subject subject = SecurityUtils.subject

        if (subject != null) {
            def principal = getTypedPrincipal((String) attrs.type)
            if (principal != null) {
                String prop = attrs.property
                out << getPrincipalProperty(prop, principal).encodeAsHTML()
            }
        }
    }

    private String getPrincipalProperty(String prop, principal) {
        if (principal) {
            if (prop &&
                    ((principal instanceof Map && principal.containsKey(prop)) ||
                            principal.hasProperty(prop))) {
                return principal."${prop}".toString()
            }
            if (prop) {
                throwTagError("No property ${prop} on principal type ${principal.getClass().name}")
            }
            return principal.toString()
        }
        //this shouldn't happen so throw an NPE if it does.
        throw new NullPointerException("Principal is not set.")
    }

    private static def getTypedPrincipal(String type) {
        if (type) {
            Class clazz = Class.forName(type)
            return SecurityUtils.subject.getPrincipals().oneByType((Class) clazz)
        }
        return SecurityUtils.subject.principal
    }


    /**
     * This tag only writes its body to the output if the current user
     * has the given role.
     */
    def hasRole = { attrs, body ->
        if (checkRole(attrs, "hasRole")) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * does not have the given role.
     */
    def lacksRole = { attrs, body ->
        if (!checkRole(attrs, "lacksRole")) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * has all the given roles (inversion of lacksAnyRole).
     */
    def hasAllRoles = { attrs, body ->
        Collection<String> inList = (Collection<String>) attrs.in
        if (!inList)
            throwTagError("Tag [hasAllRoles] must have [in] attribute.")
        if (SecurityUtils.subject.hasAllRoles(inList)) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * has none of the given roles (inversion of hasAllRoles).
     */
    def lacksAnyRole = { attrs, body ->
        Collection<String> inList = (Collection<String>) attrs.in
        if (!inList)
            throwTagError("Tag [lacksAnyRole] must have [in] attribute.")
        if (!SecurityUtils.subject.hasAllRoles(inList)) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * has any of the given roles (inversion of lacksAllRoles).
     */
    def hasAnyRole = { attrs, body ->
        List<String> inList = (List<String>) attrs.in
        if (!inList)
            throwTagError("Tag [hasAnyRole] must have [in] attribute.")
        if (SecurityUtils.subject.hasRoles(inList).any()) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * doesn't have all of the given roles (inversion of hasAnyRole).
     */
    def lacksAllRoles = { attrs, body ->
        List<String> inList = (List<String>) attrs.in
        if (!inList)
            throwTagError("Tag [lacksAllRoles] must have [in] attribute.")
        if (!SecurityUtils.subject.hasRoles(inList).any()) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * has the given permission.
     */
    def hasPermission = { attrs, body ->
        if (checkPermission(attrs, "hasPermission")) {
            out << body()
        }
    }

    /**
     * This tag only writes its body to the output if the current user
     * does not have the given permission.
     */
    def lacksPermission = { attrs, body ->
        if (!checkPermission(attrs, "lacksPermission")) {
            out << body()
        }
    }

    /**
     * Checks whether the current user is authenticated or not. Returns
     * <code>true</code> if the user is authenticated, otherwise
     * <code>false</code>.
     */
    private static boolean checkAuthenticated() {
        // Get the user's security context.
        return SecurityUtils.subject.authenticated
    }

    /**
     * Checks whether the current user has the role specified in the
     * given tag attributes. Returns <code>true</code> if the user
     * has the role, otherwise <code>false</code>.
     */
    private boolean checkRole(attrs, tagname) {
        String roleName = attrs.name
        List<String> inList = (List<String>) attrs.in
        if (roleName) {
            return SecurityUtils.subject.hasRole(roleName)
        } else if (inList) {
            log.warn("Use of tags [hasRole/lacksRole] with attribute [in] is deprecated. Use tags [hasAnyRole/lacksAllRoles] instead.")
            boolean[] results = SecurityUtils.subject.hasRoles(inList)
            return results.any()
        }
        throwTagError("Tag [$tagname] must have one of [name] or [in] attributes.")
        return false
    }

    /**
     * Checks whether the current user has the permission specified in
     * the given tag attributes. Returns <code>true</code> if the user
     * has the permission, otherwise <code>false</code>.
     */
    private boolean checkPermission(attrs, tagName) {

        def permission = attrs.permission

        if (permission && ((permission instanceof Permission) || (permission instanceof CharSequence))) {
            if (permission instanceof Permission) {
                return SecurityUtils.subject.isPermitted((Permission) permission)
            }
            return SecurityUtils.subject.isPermitted((String) permission)
        }

        throwTagError("Tag $tagName must have a [permission] which is a string or an instance of org.apache.shiro.authz.Permission.")
        return false
    }
}
