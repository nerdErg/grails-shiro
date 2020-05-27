package org.apache.shiro.grails

/**
 * User: pmcneil
 * Date: 27/5/20
 *
 * replace apache.commons.NotImplementedException
 */
class NotImplementedException extends Exception {

    NotImplementedException(String message) {
        super(message)
    }

}
