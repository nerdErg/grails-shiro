package org.apache.shiro.grails;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.mindrot.jbcrypt.BCrypt;

/**
 * User: pmcneil
 * Date: 10/07/19
 */
public class BcryptCredentialMatcher implements CredentialsMatcher, PasswordService {

    private int rounds;

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        UsernamePasswordToken userToken = (UsernamePasswordToken) token;
        String password = new String(userToken.getPassword());
        String hashed = (String) info.getCredentials();
        return BCrypt.checkpw(password, hashed);
    }


    @Override
    public String encryptPassword(Object plaintextPassword) throws IllegalArgumentException {
        String salt  = BCrypt.gensalt(rounds);
        return BCrypt.hashpw(plaintextPassword.toString(), salt);
    }

    @Override
    public boolean passwordsMatch(Object submittedPlaintext, String encrypted) {
        return BCrypt.checkpw(submittedPlaintext.toString(), encrypted);
    }
}
