/*
 * Original Copyright 2007 Peter Ledbrook.
 * Copyright 2019 Peter McNeil.
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
 * Modified 2009 Bradley Beddoes, Intient Pty Ltd, Ported to Apache Ki
 * Modified 2009 Kapil Sachdeva, Gemalto Inc, Ported to Apache Shiro
 * Modified 2015 Yellowsnow, Arkilog, Migrated to Grails 3
 * Re-written 2019 Peter McNeil
 */

description("Sets up a basic security system with a wildcard realm, auth controller, etc.") {
    usage "grails shiro-quick-start [--realm=realmPrefix] [--domain=domainPrefix] [--controller=controllerPrefix]"

    flag name: 'realm', description: "The prefix to add to the realm. " +
            "e.g. --realm=com.nerderg.security.Wild -> ..realms/com/nerderg/security/WildRealm.groovy. Default: ShiroWildcardDbRealm"
    flag name: 'domain', description: "The prefix to add to the domain classes. " +
            "e.g. --domain=com.nerderg.security.Blarg -> ..domain/com/nerderg/security/BlargUser.groovy. Default: ShiroUser/ShiroRole"
    flag name: 'controller', description: "The prefix to add to the controller and Interceptor classes. " +
            "e.g. --controller=com.nerderg.security.Auth -> controllers/com/nerderg/security/AuthController.groovy. Default: AuthController"
    flag name: 'package', description: "A package to use for all artifacts. e.g. --package=com.nerderg.security"
}
String realmName = argsMap.realm ?: 'ShiroWildcardDb'
String domain = argsMap.domain ?: 'Shiro'
String controllerName = argsMap.controller ?: 'Auth'
String pack = argsMap.package ? argsMap.package : ''

if (pack) {
    createWildcardRealm(realmName, "--domain=$domain", "--package=$pack")
    createAuthController("${pack}.$controllerName")
} else {
    createWildcardRealm(realmName, "--domain=$domain")
    createAuthController(controllerName)
}
