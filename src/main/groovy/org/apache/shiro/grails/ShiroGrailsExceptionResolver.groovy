package org.apache.shiro.grails

import grails.artefact.controller.support.ResponseRedirector
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.UnauthenticatedException
import org.codehaus.groovy.runtime.InvokerInvocationException
import org.grails.exceptions.ExceptionUtils
import org.grails.web.servlet.mvc.exceptions.GrailsMVCException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.InvocationTargetException
/**
 * User: pmcneil
 * Date: 5/07/19
 *
 */
class ShiroGrailsExceptionResolver  extends SimpleMappingExceptionResolver implements GrailsApplicationAware, ResponseRedirector {

    protected GrailsApplication grailsApplication

    @Override
    ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                  Object handler, Exception ex) {

        Exception authEx = findAuthException(ex)
        if (authEx) {
            if(authEx instanceof UnauthenticatedException) {
                loginRedirect(grailsApplication, request)
            }
            if(authEx instanceof AuthorizationException) {
                unauthorizedRedirect(grailsApplication, request)
            }
            return new ModelAndView()
        }
        return null
    }

    private static Exception findAuthException(Exception ex) {
        Throwable e = findWrappedException(ex)
        if(e instanceof UnauthenticatedException || e instanceof AuthorizationException) {
            return e
        }
        if(e instanceof InvocationTargetException) {
            if(e.targetException instanceof UnauthenticatedException) {
                return (Exception) e.targetException
            }
            e = getRootCause(e)
            if(e instanceof AuthorizationException) {
                return (Exception) e
            }
        }
        return null
    }

    private static Exception findWrappedException(Exception e) {
        if ((e instanceof InvokerInvocationException)||(e instanceof GrailsMVCException)) {
            Throwable t = getRootCause(e)
            if (t instanceof Exception) {
                e = (Exception) t
            }
        }
        return e
    }

    private static Throwable getRootCause(Throwable ex) {
        return ExceptionUtils.getRootCause(ex)
    }

    private static void loginRedirect(GrailsApplication grailsApplication, HttpServletRequest request) {

        String targetUri = getTargetUri(request)
        String redirectUri = grailsApplication.config.getProperty('security.shiro.login.uri')

        if (redirectUri) {
            redirectUri(uri: redirectUri)
        } else {
            redirectUri(
                    controller: (grailsApplication.config.getProperty('security.shiro.login.controller') ?: "auth"),
                    action: (grailsApplication.config.getProperty('security.shiro.login.action') ?: "login"),
                    params: [targetUri: targetUri.toString()]
            )
        }
    }

    static void unauthorizedRedirect(GrailsApplication grailsApplication, HttpServletRequest request) {
        String targetUri = getTargetUri(request)
        String redirectUri = grailsApplication.config.getProperty('security.shiro.unauthorized.uri')
        if (redirectUri) {
            redirectUri(uri: redirectUri)
        } else {
            redirectUri(
                    controller: (grailsApplication.config.getProperty('security.shiro.unauthorized.controller') ?: "auth"),
                    action: (grailsApplication.config.getProperty('security.shiro.unauthorized.action') ?: "unauthorized"),
                    params: [targetUri: targetUri.toString()]
            )
        }
    }

    private static String getTargetUri(HttpServletRequest request) {
        // we only redirect relative to our application context to prevent login redirect spoofing
        request.forwardURI[request.contextPath.size()..-1] + cleanUpQueryString(request.queryString)
    }

    private static String cleanUpQueryString(String query) {
        query ? '?' + query.replaceFirst(/^\?/, '') : ''
    }

    @Override
    void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
    }
}
