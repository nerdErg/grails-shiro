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
package org.apache.shiro.grails;

import org.apache.shiro.authz.annotation.*;

import java.lang.reflect.Method;

/**
 * User: pmcneil
 * Date: 16/09/13
 */
public class AuthorizationAttributeSourceAdvisor extends org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor {

    private static final long serialVersionUID = 1;

    @Override
    public boolean matches(Method method, Class targetClass) {
        return ((method.getAnnotation(RequiresPermissions.class) != null) ||
                (method.getAnnotation(RequiresRoles.class) != null) ||
                (method.getAnnotation(RequiresUser.class) != null) ||
                (method.getAnnotation(RequiresGuest.class) != null) ||
                (method.getAnnotation(RequiresAuthentication.class) != null) ||
                (targetClass.getAnnotation(RequiresPermissions.class) != null) ||
                (targetClass.getAnnotation(RequiresRoles.class) != null) ||
                (targetClass.getAnnotation(RequiresUser.class) != null) ||
                (targetClass.getAnnotation(RequiresGuest.class) != null) ||
                (targetClass.getAnnotation(RequiresAuthentication.class) != null));
    }
}
