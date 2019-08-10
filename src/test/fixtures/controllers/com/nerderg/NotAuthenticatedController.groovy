package com.nerderg

class NotAuthenticatedController {

    def index() {
        render ("index")
    }

    def list() {
        render ("list")
    }

    //interceptor excludes this action from accessControl
    def publicAction() {
        render ("public")
    }
}