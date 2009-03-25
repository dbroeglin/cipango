// ========================================================================
// Copyright 2007-2008 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.cipango.util;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.Password;

/**
 * JMXAuthenticator using HashUserRealm.
 */
public class HashAuthenticator implements JMXAuthenticator
{

	private HashUserRealm _userRealm;
	private String _role = "rmi";
	
	public HashAuthenticator() throws IOException {
		_userRealm = new HashUserRealm("JMXAuthenticator");
	}
	
	public HashAuthenticator(String config) throws IOException {
		_userRealm = new HashUserRealm("JMXAuthenticator", config);
	}
	
	public void setConfig(String config) throws IOException {
		_userRealm.setConfig(config);
	}
	
	public void setRole(String role) {
		_role = role;
	}

   public Subject authenticate(Object credentials) throws SecurityException
   {
      if (!(credentials instanceof String[])) {
    	  throw new SecurityException("Bad credentials");
      }
      String[] creds = (String[])credentials;
      if (creds.length != 2) {
    	  throw new SecurityException("Bad credentials");
      }

      String user = creds[0];

      if (creds[1] == null) {
    	  throw new SecurityException("Bad password");
      }
      
      Password password = new Password(creds[1]);

      Principal principal = _userRealm.authenticate(user, password, null);
      if (principal == null) {
    	  throw new SecurityException("Invalid user or password");
      }
      
      if (_userRealm.isUserInRole(principal, _role)) {
    	  Set principals = new HashSet();
          principals.add(new JMXPrincipal(user));
          return new Subject(true, principals, Collections.EMPTY_SET, Collections.EMPTY_SET);
      } else {
    	  throw new SecurityException("User is not in " + _role + " role");
      }     
    }

}
