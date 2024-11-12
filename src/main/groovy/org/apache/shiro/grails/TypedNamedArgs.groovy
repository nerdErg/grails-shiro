/*
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
 */
package org.apache.shiro.grails

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * For typing and checking named arguments as a Map
 *
 * set up the arguments by supplying a method signature in the form of a map of arguments names and their type, e.g.
 * [name: String, age, Integer]
 *
 * User: pmcneil
 * Date: 20/06/19
 *
 */
@CompileStatic
@TypeChecked
trait TypedNamedArgs {

    Map<String, Class> allowedArgs
    Map args

    def setUpArgs(Map args) {
        assert allowedArgs
        this.args = args
        validateArgs()
    }

    def setUpArgs(Map signature, Map args) {
        allowedArgs = signature
        this.args = args
        validateArgs()
    }

    boolean validateArgs() {
        List<String> errors = []
        args.each { k, v ->
            String key = k
            if (!allowedArgs.containsKey(key)) {
                errors.add("argument '$k' is not valid. Try one of these ${allowedArgs.keySet()}.".toString())
            } else if (v != null && !allowedArgs[key].isAssignableFrom(v.class)) {
                errors.add("argument '$k' is ${v.class} but should be ${allowedArgs[key]}.".toString())
            }
        }
        if (errors.size() > 0) {
            throw new IllegalArgumentException(errors.join('\n'))
        }
        return true
    }

    Object notNull(Object value, String name) {
        if (value == null) throw new NullPointerException("$name can not be null.")
        return value
    }

    /**
     * Converts a delimiter-separated string of things into a set of Strings.
     * Supported delimiters are ',', ';', and whitespace.
     */
    static Set<String> argStringToSet(String actions) {
        return new HashSet(Arrays.asList(actions.split(/[,;\s][\s]*/)))
    }

}
