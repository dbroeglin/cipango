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
package org.cipango.diameter.log;

import org.cipango.diameter.DiameterConnection;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.DiameterMessageListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;

public class MessageListenerCollection extends AbstractLifeCycle implements DiameterMessageListener
{
	private DiameterMessageListener[] _listeners;
	private Server _server;

	
	public void messageReceived(DiameterMessage message, DiameterConnection connection)
	{
		for (int i = 0; _listeners != null && i < _listeners.length; i++)
			_listeners[i].messageReceived(message, connection);
	}

	public void messageSent(DiameterMessage message, DiameterConnection connection)
	{
		for (int i = 0; _listeners != null && i < _listeners.length; i++)
			_listeners[i].messageSent(message, connection);
	}

	public DiameterMessageListener[] getMessageListeners()
	{
		return _listeners;
	}

	public void setMessageListeners(DiameterMessageListener[] loggers)
	{
		DiameterMessageListener[] oldLoggers = _listeners == null ? null : _listeners.clone();
		if (_server != null)
			_server.getContainer().update(this, oldLoggers, loggers, "loggers", true);
		_listeners = loggers;

		MultiException mex = new MultiException();
		for (int i = 0; oldLoggers != null && i < oldLoggers.length; i++)
		{
			if (oldLoggers[i] != null)
			{
				try
				{
					if (oldLoggers[i] instanceof LifeCycle)
					{
						LifeCycle lifeCycle = (LifeCycle) oldLoggers[i];
						if (lifeCycle.isStarted())
							lifeCycle.stop();
					}
				}
				catch (Throwable e)
				{
					mex.add(e);
				}
			}
		}
		if (isStarted())
			try { doStart(); } catch (Throwable e) { mex.add(e); }
		
		mex.ifExceptionThrowRuntime();
	}
	
	public void addMessageListener(DiameterMessageListener accessLog)
    {
        setMessageListeners((DiameterMessageListener[])LazyList.addToArray(getMessageListeners(), accessLog, DiameterMessageListener.class));
    }
    
    public void removeMessageListener(DiameterMessageListener accessLog)
    {
    	DiameterMessageListener[] loggers = getMessageListeners();
        
        if (loggers!=null && loggers.length>0 )
            setMessageListeners((DiameterMessageListener[])LazyList.removeFromArray(loggers, accessLog));
    }

	public void setServer(Server server)
	{
		_server = server;
	}

	@Override
	protected void doStart() throws Exception
	{
		for (int i = 0; _listeners != null && i < _listeners.length; i++)
		{
			try
			{
				if (_listeners[i] instanceof LifeCycle)
					((LifeCycle) _listeners[i]).start();
			}
			catch (Exception e)
			{
				Log.warn(e);
			}
		}
		super.doStart();
	}

	@Override
	protected void doStop()
	{
		for (int i = 0; _listeners != null && i < _listeners.length; i++)
		{
			try
			{
				if (_listeners[i] instanceof LifeCycle)
					((LifeCycle) _listeners[i]).stop();
			}
			catch (Exception e)
			{
				Log.warn(e);
			}

		}
	}

	
}
