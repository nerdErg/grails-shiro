package com.nerderg

import org.apache.shiro.authz.annotation.RequiresRoles

class InterceptController {

    def index() {
        render ("index")
    }

    def list(Integer max) {
        render("list")
    }

    def create() {
        render("create")
    }

    def save() {
        render("save")
    }

    @RequiresRoles('test')
    def show(Long id) {
        render("show")
    }

    def edit(Long id) {
        render("edit")
    }

    def update(Long id, Long version) {
        render("update")
    }

    def delete(Long id) {
        render("delete")
    }

    @RequiresRoles('test')
    def annotated() {
        render("annotated")
    }
}
