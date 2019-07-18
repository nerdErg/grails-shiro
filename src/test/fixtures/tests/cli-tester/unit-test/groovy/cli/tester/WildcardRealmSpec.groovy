package cli.tester

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class WildcardRealmSpec extends Specification implements GrailsUnitTest {

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
    void "test wildcard #command"() {
        given:
        String packagePath = pack.replaceAll('\\.', '/')
        File realmFile = new File("grails-app/realms/$packagePath/${realmName}.groovy")
        File userFile = new File("grails-app/domain/$packagePath/${userName}.groovy")
        File roleFile = new File("grails-app/domain/$packagePath/${roleName}.groovy")
        println "${realmName}: $realmFile.absolutePath"
        println "${userName}: $userFile.absolutePath"
        println "${roleName}: $roleFile.absolutePath"

        expect: "There to be no files yet"
        !realmsDir.exists()
        !realmFile.exists()
        !userFile.exists()
        !roleFile.exists()

        when: "I run create-wildcard-realm"
        Map result = executeScript(command)

        then: "It works without error and ll the files have been created"
        result.worked
        realmsDir.exists()
        realmFile.exists()
        userFile.exists()
        roleFile.exists()

        and: "The realm looks right"
        String realmContent = realmFile.text.trim()
        realmContent.startsWith("package ${pack}\n")
        realmContent.contains("class ${realmName} implements GrailsShiroRealm, SimplifiedRealm")
        realmContent.contains("class ${realmName - 'Realm'}PrincipalHolder implements Serializable, PrincipalHolder {")

        and: "The user looks right"
        String userContent = userFile.text.trim()
        userContent.startsWith("package ${pack}\n")
        userContent.contains("class ${userName} {")
        userContent.contains("static hasMany = [ roles: ${roleName}")

        and: "The role looks right"
        String roleContent = roleFile.text.trim()
        roleContent.startsWith("package ${pack}\n")
        roleContent.contains("class ${roleName} {")
        roleContent.contains("static hasMany = [ users: ${userName},")

        cleanup:
        realmFile?.delete()
        userFile?.delete()
        roleFile?.delete()

        where:
        command                                                                       | pack              | realmName              | userName    | roleName
        'create-wildcard-realm'                                                       | 'cli.tester'      | 'ShiroWildcardDbRealm' | 'ShiroUser' | 'ShiroRole'
        'create-wildcard-realm Wild'                                                  | 'cli.tester'      | 'WildRealm'            | 'ShiroUser' | 'ShiroRole'
        'create-wildcard-realm Wildcat --domain=My'                                   | 'cli.tester'      | 'WildcatRealm'         | 'MyUser'    | 'MyRole'
        'create-wildcard-realm org.amaze.balls.Wildcat --domain=org.amaze.balls.Flap' | 'org.amaze.balls' | 'WildcatRealm'         | 'FlapUser'  | 'FlapRole'
        'create-wildcard-realm Wild --package=org.amaze.balls --domain=Flap'          | 'org.amaze.balls' | 'WildRealm'            | 'FlapUser'  | 'FlapRole'
        'create-wildcard-realm --package=org.amaze.balls'                             | 'org.amaze.balls' | 'ShiroWildcardDbRealm' | 'ShiroUser' | 'ShiroRole'
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