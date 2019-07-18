package org.apache.shiro.grails

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class AccessControlMethodArgsSpec extends Specification implements GrailsUnitTest {

    def setup() {
    }

    def cleanup() {
    }

    void "test access control args works"() {
        when: "I make an new valid access control args"
        AccessControlMethodArgs args = new AccessControlMethodArgs([auth: val])

        then: "it works"
        args.auth == expected

        where:
        val   | expected
        true  | true
        false | false
        null  | null
    }

    void "test access control args exception cases"() {
        when: "I make an new access control args with an invalid type"
        new AccessControlMethodArgs([auth: 'true'])

        then: "it throws an IAE"
        IllegalArgumentException e = thrown()
        e.message == "argument 'auth' is class java.lang.String but should be class java.lang.Boolean."

        when: "I make an new access control args with an invalid argument name"
        new AccessControlMethodArgs([auth: true, blob: 'frack'])

        then: "it throws an IAE"
        IllegalArgumentException e2 = thrown()
        e2.message == "argument 'blob' is not valid. Try one of these [auth]."
    }

}