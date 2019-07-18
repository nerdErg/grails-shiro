package org.apache.shiro.grails

/**
 * User: pmcneil
 * Date: 10/07/19
 *
 */
/**
 * A simple holder for a principle that abstracts the underlying object
 * This *must* implement Serializable or remember me won't work.
 */
interface PrincipalHolder {

        Set<String> getRoles()

        Set<String> getPermissions()

        String toString()
}