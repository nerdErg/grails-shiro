package shiro.tester

import geb.spock.GebReportingSpec
import geb.spock.GebSpec
import pages.LoginPage
import spock.lang.Unroll

import grails.testing.mixin.integration.Integration

@Integration(applicationClass = Application)
class AccessByConventionSpec extends GebSpec {

/*
Users and roles
User: admin -> Roles: Administrator, Permissions: none
User: dilbert -> Roles: user, Permissions: book:show,index,read
User: test1 -> Roles: user, test, Permissions: book:*, custom:read,write
User: fbloggs -> Roles: user, Permissions: book:index,list book:show:* cart:*
User: pmcneil -> Roles: admin, editor, user, Permissions: book:* user:* book:read,write book:index,list book:show:* book:write
 */

    @Unroll
    def "Book controller requires authentication #theUrl"() {
        given:
        go 'auth/signOut'

        when:
        go theUrl

        then:
        at LoginPage

        where:
        theUrl        | run
        'book/index'  | 1
        'book/show'   | 2
        'book/create' | 3
        'book/save'   | 4
        'book/edit'   | 5
        'book/update' | 6
        'book/delete' | 7
    }

    @Unroll
    def "User #user/#password with correct Roles has [#val] for url: #theUrl"() {
        given:
        go 'auth/signOut'

        when:
        go theUrl

        then:
        at LoginPage

        when: "The user"
        loginForm.username = user
        loginForm.password = password
        signIn.click()

        then:
        println "\n----Test $user gets $val trying $theUrl"
        println $().text()
        $().text().contains(val)

        where:
        user      | password   | theUrl        | val
        'admin'   | 'admin'    | 'book/index'  | 'Not Authorized'
        'admin'   | 'admin'    | 'book/create' | 'Not Authorized'
        'admin'   | 'admin'    | 'book/save'   | 'Not Authorized'
        'admin'   | 'admin'    | 'book/show/1' | 'Not Authorized'
        'admin'   | 'admin'    | 'book/edit/1' | 'Not Authorized'
        'admin'   | 'admin'    | 'book/update' | 'Not Authorized'
        'admin'   | 'admin'    | 'book/delete' | 'Not Authorized'

        'test1'   | 'test1'    | 'book/index'  | 'Book List'
        'test1'   | 'test1'    | 'book/create' | 'Create Book'
        'test1'   | 'test1'    | 'book/show/1' | 'Show Book'
        'test1'   | 'test1'    | 'book/edit/1' | 'Edit Book'
        // save update and delete need a post - this is enough though

        'dilbert' | 'password' | 'book/index'  | 'Book List'
        'dilbert' | 'password' | 'book/create' | 'Not Authorized'
        'dilbert' | 'password' | 'book/save'   | 'Not Authorized'
        'dilbert' | 'password' | 'book/show/1' | 'Show Book'
        'dilbert' | 'password' | 'book/edit'   | 'Not Authorized'
        'dilbert' | 'password' | 'book/update' | 'Not Authorized'
        'dilbert' | 'password' | 'book/delete' | 'Not Authorized'

        //LDAP users
        'pmcneil' | 'idunno'   | 'book/index'  | 'Book List'
        'pmcneil' | 'idunno'   | 'book/create' | 'Create Book'
        'pmcneil' | 'idunno'   | 'book/show/1' | 'Show Book'
        'pmcneil' | 'idunno'   | 'book/edit/1' | 'Edit Book'

        'fbloggs' | 'password' | 'book/index'  | 'Book List'
        'fbloggs' | 'password' | 'book/create' | 'Not Authorized'
        'fbloggs' | 'password' | 'book/save'   | 'Not Authorized'
        'fbloggs' | 'password' | 'book/show/1' | 'Show Book'
        'fbloggs' | 'password' | 'book/edit'   | 'Not Authorized'
        'fbloggs' | 'password' | 'book/update' | 'Not Authorized'
        'fbloggs' | 'password' | 'book/delete' | 'Not Authorized'

    }
}