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

package org.cipango.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletMessage;

import org.cipango.CallSession;
import org.cipango.SessionManager.*;
import org.cipango.Server;
import org.cipango.SipHandler;
import org.cipango.SipMessage;
import org.cipango.SipRequest;
import org.cipango.sipapp.SipAppContext;
import org.cipango.util.ID;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.log.Log;

/**
 * Performs call lock for received messages. 
 */
public class CallSessionHandler extends HandlerWrapper implements SipHandler 
{
	private Server _server;
	
	@Override
	protected void doStart() throws Exception
	{
		super.doStart();
		_server = ((Server) getServer());
	}
	
	public String getCallSessionId(SipMessage message)
	{		
		if (message.isRequest())
		{
			SipRequest request = (SipRequest) message;
			
			if (request.isInitial())
			{
				String sessionKey = (String) request.getHandlerAttribute(ID.SESSION_KEY_ATTRIBUTE);
			
				if (sessionKey != null)
				{
					SipAppContext context = (SipAppContext) request.getHandlerAttribute(ID.CONTEXT_ATTRIBUTE);
					return ID.getIdFromSessionKey(context.getName(), sessionKey);
				}
			}
			else
			{		
				String appSessionId = request.getParameter(ID.APP_SESSION_ID_PARAMETER);
				if (appSessionId != null && ID.isFromSessionKey(appSessionId))
					return appSessionId;
			}
		}
		return ID.getCallSessionId(message.getCallId());
	}
	
	public void handle(SipServletMessage message) throws IOException, ServletException
	{
		SipMessage baseMessage = (SipMessage) message;
		
		String callSessionId = getCallSessionId(baseMessage); 
		
		if (Log.isDebugEnabled())
			Log.debug("handling message {} for call session: {}", baseMessage.getRequestLine(), callSessionId);
		
		CallSession callSession = null;
		SessionTransaction transaction = null;
		
		while (callSession == null)
		{	
			transaction = _server.getSessionManager().begin(callSessionId);
			if (transaction == null)
				try { Thread.sleep(500); } catch (InterruptedException e) { }
			
			callSession = transaction.getCallSession();
		}
		
		if (Log.isDebugEnabled())
			Log.debug("started transaction for call session {}", callSession);
		
		try
		{
			baseMessage.setCallSession(callSession);
			((SipHandler) getHandler()).handle(message);
		}
		finally
		{
			transaction.done(); 
			
			if (Log.isDebugEnabled())
				Log.debug("transaction done for call session {}", callSession);
		}
	}
}
