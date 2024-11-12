/*
 * Copyright 2019 Peter McNeil.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    /**
     * Retrieves the number of rounds used for generating the BCrypt hash.
     *
     * @return the number of rounds for BCrypt hashing
     */
    public int getRounds() {
        return rounds;
    }

    /**
     * Sets the number of rounds to be used in generating the BCrypt hash.
     *
     * @param rounds the number of rounds to set for BCrypt hashing
     */
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
