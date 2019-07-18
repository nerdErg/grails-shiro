package com.nerderg

import org.apache.shiro.authz.annotation.Logical
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.apache.shiro.authz.annotation.RequiresRoles

@RequiresAuthentication
@RequiresRoles(value=["User", "test"], logical=Logical.OR)
class AnnotatedController {

    def index() {
        redirect(action: "list", params: params)
    }

    @RequiresPermissions('book:list')
    def list(Integer max) {
        render("list")
    }

    @RequiresPermissions('book:create')
    def create() {
        render("create")
    }

    @RequiresPermissions('book:save')
    def save() {
        render("save")
    }

    @RequiresPermissions('book:view')
    def show(Long id) {
        render("show")
    }

    @RequiresPermissions('book:edit')
    def edit(Long id) {
        render("edit")
    }

    @RequiresPermissions('book:update')
    def update(Long id, Long version) {
        render("update")
    }

    @RequiresPermissions('book:delete')
    def delete(Long id) {
        render("delete")
    }
}
