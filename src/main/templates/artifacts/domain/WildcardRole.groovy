package ${packageName}

class ${className}Role {
    String name

    static hasMany = [ users: ${className}User, permissions: String ]
    static belongsTo = ${className}User

    static constraints = {
        name(nullable: false, blank: false, unique: true)
    }
}
