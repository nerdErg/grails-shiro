package org.apache.shiro.grails

import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import javax.servlet.http.HttpServletRequest
import grails.artefact.Interceptor

@CompileStatic
@TypeChecked
class AccessControl {

    /**
     * Implementation of the "accessControl()" dynamic method available to Interceptors. It requires a reference to the
     * interceptor so that it can access the dynamic properties and methods, such as "request" and "redirect()".
     *
     * Called from ShiroGrailsPlugin
     *
     * @param grailsApplication
     * @param interceptor
     * @param authcRequired - boolean if authentication is required by default
     * @param args
     * @param closure
     * @return true/false true if access is granted
     */
    static boolean accessControlMethod(GrailsApplication grailsApplication,
                                       Interceptor interceptor,
                                       boolean authcRequired,
                                       Map args = [:],
                                       Closure closure = null) {

        AccessControlMethodArgs accessControlArgs = new AccessControlMethodArgs(args)
        Subject subject = SecurityUtils.subject

        // If the user needs to be authenticate this session. (auth arg trumps authcRequired)
        boolean authenticatedUserRequired = accessControlArgs.auth || (accessControlArgs.auth == null && authcRequired)
        boolean authenticationRequired = subject.principal == null || (authenticatedUserRequired && !subject.authenticated)

        if (authenticationRequired) {

            // Call the interceptors onNotAuthenticated if it has one.
            boolean doDefault = onNotAuthenticated(subject, interceptor)

            if (doDefault) {
                loginRedirect(grailsApplication, interceptor)
            }

            return false
        }

        boolean permitted = (closure ? accessByAuthClosure(subject, interceptor, closure) : accessByConvention(interceptor, subject))
        if (!permitted) {

            // Call the interceptors onUnauthorized if it has one.
            boolean doDefault = onUnauthorized(subject, interceptor)

            if (doDefault) {
                unauthorizedRedirect(grailsApplication, interceptor)
            }
        }
        return permitted
    }

    static void loginRedirect(GrailsApplication grailsApplication, Interceptor interceptor) {

        String targetUri = getTargetUri(interceptor.request)
        String redirectUri = grailsApplication.config.getProperty('security.shiro.login.uri')

        if (redirectUri) {
            interceptor.redirect(uri: redirectUri) // + "?targetUri=${targetUri.encodeAsURL()}")
        } else {
            interceptor.redirect(
                    controller: (grailsApplication.config.getProperty('security.shiro.login.controller') ?: "auth"),
                    action: (grailsApplication.config.getProperty('security.shiro.login.action') ?: "login"),
                    params: [targetUri: targetUri.toString()]
            )
        }
    }

    static void unauthorizedRedirect(GrailsApplication grailsApplication, Interceptor interceptor) {
        String targetUri = getTargetUri(interceptor.request)
        String redirectUri = grailsApplication.config.getProperty('security.shiro.unauthorized.uri')
        if (redirectUri) {
            interceptor.redirect(uri: redirectUri)
        } else {
            interceptor.redirect(
                    controller: (grailsApplication.config.getProperty('security.shiro.unauthorized.controller') ?: "auth"),
                    action: (grailsApplication.config.getProperty('security.shiro.unauthorized.action') ?: "unauthorized"),
                    params: [targetUri: targetUri.toString()]
            )
        }
    }

    private static boolean onNotAuthenticated(Subject subject, Interceptor interceptor) {
        Class interceptorClass = interceptor.class
        if (interceptorClass.metaClass.respondsTo(interceptorClass, "onNotAuthenticated")) {
            return interceptor.invokeMethod('onNotAuthenticated',[subject, interceptor])
        }
        return true
    }

    private static boolean onUnauthorized(Subject subject, Interceptor interceptor) {
        Class interceptorClass = interceptor.class
        if (interceptorClass.metaClass.respondsTo(interceptorClass, "onUnauthorized")) {
            return interceptor.invokeMethod('onUnauthorized',[subject, interceptor])
        }
        return true
    }

    private static String getTargetUri(HttpServletRequest request) {
        println "forwardUri: " + request.forwardURI[request.contextPath.size()..-1] //we only redirect relative to our
        // application context to prevent login redirect spoofing
        request.forwardURI[request.contextPath.size()..-1] + cleanUpQueryString(request.queryString)
    }

    private static String cleanUpQueryString(String query) {
        query ? '?' + query.replaceFirst(/^\?/, '') : ''
    }


    /*
     * Call the closure with the access control builder and check the result.
     * The closure will return true if the user is permitted access.
     */
    private static boolean accessByAuthClosure(Subject subject, Interceptor interceptor, Closure closure) {
        closure.delegate = new FilterAccessControlBuilder(subject, interceptor)
        return closure.call()
    }

    private static boolean accessByConvention(Interceptor interceptor, Subject subject) {
        StringBuilder permString = new StringBuilder()
        permString << interceptor.controllerName << ':' << (interceptor.actionName ?: "index")

        // Add the ID if it's in the web parameters.
        if (interceptor.params.id) {
            permString << ':' << interceptor.params.list('id').join(',')
        }

        return subject.isPermitted(permString.toString())
    }

}
