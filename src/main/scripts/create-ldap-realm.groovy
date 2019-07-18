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
 * Rewritten 2019 Peter McNeil
 */


description("Creates a new LDAP realm from a template that works with wildcard permissions.") {
	usage "grails create-ldap-realm [name] [--package=com.blah.bang]"
	argument name: "Realm Name", description: "What to name the Realm (Optional). Defaults to 'ShiroLdapRealm'", required: false
	flag name: 'domain', description: """Set the prefix to add to the names of the domain classes.
             This may include a package, e.g. --domain=com.nerderg.security"""
	flag name: 'package', description: "A package to use for all artifacts. e.g. --package=com.nerderg.security"
}

String name = args[0] ?: "ShiroLdap"
String pack = argsMap.package ? argsMap.package + '.' : ''

Model realmModel = model("$pack$name")

render template: "artifacts/realms/LdapRealm.groovy",
		destination: file("grails-app/realms/${realmModel.packagePath}/${realmModel.convention('Realm')}.groovy"),
		model: [
				packageName: realmModel.packageName,
				className: realmModel.convention('Realm')
		]
 