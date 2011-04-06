// ========================================================================
// Copyright 2003-2011 the original author or authors.
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
package org.cipango.sipatra;

import java.io.IOException;

import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;

/**
 * Sipatra Main Servlet
 * 
 * @author Dominique Broeglin <dominique.broeglin@gmail.com>, Jean-Baptiste Morin
 */
public class SipatraServlet extends SipServlet
{
	private static final Logger _log = org.slf4j.LoggerFactory.getLogger(SipatraServlet.class);

	@Override
	public void doRequest(SipServletRequest request) throws IOException
	{
		invokeMethod(request, "do_request");
	}

	@Override
	public void doResponse(SipServletResponse response) throws IOException
	{
		invokeMethod(response, "do_response");
	}	

	private void invokeMethod(SipServletMessage message, String methodName) 
	{
		ScriptingContainer container = null;
		try 
		{
			GenericObjectPool pool = (GenericObjectPool) message.getSession().getServletContext().getAttribute(Attributes.POOL);
			container = (ScriptingContainer) pool.borrowObject();
            long beginTime = System.currentTimeMillis();
			try 
			{
				Object app = container.runScriptlet("Sipatra::Application::new");

				container.callMethod(app, "set_bindings", new Object[] { 
						message.getSession().getServletContext(),  
						message.getSession().getServletContext().getAttribute(SipServlet.SIP_FACTORY), 
						message.getSession(), 
						message, _log});
				container.callMethod(app, methodName);
			} 
			catch(Exception e) 
			{
				pool.invalidateObject(container);
				container = null;
			} 
			finally 
			{
				if(container != null) 
				{
					pool.returnObject(container);
				}
                _log.trace("Processed '%s' (%s, %s) in %dms", new Object[] { 
                           message.getMethod(), message.getSession().getId(), 
                           message.getCallId(), System.currentTimeMillis() - beginTime });
			}
		} 
		catch(Exception e) 
		{
			_log.error("ERROR >> Failed to borrow a JRuby Runtime ", e);
			// TODO: failed to borrow a runtime... What should we do?
			//throw e?
		}
	}
}
