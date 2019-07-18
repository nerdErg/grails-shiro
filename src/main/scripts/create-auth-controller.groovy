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
 * Modified 2009 Bradley Beddoes, Intient Pty Ltd, Ported to Apache Ki
 * Modified 2009 Kapil Sachdeva, Gemalto Inc, Ported to Apache Shiro
 * Modified 2015 Yellowsnow, Arkilog, Migrated to Grails 3
 * re-written 2019 Peter McNeil, Migrated to Grails 3.3.10 and modified to include an AuthInterceptor
 */


description("Creates a new Shiro AuthController and AuthInterceptor from a template.") {
    usage "grails create-auth-controller [name]"
    argument name: "name", description: "The name for the Controller, including package. (Optional)", required: false
}

String name = (args[0] ?: 'Auth').replaceAll(/Controller/, '')
Model theModel = model(name)

render template: "artifacts/controllers/AuthController.groovy",
        destination: file("grails-app/controllers/${theModel.packagePath}/${theModel.convention('Controller')}.groovy"),
        model: [
                packageName: theModel.packageName,
                className  : theModel.convention('Controller')
        ]

render template: "artifacts/views/auth/login.gsp",
        destination: file("grails-app/views/${theModel.modelName}/login.gsp")

render template: "artifacts/views/auth/unauthorized.gsp",
        destination: file("grails-app/views/${theModel.modelName}/unauthorized.gsp")

render template: "artifacts/interceptors/AuthInterceptor.groovy",
        destination: file("grails-app/controllers/${theModel.packagePath}/${theModel.convention('Interceptor')}.groovy"),
        model: [
                packageName: theModel.packageName,
                className  : theModel.convention('Interceptor')
        ]
