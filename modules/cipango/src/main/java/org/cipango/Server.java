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

package org.cipango;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;

import org.cipango.ar.ApplicationRouterLoader;
import org.cipango.ar.RouterInfoUtil;
import org.cipango.handler.SipContextHandlerCollection;
import org.cipango.log.EventLog;
import org.cipango.sip.ClientTransaction;
import org.cipango.sip.ClientTransactionListener;
import org.cipango.sip.SipConnector;
import org.cipango.sip.TransactionManager;
import org.cipango.sip.ConnectorManager;
import org.cipango.sipapp.SipAppContext;
import org.mortbay.component.LifeCycle;
import org.mortbay.log.Log;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;
import org.mortbay.util.MultiException;

/**
 * Cipango SIP/HTTP Servlet Server.
 * It extends Jetty HTTP Server to add SIP capabilities.
 */
public class Server extends org.mortbay.jetty.Server implements SipHandler
{
	private static String __sipVersion = (Server.class.getPackage() != null && Server.class.getPackage().getImplementationVersion() != null)
    	?Server.class.getPackage().getImplementationVersion() : "1.0.x";
    	
	private ThreadPool _sipThreadPool;
    
    private ConnectorManager _connectorManager = new ConnectorManager();
    private TransactionManager _transactionManager = new TransactionManager();
    
    private SessionManager _sessionManager;    
    private SipApplicationRouter _applicationRouter;

    private long _statsStartedAt = -1;
    private Object _statsLock = new Object();
    
    private long _messages;
    
    public Server()
    {
    	setConnectorManager(_connectorManager);
		setTransactionManager(_transactionManager);
    }
	
	@Override
	protected void doStart() throws Exception 
    {
		Log.info("cipango-" + __sipVersion);
		SipGenerator.setServerVersion(__sipVersion);

		MultiException mex = new MultiException();
		
		if (_sipThreadPool == null) 
			setSipThreadPool(new BoundedThreadPool());

		try
		{
			if (_sipThreadPool instanceof LifeCycle)
				((LifeCycle) _sipThreadPool).start();
		}
		catch (Throwable t) { mex.add(t); }
		
		try
		{
			super.doStart();
		}
		catch (Throwable t) { mex.add(t); }

		if (_applicationRouter == null)
			setApplicationRouter(ApplicationRouterLoader.loadApplicationRouter());
			
		if (_sessionManager == null)
			setSessionManager(new SessionManager());
		
		try 
		{
			_applicationRouter.init();
			
			List<String> appNames = new ArrayList<String>();
			SipAppContext[] contexts = ((SipContextHandlerCollection) getHandler()).getSipContexts();
			if (contexts != null)
			{
				for (SipAppContext context : contexts)
					if (context.hasSipServlets())
						appNames.add(context.getName());
			}
			_applicationRouter.applicationDeployed(appNames);
			
			_sessionManager.start();
			_connectorManager.start();
			
			if (contexts != null)
			{
				for (SipAppContext context : contexts)
					context.initialized();
			}
		}
		catch (Throwable t) { mex.add(t); }
		
		mex.ifExceptionThrow();
		
		EventLog.log(EventLog.START, "Cipango " + __sipVersion + " started");
	}
	
	@Override
    protected void doStop() throws Exception
    {
		EventLog.log(EventLog.START, "Stopping Cipango " + __sipVersion);
    	MultiException mex = new MultiException();
        
        try 
        {
        	_applicationRouter.destroy();
        	_connectorManager.stop();
    		_sessionManager.stop();
		} 
        catch (Throwable e) { mex.add(e); }

        try 
        {
			super.doStop();
		} 
        catch (Throwable e) { mex.add(e); }
        
        try
        {
        	if (_sipThreadPool instanceof LifeCycle)
        		((LifeCycle) _sipThreadPool).stop();
        }
        catch (Throwable e) { mex.add(e); }
       
        mex.ifExceptionThrow();
    }
    

	public void setApplicationRouter(SipApplicationRouter applicationRouter)
	{
		getContainer().update(this, _applicationRouter, applicationRouter, "applicationRouter");
		_applicationRouter = applicationRouter;
	}
	
	public SipApplicationRouter getApplicationRouter()
	{
		return _applicationRouter;
	}
	
    public void applicationDeployed(SipAppContext context)
    {
    	if (isStarted())
    		_applicationRouter.applicationDeployed(Collections.singletonList(context.getName()));
    }
    
