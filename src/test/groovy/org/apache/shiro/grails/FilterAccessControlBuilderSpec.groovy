package org.apache.shiro.grails

import grails.artefact.Interceptor
import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject
import org.grails.testing.GrailsUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test case for {@link FilterAccessControlBuilder}.
 */
class FilterAccessControlBuilderSpec extends Specification implements GrailsUnitTest {

    @Shared
    Interceptor interceptor = Mock()

    /**
     * Tests the simple case where 'role()' is called with a single
     * string argument.
     */
    void "test role"() {
        given:
        Subject mockSubject = Mock()

        when:
        def testBuilder = new FilterAccessControlBuilder(mockSubject, interceptor)
        Boolean result = testBuilder.role(role)

        then:
        1 * mockSubject.hasRole(role) >> response
        result == response

        where:
        role            | response
        "Administrator" | true
        "Observer"      | false
    }

    /**
     * Tests the case where the given argument(s) is/are invalid.
     */
    void "test invalid arguments for role(Map args)"() {
        given:
        Subject mockSubject = Mock()
        mockSubject.hasRole("Administrator") >> true

        when:
        def testBuilder = new FilterAccessControlBuilder(mockSubject, interceptor)
        testBuilder.role(target: "Administrator")

        then:
        IllegalArgumentException mme = thrown()
        println mme.message
        mme.message.contains("Invalid parameter")

        when:
        testBuilder.role("myRole", "observer")

        then:
        IllegalArgumentException mme2 = thrown()
        println mme2.message
        mme2.message.contains("Invalid parameter")
    }

    /**
     * Tests the simple cases where 'permission()' is called with a
     * valid Permission instance.
     */
    void "test Permission"() {
        given:
        def testPermission = new WildcardPermission("profile")
        Subject mockSubject = Mock()

        when:
        def testBuilder = new FilterAccessControlBuilder(mockSubject, interceptor)
        Boolean result = testBuilder.permission(testPermission)

        then:
        1 * mockSubject.isPermitted(testPermission) >> { Permission tp -> response }
        result == response

        where:
        name           | response
        "permission"   | true
        "no permision" | false
    }

    /**
     * Tests the simple cases where 'permission()' is called with a
     * string. In this case, a WildcardPermission should implicitly
     * be created.
     */
    void "test permission with string"() {
        given:
        def testString = "printer:hp4401:print"
        Subject mockSubject = Mock()
        def testBuilder = new FilterAccessControlBuilder(mockSubject, interceptor)

        when:
        Boolean result = testBuilder.permission(testString)

        then:
        1 * mockSubject.isPermitted(testString) >> { String p -> response }
        result == response

        where:
        name           | response
        "permission"   | true
        "no permision" | false
    }

    /**
     * Tests that 'permission()' can be called with an argument map
     * (named arguments).
     */
    void "test permission with Map"() {
        given:
        Subject mockSubject = Mock()
        Interceptor interceptor = Mock()
        def testBuilder = new FilterAccessControlBuilder(mockSubject, interceptor)

        when:
        Boolean result = testBuilder.permission(args)

        then:
        1 * interceptor.getActionName() >> action
        1 * mockSubject.isPermitted(expectedPermission) >> { String p -> expectedPermission == p }
        result == response

        where:
        args                                        | action | response | expectedPermission
        [target: "profile", actions: "show"]        | 'show' | true     | "profile"
        [target: "book", actions: ["show", "edit"]] | 'edit' | true     | "book"

    }

    /**
     * Tests that invalid arguments to permission(Map) are handled
     * correctly - usually by the method throwing an IllegalArgumentException.
     */
    @Unroll
    void "test permission with invalid args Map"() {
        given:
        Subject mockSubject = Mock()
        def testBuilder = new FilterAccessControlBuilder(mockSubject, interceptor)

        when:
        testBuilder.permission(args)

        then:
        def e = thrown(exception)
        println e.message

        where:
        args                                   | exception
        [:]                                    | NullPointerException
        [type: "test", actions: "show"]        | IllegalArgumentException
        [target: "book"]                       | NullPointerException
        [actions: "show"]                      | NullPointerException
        [target: ["profile"], actions: "test"] | IllegalArgumentException
        [target: "profile", actions: 10]       | IllegalArgumentException

    }

    @Shared
    Closure ac1 = { role("Administrator") || permission(target: "project", actions: "edit") }
    @Shared
    Closure ac2 = { role("Administrator") && permission(target: "project", actions: "edit") }

    @Unroll
    void "Test multiple controls"() {
        given:
        Subject mockSubject = Mock()
        Interceptor interceptor = Mock()
        String expectedPermission = "project"

        when:
        accessControl.delegate = new FilterAccessControlBuilder(mockSubject, interceptor)
        Boolean result = accessControl()

        then:
        result == accessControlResult
        1 * mockSubject.hasRole("Administrator") >> hasRoleResult
        isPermInvoked * interceptor.getActionName() >> "edit"
        isPermInvoked * mockSubject.isPermitted(expectedPermission) >> isPermittedResult

        where:
        accessControl | hasRoleResult | isPermittedResult | isPermInvoked | accessControlResult
        ac1           | false         | true              | 1             | true
        ac2           | true          | true              | 1             | true
        ac2           | true          | false             | 1             | false
        ac2           | false         | true              | 0             | false
    }
}
