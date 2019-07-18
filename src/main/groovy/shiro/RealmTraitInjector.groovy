package shiro

import grails.compiler.traits.TraitInjector
import groovy.transform.CompileStatic
import org.apache.shiro.grails.GrailsShiroRealm

/**
 * User: pmcneil
 * Date: 11/07/19
 *
 */
@CompileStatic
class RealmTraitInjector implements TraitInjector{

    @Override
    Class getTrait() {
        GrailsShiroRealm
    }

    @Override
    String[] getArtefactTypes() {
        ['Realm'] as String[]
    }
}
