package ${packageName}

class ${className}User {
    String username
    String passwordHash
    
    static hasMany = [ roles: ${className}Role, permissions: String ]

    static constraints = {
        username(nullable: false, blank: false, unique: true)
    }
}
