package com.nerderg

import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresGuest
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.apache.shiro.authz.annotation.RequiresRoles
import org.apache.shiro.authz.annotation.RequiresUser

class SecuredMethodsService {

    /**
     * Public access
     */
    def methodOne() {
        return 'one'
    }

    /**
     * Requires the current Subject to be a &quot;guest&quot;, that is, they are not authenticated <em>or</em> remembered
     * from a previous session for the annotated class/instance/method to be accessed or invoked.
     */
    @RequiresGuest
    def methodTwo() {
        return 'two'
    }

    /**
     * Requires the current Subject to be an application <em>user</em> for the annotated class/instance/method to be
     * accessed or invoked.  This is <em>less</em> restrictive than the {@link RequiresAuthentication RequiresAuthentication}
     * annotation.
     */
    @RequiresUser
    def methodThree() {
        return 'three'
    }

    /**
     * Requires the current Subject to have been authenticated <em>during their current session</em> for the annotated
     * class/instance/method to be accessed or invoked.  This is <em>more</em> restrictive than the
     * {@link RequiresUser RequiresUser} annotation.
     */
    @RequiresAuthentication
    def methodFour() {
        return 'four'
    }

    /**
     * Requires the currently executing {@link org.apache.shiro.subject.Subject Subject} to have all of the
     * specified roles. If they do not have the role(s), the method will not be executed and
     * an {@link org.apache.shiro.authz.AuthorizationException AuthorizationException} is thrown.
     */
    @RequiresRoles('user')
    def methodFive() {
        return 'five'
    }

    /**
     * Requires the current Subject to imply a particular permission in
     * order to execute the annotated method.
     */
    @RequiresPermissions("book:view")
    def methodSix() {
        return 'six'
    }
}
