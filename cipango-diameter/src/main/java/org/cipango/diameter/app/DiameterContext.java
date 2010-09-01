// ========================================================================
// Copyright 2009 NEXCOM Systems
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

package org.cipango.diameter.app;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cipango.diameter.DiameterHandler;
import org.cipango.diameter.DiameterMessage;
import org.cipango.server.session.AppSessionIf;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;

public class DiameterContext implements DiameterHandler
{
	private SipAppContext _defaultContext;
	private Map<String, DiameterListener[]> _listeners = new ConcurrentHashMap<String, DiameterListener[]>();
	
	private Method _handleMsg;
	
	public DiameterContext()
	{
		try 
		{
		 _handleMsg = DiameterListener.class.getMethod("handle", DiameterMessage.class);
		} 
        catch (NoSuchMethodException e)
        {
            throw new ExceptionInInitializerError(e);
        }
	}
	
	public void addListeners(DiameterListener[] listeners, WebAppContext context)
	{
		_listeners.put(context.getContextPath(), listeners);
		if (_defaultContext == null)
			_defaultContext = (SipAppContext) context;
	}
	
	public void removeListeners(WebAppContext context)
	{
		_listeners.remove(context.getContextPath());
		
		if (_defaultContext == context)
			_defaultContext = null;
	}
	//TODO init default context
	
	public void handle(DiameterMessage message) throws IOException
	{
		DiameterListener[] listeners = null;
		SipAppContext context = null;
		AppSessionIf appSession = (AppSessionIf) message.getApplicationSession();
		if (appSession != null)
			context = appSession.getAppSession().getContext();
		
		if (context == null)
			context = _defaultContext;
		
		if (context != null)
			listeners = _listeners.get(context.getContextPath());

		if (listeners != null && listeners.length != 0)
			context.fire(listeners, _handleMsg, message);
		else
			Log.warn("No diameter listeners for context {} to handle message {}", 
					context == null ? "" : context.getName(), message);	
	}
}
