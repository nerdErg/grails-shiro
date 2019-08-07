package shiro.tester

import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import org.apache.shiro.web.mgt.CookieRememberMeManager
import spock.lang.Specification

@Integration
class ShiroGrailsSpec extends Specification {

    GrailsApplication grailsApplication
    CookieRememberMeManager shiroRememberMeManager

    def setup() {
    }

    def cleanup() {
    }

    void "test get cipher key"() {

        when: "I get the key back from the rmm"
        byte[] expectedBytes = 'abcdefghijklmnopqrstuvwx'.getBytes('US-ASCII')
        byte[] key = shiroRememberMeManager.cipherKey

        then: "the key is what we set"
        key.size() == 24
        for (int i = 0; i < 24; i++) {
            key[i] == expectedBytes[i]
        }

        when: "we use a unicode string"
        byte[] bytes = 'ĠĠĠdefghijklmnopqrstuvwx'.getBytes('US-ASCII')
        println bytes

        then: "we still get 24 bytes due to ASCII encoding"
        bytes.size() == 24

    }
}