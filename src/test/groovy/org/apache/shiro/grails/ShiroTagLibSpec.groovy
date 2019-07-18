/*
 * Copyright 2007 Peter Ledbrook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.shiro.grails

import grails.testing.web.taglib.TagLibUnitTest
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermission
import spock.lang.Specification
import spock.lang.Shared

import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.Subject
import org.grails.taglib.GrailsTagException
import spock.lang.Unroll

/**
 * Test case for {@link ShiroTagLib}.
 */
class ShiroTagLibSpec extends Specification implements TagLibUnitTest<ShiroTagLib> {
    @Shared
    Map mockSubject

    void setup() {

        // A map that backs the mock Subject instance.
        mockSubject = [
                isAuthenticated: { -> false },
                isPermitted    : { Object -> true }
        ]

        SecurityUtils.metaClass.static.getSubject = { -> mockSubject as Subject }

    }

    void "test IsLoggedIn, authenticated"() {
        expect: "Not to be logged in"
        "" == applyTemplate('<shiro:isLoggedIn>You are logged in!</shiro:isLoggedIn>')
        "" == applyTemplate('<shiro:authenticated>You are logged in!</shiro:authenticated>')

        when: "we authenticate the user"
        mockSubject["isAuthenticated"] = { -> true }

        then: "we are logged in"
        "You are logged in!" == applyTemplate('<shiro:isLoggedIn>You are logged in!</shiro:isLoggedIn>')
        "You are logged in!" == applyTemplate('<shiro:authenticated>You are logged in!</shiro:authenticated>')
    }

    void "test IsNotLoggedIn, notAuthenticated"() {
        expect: "not to be logged in"
        "You are not logged in!" == applyTemplate('<shiro:isNotLoggedIn>You are not logged in!</shiro:isNotLoggedIn>')
        "You are not logged in!" == applyTemplate('<shiro:notAuthenticated>You are not logged in!</shiro:notAuthenticated>')

        when: "we authenticate the user"
        mockSubject["isAuthenticated"] = { -> true }

        then: "we are logged in"
        "" == applyTemplate('<shiro:isNotLoggedIn>You are not logged in!</shiro:isNotLoggedIn>')
        "" == applyTemplate('<shiro:notAuthenticated>You are not logged in!</shiro:notAuthenticated>')
    }

    @Unroll
    void "test principal tag #name"() {
        given: "no user"
        mockSubject["getPrincipal"] = { -> null }

        expect: "tag to return (noUser)"
        noUser == applyTemplate(tag)

        when: "Now try with a 'remembered' user, i.e. the principal is not null, but the user is not authenticated."
        mockSubject["getPrincipal"] = { -> "admin" }

        then: "The tag returns the (remembered)"
        remembered == applyTemplate(tag)

        when: "we authenticate the user."
        mockSubject["isAuthenticated"] = { -> true }

        then: "the tag returns the (authenticated)"
        authenticated == applyTemplate(tag)

        where:
        name            | tag                                               | noUser | remembered | authenticated
        'user'          | '<shiro:user>blah</shiro:user>'                   | ''     | 'blah'     | 'blah'
        'notUser'       | '<shiro:notUser>blah</shiro:notUser>'             | 'blah' | ''         | ''
        'remembered'    | '<shiro:remembered>blah</shiro:remembered>'       | ''     | 'blah'     | ''
        'notRemembered' | '<shiro:notRemembered>blah</shiro:notRemembered>' | 'blah' | ''         | 'blah'
        'principal'     | '<shiro:principal/>'                              | ''     | 'admin'    | 'admin'
    }

    void "test principal tag encoding"() {
        given: "a principal called <admin>"
        mockSubject["getPrincipal"] = { -> "<admin>" }

        expect: "we get an html encoded response"
        "&lt;admin&gt;" == applyTemplate('<shiro:principal/>')
    }

