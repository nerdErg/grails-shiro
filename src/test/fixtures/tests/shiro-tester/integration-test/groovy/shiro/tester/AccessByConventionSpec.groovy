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
        'admin'   | 'admin'    | 'book/index'  | 'Unauthorized'
        'admin'   | 'admin'    | 'book/create' | 'Unauthorized'
        'admin'   | 'admin'    | 'book/save'   | 'Unauthorized'
        'admin'   | 'admin'    | 'book/show/1' | 'Unauthorized'
        'admin'   | 'admin'    | 'book/edit/1' | 'Unauthorized'
        'admin'   | 'admin'    | 'book/update' | 'Unauthorized'
        'admin'   | 'admin'    | 'book/delete' | 'Unauthorized'

        'test1'   | 'test1'    | 'book/index'  | 'Book List'
        'test1'   | 'test1'    | 'book/create' | 'Create Book'
        'test1'   | 'test1'    | 'book/show/1' | 'Show Book'
        'test1'   | 'test1'    | 'book/edit/1' | 'Edit Book'
        // save update and delete need a post - this is enough though

        'dilbert' | 'password' | 'book/index'  | 'Book List'
        'dilbert' | 'password' | 'book/create' | 'Unauthorized'
        'dilbert' | 'password' | 'book/save'   | 'Unauthorized'
        'dilbert' | 'password' | 'book/show/1' | 'Show Book'
        'dilbert' | 'password' | 'book/edit'   | 'Unauthorized'
        'dilbert' | 'password' | 'book/update' | 'Unauthorized'
        'dilbert' | 'password' | 'book/delete' | 'Unauthorized'

        //LDAP users
        'pmcneil' | 'idunno'   | 'book/index'  | 'Book List'
        'pmcneil' | 'idunno'   | 'book/create' | 'Create Book'
        'pmcneil' | 'idunno'   | 'book/show/1' | 'Show Book'
        'pmcneil' | 'idunno'   | 'book/edit/1' | 'Edit Book'

        'fbloggs' | 'password' | 'book/index'  | 'Book List'
        'fbloggs' | 'password' | 'book/create' | 'Unauthorized'
        'fbloggs' | 'password' | 'book/save'   | 'Unauthorized'
        'fbloggs' | 'password' | 'book/show/1' | 'Show Book'
        'fbloggs' | 'password' | 'book/edit'   | 'Unauthorized'
        'fbloggs' | 'password' | 'book/update' | 'Unauthorized'
        'fbloggs' | 'password' | 'book/delete' | 'Unauthorized'

    }
}