package cli.tester

import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class AuthControllerSpec extends Specification implements GrailsUnitTest {

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
    void "test auth controller #command"() {
        given:
        String packagePath = pack.replaceAll('\\.', '/')
        File controllerFile = new File(controllerDir, "${packagePath}/${controllerName}.groovy")
        File interceptorFile = new File(controllerDir, "${packagePath}/${interceptorName}.groovy")
        File loginFile = new File(viewDir, "$viewParent/login.gsp")
        File unauthorizedFile = new File(viewDir, "$viewParent/unauthorized.gsp")

        when: "I run create-auth-controller"
        Map result = executeScript(command)

        then: "It works without error and the files have been created"
        result.worked
        controllerFile.exists()
        interceptorFile.exists()
        loginFile.exists()
        unauthorizedFile.exists()

        and: "The AuthController looks right"
        String authContent = controllerFile.text.trim()
        authContent.startsWith("package $pack")
        authContent.contains("class $controllerName {")

        and: "The AuthInterceptor looks right"
        String authInterceptorContent = interceptorFile.text.trim()
        authInterceptorContent.startsWith("package $pack")
        authInterceptorContent.contains("class $interceptorName {")

        and: "The login view looks right"
        String viewContent = loginFile.text.trim()
        viewContent.contains("<title>Login</title>")

        and: "The unauthorized view looks right"
        String uauthorizedContent = unauthorizedFile.text.trim()
        uauthorizedContent.contains("<title>Unauthorized</title>")

        cleanup:
        controllerFile?.delete()
        interceptorFile?.delete()
        loginFile?.delete()
        unauthorizedFile?.delete()
        new File(viewDir, viewParent).deleteDir()

        where:
        command                                         | pack          | controllerName         | interceptorName         | viewParent
        'create-auth-controller'                        | 'cli.tester'   | 'AuthController'       | 'AuthInterceptor'       | 'auth'
        'create-auth-controller com.dom.pom.AuthOritar' | 'com.dom.pom' | 'AuthOritarController' | 'AuthOritarInterceptor' | 'authOritar'
        'create-auth-controller obay'                   | 'cli.tester'   | 'ObayController'       | 'ObayInterceptor'       | 'obay'
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