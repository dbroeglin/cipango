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
package org.cipango.server.log;

import org.cipango.server.SipConnection;
import org.cipango.server.SipMessage;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiException;

public class AccessLogCollection extends AbstractLifeCycle implements AccessLog
{
	private AccessLog[] _loggers;
	private Server _server;

	public void messageReceived(SipMessage message, SipConnection connection)
	{
		for (int i = 0; _loggers != null && i < _loggers.length; i++)
			_loggers[i].messageReceived(message, connection);
	}

	public void messageSent(SipMessage message, SipConnection connection)
	{
		for (int i = 0; _loggers != null && i < _loggers.length; i++)
			_loggers[i].messageSent(message, connection);
	}

	public AccessLog[] getLoggers()
	{
		return _loggers;
	}

	public void setLoggers(AccessLog[] loggers)
	{
		AccessLog[] oldLoggers = _loggers == null ? null : _loggers.clone();
		if (_server != null)
			_server.getContainer().update(this, oldLoggers, loggers, "loggers", true);
		_loggers = loggers;

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
	
	public void addLogger(AccessLog accessLog)
    {
        setLoggers((AccessLog[])LazyList.addToArray(getLoggers(), accessLog, AccessLog.class));
    }
    
    public void removeLogger(AccessLog accessLog)
    {
    	AccessLog[] loggers = getLoggers();
        
        if (loggers!=null && loggers.length>0 )
            setLoggers((AccessLog[])LazyList.removeFromArray(loggers, accessLog));
    }

	public void setServer(Server server)
	{
		_server = server;
	}

	@Override
	protected void doStart() throws Exception
	{
		for (int i = 0; _loggers != null && i < _loggers.length; i++)
		{
			try
			{
				if (_loggers[i] instanceof LifeCycle)
					((LifeCycle) _loggers[i]).start();
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
		for (int i = 0; _loggers != null && i < _loggers.length; i++)
		{
			try
			{
				if (_loggers[i] instanceof LifeCycle)
					((LifeCycle) _loggers[i]).stop();
			}
			catch (Exception e)
			{
				Log.warn(e);
			}

		}
	}

}
