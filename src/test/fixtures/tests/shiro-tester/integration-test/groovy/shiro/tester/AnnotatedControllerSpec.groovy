package shiro.tester

import shiro.tester.Application
import geb.spock.GebReportingSpec
import geb.spock.GebSpec
import pages.LoginPage
import spock.lang.Unroll

import grails.testing.mixin.integration.Integration

@Integration(applicationClass = Application)
class AnnotatedControllerSpec extends GebSpec {

/*
Users and roles
User: admin -> Roles: Administrator, Permissions: none
User: dilbert -> Roles: user, Permissions: book:show,index,read
User: test1 -> Roles: user, test, Permissions: book:*, custom:read,write
User: fbloggs -> Roles: user, Permissions: book:index,list book:show:* cart:*
User: pmcneil -> Roles: admin, editor, user, Permissions: book:* user:* book:read,write book:index,list book:show:* book:write
 */

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
        println "\n----Test $user gets $val trying $theUrl"
        println $().text() //.contains(val)
        $().text().startsWith(val)

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

        'dilbert' | 'password' | 'annotated/index'  | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/list'   | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/create' | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/save'   | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/show'   | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/edit'   | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/update' | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotated/delete' | 'Not Authorized (403)'

        //LDAP users
        'pmcneil' | 'idunno'   | 'annotated/index'  | 'list'
        'pmcneil' | 'idunno'   | 'annotated/list'   | 'list'
        'pmcneil' | 'idunno'   | 'annotated/create' | 'create'
        'pmcneil' | 'idunno'   | 'annotated/save'   | 'save'
        'pmcneil' | 'idunno'   | 'annotated/show'   | 'show'
        'pmcneil' | 'idunno'   | 'annotated/edit'   | 'edit'
        'pmcneil' | 'idunno'   | 'annotated/update' | 'update'
        'pmcneil' | 'idunno'   | 'annotated/delete' | 'delete'

        'fbloggs' | 'password' | 'annotated/index'  | 'list'
        'fbloggs' | 'password' | 'annotated/list'   | 'list'
        'fbloggs' | 'password' | 'annotated/create' | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/save'   | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/show'   | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/edit'   | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/update' | 'Not Authorized (403)'
        'fbloggs' | 'password' | 'annotated/delete' | 'Not Authorized (403)'

    }
}
