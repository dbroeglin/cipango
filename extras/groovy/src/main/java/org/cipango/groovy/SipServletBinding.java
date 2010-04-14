// ========================================================================
// Copyright 2010 NEXCOM Systems
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

package org.cipango.groovy;

import java.util.ArrayList;
import java.util.List;

import groovy.lang.Binding;

import javax.servlet.ServletContext;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;

/**
 * SIP-Servlet-specific binding extension.
 * <p>
 * <h3>Variables</h3>
 * <ul>
 *    <li><tt>"context"</tt> : the ServletContext object</li>
 *    <li><tt>"session"</tt> : shorthand for <code>request.getSession()</code></li>
 *    <li><tt>"sipFactory"</tt> : SIP servlet factory</code></li>
 * </ul>
 * <p/>
 */
public class SipServletBinding extends Binding
{

	public static final String
		CONTEXT = "context",
		SESSION = "session",
		SIP_FACTORY = "sipFactory";
	
	protected List<String> _reservedNames = new ArrayList<String>();
	
	public SipServletBinding(SipServletMessage message, ServletContext context)
	{
		super.setVariable(CONTEXT, context);
		super.setVariable(SIP_FACTORY, context.getAttribute(SipServlet.SIP_FACTORY));
		super.setVariable(SESSION, message.getSession());
	}

	@Override
	public void setVariable(String name, Object value)
	{
		excludeReservedName(name);
		super.setVariable(name, value);
	}

	protected void excludeReservedName(String name)
	{
		if (_reservedNames.contains(name))
			throw new IllegalArgumentException("Can't bind variable to key named '" + name + "'.");
	}

}
