

class ShiroUrlMappings {

    static mappings = {
        "401"(controller: "auth", action: "login")
        "403"(controller: "auth", action: "unauthorized")
    }
}
