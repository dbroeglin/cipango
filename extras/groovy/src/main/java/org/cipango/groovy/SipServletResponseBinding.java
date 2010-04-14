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

import javax.servlet.ServletContext;
import javax.servlet.sip.SipServletResponse;

/**
 * SIP-Servlet response specific binding extension.
 * <p>
 * <h3>Variables</h3>
 * <ul>
 *    <li><tt>"response"</tt> : the <code>SipServletResponse</code> object</li>
 * </ul>
 * <p/>
 */
public class SipServletResponseBinding extends SipServletBinding
{

	public static final String 
		RESPONSE = "response";

	public SipServletResponseBinding(SipServletResponse response, ServletContext context)
	{
		super(response, context);
		setVariable(RESPONSE, response);
	}


}
