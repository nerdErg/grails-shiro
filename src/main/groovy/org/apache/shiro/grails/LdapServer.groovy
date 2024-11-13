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
package org.apache.shiro.grails

import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.credential.CredentialsMatcher
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.Attribute
import javax.naming.directory.Attributes
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchResult

/**
 * User: pmcneil
 * Date: 11/07/19
 *
 */
class LdapServer implements CredentialsMatcher {

    final static Logger LOG = LoggerFactory.getLogger(LdapServer.class)

    String searchBase
    String usernameAttribute
    String searchUser
    String searchPass
    List<String> ldapUrls
    String groupOu
    String groupMemberElement
    String groupMemberPrefix
    String groupMemberPostfix

    String permSubCn
    String permMemberElement
    String permMemberPrefix

    private String cachedUrl

    LdapServer() {
    }

    @Override
    boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        UsernamePasswordToken userToken = (UsernamePasswordToken) token
        String password = new String(userToken.getPassword())

        ldapSearch { InitialDirContext ctx, String ldapUrl ->

            // Look up the DN for the LDAP entry that has a 'uid' value matching the given username.
            def matchAttrs = new BasicAttributes(true)
            matchAttrs.put(new BasicAttribute(usernameAttribute, userToken.username))

            NamingEnumeration<SearchResult> result = ctx.search(searchBase, matchAttrs)
            if (!result.hasMore()) {
                return false
            }

            //check we can log in as the user we just found using the password supplied
            SearchResult searchResult = result.next()
            try {
                getLDAPContext(searchResult.nameInNamespace, password, ldapUrl)
                //we don't care about the context, just that we don't get an exception from logging in
                return true

            } catch (AuthenticationException ex) {
                LOG.info "Invalid password $ex.message"
                return false
            }
        }
    }

    List<String> roles(String userName) {
        List<String> roles = []
        ldapSearch { InitialDirContext ctx, String ldapUrl ->
            BasicAttributes matchAttrs = new BasicAttributes(true)
            matchAttrs.put(new BasicAttribute(groupMemberElement, "$groupMemberPrefix$userName$groupMemberPostfix"))

            NamingEnumeration<SearchResult> result = ctx.search(groupOu, matchAttrs)

            while (result.hasMore()) {
                SearchResult group = result.next()
                Attribute cnAttr = group.attributes.get('cn')
                List<String> names = cnAttr.all.collect { it as String }
                roles.addAll(names)
            }
        }
        return roles
    }

    List<String> rolePermissions(String roleName) {
        List<String> rolesPerms = []
        ldapSearch { InitialDirContext ctx, String ldapUrl ->

            String groupPerm = "${permSubCn},cn=$roleName,$groupOu"
            try {
                Attributes result = ctx.getAttributes(groupPerm)
                Attribute permsAttr = result.get(permMemberElement)
                rolesPerms.addAll(permsAttr.all.collect { cleanPermString((String) it) })
            } catch (e) {
                LOG.info "Got: $e.message (trying to get role permissions)"
            }
        }
        return rolesPerms
    }

    List<String> userPermissions(String userCommonName) {
        List<String> userPerms = []
        ldapSearch { InitialDirContext ctx, String ldapUrl ->

            String groupPerm = "${permSubCn},cn=$userCommonName,$searchBase"
            try {
                Attributes result = ctx.getAttributes(groupPerm)
                Attribute permsAttr = result.get(permMemberElement)
                userPerms.addAll(permsAttr.all.collect { cleanPermString((String) it) })
            } catch (e) {
                LOG.info "Got: $e.message (trying to get user permissions)"
            }
        }
        return userPerms
    }

    private String cleanPermString(String perm) {
        if (perm.startsWith(permMemberPrefix)) {
            perm = perm.substring(permMemberPrefix.size())
        }
        perm.replaceAll('"', '')
    }

    protected ldapSearch(Closure work) {
        String ldapUrl = findLDAPServerUrlToUse(searchUser, searchPass)
        if (ldapUrl) {
            InitialDirContext ctx = getLDAPContext(searchUser, searchPass, ldapUrl)
            return work(ctx, ldapUrl)
        } else {
            throw new AuthenticationException("No LDAP server available.")
        }
    }

    boolean checkConnection() {
        String ldapUrl = findLDAPServerUrlToUse(searchUser, searchPass)
        return ldapUrl != null
    }

    /**
     * Get the attributes for a user as a map. Some attributes will be a list.
     * @param userName
     * @return
     */
    Map getUserAttributes(String userName) {
        Map attMap = [:]
        ldapSearch { InitialDirContext ctx, String ldapUrl ->

            // Look up the DN for the LDAP entry that has a 'uid' value
            // matching the given username.
            BasicAttributes matchAttrs = new BasicAttributes(true)
            matchAttrs.put(new BasicAttribute(usernameAttribute, userName))

            NamingEnumeration<SearchResult> result = ctx.search(searchBase, matchAttrs)
            if (result.hasMore()) {
                SearchResult searchResult = result.next()
                searchResult.attributes.all.each { Attribute attr ->
                    List vals = attr.all.collect { it }
                    if (vals.size() > 1) {
                        attMap.put(attr.ID, vals)
                    } else {
                        attMap.put(attr.ID, vals.first())
                    }
                }
            }
        }
        return attMap
    }

    private String findLDAPServerUrlToUse(String user, String password) {
        if (!cachedUrl) {
            // Set up the configuration for the LDAP search we are about to do.
            Hashtable env = getBaseLDAPEnvironment(user, password)

            // Find an LDAP server that we can connect to.
            InitialDirContext ctx = null
            String urlUsed = ldapUrls.find { url ->
                LOG.info "Trying LDAP server ${url} ..."
                env[Context.PROVIDER_URL] = url

                // If an exception occurs, log it.
                try {
                    ctx = new InitialDirContext(env)
                    return true
                }
                catch (NamingException e) {
                    LOG.error "Could not connect to ${url}: ${e}"
                    return false
                }
            }
            cachedUrl = urlUsed
        }
        return cachedUrl
    }

    private static InitialDirContext getLDAPContext(String user, String password, String ldapUrl) {
        // Set up the configuration for the LDAP search we are about to do.
        Hashtable env = getBaseLDAPEnvironment(user, password)
        env[Context.PROVIDER_URL] = ldapUrl
        return new InitialDirContext(env)
    }

    private static Hashtable getBaseLDAPEnvironment(String user, String password) {
        def env = new Hashtable()
        env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        if (user) {
            // Non-anonymous access for the search.
            env[Context.SECURITY_AUTHENTICATION] = "simple"
            env[Context.SECURITY_PRINCIPAL] = user
            env[Context.SECURITY_CREDENTIALS] = password
        }
        return env
    }

}

class LdapUser implements PrincipalHolder {

    String fullName
    String userName
    String lastName
    LdapServer ldapServer

    LdapUser(LdapServer ldapServer, String userName) {
        this.userName = userName
        this.ldapServer = ldapServer
        Map attrs = getAttributes()
        fullName = attrs.cn
        lastName = attrs.sn
    }

    Map getAttributes() {
        ldapServer.getUserAttributes(userName)
    }

    @Override
    Set<String> getRoles() {
        ldapServer.roles(userName)
    }

    @Override
    Set<String> getPermissions() {
        Set<String> permissions = []
        ldapServer.roles(userName).each { String roleName ->
            permissions.addAll(ldapServer.rolePermissions(roleName))
        }
        permissions.addAll(ldapServer.userPermissions(fullName))
        return permissions
    }

    String toString() {
        userName
    }

}