package cli.tester

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class LdapRealmSpec extends Specification implements GrailsUnitTest {

    private File realmsDir
    private File domainDir
    private File controllerDir
    private File viewDir

    def setup() {

        realmsDir = new File("grails-app/realms")
        domainDir = new File("grails-app/domain")
        controllerDir = new File("grails-app/controllers")
        viewDir = new File("grails-app/views")
        cleanup()
    }

    def cleanup() {
        realmsDir.deleteDir()
        domainDir.deleteDir()
        domainDir.mkdir()
        new File(controllerDir, 'com').deleteDir()
        new File(controllerDir, 'org').deleteDir()
    }

    @Unroll
    void "test ldap #command"() {
        given:
        String packagePath = pack.replaceAll('\\.', '/')
        File realmFile = new File("grails-app/realms/$packagePath/${realmName}.groovy")
        println "${realmName}: $realmFile.absolutePath"

        expect: "There to be no files yet"
        !realmsDir.exists()
        !realmFile.exists()

        when: "I run create-wildcard-realm"
        Map result = executeScript(command)

        then: "It works without error and ll the files have been created"
        result.worked
        realmsDir.exists()
        realmFile.exists()

        and: "The realm looks right"
        String realmContent = realmFile.text.trim()
        realmContent.startsWith("package ${pack}\n")
        realmContent.contains("class ${realmName} implements GrailsShiroRealm, SimplifiedRealm")

        cleanup:
        realmFile?.delete()

        where:
        command                                            | pack              | realmName
        'create-ldap-realm'                                | 'cli.tester'       | 'ShiroLdapRealm'
        'create-ldap-realm Wild'                           | 'cli.tester'       | 'WildRealm'
        'create-ldap-realm org.amaze.balls.Wildcat'        | 'org.amaze.balls' | 'WildcatRealm'
        'create-ldap-realm Wild --package=org.amaze.balls' | 'org.amaze.balls' | 'WildRealm'
        'create-ldap-realm --package=org.amaze.balls'      | 'org.amaze.balls' | 'ShiroLdapRealm'
    }

    private static Map executeScript(String scriptName) {
        println "executing $scriptName"
        Process p = "grails $scriptName".execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        p.consumeProcessOutput(out, err)
        p.waitFor()
        if (out.size() > 0) println out
        if (err.size() > 0) println err
        Integer exit = p.exitValue()
        boolean worked = exit == 0 && err.size() == 0
        [worked: worked, exitVal: exit, out: out.toString(), err: err.toString()]
    }
}