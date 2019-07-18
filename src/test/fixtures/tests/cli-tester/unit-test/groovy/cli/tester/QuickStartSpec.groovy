package cli.tester

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class QuickStartSpec extends Specification implements GrailsUnitTest {

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
        new File(controllerDir, 'net').deleteDir()
    }

    @Unroll
    void "test quick start #command"() {
        given:
        String packagePath = pack.replaceAll('\\.', '/')
        File wildRealmFile = new File(realmsDir, "${packagePath}/${realmName}.groovy")
        File userDomainFile = new File(domainDir, "${packagePath}/${userName}.groovy")
        File roleDomainFile = new File(domainDir, "${packagePath}/${roleName}.groovy")
        File authControllerFile = new File(controllerDir, "${packagePath}/${controllerName}.groovy")
        File authInterceptorFile = new File(controllerDir, "${packagePath}/${interceptorName}.groovy")
        File loginViewFile = new File(viewDir, "$viewParent/login.gsp")
        File unauthorizedViewFile = new File(viewDir, "$viewParent/unauthorized.gsp")

        expect: "There to be no files yet"
        !wildRealmFile.exists()
        !userDomainFile.exists()
        !roleDomainFile.exists()
        !authControllerFile.exists()
        !authInterceptorFile.exists()
        !loginViewFile.exists()
        !unauthorizedViewFile.exists()
        !realmsDir.exists()

        when: "I run shiro-quick-start"
        Map result = executeScript(command)

        then: "It works without error"
        result.worked

        then: "All the correct files have been created"
        wildRealmFile.exists()
        userDomainFile.exists()
        roleDomainFile.exists()
        authControllerFile.exists()
        authInterceptorFile.exists()
        loginViewFile.exists()
        unauthorizedViewFile.exists()
        realmsDir.exists()

        then: "The realm looks right"
        String realmContent = wildRealmFile.text.trim()
        realmContent.startsWith("package ${pack}\n")
        realmContent.contains("class ${realmName} implements GrailsShiroRealm, SimplifiedRealm")

        then: "The user domain looks right"
        String userContent = userDomainFile.text.trim()
        userContent.startsWith("package ${pack}\n")
        userContent.contains("class ${userName} {")

        then: "The role domain looks right"
        String roleContent = roleDomainFile.text.trim()
        roleContent.startsWith("package ${pack}\n")
        roleContent.contains("class ${roleName} {")

        then: "The AuthController looks right"
        String authContent = authControllerFile.text.trim()
        authContent.startsWith("package ${pack}\n")
        authContent.contains("class ${controllerName} {")

        then: "The AuthInterceptor looks right"
        String authInterceptorContent = authInterceptorFile.text.trim()
        authInterceptorContent.startsWith("package ${pack}\n")
        authInterceptorContent.contains("class ${interceptorName} {")

        then: "The login view looks right"
        String loginContent = loginViewFile.text.trim()
        loginContent.contains("<title>Login</title>")

        then: "The unauthorized view looks right"
        String uauthorizedContent = unauthorizedViewFile.text.trim()
        uauthorizedContent.contains("<title>Unauthorized</title>")

        cleanup:
        wildRealmFile?.delete()
        userDomainFile?.delete()
        roleDomainFile?.delete()
        authControllerFile?.delete()
        authInterceptorFile?.delete()
        loginViewFile?.delete()
        unauthorizedViewFile?.delete()
        loginViewFile?.parentFile?.deleteDir() //delete the auth view directory

        where:
        command                                                                                | pack        | realmName              | userName    | roleName    | controllerName   | interceptorName   | viewParent
        'shiro-quick-start'                                                                    | 'cli.tester' | 'ShiroWildcardDbRealm' | 'ShiroUser' | 'ShiroRole' | 'AuthController' | 'AuthInterceptor' | 'auth'
        'shiro-quick-start --domain=Holy'                                                      | 'cli.tester' | 'ShiroWildcardDbRealm' | 'HolyUser'  | 'HolyRole'  | 'AuthController' | 'AuthInterceptor' | 'auth'
        'shiro-quick-start --realm=net.bat.Man --domain=net.bat.Holy --controller=net.bat.Orf' | 'net.bat'   | 'ManRealm'             | 'HolyUser'  | 'HolyRole'  | 'OrfController'  | 'OrfInterceptor'  | 'orf'
        'shiro-quick-start --package=net.bat'                                                  | 'net.bat'   | 'ShiroWildcardDbRealm' | 'ShiroUser' | 'ShiroRole' | 'AuthController' | 'AuthInterceptor' | 'auth'
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