package org.apache.shiro.grails

import grails.config.Config
import grails.core.GrailsApplication
import grails.web.mapping.ResponseRedirector
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.UnauthenticatedException
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.testing.GrailsUnitTest
import org.springframework.web.servlet.ModelAndView
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.InvocationTargetException

/**
 * User: pmcneil
 * Date: 27/5/20
 *
 */
class ShiroExceptionResolverSpec extends Specification implements GrailsUnitTest {

    ResponseRedirector mockRedirector
    GrailsApplication grailsApplication
    Config config
    Map configMap = [:]

    def setup() {
        mockRedirector = Mock()
        grailsApplication = Mock()
        config = Mock()
        grailsApplication.config >> config
        config.getProperty(_) >> { String v ->
            configMap[v]
        }
    }

    def cleanup() {
    }

    void "test getAuthmsg"() {
        when:
        Exception ex = new UnauthenticatedException("This subject is anonymous - you can't do that naughty boy")

        then: "we get the replacement message"
        ShiroGrailsExceptionResolver.getAuthmsg(ex) == 'You need to log in to do this.'

        when:
        ex = new AuthorizationException()

        then:
        ShiroGrailsExceptionResolver.getAuthmsg(ex) == null

        when:
        ex = new AuthorizationException("Sorry Dave, can't let you do that")

        then:
        ShiroGrailsExceptionResolver.getAuthmsg(ex) == "Sorry Dave, can't let you do that"
    }

    void "test redirects"() {
        given:
        ShiroGrailsExceptionResolver exceptionResolver = new ShiroGrailsExceptionResolver()
        exceptionResolver.setResponseRedirector(mockRedirector)
        exceptionResolver.grailsApplication = grailsApplication
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest()
        mockRequest.setQueryString("?max=100")
        mockRequest.setContextPath("/rainbow")
        mockRequest.setForwardURI("/rainbow/somewhere/something")
        HttpServletResponse mockResponse = Mock()
        Map args = [url: 'blah']

        when: "we call redirect"
        exceptionResolver.redirect(args, mockRequest, mockResponse)

        then: "the responseRedirector.redirect method is called"
        1 * mockRedirector.redirect(mockRequest, mockResponse, args)

        when: "I call login redirect with login uri set"
        configMap.put('security.shiro.login.uri', 'secruity/womble/login')
        exceptionResolver.loginRedirect(mockRequest, mockResponse)

        then: "it redirects to secruity/womble/login"
        1 * mockRedirector.redirect(mockRequest, mockResponse, [uri: 'secruity/womble/login'])

        when: "I call login redirect without login uri set"
        configMap.remove('security.shiro.login.uri')
        exceptionResolver.loginRedirect(mockRequest, mockResponse)

        then: "it redirects to default auth/login via controller/action map"
        1 * mockRedirector.redirect(mockRequest, mockResponse,
                [controller: 'auth',
                 action    : 'login',
                 params    : [targetUri: '/somewhere/something?max=100']])

        when: "I call login redirect with controller and action set"
        configMap.put('security.shiro.login.controller', 'foo')
        configMap.put('security.shiro.login.action', 'bar')
        exceptionResolver.loginRedirect(mockRequest, mockResponse)

        then: "it redirects to foo/bar via controller/action map"
        1 * mockRedirector.redirect(mockRequest, mockResponse,
                [controller: 'foo',
                 action    : 'bar',
                 params    : [targetUri: '/somewhere/something?max=100']])

        /* * */

        when: "I call unauthorized redirect with unauthorized uri set"
        configMap.put('security.shiro.unauthorized.uri', 'secruity/womble/bad-bad-person')
        exceptionResolver.unauthorizedRedirect(mockRequest, mockResponse)

        then: "it redirects to secruity/womble/bad-bad-person"
        1 * mockRedirector.redirect(mockRequest, mockResponse, [uri: 'secruity/womble/bad-bad-person'])

        when: "I call unauthorized redirect without unauthorized uri set"
        configMap.remove('security.shiro.unauthorized.uri')
        exceptionResolver.unauthorizedRedirect(mockRequest, mockResponse)

        then: "it redirects to default auth/unauthorized via controller/action map"
        1 * mockRedirector.redirect(mockRequest, mockResponse,
                [controller: 'auth',
                 action    : 'unauthorized',
                 params    : [targetUri: '/somewhere/something?max=100']])

        when: "I call unauthorized redirect with controller and action set"
        configMap.put('security.shiro.unauthorized.controller', 'foodo')
        configMap.put('security.shiro.unauthorized.action', 'bad-bar')
        exceptionResolver.unauthorizedRedirect(mockRequest, mockResponse)

        then: "it redirects to foodo/bad-bar via controller/action map"
        1 * mockRedirector.redirect(mockRequest, mockResponse,
                [controller: 'foodo',
                 action    : 'bad-bar',
                 params    : [targetUri: '/somewhere/something?max=100']])
    }

    void "test find Auth Exception"() {
        when:
        Exception aux = new AuthorizationException("You can't")
        Exception uax = new UnauthenticatedException("Authenticate!",aux)

        then: "Should find Unauthenticated first"
        ShiroGrailsExceptionResolver.findAuthException(uax) == uax
        ShiroGrailsExceptionResolver.findAuthException(aux) == aux
        ShiroGrailsExceptionResolver.findAuthException(new Exception("wrap",uax)) == uax
        ShiroGrailsExceptionResolver.findAuthException(new Exception("wrap",aux)) == aux

    }

    void "resolving exceptions should work"() {
        given:
        ShiroGrailsExceptionResolver exceptionResolver = new ShiroGrailsExceptionResolver()
        exceptionResolver.setResponseRedirector(mockRedirector)
        exceptionResolver.grailsApplication = grailsApplication
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest()
        mockRequest.setQueryString("?max=100")
        mockRequest.setContextPath("/rainbow")
        mockRequest.setForwardURI("/rainbow/somewhere/something")
        HttpServletResponse mockResponse = Mock()
        Exception aux = new AuthorizationException("You can't")
        Exception uax = new UnauthenticatedException("Authenticate!",aux)

        when: "we try to resolve a wrapped Unauthenticated exception"
        Exception targetEx = new InvocationTargetException(uax)
        ModelAndView mv1 = exceptionResolver.resolveException(mockRequest, mockResponse, null, targetEx)

        then: "it redirects to default auth/login via controller/action map"
        mv1
        mv1 instanceof ModelAndView
        1 * mockRedirector.redirect(mockRequest, mockResponse,
                [controller: 'auth',
                 action    : 'login',
                 params    : [targetUri: '/somewhere/something?max=100']])

        when: "we try to resolve a wrapped Authorization exception"
        Exception targetEx2 = new InvocationTargetException(aux)
        ModelAndView mv2 = exceptionResolver.resolveException(mockRequest, mockResponse, null, targetEx2)

        then: "it redirects to default auth/unauthorized via controller/action map"
        mv2
        mv2 instanceof ModelAndView
        1 * mockRedirector.redirect(mockRequest, mockResponse,
                [controller: 'auth',
                 action    : 'unauthorized',
                 params    : [targetUri: '/somewhere/something?max=100']])

        when: "we try to resolve a wrapped other exception"
        Exception targetEx3 = new InvocationTargetException(new Exception("foo"))
        ModelAndView mv3 = exceptionResolver.resolveException(mockRequest, mockResponse, null, targetEx3)

        then: "it doesn't redirect and returns null"
        mv3 == null
        0 * mockRedirector.redirect(_,_,_)
    }
}
