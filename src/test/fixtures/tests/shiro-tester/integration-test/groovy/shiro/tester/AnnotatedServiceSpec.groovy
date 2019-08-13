package shiro.tester

import shiro.tester.Application
import geb.spock.GebReportingSpec
import geb.spock.GebSpec
import org.openqa.selenium.Cookie
import pages.*
import spock.lang.Unroll
import grails.testing.mixin.integration.Integration

@Integration(applicationClass = Application)
class AnnotatedServiceSpec extends GebSpec {

/*
Users and roles
User: admin -> Roles: Administrator, Permissions: none
User: dilbert -> Roles: user, Permissions: book:show,index,read
User: test1 -> Roles: user, test, Permissions: book:*, custom:read,write
User: fbloggs -> Roles: user, Permissions: book:index,list book:show:* cart:*
User: pmcneil -> Roles: admin, editor, user, Permissions: book:* user:* book:read,write book:index,list book:show:* book:write
 */

    @Unroll
    def "When not logged in service method annotations return [#value] for #theUrl @#select"() {
        when:
        go theUrl

        then:
        println "\n----Test anonymous gets $value trying $theUrl"
        if (select) {
            $(select).text().contains(value)
        } else {
            $().text().contains(value)
        }

        where:
        theUrl                        | select  | value
        'annotatedService/unsecured'  | ''      | 'Unsecured: one'
        'annotatedService/guest'      | ''      | 'Guest: two'
        'annotatedService/role'       | 'title' | 'Login'
        'annotatedService/permission' | 'title' | 'Login'
        'annotatedService/user'       | 'title' | 'Login'
    }

    @Unroll
    def "When logged in as #user/#password service method annotations returns [#value] for #theUrl @#select"() {
        given:
        go 'auth/signOut'

        when:
        to LoginPage

        then:
        at LoginPage

        when:
        loginForm.username = user
        loginForm.password = password
        signIn.click()

        and:
        go theUrl

        then:
        println "\n----Test $user gets $value trying $theUrl"
        println $().text()
        $().text().startsWith(value)

        where:
        user      | password   | theUrl                        | value
        'admin'   | 'admin'    | 'annotatedService/unsecured'  | 'Unsecured: one'
        'admin'   | 'admin'    | 'annotatedService/guest'      | 'Login' //not a guest, authenticated
        'admin'   | 'admin'    | 'annotatedService/user'       | 'User: three'
        'admin'   | 'admin'    | 'annotatedService/role'       | 'Unauthorized'
        'admin'   | 'admin'    | 'annotatedService/permission' | 'Unauthorized'
        'dilbert' | 'password' | 'annotatedService/role'       | 'Role: five'
        'dilbert' | 'password' | 'annotatedService/permission' | 'Unauthorized'
        //LDAP users
        'fbloggs' | 'password' | 'annotatedService/unsecured'  | 'Unsecured: one'
        'fbloggs' | 'password' | 'annotatedService/guest'      | 'Login' //not a guest, authenticated
        'fbloggs' | 'password' | 'annotatedService/user'       | 'User: three'
        'fbloggs' | 'password' | 'annotatedService/role'       | 'Role: five'
        'fbloggs' | 'password' | 'annotatedService/permission' | 'Unauthorized'
        'pmcneil' | 'idunno'   | 'annotatedService/role'       | 'Role: five'
        'pmcneil' | 'idunno'   | 'annotatedService/permission' | 'Permission: six'
    }

    def "When rememberMe is used and not authenticated in this session"() {
        given:
        go 'auth/signOut'

        when:
        to LoginPage

        then:
        at LoginPage

        when:
        loginForm.username = 'admin'
        loginForm.password = 'admin'
        loginForm.rememberMe = true
        signIn.click()

        and:
        browser.driver.manage().deleteCookieNamed("JSESSIONID")
        go 'annotatedService/user'

        then:
        //has the rememberMe cookie set
        browser.driver.manage().cookies.find { Cookie cookie -> cookie.name == 'rememberMe' } != null
        $().text().contains('User: three')

        when:
        go 'annotatedService/authenticated'

        then:
        at LoginPage

        when:
        loginForm.username = 'admin'
        loginForm.password = 'admin'
        signIn.click()

        then:
        $().text().contains('Authenticated: four')
    }

    def "Wildcard permissions should work"() {
        given:
        go 'auth/signOut'

        when:
        go 'annotatedService/permission'

        then:
        at LoginPage

        when:
        //test1 user has permission book:*
        loginForm.username = 'test1'
        loginForm.password = 'test1'
        signIn.click()

        then:
        //should redirect
        $().text().contains('Permission: six')
    }

    def "Check secured Class requires authentication asks for login"() {
        given:
        go 'auth/signOut'

        when:
        go 'annotatedService/unrestricted'

        then:
        at LoginPage

        when:
        //test1 user has permission book:*
        loginForm.username = 'dilbert'
        loginForm.password = 'password'
        signIn.click()

        then:
        //should redirect
        $().text().contains('secure class: unrestricted')

        when:
        go 'annotatedService/administrator'

        then:
        $().text().contains('Unauthorized')

    }

    def "Check secured Class requires authentication asks for login before refusing on role"() {
        given:
        go 'auth/signOut'

        when:
        go 'annotatedService/administrator'

        then:
        at LoginPage

        when:
        loginForm.username = 'dilbert'
        loginForm.password = 'password'
        signIn.click()

        then:
        $().text().contains('Unauthorized')
    }

}
