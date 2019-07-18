package cli.tester

import org.grails.testing.GrailsUnitTest
import org.junit.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class ControllerInterceptorSpec extends Specification implements GrailsUnitTest {

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
    void "test interceptor #command"() {
        given:
        String packagePath = pack.replaceAll('\\.', '/')
        File interceptorFile = new File(controllerDir, "${packagePath}/${interceptorName}.groovy")

        expect:
        !interceptorFile.exists()

        when: "I run #command"
        Map result = executeScript(command)

        then: "It works without error and the file is created"
        result.worked
        interceptorFile.exists()

        and: "The BookInterceptor looks right"
        String controllerInterceptorContent = interceptorFile.text.trim()
        controllerInterceptorContent.startsWith("package $pack")
        controllerInterceptorContent.contains("class $interceptorName {")

        cleanup:
        interceptorFile?.delete()

        where:
        pack            | command                                                         | interceptorName
        'cli.tester'     | 'create-shiro-controller-interceptor Book'                      | 'BookInterceptor'
        'cli.tester'     | 'create-shiro-controller-interceptor BookController'            | 'BookInterceptor'
        'cli.tester'     | 'create-shiro-controller-interceptor BookInterceptor'           | 'BookInterceptor'
        'cli.tester'     | 'create-shiro-controller-interceptor BookControllerInterceptor' | 'BookInterceptor'
        'org.pet.moggy' | 'create-shiro-controller-interceptor org.pet.moggy.Cat'         | 'CatInterceptor'
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