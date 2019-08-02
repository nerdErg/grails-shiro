package shiro.tester

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration
import pages.LoginPage
import shiro.tester.Application
import spock.lang.Unroll

@Integration(applicationClass = Application)
class InterceptControllerSpec extends GebSpec {

    @Unroll
    def "Authentication required for action #theUrl"() {
        given:
        go 'auth/signOut'

        when:
        go theUrl

        then: "needs authentication due to accessControl"
        at LoginPage

        where:
        theUrl             | val
        'intercept/index'  | ''
        'intercept/list'   | ''
        'intercept/create' | ''
        'intercept/save'   | ''
        'intercept/show'   | ''
        'intercept/edit'   | ''
        'intercept/update' | ''
        'intercept/delete' | ''
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
        $().text().startsWith(val)

        where:
        user      | password   | theUrl                | val
        'admin'   | 'admin'    | 'intercept/index'     | 'index'
        'admin'   | 'admin'    | 'intercept/list'      | 'list'
        'admin'   | 'admin'    | 'intercept/show'      | 'Unauthorized' //because there is a @RequiresRoles("test")
        'admin'   | 'admin'    | 'intercept/create'    | 'create'
        'admin'   | 'admin'    | 'intercept/save'      | 'save'
        'admin'   | 'admin'    | 'intercept/edit'      | 'edit'
        'admin'   | 'admin'    | 'intercept/update'    | 'update'
        'admin'   | 'admin'    | 'intercept/delete'    | 'delete'
        'admin'   | 'admin'    | 'intercept/annotated' | 'Unauthorized' //because there is a @RequiresRoles("test")

        'test1'   | 'test1'    | 'intercept/index'     | 'index'
        'test1'   | 'test1'    | 'intercept/list'      | 'list'
        'test1'   | 'test1'    | 'intercept/show'      | 'show'
        'test1'   | 'test1'    | 'intercept/create'    | 'create'
        'test1'   | 'test1'    | 'intercept/save'      | 'save'
        'test1'   | 'test1'    | 'intercept/edit'      | 'edit'
        'test1'   | 'test1'    | 'intercept/update'    | 'update'
        'test1'   | 'test1'    | 'intercept/delete'    | 'delete'
        'test1'   | 'test1'    | 'intercept/annotated' | 'Unauthorized' //because accessControl says no

        'dilbert' | 'password' | 'intercept/index'     | 'index'
        'dilbert' | 'password' | 'intercept/list'      | 'list'
        'dilbert' | 'password' | 'intercept/show'      | 'Unauthorized' //because there is a @RequiresRoles("test")
        'dilbert' | 'password' | 'intercept/create'    | 'Unauthorized'
        'dilbert' | 'password' | 'intercept/save'      | 'Unauthorized'
        'dilbert' | 'password' | 'intercept/edit'      | 'Unauthorized'
        'dilbert' | 'password' | 'intercept/update'    | 'Unauthorized'
        'dilbert' | 'password' | 'intercept/delete'    | 'Unauthorized'
        //LDAP users
        'fbloggs' | 'password' | 'intercept/index'     | 'Unauthorized' // no book:read
        'fbloggs' | 'password' | 'intercept/list'      | 'Unauthorized' // no book:read
        'fbloggs' | 'password' | 'intercept/show'      | 'Unauthorized' //because there is a @RequiresRoles("test")
        'fbloggs' | 'password' | 'intercept/create'    | 'Unauthorized'
        'fbloggs' | 'password' | 'intercept/save'      | 'Unauthorized'
        'fbloggs' | 'password' | 'intercept/edit'      | 'Unauthorized'
        'fbloggs' | 'password' | 'intercept/update'    | 'Unauthorized'
        'fbloggs' | 'password' | 'intercept/delete'    | 'Unauthorized'

        'pmcneil' | 'idunno'   | 'intercept/index'     | 'index'
        'pmcneil' | 'idunno'   | 'intercept/list'      | 'list'
        'pmcneil' | 'idunno'   | 'intercept/show'      | 'Unauthorized' //because there is a @RequiresRoles("test")
        'pmcneil' | 'idunno'   | 'intercept/create'    | 'create'
        'pmcneil' | 'idunno'   | 'intercept/save'      | 'save'
        'pmcneil' | 'idunno'   | 'intercept/edit'      | 'edit'
        'pmcneil' | 'idunno'   | 'intercept/update'    | 'update'
        'pmcneil' | 'idunno'   | 'intercept/delete'    | 'delete'
        'pmcneil' | 'idunno'   | 'intercept/annotated' | 'Unauthorized' //because there is a @RequiresRoles("test")

    }
}
