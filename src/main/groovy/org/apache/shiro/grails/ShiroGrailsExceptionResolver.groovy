package org.apache.shiro.grails

import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import grails.web.api.WebAttributes
import grails.web.mapping.LinkGenerator
import grails.web.mapping.ResponseRedirector
import grails.web.mapping.mvc.RedirectEventListener
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.UnauthenticatedException
import org.codehaus.groovy.runtime.InvokerInvocationException
import org.grails.exceptions.ExceptionUtils
import org.grails.web.servlet.mvc.exceptions.GrailsMVCException
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
import org.springframework.web.servlet.support.RequestDataValueProcessor

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.InvocationTargetException
/**
 * User: pmcneil
 * Date: 5/07/19
 *
 */
class ShiroGrailsExceptionResolver  extends SimpleMappingExceptionResolver implements GrailsApplicationAware {

    protected GrailsApplication grailsApplication
    private LinkGenerator linkGenerator
    private RequestDataValueProcessor requestDataValueProcessor
    private Collection<RedirectEventListener> redirectListeners

    @Autowired(required=false)
    void setRedirectListeners(Collection<RedirectEventListener> redirectListeners) {
        this.redirectListeners = redirectListeners
    }

    @Autowired(required = false)
    void setRequestDataValueProcessor(RequestDataValueProcessor requestDataValueProcessor) {
        this.requestDataValueProcessor = requestDataValueProcessor
    }

    @Autowired
    void setGrailsLinkGenerator(LinkGenerator linkGenerator) {
        this.linkGenerator = linkGenerator
    }

    LinkGenerator getGrailsLinkGenerator() {
        if(this.linkGenerator == null) {
            this.linkGenerator = webRequest.getApplicationContext().getBean(LinkGenerator)
        }
        return this.linkGenerator
    }

    @Override
    ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                  Object handler, Exception ex) {
        if(request.getAttribute(GrailsApplicationAttributes.REDIRECT_ISSUED)){
            println("************ Already redirected!")
        }
        Exception authEx = findAuthException(ex)
        if (authEx) {
            if(authEx instanceof UnauthenticatedException) {
                loginRedirect(request, response)
            }
            if(authEx instanceof AuthorizationException) {
                unauthorizedRedirect(request, response)
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

    private void loginRedirect(HttpServletRequest request, HttpServletResponse response) {

        String targetUri = getTargetUri(request)
        String redirectUri = grailsApplication.config.getProperty('security.shiro.login.uri')

        if (redirectUri) {
            redirect([uri: redirectUri],request, response)
        } else {
            redirect(
                    [controller: (grailsApplication.config.getProperty('security.shiro.login.controller') ?: "auth"),
                    action: (grailsApplication.config.getProperty('security.shiro.login.action') ?: "login"),
                    params: [targetUri: targetUri?.toString()]],request, response
            )
        }
    }

    private void unauthorizedRedirect(HttpServletRequest request, HttpServletResponse response) {
        String targetUri = getTargetUri(request)
        String redirectUri = grailsApplication.config.getProperty('security.shiro.unauthorized.uri')
        if (redirectUri) {
            redirect([uri: redirectUri],request, response)
        } else {
            redirect(
                    [controller: (grailsApplication.config.getProperty('security.shiro.unauthorized.controller') ?: "auth"),
                    action: (grailsApplication.config.getProperty('security.shiro.unauthorized.action') ?: "unauthorized"),
                    params: [targetUri: targetUri.toString()]], request, response
            )
        }
    }

    /**
     * Redirects for the given arguments.
     *
     * @param argMap The arguments
     * @return null
     */
    void redirect(Map argMap, HttpServletRequest request, HttpServletResponse response) {

        if (argMap.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments for method 'redirect': $argMap")
        }

        getGrailsLinkGenerator()

        ResponseRedirector redirector = new ResponseRedirector(getGrailsLinkGenerator())
        redirector.setRedirectListeners redirectListeners
        redirector.setRequestDataValueProcessor requestDataValueProcessor
        redirector.setUseJessionId false

        redirector.redirect request, response, argMap
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
