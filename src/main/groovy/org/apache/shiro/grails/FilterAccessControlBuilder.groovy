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
 * rewritten 2019 Peter McNeil
 */

package org.apache.shiro.grails

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.shiro.subject.Subject
import org.apache.shiro.authz.Permission
import grails.artefact.Interceptor

@CompileStatic
@TypeChecked
class FilterAccessControlBuilder {
    private Subject subject
    private Interceptor interceptor

    FilterAccessControlBuilder(Subject subject, Interceptor interceptor) {
        this.subject = subject
        this.interceptor = interceptor
    }

    /**
     * Checks whether the user associated with the builder's security
     * context has the given role or not.
     *
     * @param roleName
     * @return true if subject has the named role
     */
    boolean role(String roleName) {
        return this.subject.hasRole(roleName.toString())
    }

    /**
     * This is to help people using the closure DSL understand the problem when they supply a non String argument.
     *
     * Otherwise they get a MissingMethodException on FilterAccessControlBuilder which seems pretty abstract to the
     * coder who doesn't know about this delegate.
     * @param roleName
     * @return
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    boolean role(Object[] illegalArguments) {
        throw new IllegalArgumentException("Invalid parameter ${illegalArguments} for role(String roleName) in the accessControl closure.")
    }

    /**
     * Checks whether the user associated with the builder's security
     * context has the given permission or not.
     *
     * @param permission
     * @return true if the user has the permission
     */
    boolean permission(Permission permission) {
        return this.subject.isPermitted(permission)
    }

    /**
     * <p>Checks whether the user associated with the builder's security
     * context has the given permission or not. The permission is a
     * string that complies with the format supported by Shiro's
     * WildcardPermission, i.e. parts separated by a colon and sub-parts
     * separated by commas. For example, you might have
     * "book:*:view,create,save", where the first part is a type of
     * resource (a "book"), the second part is the ID of the resource
     * ("*" means "all books"), and the last part is a list of actions
     * (sub-parts).</p>
     *
     * <p>The string can contain any number of parts and sub-parts
     * because it is not interpreted by the framework at all. The parts
     * and sub-parts only mean something to the application. The only
     * time the framework effectively "interprets" the strings is when
     * it checks whether one permission implies the other, but this only
     * relies on the logic of parts and sub-parts, not their semantic
     * meaning in the application. See the documentation for Shiro's
     * WildcardPermission for more information.</p>
     *
     * @param permissionString
     * @return true if the user has the permission
     */
    boolean permission(String permissionString) {
        return this.subject.isPermitted(permissionString)
    }

    /**
     * <p>Checks whether the user associated with the builder's security
     * context has permission for a given type and set of actions. The
     * map must have 'target' and optionally 'actions' entries. The method should be
     * called like this:</p>
     * <pre>
     *     permission(target: 'profile', actions: 'edit')
     *     permission(target: 'book', actions: [ 'show', 'modify' ])
     *     permission(target: 'book', actions: 'show, modify')
     * </pre>
     *
     * where 'target' is shiro language for the permission string
     * When actions is null it's assumed all actions require the permission (i.e. the Interceptor has filtered the action)
     *
     * This uses WildCardPermission strings
     *
     * If the action doesn't match the supplied actions then returns false
     *
     * @param args map
     * @return
     */
    boolean permission(Map args) {
        AccessControlPermissionArgs params = new AccessControlPermissionArgs(args)
        String action = interceptor.actionName ?: 'index'
        //Call the string version so we don't need to know what Type of Permission to use
        boolean result = (params.actions == null || params.actions.contains(action)) &&
                this.subject.isPermitted(params.target)
        return result
    }

    /**
     * This is to help people using the closure DSL understand the problem when they supply an incorrect argument.
     *
     * Otherwise they get a MissingMethodException on FilterAccessControlBuilder which seems pretty abstract to the
     * coder who doesn't know about this delegate.
     * @param roleName
     * @return
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    boolean permission(Object[] illegalArguments) {
        throw new IllegalArgumentException("""Invalid parameter ${illegalArguments} for permission() in the accessControl closure.
You may supply a Permission object, a permission String or a Map [target: 'myTarget', actions: 'view, edit']""")
    }

}

@CompileStatic
@TypeChecked
class AccessControlPermissionArgs implements TypedNamedArgs {

    final String target
    final Collection<String> actions

    AccessControlPermissionArgs(Map args) {
        //backwards compatible to allow actions to be string or collection convert string to collection
        if (args.actions instanceof String) {
            args.actions = argStringToSet((String) args.actions)
        }
        setUpArgs([target: String, actions: Collection], args)
        //validated args are correct type
        this.target = (String) notNull(args.target, 'type')
        this.actions = (Collection<String>) notNull(args.actions, 'action')
    }

}
