package shiro


import grails.artefact.Interceptor
import grails.core.ArtefactHandler

/*
 * Copyright 2007 Peter Ledbrook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * Modified 2009 Bradley Beddoes, Intient Pty Ltd, Ported to Apache Ki
 * Modified 2009 Kapil Sachdeva, Gemalto Inc, Ported to Apache Shiro
 * Modified 2015 Yellowsnow, Arkilog, Migrated to Grails 3
 * Refactored 2019 Peter McNeil, Grails 3.3.10 and cleanup/refactor
 */

import grails.core.GrailsClass
import grails.plugins.Plugin
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy
import org.apache.shiro.authc.pam.ModularRealmAuthenticator
import org.apache.shiro.authz.permission.WildcardPermissionResolver
import org.apache.shiro.cache.ehcache.EhCacheManager
import org.apache.shiro.crypto.AesCipherService
import org.apache.shiro.grails.*
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO
import org.apache.shiro.spring.LifecycleBeanPostProcessor
import org.apache.shiro.spring.web.ShiroFilterFactoryBean
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
import org.apache.shiro.web.mgt.CookieRememberMeManager
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.apache.shiro.web.servlet.SimpleCookie
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager
import org.slf4j.LoggerFactory
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered
import java.security.InvalidKeyException
import static javax.servlet.DispatcherType.ERROR
import static javax.servlet.DispatcherType.REQUEST

class ShiroGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "4.0.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = []

    def title = "Apache Shiro Integration for Grails"
    def author = "Peter McNeil"
    def authorEmail = "peter@nerderg.com"
    def description = '''\
Enables Grails applications to take advantage of the Apache Shiro security layer, adding easy authentication and access control via roles and permissions.'''

    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://nerderg.com/Grails+shiro"

    def license = "Apache-2.0"
    def organization = [name: "nerdErg Pty Ltd", url: "http://www.nerderg.com/"]
    def issueManagement = [url: "https://github.com/nerdErg/grails-shiro/issues"]
    def scm = [url: "https://github.com/nerdErg/grails-shiro"]

    def loadAfter = ["controllers", "services", "logging"]
    def observe = ["controllers"]
    def watchedResources = "file:./grails-app/realms/**/*Realm.groovy"

    List<ArtefactHandler> artefacts = [RealmArtefactHandler] as List<ArtefactHandler>

    final static log = LoggerFactory.getLogger(ShiroGrailsPlugin)
    private List<String> realmBeanNames = []

    Closure doWithSpring() {
        { ->
            log.info 'Configuring security framework - Shiro ...'

            // Configure realms defined in the project.
            GrailsClass[] realmClasses = grailsApplication.realmClasses
            realmClasses.each { GrailsRealmClass realmClass ->
                log.info "Registering realm: ${realmClass.fullName}"
                configureRealm.delegate = delegate
                realmBeanNames << configureRealm(realmClass)
            }

            //Replace the GrailsExceptionResolver to catch authentication errors
            boolean handleExceptions = grailsApplication.config.getProperty('security.shiro.handleExceptions', boolean, true)
            if (handleExceptions) {
                exceptionResolver(ShiroGrailsExceptionResolver) {
                    exceptionMappings = ['java.lang.Exception': '/error']
                }
            }

            shiroLifecycleBeanPostProcessor(LifecycleBeanPostProcessor)

            // Shiro annotation support. This intercepts method calls to handle Annotations.
            shiroAdvisorAutoProxyCreator(DefaultAdvisorAutoProxyCreator) { bean ->
                bean.dependsOn = "shiroLifecycleBeanPostProcessor"
                proxyTargetClass = true
            }

            shiroAuthorizationAttributeSourceAdvisor(AuthorizationAttributeSourceAdvisor) { bean ->
                securityManager = ref("shiroSecurityManager")
            }

            // The default credential matcher.
            credentialMatcher(BcryptCredentialMatcher) { bean ->
                rounds = grailsApplication.config.getProperty('security.shiro.bycrypt.rounds', Integer, 10)
            }

            // Default permission resolver: WildcardPermissionResolver.
            // This converts permission strings into WildcardPermission
            // instances.
            shiroPermissionResolver(WildcardPermissionResolver)

            // Default authentication strategy
            shiroAuthenticationStrategy(AtLeastOneSuccessfulStrategy)

            // Default authenticator
            shiroAuthenticator(ModularRealmAuthenticator) {
                authenticationStrategy = ref("shiroAuthenticationStrategy")
            }

            //get optional remember me key
            byte[] cipherKeyBytes = null
            String cipherKeyString = grailsApplication.config.getProperty('security.shiro.rememberMe.cipherKey', String, null)
            Integer cipherKeySize = grailsApplication.config.getProperty('security.shiro.rememberMe.keySize', Integer, 256)
            if(!cipherKeyString) {
                AesCipherService aesCipherService = new AesCipherService()
                cipherKeyBytes = aesCipherService.generateNewKey(cipherKeySize).encoded
            } else {
                cipherKeyBytes = cipherKeyString?.getBytes('US-ASCII')
            }

            if (cipherKeyBytes) {
                if (!(cipherKeyBytes.size() in [16, 24, 32])) {
                    throw new InvalidKeyException("Default AES crypt service allows only 128, 196 and 256 bit key.")
                }
            }

            // set up remember-me manager.
            shiroRememberMeManager(CookieRememberMeManager) {
                if (cipherKeyBytes) {
                    cipherKey = cipherKeyBytes
                }
            }

            def sessionMode = grailsApplication.config.getProperty('security.shiro.session.mode')
            if (sessionMode?.equalsIgnoreCase('native')) {
                log.debug("Using shiro native session manager with sensible defaults")
                shiroSessionIdCookie(SimpleCookie) {
                    name = 'MYSESSIONID'
                    maxAge = 288000
                }
                shiroSessionCacheManager(EhCacheManager)
                shiroSessionDAO(EnterpriseCacheSessionDAO) {
                    cacheManager = ref('shiroSessionCacheManager')
                }
                shiroSessionManager(DefaultWebSessionManager) {
                    cacheManager = ref('shiroSessionCacheManager')
                    sessionDAO = ref('shiroSessionDAO')
                    sessionIdCookie = ref('shiroSessionIdCookie')
                    sessionValidationSchedulerEnabled = true
                }
            } else {
                log.debug("Using servlet container session manager")
                shiroSessionManager(ServletContainerSessionManager)
            }


            // The real security manager instance.
            shiroSecurityManager(DefaultWebSecurityManager) { bean ->
                // Shiro doesn't like an empty collection of realms, so we
                // only configure the "realms" property if there are some.
                if (!realmBeanNames.isEmpty()) {
                    realms = realmBeanNames.collect {
                        log.debug("Adding realm bean $it")
                        ref(it)
                    }
                }
                sessionManager = ref('shiroSessionManager')
                authenticator = ref("shiroAuthenticator")
                rememberMeManager = ref("shiroRememberMeManager")
            }

            boolean enableBasicFilter = grailsApplication.config.getProperty('security.shiro.filter.basic.enabled')
            String basicAppName = grailsApplication.config.getProperty('security.shiro.filter.basic.appName') ?: grailsApplication.config.info.app.name
            if (enableBasicFilter) {
                authcBasicFilter(BasicHttpAuthenticationFilter) {
                    applicationName = basicAppName
                }
            }

            // Create the main security filter.
            shiroFilter(ShiroFilterFactoryBean) { bean ->
                securityManager = ref("shiroSecurityManager")
                loginUrl = grailsApplication.config.getProperty('security.shiro.filter.loginUrl', String, "/auth/login")
                unauthorizedUrl = grailsApplication.config.getProperty('security.shiro.filter.unauthorizedUrl', String, "/auth/unauthorized")
                successUrl = grailsApplication.config.getProperty('security.shiro.filter.successUrl')

                if (grailsApplication.config.getProperty('security.shiro.filter.filterChainDefinitions')) {
                    filterChainDefinitions = grailsApplication.config.getProperty('security.shiro.filter.filterChainDefinitions')
                }

                if (enableBasicFilter) {
                    filters = [authcBasic: ref("authcBasicFilter")]
                }
            }

            //New in Grails 3.0.x
            //instead of web.xml configuration
            log.debug('Filter definition via FilterRegistrationBean')
            servletShiroFilter(FilterRegistrationBean) {
                filter = ref('shiroFilter')
                urlPatterns = ['/*']
                dispatcherTypes = EnumSet.of(REQUEST, ERROR)
                order = Ordered.HIGHEST_PRECEDENCE + 1000
            }
            log.info 'Security layer configured.'
        }
    }

    void doWithApplicationContext() {
        def mgr = applicationContext.getBean("shiroSecurityManager")

        if (mgr.realms == null) {
            log.warn "No Shiro realms configured - access control won't work!"
        }
    }

    // add accessControl() to Interceptors
    void doWithDynamicMethods() {

        //if authentication required
        boolean authcRequired = grailsApplication.config.getProperty('security.shiro.authc.required', Boolean, true)

        // Add an 'accessControl' method to Interceptor.
        def mc = Interceptor.metaClass

        mc.accessControl << { ->
            return AccessControl.accessControlMethod(grailsApplication, (Interceptor) delegate, authcRequired)
        }
        mc.accessControl << { Map args ->
            return AccessControl.accessControlMethod(grailsApplication, (Interceptor) delegate, authcRequired, args)
        }
        mc.accessControl << { Closure c ->
            return AccessControl.accessControlMethod(grailsApplication, (Interceptor) delegate, authcRequired, [:], c)
        }
        mc.accessControl << { Map args, Closure c ->
            return AccessControl.accessControlMethod(grailsApplication, (Interceptor) delegate, authcRequired, args, c)
        }
    }

    void onChange(Map<String, Object> event) {
        log.debug "onChange -> $event"
        if (grailsApplication.isRealmClass(event.source)) {
            log.info "Realm modified!"

            def context = event.ctx
            if (!context) {
                log.error("grailsApplication context not found - can't reload.")
                return
            }

            // Make sure the new realm class is registered.
            GrailsRealmClass realmClass = (GrailsRealmClass) grailsApplication.addArtefact(RealmArtefactHandler.TYPE, (Class) event.source)

            beans {
                String name = configureRealm((GrailsRealmClass) realmClass)
                if (realmBeanNames.contains(name)) {
                    log.info "Updated realm $name"
                } else {
                    //too many issues adding a realm when live, so just indicate that
                    log.error "We acknowledge your enthusiasim in adding realm ${name - 'Instance'}, but we can't " +
                            "dynamically load it into the security manager. Please restart the app."
                }
            }
        }
    }

    Closure configureRealm = { GrailsRealmClass realmClass ->
        String realmName = realmClass.shortName

        // Create the realm bean.
        "${realmName}Instance"(realmClass.clazz) { bean ->
            bean.autowire = "byName"
            bean.dependsOn = "shiroLifecycleBeanPostProcessor"
            permissionResolver = ref("shiroPermissionResolver")
        }

        // Return the bean name for this realm.
        return "${realmName}Instance".toString()
    }
}
