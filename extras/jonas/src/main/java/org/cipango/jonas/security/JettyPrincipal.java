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
 * $Id:JettyPrincipal.java 10798 2007-07-01 20:56:02Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.cipango.jonas.security;

import java.security.Principal;
import java.util.List;

/**
 * Define a principal which can be used by Jetty.
 * @author Florent Benoit
 */
public class JettyPrincipal implements Principal {

    /**
     * Name of the principal.
     */
    private String name;

    /**
     * Roles for this principal.
     */
    private List<String> roles;

    /**
     * Constructor.
     * @param name name of this principal
     * @param roles roles for this principal
     */
    public JettyPrincipal(final String name, final List<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    /**
     * Get the name of this principal.
     * @return the name of this principal
     */
    public String getName() {
        return name;
    }

    /**
     * This user is authenticated ?
     * @return true if it is authenticated
     */
    public boolean isAuthenticated() {
        return true;
    }

    /**
     * Check if the given role is in the user's roles.
     * @param role the given role
     * @return true if the role is in the user's roles
     */
    public boolean isUserInRole(final String role) {
        if (roles == null) {
            return (false);
        }
        return roles.contains(role);
    }

    /**
     * Gets the roles of this user.
     * @return roles of this user
     */
    public List<String> getRoles() {
        return roles;
    }

}
