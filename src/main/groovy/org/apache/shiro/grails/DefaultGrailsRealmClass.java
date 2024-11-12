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
 * Modified 2019,2024 Peter McNeil
 */
package org.apache.shiro.grails;

import org.grails.core.AbstractInjectableGrailsClass;

/**
 * Represents a default implementation of the {@link GrailsRealmClass} interface.
 * This class is a wrapper around the specified class to provide meta-information and behavior specific to
 * Realms.
 * Extends {@link AbstractInjectableGrailsClass} and implements the {@link GrailsRealmClass}
 * interface to conform to the expected behaviour for a realm.
 */
public class DefaultGrailsRealmClass extends AbstractInjectableGrailsClass implements GrailsRealmClass {
    public DefaultGrailsRealmClass(Class<?> wrappedClass) {
        super(wrappedClass, RealmArtefactHandler.TYPE);
    }
}
