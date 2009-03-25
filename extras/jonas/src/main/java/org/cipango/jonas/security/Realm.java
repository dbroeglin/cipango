/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2007 Bull S.A.S.
 * Contact: jonas-team@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * --------------------------------------------------------------------------
 * $Id: Realm.java 13058 2008-03-05 16:56:28Z alitokmen $
 * --------------------------------------------------------------------------
 */

package org.cipango.jonas.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.Subject;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.UserRealm;

import org.ow2.jonas.lib.security.context.SecurityContext;
import org.ow2.jonas.lib.security.context.SecurityCurrent;
import org.ow2.jonas.security.auth.callback.NoInputCallbackHandler;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;



/**
 * Realm used by Jetty 6 service.
 * @author Florent Benoit
 */
public class Realm implements UserRealm {

    /**
     * Name used in the JAAS config file.
     */
    private static final String JAAS_CONFIG_NAME = "jetty";

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(Realm.class);

    /**
     * Name of this realm.
     */
    private String name;

    /**
     * List of authenticated users.
     */
    private Map<String, JettyPrincipal> users = null;


    /**
     * Default Constructor.
     */
    public Realm() {
        this("Jetty Realm");
    }

    /**
     * Default Constructor.
     * @param name of the realm
     */
    public Realm(final String name) {
        this.users = new ConcurrentHashMap<String, JettyPrincipal>();
        setName(name);
    }

    /**
     * Authenticate a user with a specific username and credentials.
     * @param username name of the user
     * @param credentials credential of the user
     * @param request httprequest
     * @return a Jetty principal
     */
    public Principal authenticate(final String username, final Object credentials, final Request request) {

        // No authentication can be made with a null username
        if (username == null) {
            return null;
        }

        Principal jettyPrincipal = (Principal) getUsers().get(username);
        // User previously authenticated --> remove from the cache
        if (jettyPrincipal != null) {
            removeUser(username);
        }

        NoInputCallbackHandler noInputCH = null;
        LoginContext loginContext = null;
        noInputCH = new NoInputCallbackHandler(username, (String) credentials, null);

        //Establish a LoginContext to use for authentication
        try {
            loginContext = new LoginContext(JAAS_CONFIG_NAME, noInputCH);
        } catch (LoginException e) {
            logger.error("Cannot create a login context for the user ''{0}''", username, e);
            return null;
        }

        // Negotiate a login via this LoginContext
        Subject subject = null;
        try {
            loginContext.login();
            subject = loginContext.getSubject();
            if (subject == null) {
                logger.error("No Subject for user ''{0}''", username);
                return null;
            }
        } catch (AccountExpiredException e) {
            logger.error("Account expired for user ''{0}''", username, e);
            return null;
        } catch (CredentialExpiredException e) {
            logger.error("Credential Expired for user ''{0}''", username, e);
            return null;
        } catch (FailedLoginException e) {
            logger.error("Failed Login exception for user ''{0}''", username, e);
            return null;
        } catch (LoginException e) {
            logger.error("Login exception for user ''{0}''", username, e);
            return null;
        }

        // Retrieve first principal name found (without groups)
        Iterator<Principal> iterator = subject.getPrincipals(Principal.class).iterator();
        String userName = null;
        while (iterator.hasNext() && (userName == null)) {
            Principal principal = iterator.next();
            if (!(principal instanceof Group)) {
                userName = principal.getName();
            }
        }

        // No name --> error
        if (userName == null) {
            logger.error("No Username found in the subject");
            return null;
        }

        // Retrieve all roles of the user (Roles are members of the Group class)
        Set<Group> groups = subject.getPrincipals(Group.class);
        List<String> roles = new ArrayList<String>();

        for (Group group : groups) {
            Enumeration<? extends Principal> e = group.members();
            while (e.hasMoreElements()) {
                Principal p = e.nextElement();
                roles.add(p.getName());
            }
        }

        // Create a JettyPrincipal for Jetty
        JettyPrincipal principal = new JettyPrincipal(userName, roles);

        // Register the subject in the security context
        //SecurityContext ctx = new SecurityContext(subject);
        SecurityContext ctx = new SecurityContext(userName, roles);
        SecurityCurrent current = SecurityCurrent.getCurrent();
        current.setSecurityContext(ctx);

        // Add to cache
        addUser(username, principal);

        return principal;
    }

    /**
     * @return the users.
     */
    protected Map<String, JettyPrincipal> getUsers() {
        return users;
    }

    /**
     * Add a user to the current map.
     * @param username name of the user
     * @param principal object
     */
    protected void addUser(final String username, final JettyPrincipal principal) {
        users.put(username, principal);
    }

    /**
     * Remove a specific user.
     * @param username user to remove
     */
    protected void removeUser(final String username) {
        users.remove(username);
    }

    /**
     * Check if a user is in a role.
     * @param user The user, which must be from this realm
     * @param roleName the role to test for the given user
     * @return True if the user can act in the role.
     */
    public synchronized boolean isUserInRole(final Principal user, final String roleName) {
        if (user == null) {
            return false;
        }

        if (user instanceof JettyPrincipal) {
            return ((JettyPrincipal) user).isUserInRole(roleName);
        } else {
            logger.error("The user ''{0}'' is not instance of JettyPrincipal", user);
            return false;
        }
    }

    /**
     * Gets the principal with the given username.
     * @param username the given username
     * @return the principal with the given username
     */
    public Principal getPrincipal(final String username) {
        logger.debug("Get principal with username ''{0}''", username);

        JettyPrincipal principal = (JettyPrincipal) users.get(username);
        SecurityContext ctx = new SecurityContext(principal.getName(), principal.getRoles());
        SecurityCurrent current = SecurityCurrent.getCurrent();
        current.setSecurityContext(ctx);
        return principal;
    }

    /**
     * Disassociate a user.
     * @param user the given user
     */
    public void disassociate(final Principal user) {
        // Set anonymous identity
        SecurityCurrent.getCurrent().setSecurityContext(new SecurityContext());
    }

    /**
     * Push a role to a user (not implemented).
     * @param user the given user
     * @param role the role to push
     * @return the new principal
     */
    public Principal pushRole(final Principal user, final String role) {
        return user;
    }

    /**
     * Pop a role to a user (not implemented).
     * @param user the given user
     * @return the new principal
     */
    public Principal popRole(final Principal user) {
        return user;
    }

    /**
     * Log out a specific user.
     * @param user the user to logout
     */
    public void logout(final Principal user) {
    }

    /**
     * Check if the specific user is authenticated.
     * @param user the user to reauthenticate
     * @return true if the user is authenthicated
     */
    public boolean reauthenticate(final Principal user) {
        if (user instanceof JettyPrincipal) {
            return ((JettyPrincipal) user).isAuthenticated();
        } else {
            return false;
        }
    }

    /**
     * Sets the name of the realm.
     * @param name The name to set.
     */
    protected void setName(final String name) {
        this.name = name;
    }

    /**
     * @return The realm name.
     */
    public String getName() {
        return name;
    }

}
