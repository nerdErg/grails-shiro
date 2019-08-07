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
        when: "I get the key from the rmm"
        byte[] key = shiroRememberMeManager.cipherKey

        then: "the key is 32 bytes long"
        key.size() == 32
    }
}