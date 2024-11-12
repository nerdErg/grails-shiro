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

import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authz.Permission

/**
 * User: pmcneil
 * Date: 1/07/19
 *
 */
interface SimplifiedRealm {

    AuthenticationInfo authenticate(AuthenticationToken authenticationToken) throws AuthenticationException

    boolean hasRole(Object principal, String roleName)
    boolean hasAllRoles(Object principal, Collection<String> roles)
    boolean isPermitted(Object principal, Permission requiredPermission)

}