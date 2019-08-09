package shiro.tester

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration
import shiro.tester.Application
import spock.lang.Unroll

@Integration(applicationClass = Application)
class NotAuthenticatedControllerSpec extends GebSpec {

    @Unroll
    def "Authentication required for action #theUrl"() {
        given:
        go 'auth/signOut'

        when:
        go theUrl

        then: "needs authentication due to accessControl - overridden by interceptor's onNotAuthenticated method"
        $().text().startsWith(val)

        where:
        theUrl                              | val
        'notAuthenticated/index'            | 'override'
        'notAuthenticated/list'             | 'override'
    }

    @Unroll
    def "Authentication NOT required for action #theUrl"() {
        given:
        go 'auth/signOut'

        when:
        go theUrl

        then: "does not need any authentication due to interceptor exclusion"
        $().text().startsWith(val)

        where:
        theUrl                              | val
        'notAuthenticated/publicAction'     | 'public'
    }
}
