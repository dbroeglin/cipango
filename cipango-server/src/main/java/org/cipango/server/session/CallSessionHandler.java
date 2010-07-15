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

package org.cipango.server.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletMessage;

import org.cipango.server.session.SessionManager.*;
import org.cipango.server.ID;
import org.cipango.server.Server;
import org.cipango.server.SipHandler;
import org.cipango.server.SipMessage;
import org.cipango.server.SipRequest;
import org.cipango.sipapp.SipAppContext;

import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;

/**
 * Performs call lock for received messages. 
 */
public class CallSessionHandler extends HandlerWrapper implements SipHandler 
{
	private Server _server;
	private Map<String, Queue> _queues = new HashMap<String, Queue>();
	
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
					return ID.getIdFromKey(context.getName(), sessionKey);
				}
			}
			else
			{		
				String appSessionId = request.getParameter(ID.APP_SESSION_ID_PARAMETER);
				if (appSessionId != null && ID.isKey(appSessionId))
					return appSessionId;
			}
		}
		else
		{
			String appSessionId = message.getTopVia().getParameter(ID.APP_SESSION_ID_PARAMETER);
			if (appSessionId != null && ID.isKey(appSessionId))
				return appSessionId;
		}
		return ID.getCallSessionId(message.getCallId());
	}
	
	public void handle(SipServletMessage message) throws IOException, ServletException
	{
		SipMessage baseMessage = (SipMessage) message;
	
		String id = getCallSessionId(baseMessage);
		
		if (Log.isDebugEnabled())
			Log.debug("handling message {} for call session: {}", baseMessage.getRequestLine(), id);
			
		Queue queue = null;
		synchronized (_queues) 
		{
			queue = _queues.get(id);
			if (queue == null)
			{
				queue = new Queue(id);
				_queues.put(id, queue);
			}
			queue.add(baseMessage);
		}
		queue.handle();
	}
	
	class Queue
	{
		private static final int INITIAL = 0;
		private static final int HANDLING = 1;
		private static final int DONE = 2;
		
		private String _id;
		private Object _messages;
		
		private int _state = INITIAL;
		
		public Queue(String id)
		{
			_id = id;
		}
		
		public synchronized void add(SipMessage message)
		{
			_messages = LazyList.add(_messages, message);
		}
		
		public synchronized SipMessage poll()
		{
			if (LazyList.size(_messages) == 0)
				return null;
			SipMessage message = (SipMessage) LazyList.get(_messages, 0);
			_messages = LazyList.remove(_messages, 0);
			return message;
		}
		
		private boolean isDone()
		{
			synchronized (_queues) 
			{
				synchronized (this)
				{
					if (LazyList.size(_messages) == 0)
					{
						_queues.remove(_id);
						_state = DONE;
						return true;
					}
				}
			}
			return false;
		}
		
		public void handle()
		{
			synchronized (this)
			{
				if (_state != INITIAL)
					return;
				else
					_state = HANDLING;
			}
			
			CallSession callSession = null;
			SessionScope scope = null;
			
			while (callSession == null)
			{	
				scope = _server.getSessionManager().openScope(_id);
				
				callSession = scope.getCallSession();
				
				if (callSession == null)
					try { Thread.sleep(500); } catch (InterruptedException e) { } // TODO
			}
			
			try
			{
				SipMessage message = null;
				do
				{
					while ((message = poll()) != null)
					{
						try
						{
							message.setCallSession(callSession);
							((SipHandler) getHandler()).handle(message);
						}
						catch (Exception e)
						{
							Log.ignore(e);
						}
					}
				}
				while (!isDone());
			}
			finally 
			{
				scope.close();
			}
		}
	}
}
