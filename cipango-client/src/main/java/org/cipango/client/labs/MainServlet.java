// ========================================================================
// Copyright 2011 NEXCOM Systems
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
package org.cipango.client.labs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.client.labs.interceptor.MessageInterceptor;
import org.eclipse.jetty.util.log.Log;

public class MainServlet extends SipServlet
{
	
	private UaManager _cipangoClient;
	
	public MainServlet(UaManager cipangoClient)
	{
		_cipangoClient = cipangoClient;
	}

	@Override
	protected void doRequest(SipServletRequest request) throws ServletException, IOException
	{
		Session session = (Session) request.getSession().getAttribute(SipSession.class.getName());

		SipRequestImpl sipRequest = new SipRequestImpl(request);
		if (session == null)
		{
			session = _cipangoClient.findUasSession(sipRequest);
			if (session == null)
			{
				Log.warn("Received initial request and there is no UAS session to handle it.\n" + request);
				request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "No UAS session found");
				return;
			}
			session.setSipSession(request.getSession());
		}
		synchronized (session)
		{
			session.addSipRequest(sipRequest);
			for (MessageInterceptor interceptor : session.getMessageInterceptors())
			{
				try
				{
					if (interceptor.intercept(sipRequest))
					{
						return;
					}
				} 
				catch (Throwable e) {
					Log.warn("Failed to intercept message " + request + " with " + interceptor, e);
				}
			}
			session.notify();
		}
		
	}

	@Override
	protected void doResponse(SipServletResponse response) throws ServletException, IOException
	{
		SipServletRequest request = response.getRequest();

		SipRequestImpl sipRequest = (SipRequestImpl) request.getAttribute(SipMessage.class.getName());
		synchronized (sipRequest)
		{
			SipResponseImpl sipResponse = new SipResponseImpl(response);
			sipRequest.addSipResponse(sipResponse);
			for (MessageInterceptor interceptor : sipRequest.session().getMessageInterceptors())
			{
				try
				{
					if (interceptor.intercept(sipResponse))
					{
						return;
					}
				} 
				catch (Throwable e) {
					Log.warn("Failed to intercept message " + response + " with " + interceptor, e);
				}
			}
			sipRequest.notify();
		}
	}
	
}
