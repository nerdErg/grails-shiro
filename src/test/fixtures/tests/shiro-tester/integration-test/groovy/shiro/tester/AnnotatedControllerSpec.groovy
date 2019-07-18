package shiro.tester

import shiro.tester.Application
import geb.spock.GebReportingSpec
import geb.spock.GebSpec
import pages.LoginPage
import spock.lang.Unroll

import grails.testing.mixin.integration.Integration

@Integration(applicationClass = Application)
class AnnotatedControllerSpec extends GebSpec {

    @Unroll
    def "RequiresAuthentication on controller class works on every action #theUrl"() {
        given:
        go 'auth/signOut'

        when:
        go theUrl

        then:
        at LoginPage

        where:
        theUrl             | val
        'annotated/index'  | ''
        'annotated/list'   | ''
        'annotated/create' | ''
        'annotated/save'   | ''
        'annotated/show'   | ''
        'annotated/edit'   | ''
        'annotated/update' | ''
        'annotated/delete' | ''
    }

    @Unroll
    def "User #user/#password gets '#val' for url: #theUrl"() {
        given:
        go 'auth/signOut'

        when:
        go theUrl

        then:
        at LoginPage

        when:
        //test1 user has role test
        loginForm.username = user
        loginForm.password = password
        signIn.click()

        then:
        println $().text() //.contains(val)
        $().text().contains(val)

        where:
        user      | password   | theUrl             | val
        'admin'   | 'admin'    | 'annotated/index'  | 'Not Authorized (403)'
        'admin'   | 'admin'    | 'annotated/list'   | 'Not Authorized (403)'
        'admin'   | 'admin'    | 'annotated/create' | 'Not Authorized (403)'
        'admin'   | 'admin'    | 'annotated/save'   | 'Not Authorized (403)'
        'admin'   | 'admin'    | 'annotated/show'   | 'Not Authorized (403)'
        'admin'   | 'admin'    | 'annotated/edit'   | 'Not Authorized (403)'
        'admin'   | 'admin'    | 'annotated/update' | 'Not Authorized (403)'
        'admin'   | 'admin'    | 'annotated/delete' | 'Not Authorized (403)'

        'test1'   | 'test1'    | 'annotated/index'  | 'list'
        'test1'   | 'test1'    | 'annotated/list'   | 'list'
        'test1'   | 'test1'    | 'annotated/create' | 'create'
        'test1'   | 'test1'    | 'annotated/save'   | 'save'
        'test1'   | 'test1'    | 'annotated/show'   | 'show'
        'test1'   | 'test1'    | 'annotated/edit'   | 'edit'
        'test1'   | 'test1'    | 'annotated/update' | 'update'
        'test1'   | 'test1'    | 'annotated/delete' | 'delete'

        'dilbert' | 'password' | 'annotated/index'  | 'list'
        'dilbert' | 'password' | 'annotated/list'   | 'list'
        'dilbert' | 'password' | 'annotated/create' | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/save'   | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/show'   | 'show'
        'dilbert' | 'password' | 'annotated/edit'   | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/update' | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/delete' | 'Not Authorized (403)'

        //LDAP users
        'pmcneil' | 'idunno'   | 'annotated/index'  | 'Not Authorized (403)' //not in group User with capital U
        'pmcneil' | 'idunno'   | 'annotated/list'   | 'list'
        'pmcneil' | 'idunno'   | 'annotated/create' | 'create'
        'pmcneil' | 'idunno'   | 'annotated/save'   | 'save'
        'pmcneil' | 'idunno'   | 'annotated/show'   | 'show'
        'pmcneil' | 'idunno'   | 'annotated/edit'   | 'edit'
        'pmcneil' | 'idunno'   | 'annotated/update' | 'update'
        'pmcneil' | 'idunno'   | 'annotated/delete' | 'delete'

        'fbloggs' | 'password' | 'annotated/index'  | 'Not Authorized (403)' //not in group User with capital U
        'fbloggs' | 'password' | 'annotated/list'   | 'list'
        'fbloggs' | 'password' | 'annotated/create' | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/save'   | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/show'   | 'show'
        'fbloggs' | 'password' | 'annotated/edit'   | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/update' | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/delete' | 'Not Authorized (403)'

    }
}
