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

    @Unroll
    def "When not logged in service method annotations return [#value] for #theUrl @#select"() {
        when:
        go theUrl

        then:
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
        if (select) {
            $(select).text().contains(value)
        } else {
            $().text().contains(value)
        }

        where:
        user      | password   | theUrl                        | select | value
        'admin'   | 'admin'    | 'annotatedService/unsecured'  | ''     | 'Unsecured: one'
        'admin'   | 'admin'    | 'annotatedService/guest'      | ''     | 'Guest: two'
        'admin'   | 'admin'    | 'annotatedService/user'       | ''     | 'User: three'
        'admin'   | 'admin'    | 'annotatedService/role'       | ''     | 'Not Authorized (403)'
        'admin'   | 'admin'    | 'annotatedService/permission' | ''     | 'Not Authorized (403)'
        'dilbert' | 'password' | 'annotatedService/role'       | ''     | 'Role: five'
        'dilbert' | 'password' | 'annotatedService/permission' | ''     | 'Permission: six'
        //LDAP users
        'fbloggs' | 'password' | 'annotatedService/unsecured'  | ''     | 'Unsecured: one'
        'fbloggs' | 'password' | 'annotatedService/guest'      | ''     | 'Guest: two'
        'fbloggs' | 'password' | 'annotatedService/user'       | ''     | 'User: three'
        'fbloggs' | 'password' | 'annotatedService/role'       | ''     | 'Not Authorized (403)' //not User but user
        'fbloggs' | 'password' | 'annotatedService/permission' | ''     | 'Permission: six'
        'pmcneil' | 'idunno'   | 'annotatedService/role'       | ''     | 'Not Authorized (403)' //not User but user
        'pmcneil' | 'idunno'   | 'annotatedService/permission' | ''     | 'Permission: six'
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
        $().text().contains('Not Authorized (403)')

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
        $().text().contains('Not Authorized (403)')
    }

}
