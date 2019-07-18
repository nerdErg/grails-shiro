package com.nerderg

class AnnotatedServiceController {

    def securedMethodsService
    def securedService

    def index() {render "index" }

    def show() { render "Show page" }

    def create() { render "Create page" }

    def edit() { render "Edit page" }

    def delete() { render "Delete page" }

    def unsecured() {
        render("Unsecured: " + securedMethodsService.methodOne())
    }

    def guest() {
        render("Guest: " + securedMethodsService.methodTwo())
    }

    def user() {
        render("User: " + securedMethodsService.methodThree())
    }

    def authenticated() {
        render("Authenticated: " + securedMethodsService.methodFour())
    }

    def role() {
        render("Role: " + securedMethodsService.methodFive())
    }

    def permission() {
        render("Permission: " + securedMethodsService.methodSix())
    }

    def unrestricted() {
        render("secure class: " + securedService.unrestricted())
    }

    def administrator() {
        render("secure class: " + securedService.requiresRoleAdministrator())
    }
}

