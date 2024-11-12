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
 * Re-written 2019 Peter McNeil re purposed to fit default controller interceptor
 */
import grails.codegen.model.Model

description("Creates a new Shiro security interceptor for a controller.") {
    usage "grails create-shiro-controller-interceptor [ControllerName]."
    argument name: "Controller Name",
            description: "The name of the Controller this Interceptor is for. e.g. grails create-shiro-controller-interceptor Book",
            required: true
}

//clean up the supplied name as the user may think it needs to be BookController or BookInterceptor
String name = args[0].replaceAll(/(Controller|Interceptor)/, '') + 'Interceptor'
Model model = model(name)

render template: "artifacts/interceptors/SecurityInterceptor.groovy",
        destination: file("grails-app/controllers/${model.packagePath}/${model.className}.groovy"),
        model: model
