package org.apache.shiro.grails

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * User: pmcneil
 * Date: 20/06/19
 *
 */
@CompileStatic
@TypeChecked
class AccessControlMethodArgs implements TypedNamedArgs{

    final Boolean auth

    AccessControlMethodArgs(Map args) {
        setUpArgs(['auth' : Boolean], args)
        this.auth = (Boolean)args.auth
    }

}
