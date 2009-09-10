// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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

package org.cipango.kaleo.sip;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;

import org.cipango.kaleo.Constants;
import org.cipango.kaleo.location.event.RegEventPackage;

public class KaleoServlet extends SipServlet
{
	private static final long serialVersionUID = 1L;

	protected void doRequest(SipServletRequest request) // throws ServletException, IOException
	{
		try
		{
			super.doRequest(request);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected void doRegister(SipServletRequest register) throws ServletException, IOException
	{
		getServletContext().getNamedDispatcher("registrar").forward(register, null);
	}
	
	protected void doPublish(SipServletRequest publish) throws ServletException, IOException
	{
		getServletContext().getNamedDispatcher("presence").forward(publish, null);
	}
	
	protected void doSubscribe(SipServletRequest subscribe) throws ServletException, IOException
	{
		String event = subscribe.getHeader(Constants.EVENT);
		if (RegEventPackage.NAME.equals(event))
			getServletContext().getNamedDispatcher("registrar").forward(subscribe, null);
		else
			getServletContext().getNamedDispatcher("presence").forward(subscribe, null);
	}
	
	protected void doInvite(SipServletRequest invite) throws ServletException, IOException
	{
		getServletContext().getNamedDispatcher("proxy").forward(invite, null);
	}
}

