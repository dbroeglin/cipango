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

import org.cipango.Call;
import org.cipango.CallManager;
import org.cipango.Server;
import org.cipango.SipHandler;
import org.cipango.SipMessage;
import org.cipango.util.ID;
import org.mortbay.jetty.handler.HandlerWrapper;

/**
 * Performs call lock for received messages. 
 */
public class CallHandler extends HandlerWrapper implements SipHandler 
{
	private CallManager _callManager;
	
	@Override
	protected void doStart() throws Exception
	{
		super.doStart();
		_callManager = ((Server) getServer()).getCallManager();
	}
	
	public void handle(SipServletMessage message) throws IOException, ServletException
	{
		String cid = ID.getCallId(message.getCallId());
		
		Call call = null;
		
		while (call == null)
		{	
			try
			{			
				call = _callManager.lock(cid);
				if (call == null)
					Thread.sleep(500); // TODO async retry 
			}
			catch (InterruptedException e)
			{
				throw new IOException(e.getMessage());
			}
		}
		
		try
		{
			((SipMessage) message).setCall(call);
			((SipHandler) getHandler()).handle(message);
		}
		finally
		{
			_callManager.unlock(call);
		}
	}
}
