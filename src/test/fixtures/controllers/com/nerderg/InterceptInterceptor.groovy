package com.nerderg

/**
 * Generated by the Shiro plugin. This interceptor class protects all URLs
 * via access control by convention.
 */
class InterceptInterceptor {

    //customize me
    int order = HIGHEST_PRECEDENCE + 100

    boolean before() {
        accessControl(auth: false) {
            role('Administrator') ||
            ((role('User') || role('user')) &&
                    (
                            permission(target: 'book:read', actions: 'index, list, show') ||
                                    permission(target: 'book:write', actions: 'create, edit, delete, save, update')
                    )
            )
        }
    }

    void afterView() {
    }
}
