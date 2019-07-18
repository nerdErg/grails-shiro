import grails.codegen.model.Model

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
 * Re-written 2019 Peter McNeil
 */



description("Creates a new database realm from a template that works with wildcard permissions.") {
    usage "grails create-wildcard-realm [name] [--domain=blah] [--package=com.blah.security]"
    argument name: "Realm Name", description: "What to name the realm (Optional). Defaults to 'ShiroWildcardDbRealm.", required: false
    flag name: 'domain', description: "The prefix to add to the names of the domain classes. This may include a package, e.g. --domain=com.nerderg.security"
    flag name: 'package', description: "The package to use for both the realm and the domain classes. This will PREFIX to the Realm and Domain names."
}

String name = args[0] ?: "ShiroWildcardDb"
String domain = argsMap.domain ?: 'Shiro'
String pack = argsMap.package ? argsMap.package + '.' : ''

Model realmModel = model("$pack$name")
Model domainModel = model("$pack$domain")

String domainPath = "grails-app/domain/${domainModel.packagePath}"
ant.mkdir(dir: "${baseDir}/${domainPath}")

render template: "artifacts/domain/WildcardUser.groovy",
        destination: file("${domainPath}/${domainModel.convention('User')}.groovy"),
        model: domainModel

render template: "artifacts/domain/WildcardRole.groovy",
        destination: file("${domainPath}/${domainModel.convention('Role')}.groovy"),
        model: domainModel

render template: "artifacts/realms/WildcardDbRealm.groovy",
        destination: file("grails-app/realms/${realmModel.packagePath}/${realmModel.convention('Realm')}.groovy"),
        model: [
                packageName: realmModel.packageName,
                className: realmModel.convention('Realm'),
                userClassName: domainModel.convention('User'),
                roleClassName: domainModel.convention('Role'),
                principalHolderClassName: realmModel.convention('PrincipalHolder')
        ]
 