package org.apache.shiro.grails

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification

class TypedNamedArgsSpec extends Specification implements GrailsUnitTest, TypedNamedArgs {

    def setup() {
    }

    def cleanup() {
    }

    void "test good validation"() {
        when: "given a setup "
        setUpArgs([name: String, age: Integer, altWrong: Boolean, myCollection: Collection], args)

        then: "these validations should work"
        validateArgs()

        where:
        count | args
        1     | [name: 'Peter', age: 21, altWrong: false, myCollection: [] as List]
        2     | [name: null, age: 21, altWrong: false, myCollection: [] as Set]
        3     | [name: 'Peter', age: null, altWrong: false, myCollection: [] as Collection]
        4     | [name: 'Peter', age: 21, altWrong: null, myCollection: null]
    }

    void "test bad validation"() {
        when: "when I supply the wrong type argument"
        setUpArgs([name: String, age: Integer, altWrong: Boolean, myCollection: Collection],[name: 'Peter', age: '21', altWrong: null, myCollection: null])

        then: "I get an exception"
        IllegalArgumentException iia = thrown()
        iia.message == "argument 'age' is class java.lang.String but should be class java.lang.Integer."

        when: "when I supply an argument that doesn't exist"
        setUpArgs([name: String, age: Integer, altWrong: Boolean, myCollection: Collection],[firstName: 'Peter', age: 21, altWrong: null, myCollection: null])

        then: "I get an exception"
        IllegalArgumentException iia2 = thrown()
        iia2.message.startsWith "argument 'firstName' is not valid. Try one of these "

        when: "when I supply an argument that doesn't exist after a wrong type"
        setUpArgs([name: String, age: Integer, altWrong: Boolean, myCollection: Collection],[name: 'Peter', age: '21', totallyWrong: null, myCollection: null])

        then: "I get the all the problems in one exception message"
        IllegalArgumentException iia3 = thrown()
        iia3.message.contains "argument 'age' is class java.lang.String but should be class java.lang.Integer."
        iia3.message.contains "argument 'totallyWrong' is not valid. Try one of these "
    }

    void "test getting typed arguments"() {
        when: "Given a setup"
        Map args = [name: 'Peter', age: 21, altWrong: false, myCollection: [] as List]
        setUpArgs([name: String, age: Integer, altWrong: Boolean, myCollection: Collection], args)
        Integer age = (Integer) args.age

        then: "I can get a corrently typed argument from the map"
        age == 21
    }
}