    void "test principal tag with type"() {
        given:
        mockSubject["getPrincipal"] = { -> null }

        PrincipalCollection principalCollection = Mock()
        //noinspection GroovyAssignabilityCheck
        principalCollection.oneByType(_) >> { Class clazz ->
            if (clazz == String) return "admin"
            if (clazz == Map) return [name: "admin", first: "Peter"]
            if (clazz == TestPrincipal) return new TestPrincipal()
            return null
        }
        mockSubject["getPrincipals"] = { -> principalCollection }

        expect:
        "" == applyTemplate('<shiro:principal type="java.lang.Integer"/>')
        "admin" == applyTemplate('<shiro:principal type="java.lang.String"/>')
        "Peter" == applyTemplate('<shiro:principal type="java.util.Map" property="first"/>')
        "Steve" == applyTemplate('<shiro:principal type="org.apache.shiro.grails.TestPrincipal" property="firstName"/>')

        when: "I ask for a 'property'(key) on a Map that doesn't exist"
        applyTemplate('<shiro:principal type="java.util.Map" property="second"/>')

        then: "I get an exception"
        GrailsTagException ex = thrown()
        ex.message.contains("No property second")

        when: "I ask for a 'property' on an Object that doesn't exist"
        applyTemplate('<shiro:principal type="org.apache.shiro.grails.TestPrincipal" property="first"/>')

        then: "I get an exception"
        GrailsTagException ex2 = thrown()
        ex2.message.contains("No property first")
    }

    @Unroll
    void "test hasRole tag #name"() {
        when: "the user has role"
        mockSubject["hasRole"] = { String name -> name == role }

        then: "we get the expected result"
        result == applyTemplate(tag)

        where:
        name          | role   | tag                                                           | result
        'hasRole 1'   | 'User' | '<shiro:hasRole name="Administrator">yup</shiro:hasRole>'     | ''
        'hasRole 2'   | 'User' | '<shiro:hasRole name="User">yup</shiro:hasRole>'              | 'yup'
        'lacksRole 1' | 'User' | '<shiro:lacksRole name="Administrator">yup</shiro:lacksRole>' | 'yup'
        'lacksRole 2' | 'User' | '<shiro:lacksRole name="User">yup</shiro:lacksRole>'          | ''

    }

    void "test missing name attribute on hasRole"() {
        when: "we don't supply the 'name' attribute."
        mockSubject["hasRole"] = { String name -> name == role }
        applyTemplate('<shiro:hasRole>yup</shiro:hasRole>')

        then: "It thows an exception with the correct message"
        GrailsTagException ex = thrown()
        ex.message.contains("hasRole")
    }

    void "test missing name attribute on lacksRole"() {
        when: "we don't supply the 'name' attribute."
        mockSubject["hasRole"] = { String name -> name == role }
        applyTemplate('<shiro:lacksRole>yup</shiro:lacksRole>')

        then: "It throws an exception with the correct message"
        GrailsTagException ex2 = thrown()
        ex2.message.contains("lacksRole")
    }

    @Unroll
    void "test hasAllRoles tag #name"() {
        when: "The user has roles (subjectRoles)"
        mockSubject["hasAllRoles"] = { List inList -> subjectsRoles.containsAll(inList) }

        then: "does user have/lack (testRoles)"
        assert result == applyTemplate(tag, model)

        where:
        name             | subjectsRoles             | tag                                                              | model                                  | result
        'hasAllRoles 1'  | ["Administrator", "User"] | '<shiro:hasAllRoles in="${testRoles}">yup</shiro:hasAllRoles>'   | [testRoles: ["User", "Spy"]]           | ''
        'hasAllRoles 2'  | ["Administrator", "User"] | '<shiro:hasAllRoles in="${testRoles}">yup</shiro:hasAllRoles>'   | [testRoles: ["User", "Administrator"]] | 'yup'
        'hasAllRoles 3'  | ["Administrator", "User"] | '<shiro:hasAllRoles in="${testRoles}">yup</shiro:hasAllRoles>'   | [testRoles: ["Administrator"]]         | 'yup'
        'lacksAnyRole 1' | ["Administrator", "User"] | '<shiro:lacksAnyRole in="${testRoles}">yup</shiro:lacksAnyRole>' | [testRoles: ["User", "Administrator"]] | ''
        'lacksAnyRole 2' | ["Administrator", "User"] | '<shiro:lacksAnyRole in="${testRoles}">yup</shiro:lacksAnyRole>' | [testRoles: ["User", "spy"]]           | 'yup'
    }

