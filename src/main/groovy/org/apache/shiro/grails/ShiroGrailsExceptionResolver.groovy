package org.apache.shiro.grails

import grails.web.mapping.UrlMappingInfo
import grails.web.mapping.UrlMappingsHolder
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.UnauthenticatedException
import org.grails.core.exceptions.GrailsRuntimeException
import org.grails.web.errors.GrailsExceptionResolver
import org.grails.web.mapping.DefaultUrlMappingInfo
import org.grails.web.mapping.UrlMappingUtils
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.InvocationTargetException

/**
 * User: pmcneil
 * Date: 5/07/19
 *
 */
class ShiroGrailsExceptionResolver extends GrailsExceptionResolver {

    @Override
    ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                  Object handler, Exception ex) {

        Exception authEx = findAuthException(ex)
        if (authEx) {
            UrlMappingsHolder urlMappings = lookupUrlMappings()
            return resolveViewOrRedirect(authEx, urlMappings, request, response)
        } else {
            return super.resolveException(request, response, handler, ex)
        }
    }

    protected Exception findAuthException(Exception ex) {
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


    protected UrlMappingInfo matchAuthStatusCode(Exception ex, UrlMappingsHolder urlMappings) {
        int statusCode = (ex instanceof UnauthenticatedException) ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN

        UrlMappingInfo info = urlMappings.matchStatusCode(statusCode, ex)
        if (info == null) {
            info = urlMappings.matchStatusCode(statusCode,
                    getRootCause(ex))
        }
        if (info == null) {
            info = urlMappings.matchStatusCode(statusCode)
        }
        return info
    }

    protected ModelAndView resolveViewOrRedirect(Exception ex, UrlMappingsHolder urlMappings, HttpServletRequest request,
                                                 HttpServletResponse response) {

        UrlMappingInfo info = matchAuthStatusCode(ex, urlMappings)

        if (info != null) {
            Map params = extractRequestParamsWithUrlMappingHolder(urlMappings, request)
            if (params != null && !params.isEmpty()) {
                Map infoParams = info.getParameters()
                if (infoParams != null) {
                    params.putAll(info.getParameters())
                }
                info = new DefaultUrlMappingInfo(info, params, grailsApplication)
            }
        }

        try {
            if (info != null && info.getViewName() != null) {
                resolveView(request, info, new ModelAndView())
            } else if (info != null && info.getControllerName() != null) {
                String uri = determineUri(request)
                if (!response.isCommitted()) {

                    request.session["Authmsg"] = ex.message.startsWith('This subject is anonymous -') ? 'You need to log in to do this.' : ex.message
                    String forwardUrl = UrlMappingUtils.buildDispatchUrlForMapping(info)
                    response.sendRedirect(forwardUrl + "?targetUri=$uri")

                    // return an empty ModelAndView since the error handler has been processed
                    return new ModelAndView()
                }
            }
            return mv
        }
        catch (Exception e) {
            LOG.error("Unable to render errors view: " + e.getMessage(), e)
            throw new GrailsRuntimeException(e)
        }
    }

}
