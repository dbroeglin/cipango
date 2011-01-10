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

import org.cipango.diameter.api.DiameterErrorEvent;
import org.cipango.diameter.api.DiameterErrorListener;
import org.cipango.diameter.api.DiameterListener;
import org.cipango.diameter.api.DiameterServletMessage;
import org.cipango.diameter.node.DiameterAnswer;
import org.cipango.diameter.node.DiameterHandler;
import org.cipango.diameter.node.DiameterMessage;
import org.cipango.diameter.node.DiameterRequest;
import org.cipango.server.session.AppSessionIf;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;

public class DiameterContext implements DiameterHandler
{
	private SipAppContext _defaultContext;
	private Map<String, DiameterAppContext> _diameterListeners = new ConcurrentHashMap<String, DiameterAppContext>();

	private Method _handleMsg;
	private Method _noAnswerReceived;
	
	public DiameterContext()
	{
		try 
		{
		 _handleMsg = DiameterListener.class.getMethod("handle", DiameterServletMessage.class);
		 _noAnswerReceived = DiameterErrorListener.class.getMethod("noAnswerReceived", DiameterErrorEvent.class);
		} 
        catch (NoSuchMethodException e)
        {
            throw new ExceptionInInitializerError(e);
        }
	}
	
	public void addListeners(WebAppContext context, DiameterListener[] listeners, DiameterErrorListener[] errorListeners)
	{
		_diameterListeners.put(context.getContextPath(), new DiameterAppContext(listeners, errorListeners));
		if (_defaultContext == null)
			_defaultContext = (SipAppContext) context;
	}
	
	public void addListener(WebAppContext context, DiameterListener listener)
	{
		DiameterAppContext diameterAppContext = _diameterListeners.get(context.getContextPath());
		if (diameterAppContext == null)
		{
			diameterAppContext = new DiameterAppContext();
			_diameterListeners.put(context.getContextPath(), diameterAppContext);
		}
		
		diameterAppContext.addDiameterListener(listener);
		
		if (_defaultContext == null)
			_defaultContext = (SipAppContext) context;
	}
	
	public void removeListener(WebAppContext context, DiameterListener listener)
	{
		DiameterAppContext diameterAppContext = _diameterListeners.get(context.getContextPath());
		if (diameterAppContext == null)
			return;
		
		diameterAppContext.removeDiameterListener(listener);
	}
	
	public void addErrorListener(WebAppContext context, DiameterErrorListener listener)
	{
		DiameterAppContext diameterAppContext = _diameterListeners.get(context.getContextPath());
		if (diameterAppContext == null)
		{
			diameterAppContext = new DiameterAppContext();
			_diameterListeners.put(context.getContextPath(), diameterAppContext);
		}
		
		diameterAppContext.addErrorListener(listener);
		
		if (_defaultContext == null)
			_defaultContext = (SipAppContext) context;
	}
	
	public void removeErrorListener(WebAppContext context, DiameterErrorListener listener)
	{
		DiameterAppContext diameterAppContext = _diameterListeners.get(context.getContextPath());
		if (diameterAppContext == null)
			return;
		
		diameterAppContext.removeErrorListener(listener);
	}
	
	public void removeListeners(WebAppContext context)
	{
		_diameterListeners.remove(context.getContextPath());
		
		if (_defaultContext == context)
			_defaultContext = null;
	}
	//TODO init default context
	
	public void handle(DiameterMessage message) throws IOException
	{
		DiameterListener[] listeners = null;
		SipAppContext context = null;
		if (message instanceof DiameterAnswer)
			context = ((DiameterAnswer) message).getRequest().getContext();
		
		if (context == null)
		{
			AppSessionIf appSession = (AppSessionIf) message.getApplicationSession();
			if (appSession != null)
				context = appSession.getAppSession().getContext();
		}
		
		if (context == null)
			context = _defaultContext;
		
		if (context != null)
		{
			DiameterAppContext ctx = _diameterListeners.get(context.getContextPath());
			if (ctx != null)
				listeners = ctx.getDiameterListeners();
		}

		if (listeners != null && listeners.length != 0)
			context.fire(listeners, _handleMsg, message);
		else
			Log.warn("No diameter listeners for context {} to handle message {}", 
					context == null ? "" : context.getName(), message);	
	}
	
	public void fireNoAnswerReceived(DiameterRequest request, long timeout)
	{
		DiameterErrorListener[] listeners = null;
		SipAppContext context = null;
		AppSessionIf appSession = (AppSessionIf) request.getApplicationSession();
		if (appSession != null)
			context = appSession.getAppSession().getContext();
		
		if (context != null)
		{
			DiameterAppContext ctx = _diameterListeners.get(context.getContextPath());
			if (ctx != null)
				listeners = ctx.getErrorListeners();
		}
		
		if (listeners != null && listeners.length != 0)
			context.fire(listeners, _noAnswerReceived, new DiameterErrorEvent(request, timeout));		
	}
	
}

class DiameterAppContext
{
	private DiameterListener[] _diameterListeners;
	private DiameterErrorListener[] _errorListeners;
	
	public DiameterAppContext()
	{
	}
	
	public DiameterAppContext(DiameterListener[] listeners, DiameterErrorListener[] errorListeners)
	{
		_diameterListeners = listeners;
		_errorListeners = errorListeners;
	}
	
	public void addDiameterListener(DiameterListener l)
	{
		_diameterListeners = (DiameterListener[]) LazyList.addToArray(_diameterListeners, l, DiameterListener.class);
	}
	
	public void removeDiameterListener(DiameterListener l)
	{
		_diameterListeners = (DiameterListener[]) LazyList.removeFromArray(_diameterListeners, l);
	}
	
	public void addErrorListener(DiameterErrorListener l)
	{
		_errorListeners = (DiameterErrorListener[]) LazyList.addToArray(_errorListeners, l, DiameterErrorListener.class);
	}
	
	public void removeErrorListener(DiameterErrorListener l)
	{
		_errorListeners = (DiameterErrorListener[]) LazyList.removeFromArray(_errorListeners, l);
	}

	public DiameterListener[] getDiameterListeners()
	{
		return _diameterListeners;
	}

	public DiameterErrorListener[] getErrorListeners()
	{
		return _errorListeners;
	}
}