    @Unroll
    void "test hasRoles tag #name"() {
        when: "user has the role User only"
        mockSubject["hasRoles"] = { List roles -> roles.collect { subjectsRoles.contains(it) } as boolean[] }

        then: "the result is as expected"
        result == applyTemplate(tag, model)

        where:
        name              | subjectsRoles             | tag                                                                | model                                             | result
        'hasAnyRole 1'    | ["User"]                  | '<shiro:hasAnyRole in="${testRoles}">yup</shiro:hasAnyRole>'       | [testRoles: ["Administrator", "Spy", "Warlock"]]  | ''
        'hasAnyRole 2'    | ["User"]                  | '<shiro:hasAnyRole in="${testRoles}">yup</shiro:hasAnyRole>'       | [testRoles: ["Administrator", "User", "Warlock"]] | 'yup'
        'lacksAllRoles 1' | ["User", "Administrator"] | '<shiro:lacksAllRoles in="${testRoles}">yup</shiro:lacksAllRoles>' | [testRoles: ["Administrator", "User", "Warlock"]] | ''
        'lacksAllRoles 2' | ["User", "Administrator"] | '<shiro:lacksAllRoles in="${testRoles}">yup</shiro:lacksAllRoles>' | [testRoles: ["Loozer", "Spy", "Warlock"]]         | 'yup'
    }

    @Unroll
    void "Test missing 'in' attribute for #name"() {
        when: "hasAllRoles lacks an 'in' attribute"
        mockSubject["hasAllRoles"] = { List roles -> roles.containsAll(["Administrator", "User"]) }
        applyTemplate(tag)

        then: "It throws an exception with the correct message"
        GrailsTagException ex = thrown()
        ex.message.contains(error)

        where:
        name            | tag                                              | error
        'hasAllRoles'   | '<shiro:hasAllRoles">yup</shiro:hasAllRoles>'    | 'hasAllRoles'
        'lacksAnyRoles' | '<shiro:lacksAnyRole">yup</shiro:lacksAnyRole>'  | 'lacksAnyRole'
        'hasAnyRole'    | '<shiro:hasAnyRole>yup</shiro:hasAnyRole>'       | 'hasAnyRole'
        'lacksAllRoles' | '<shiro:lacksAllRoles>yup</shiro:lacksAllRoles>' | 'lacksAllRoles'

    }

    void "test hasPermission with Strings"() {
        when: "We set a permission with id"
        Integer id = 1
        GString testPermission = "user:manage:${id}"
        mockSubject["isPermitted"] = { String requestedPermission -> testPermission == requestedPermission }

        then: "hasPermission returns contents with matching permission string"
        "yup" == applyTemplate('<shiro:hasPermission permission="user:manage:1">yup</shiro:hasPermission>')
        "yup" == applyTemplate('<shiro:hasPermission permission="${aGstring}">yup</shiro:hasPermission>', [aGstring: testPermission])

        and: "doesn't when it doesn't match"
        "" == applyTemplate('<shiro:hasPermission permission="user:manage:10">yup</shiro:hasPermission>')
    }

    void "test hasPermission with Permission object"() {
        when: "I use a Permission object and use that"
        WildcardPermission usersPermissions = new WildcardPermission('user:view,modify,create,delete')
        WildcardPermission sbp = new WildcardPermission('user:view,create')
        WildcardPermission sbp2 = new WildcardPermission('user:view,create,blowup')
        mockSubject["isPermitted"] = { Permission p -> usersPermissions.implies(p) }

        then: "hasPermission returns contents with matching permission string"
        "yup" == applyTemplate('<shiro:hasPermission permission="${sbp}">yup</shiro:hasPermission>', [sbp: sbp])

        and: "doesn't when it doesn't match"
        "" == applyTemplate('<shiro:hasPermission permission="${sbp}">yup</shiro:hasPermission>', [sbp: sbp2])
    }

}

class TestPrincipal {
    String name = 'Steve Smith'
    String firstName = 'Steve'
    String secondName = 'Smith'
}