package shiro.tester

import com.nerderg.Book
import com.nerderg.security.ShiroRole
import com.nerderg.security.ShiroUser
import org.apache.shiro.authc.credential.PasswordService

class BootStrap {

    PasswordService credentialMatcher

    def init = { servletContext ->
        def adminRole = ShiroRole.findByName("Administrator")
        if (!adminRole) {
            adminRole = new ShiroRole(name: "Administrator")
            def adminUser = new ShiroUser(username: "admin", passwordHash: credentialMatcher.encryptPassword("admin"))
            adminUser.addToRoles(adminRole)
            adminUser.save()
            assert credentialMatcher.passwordsMatch('admin', adminUser.passwordHash)

            def userRole = new ShiroRole(name: "User")
            def normalUser = new ShiroUser(username: "dilbert", passwordHash: credentialMatcher.encryptPassword("password"))
            normalUser.addToRoles(userRole)
            normalUser.addToPermissions("book:show,index,read")
            normalUser.save()
            assert credentialMatcher.passwordsMatch('password', normalUser.passwordHash)

            // Users for the TestController.
            def testRole = new ShiroRole(name: "test")
            testRole.addToPermissions("book:*")
            
            def testUser1 = new ShiroUser(username: "test1", passwordHash: credentialMatcher.encryptPassword("test1"))
            testUser1.addToRoles(testRole)
            testUser1.addToRoles(userRole)
            testUser1.addToPermissions("custom:read,write")

            testUser1.save()
            assert credentialMatcher.passwordsMatch('test1', testUser1.passwordHash)

            // Some initial books that we can test against.
            new Book(name: "Colossus", author: "Niall Ferguson").save()
            new Book(name: "Misery", author: "Stephen King").save()
            new Book(name: "Guns, Germs, and Steel", author: "Jared Diamond").save()
        }
    }

    def destroy = {
    }
}