    public void applicationUndeployed(SipAppContext context)
    {
    	if (isStarted())
    		_applicationRouter.applicationUndeployed(Collections.singletonList(context.getName()));
    }
    
    public void servletInitialized(SipAppContext context, SipServlet servlet)
    {
    	if (isStarted())
    		context.fireServletInitialized(servlet);
    }
    
    /**
     * AR for outgoing message
     */
    public ClientTransaction sendRequest(SipRequest request, ClientTransactionListener listener) 
    {
    	if (!request.isInitial())
    	{
    		return request._session.sendRequest(request, listener);
    	}
    	else 
    	{
    		SipApplicationRouterInfo routerInfo = null;
    		try
    		{
	    		routerInfo = _applicationRouter.getNextApplication(
	    				request,
	    				request.getRegion(),
	    				request.getRoutingDirective(),
	    				null,
	    				request.getStateInfo());
	    		
	    		if (routerInfo != null && routerInfo.getNextApplicationName() != null)
	    		{
	    			SipConnector defaultConnector = _connectorManager.getDefaultConnector();
	    			SipURI internalRoute = new SipURIImpl(null, defaultConnector.getHost(), defaultConnector.getPort());
	    			RouterInfoUtil.encode(internalRoute, routerInfo);

	    			internalRoute.setLrParam(true);
	    			request.pushRoute(internalRoute);
	    		}
    		}
			catch (Throwable t) 
			{
				// TODO, send 500 sync/async ?
				Log.warn(t);
			}
			return request._session.sendRequest(request, listener);
       	}
    }
	
    public void handle(SipServletMessage message) throws IOException, ServletException
    {
		if (isStatsOn())
		{
			synchronized (_statsLock)
			{
				_messages++;
			}
		}
    	
		((SipHandler) getHandler()).handle(message); 
	}
	
	public void setTransactionManager(TransactionManager transactionManager) 
	{
		getContainer().update(this, _transactionManager, transactionManager, "transactionManager", true);
		_transactionManager = transactionManager;
		_transactionManager.setServer(this);
	}
	
	public void setConnectorManager(ConnectorManager connectorManager)
	{
		getContainer().update(this, _connectorManager, connectorManager, "connectorManager", true);
		_connectorManager = connectorManager;
		_connectorManager.setServer(this);
	}
	
	public void setSipThreadPool(ThreadPool sipThreadPool) 
	{
		getContainer().update(this, _sipThreadPool, sipThreadPool, "sipThreadPool", true);
		_sipThreadPool = sipThreadPool;
	}
	
	public void setSessionManager(SessionManager sessionManager) 
	{
		getContainer().update(this, _sessionManager, sessionManager, "sessionManager", true);
		_sessionManager = sessionManager;
		_sessionManager.setServer(this);
	}
	
	public ThreadPool getSipThreadPool()
	{
		return _sipThreadPool;
	}
	
	public ConnectorManager getConnectorManager()
	{
		return _connectorManager;
	}
	
	public TransactionManager getTransactionManager()
	{
		return _transactionManager;
	}
	
	public SessionManager getSessionManager()
	{
		return _sessionManager;
	}
	
	public void statsReset()
	{
		synchronized (_statsLock)
		{
			_statsStartedAt = _statsStartedAt == -1 ? -1 : System.currentTimeMillis();
			_messages = 0;
		}
	}
	
	public void setStatsOn(boolean on) 
	{
        if (on && _statsStartedAt != -1) 
        	return;
        
        Log.info("Statistics set to " + on);
        statsReset();
        _statsStartedAt = on ? System.currentTimeMillis() : -1;
    }
	
	public boolean isStatsOn() 
    {
		return _statsStartedAt != -1;
	}
	
	public void allStatsReset()
	{
		statsReset();
		getSessionManager().statsReset();
		getConnectorManager().statsReset();
		getTransactionManager().statsReset();
	}
	
	public void setAllStatsOn(boolean on) 
	{
		setStatsOn(on);
		if (getSessionManager() != null)
			getSessionManager().setStatsOn(on);
		getConnectorManager().setStatsOn(on);		
		getTransactionManager().setStatsOn(on);
	}
	
	public boolean isAllStatsOn() 
	{
		return isStatsOn()
			&& getSessionManager().isStatsOn()
			&& getConnectorManager().isStatsOn()
			&& getTransactionManager().isStatsOn();
	}
	
	public long getMessages()
	{
		return _messages;
	}

	public static String getSipVersion()
	{
		return __sipVersion;
	}
	
	@Override
	public String toString()
	{
		return "cipango-" + __sipVersion;
	}
}
