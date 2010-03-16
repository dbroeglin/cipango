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

package org.cipango.sipapp;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.sip.*;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;

import org.cipango.CallSession;
import org.cipango.NameAddr;
import org.cipango.ParameterableImpl;
import org.cipango.Server;
import org.cipango.SipFields;
import org.cipango.SipHandler;
import org.cipango.SipHeaders;
import org.cipango.SipMethods;
import org.cipango.SipParams;
import org.cipango.SipRequest;
import org.cipango.SipURIImpl;
import org.cipango.URIFactory;
import org.cipango.SessionManager.SessionTransaction;
import org.cipango.http.servlet.ConvergedSessionManager;
import org.cipango.log.EventLog;
import org.cipango.security.AuthInfoImpl;
import org.cipango.servlet.AppSession;
import org.cipango.servlet.AppSessionIf;
import org.cipango.servlet.Session;
import org.cipango.servlet.SipDispatcher;
import org.cipango.servlet.SipServletHandler;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sip.SipConnector;
import org.cipango.util.ID;
import org.cipango.util.ReadOnlySipURI;
import org.cipango.util.concurrent.AppSessionLockProxy;
import org.cipango.util.concurrent.TimerLockProxy;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ErrorPageErrorHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

public class SipAppContext extends WebAppContext implements SipHandler
{
	public static final int VERSION_10 = 10;
	public static final int VERSION_11 = 11;
	
	private static final String SIP_CONFIGURATION_CLASS =
    	"org.cipango.sipapp.SipXmlConfiguration";
    
    public final static String SIP_DEFAULTS_XML="org/cipango/sipapp/sipdefault.xml";
        
    public final static String[] EXTENSIONS = { "100rel" };
    
	public static final String[] SUPPORTED_RFC = new String[] {
		"2976", // The SIP INFO Method
		"3261", // SIP: Session Initiation Protocol
		"3262", // Reliability of Provisional Responses
		"3265", // (SIP)-Specific Event Notification. 
		"3311", // (SIP) UPDATE Method
		"3428"  // SIP Extension for Instant Messaging  
	};
    
    /*
    public final CLFireEvent<SipErrorListener, SipErrorEvent> _noAck = new CLFireEvent<SipErrorListener, SipErrorEvent>()
    {
    	public SipErrorListener[] getEventListeners() { return _errorListeners; }
    	public void fireEvent(SipErrorListener listener, SipErrorEvent event) { listener.noAckReceived(event); }
    };
    
    public final CLFireEvent<SipErrorListener, SipErrorEvent> _noPrack = new CLFireEvent<SipErrorListener, SipErrorEvent>()
    {
    	public SipErrorListener[] getEventListeners() { return _errorListeners; }
    	public void fireEvent(SipErrorListener listener, SipErrorEvent event) { listener.noPrackReceived(event); }
    };
    */

    private String _name;
    
    private TimerListener[] _timerListeners;
    private SipApplicationSessionListener[] _appSessionListeners;
    private SipErrorListener[] _errorListeners;
    private SipApplicationSessionAttributeListener[] _appSessionAttributeListeners;
    private SipSessionListener[] _sessionListeners;
    private SipSessionAttributeListener[] _sessionAttributeListeners;
    private SipServletListener[] _servletListeners;

    private int _sessionTimeout = -1;
    private int _proxyTimeout = -1;
    
    private long _nbSessions = 0;
    private Object _statsLock = new Object();
       
    private String _defaultsSipDescriptor=SIP_DEFAULTS_XML;
    private String _overrideSipDescriptor=null;
    
    private SipFactory _sipFactory = new Factory();
    private TimerService _timerService = new Timer();
    private SipSessionsUtil _sipSessionsUtil = new SessionUtil();
    private Method _sipApplicationKeyMethod;
    
    private int _specVersion;

	public SipAppContext() 
	{
		super(null,
				new SessionHandler(new ConvergedSessionManager()),
				new SipServletHandler(),
				new ErrorPageErrorHandler());
		setConfigurationClasses((String[]) LazyList.addToArray(
				getConfigurationClasses(),
				SIP_CONFIGURATION_CLASS,
				String.class));
        _scontext = new SContext();
        setSystemClasses((String[]) LazyList.addToArray(getSystemClasses(), "org.cipango.", String.class));
	}
	
	
	public SipAppContext(String sipApp, String contextPath) 
	{
		this();
		setWar(sipApp);
		setContextPath(contextPath);
	}
	
    public SipAppContext(SecurityHandler securityHandler,SessionHandler sessionHandler, ServletHandler servletHandler, ErrorHandler errorHandler) 
    {
    	super(securityHandler, sessionHandler, servletHandler, errorHandler);
    	_scontext = new SContext();
    	setSystemClasses((String[]) LazyList.addToArray(getSystemClasses(), "org.cipango.", String.class));
    	// FIXME do more???
    }

	public void addSipServlet(SipServletHolder servlet)
	{
		((SipServletHandler) _servletHandler).addSipServlet(servlet);
	}
	
	public void handle(SipServletMessage message) throws ServletException, IOException 
	{
		ClassLoader oldClassLoader = null;
		Thread currentThread = null;
		
		if (getClassLoader() != null)
		{
			currentThread = Thread.currentThread();
			oldClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(getClassLoader());
		}
		try
		{
			getSipServletHandler().handle(message);
		}
		finally
		{
			if (getClassLoader() != null)
			{
				currentThread.setContextClassLoader(oldClassLoader);
			}
		}
	}
	
	public void initialized()
	{
		ClassLoader oldClassLoader = null;
		Thread currentThread = null;
		
		if (getClassLoader() != null)
		{
			currentThread = Thread.currentThread();
			oldClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(getClassLoader());
		}
		try
		{
			SipServletHolder[] holders = getSipServletHandler().getSipServlets();
			for (SipServletHolder holder : holders)
			{
				if (holder.servlet() != null && holder.servlet() instanceof SipServlet)
				{
					fireServletInitialized((SipServlet) holder.servlet());
				}
			}
		}
		finally
		{
			if (getClassLoader() != null)
			{
				currentThread.setContextClassLoader(oldClassLoader);
			}
		}
	}
	
	public void fireServletInitialized(SipServlet servlet)
	{
		for (int i = 0; i < _servletListeners.length; i++)
		{
			try
			{
				_servletListeners[i].servletInitialized(new SipServletContextEvent(servlet.getServletContext(), servlet));
			}
			catch (Throwable t)
			{
				Log.debug(t);
			}
		}
	}
    
    public SipApplicationSessionListener[] getSipApplicationSessionListeners()
    {
        return _appSessionListeners;
    }
    
    public TimerListener[] getTimerListeners()
    {
        return _timerListeners;
    }
    
    public SipErrorListener[] getSipErrorListeners()
    {
        return _errorListeners;
    }
    
    public SipApplicationSessionAttributeListener[] getSipApplicationSessionAttributeListeners()
    {
    	return _appSessionAttributeListeners;
    }
    
    public SipSessionListener[] getSipSessionListeners()
    {
    	return _sessionListeners;
    }
    
    public SipSessionAttributeListener[] getSessionAttributeListeners()
    {
    	return _sessionAttributeListeners;
    }
    
    public void setProxyTimeout(int timeout)
    {
        _proxyTimeout = timeout;
    }
    
    public int getProxyTimeout() 
    {
        return _proxyTimeout;
    }
    
    public void setEventListeners(EventListener[] eventListeners)
    {
        super.setEventListeners(eventListeners);
        
        Object timerListeners = null;
        Object appSessionListeners = null;
        Object errorListeners = null;
        Object appSessionAttributeListeners = null;
        Object sessionListeners = null;
        Object sessionAttributesListeners = null;
        Object servletListeners = null;
        
        for (int i = 0; eventListeners != null && i < eventListeners.length; i++)
        {
            EventListener listener = eventListeners[i];
            if (listener instanceof TimerListener)
                timerListeners = LazyList.add(timerListeners, listener);
            if (listener instanceof SipApplicationSessionListener)
                appSessionListeners = LazyList.add(appSessionListeners, listener);
            if (listener instanceof SipErrorListener)
                errorListeners = LazyList.add(errorListeners, listener);
            if (listener instanceof SipApplicationSessionAttributeListener)
            	appSessionAttributeListeners = LazyList.add(appSessionAttributeListeners, listener);
            if (listener instanceof SipSessionListener)
            	sessionListeners = LazyList.add(sessionListeners, listener);
            if (listener instanceof SipSessionAttributeListener)
            	sessionAttributesListeners = LazyList.add(sessionAttributesListeners, listener);
            if (listener instanceof SipServletListener)
            	servletListeners = LazyList.add(servletListeners, listener);
        }
        _timerListeners = (TimerListener[]) 
            LazyList.toArray(timerListeners, TimerListener.class);
        _appSessionListeners = (SipApplicationSessionListener[]) 
            LazyList.toArray(appSessionListeners, SipApplicationSessionListener.class);
        _errorListeners = (SipErrorListener[])
            LazyList.toArray(errorListeners, SipErrorListener.class);
        _appSessionAttributeListeners = (SipApplicationSessionAttributeListener[])
        	LazyList.toArray(appSessionAttributeListeners, SipApplicationSessionAttributeListener.class);
        _sessionListeners = (SipSessionListener[])
        	LazyList.toArray(sessionListeners, SipSessionListener.class);
        _sessionAttributeListeners = (SipSessionAttributeListener[])
        	LazyList.toArray(sessionAttributesListeners, SipSessionAttributeListener.class);
        _servletListeners = (SipServletListener[])
        	LazyList.toArray(servletListeners, SipServletListener.class);
    }
    
    public void fire(EventListener[] listeners, Method method, Object... args)
    {
		ClassLoader oldClassLoader = null;
		Thread currentThread = null;
		
		if (getClassLoader() != null)
		{
			currentThread = Thread.currentThread();
			oldClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(getClassLoader());
		}

		for (int i = 0; i < listeners.length; i++)
		{
			try
			{
				method.invoke(listeners[i], args);
			}
			catch (Throwable t)
			{
				Log.debug(t);
			}
		}
		if (getClassLoader() != null)
		{
			currentThread.setContextClassLoader(oldClassLoader);
		}
    }
    
    @SuppressWarnings("deprecation")
	@Override
	protected void startContext() throws Exception 
	{
	    setAttribute(SipServlet.PRACK_SUPPORTED, Boolean.TRUE);
		setAttribute(SipServlet.SIP_FACTORY, getSipFactory());
		setAttribute(SipServlet.TIMER_SERVICE, getTimerService());
		setAttribute(SipServlet.SIP_SESSIONS_UTIL, getSipSessionsUtil());
		
		List<SipURI> outbounds = new ArrayList<SipURI>();

		SipConnector[] connectors = getSipServer().getConnectorManager().getConnectors();
		
		if (connectors != null)
		{
			for (SipConnector connector : connectors) 
			{
				SipURI uri = connector.getSipUri();
				if (!outbounds.contains(uri))
					outbounds.add(new ReadOnlySipURI(uri));
			}
		}
		setAttribute(SipServlet.OUTBOUND_INTERFACES, Collections.unmodifiableList(outbounds));
		setAttribute(SipServlet.SUPPORTED, Collections.unmodifiableList(Arrays.asList(EXTENSIONS)));
		setAttribute(SipServlet.SUPPORTED_RFCs, Collections.unmodifiableList(Arrays.asList(SUPPORTED_RFC)));
		
		try 
		{
			super.startContext();
	              
			if (_name == null)
			{
				_name = getContextPath();
				if (_name != null && _name.startsWith("/"))
					_name = _name.substring(1);	
			}
			
			if (_servletHandler != null && _servletHandler.isStarted())
	            ((SipServletHandler) _servletHandler).initializeSip();
		} 
		catch (Exception e) 
		{
			EventLog.log(EventLog.DEPLOY_FAIL, 
        			"Unable to deploy application " + getName()
        			+ ": " + e.getMessage());
			throw e;
		}
    }
    
    @Override
    protected void doStart() throws Exception
    {
    	super.doStart();
    	if (hasSipServlets())
    		getSipServer().applicationDeployed(this);
    }
    
	@Override
	protected void doStop() throws Exception
	{
		if (hasSipServlets())
			getSipServer().applicationUndeployed(this);
		super.doStop();
	}
	
    public SipServletHandler getSipServletHandler()
    {
        return (SipServletHandler) getServletHandler();
    }
    
    public boolean hasSipServlets()
    {
    	SipServletHolder[] holders = getSipServletHandler().getSipServlets();
    	return holders != null && holders.length != 0;
    }
    
	public void updateNbSessions(boolean increment)
	{
		synchronized (_statsLock)
		{
			if (increment)
				_nbSessions++;
			else
				_nbSessions--;
		}
	}
	
	public long getNbSessions()
	{
		return _nbSessions;
	}
	
	public void setDefaultsSipDescriptor(String defaultsDescriptor)
	{
		_defaultsSipDescriptor = defaultsDescriptor;
	}
	
	public String getDefaultsSipDescriptor()
	{
		return _defaultsSipDescriptor;
	}
	 
	/**
     * The override descriptor is a sip.xml format file that is applied to the context after the standard WEB-INF/sip.xml
     */
    public void setOverrideSipDescriptor(String overrideSipDescriptor)
    {
        _overrideSipDescriptor = overrideSipDescriptor;
    }
    
    /**
     * The override descriptor is a sip.xml format file that is applied to the context after the standard WEB-INF/sip.xml
     */
    public String getOverrideSipDescriptor()
    {
        return _overrideSipDescriptor;
    }
    
	public void setSessionTimeout(int minutes) 
    {
		if (minutes <= 0)
			_sessionTimeout = -1;
		else 
			_sessionTimeout = minutes;
	}
	
	public int getSessionTimeout() 
    {
		return _sessionTimeout;
	}
	
	public void setName(String name)
	{
		_name = name; 
	}
    
    public String getName() 
    {
    	return _name;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isServerClass(String name)
    {
        name=name.replace('/','.');
        while(name.startsWith("."))
            name=name.substring(1);

        String[] server_classes = getServerClasses();
        if (server_classes!=null)
        {
            for (int i=0;i<server_classes.length;i++)
            {
                boolean result=true;
                String c=server_classes[i];
                if (c.startsWith("-"))
                {
                    c=c.substring(1); // TODO cache
                    result=false;
                }
                
                if (c.endsWith("."))
                {
                    if (name.startsWith(c))
                        return result;
                }
                else if (name.equals(c))
                    return result;
            }
        }
        return false;
    }

    /* ------------------------------------------------------------ */
    public boolean isSystemClass(String name)
    {
        name=name.replace('/','.');
        while(name.startsWith("."))
            name=name.substring(1);
        String[] system_classes = getSystemClasses();
        if (system_classes!=null)
        {
            for (int i=0;i<system_classes.length;i++)
            {
                boolean result=true;
                String c=system_classes[i];
                
                if (c.startsWith("-"))
                {
                    c=c.substring(1); // TODO cache
                    result=false;
                }
                
                if (c.endsWith("."))
                {
                    if (name.startsWith(c))
                        return result;
                }
                else if (name.equals(c))
                    return result;
            }
        }
        
        return false;
        
    }
    
	public SipFactory getSipFactory()
	{
		return _sipFactory;
	}


	public TimerService getTimerService()
	{
		return _timerService;
	}

	public SipSessionsUtil getSipSessionsUtil()
	{
		return _sipSessionsUtil;
	}
	
	public String getSipApplicationKey(SipServletRequest request)
	{
		if (_sipApplicationKeyMethod == null)
			return null;
		try
		{
			return (String) _sipApplicationKeyMethod.invoke(null, request);
		}
		catch (Throwable e)
		{
			Log.debug("Fail to get SipApplicationKey", e);
			return null;
		}
	}
	
	public Method getSipApplicationKeyMethod()
	{
		return _sipApplicationKeyMethod;
	}
	
	public void setSipApplicationKeyMethod(Method sipApplicationKeyMethod)
	{
		_sipApplicationKeyMethod = sipApplicationKeyMethod;
	}
	
    public int getSpecVersion()
	{
		return _specVersion;
	}

	public void setSpecVersion(int specVersion)
	{
		_specVersion = specVersion;
	}
	
	public Server getSipServer()
	{
		return (Server) super.getServer();
	}
    	
    public class Timer implements TimerService
    {
        public ServletTimer createTimer(SipApplicationSession session, long delay, boolean isPersistent, Serializable info) 
        {
            return new TimerLockProxy(((AppSessionIf) session).getAppSession(), delay, isPersistent, info);
        }

        public ServletTimer createTimer(SipApplicationSession session, long delay, long period, boolean fixedDelay, boolean isPersistent, Serializable info) 
        {
        	return new TimerLockProxy(((AppSessionIf) session).getAppSession(), delay, period, fixedDelay, isPersistent, info);
        }
    }

    public class Factory implements SipFactory
    {
        private Factory() { }
       
        public URI createURI(String uri) throws ServletParseException 
        {
            return URIFactory.parseURI(uri);
        }

        public SipURI createSipURI(String user, String host) 
        {
            return new SipURIImpl(user, host, -1);
        }

        public Address createAddress(String address) throws ServletParseException 
        {
            return new NameAddr(address);
        }

        public Address createAddress(URI uri) 
        {
            return new NameAddr(uri);
        }

        public Address createAddress(URI uri, String displayName) 
        {
            return new NameAddr(uri, displayName);
        }

        public SipServletRequest createRequest(SipApplicationSession sipAppSession,
                String method, Address from, Address to) 
        {
            if (SipMethods.ACK.equalsIgnoreCase(method) || SipMethods.CANCEL.equalsIgnoreCase(method)) 
                throw new IllegalArgumentException("Method cannot be ACK nor CANCEL");
            
            NameAddr local = (NameAddr) from.clone();
            NameAddr remote = (NameAddr) to.clone();
            
            local.setParameter(SipParams.TAG, ID.newTag());
            remote.removeParameter(SipParams.TAG);
            
            AppSession appSession = ((AppSessionIf) sipAppSession).getAppSession();           
            
            String cid = ID.newCallId(appSession.getCallSession().getId());
            
            Session session = appSession.createUacSession(cid, local, remote); 
            session.setHandler(getSipServletHandler().getDefaultServlet());
            
            SipRequest request = (SipRequest) session.createRequest(method);
            request.setInitial(true);
            request.setRoutingDirective(SipApplicationRoutingDirective.NEW, null);
           
            return request;
        }

        public SipServletRequest createRequest(SipApplicationSession appSession, 
                String method, URI from, URI to) 
        {
            return createRequest(appSession, method, createAddress(from), createAddress(to));
        }

        public SipServletRequest createRequest(SipApplicationSession appSession,
                String method, String from, String to) throws ServletParseException 
        {
            return createRequest(appSession, method, createAddress(from), createAddress(to));
        }
        
        public SipServletRequest createRequest(SipServletRequest srcRequest, boolean sameCallId) 
        {
            AppSession appsession = ((SipRequest) srcRequest).appSession(); 
            SipRequest request = (SipRequest) ((SipRequest) srcRequest).clone();
            
            NameAddr from = (NameAddr) srcRequest.getFrom().clone();
            NameAddr to = (NameAddr) srcRequest.getTo().clone();
            
            from.setParameter(SipParams.TAG, ID.newTag());
            to.removeParameter(SipParams.TAG);
            
            SipFields fields = request.getFields();
            
            fields.setAddress(SipHeaders.FROM, from);
            fields.setAddress(SipHeaders.TO, to);
            fields.remove(SipHeaders.RECORD_ROUTE_BUFFER);
            fields.remove(SipHeaders.VIA);
            
            if (!request.isRegister())
                fields.remove(SipHeaders.CONTACT_BUFFER);
            
            String callId = null;
            
            if (sameCallId)
                callId = request.getCallId();
            else 
                callId = ID.newCallId(request.getCallId());
            
            fields.setString(SipHeaders.CALL_ID, callId);
            Session session = appsession.createUacSession(callId, from, to);
          
            session.setLocalCSeq(request.getCSeq().getNumber() + 1);
            session.setHandler(getSipServletHandler().getDefaultServlet());
            
            request.setSession(session);
            
            if (request.needsContact())
                fields.setAddress(SipHeaders.CONTACT, (NameAddr) session.getContact());
            
            request.setInitial(true);
            request.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, srcRequest);
            
            return request;
        }
        
        public SipApplicationSession createApplicationSession()
        {
        	Server server = getSipServer();
        	
        	SessionTransaction transaction = server.getSessionManager().begin(ID.newCallId());
	        try
	        {
	        	AppSession session = transaction.getCallSession().createAppSession(SipAppContext.this, ID.newAppSessionId());
	        	return new AppSessionLockProxy(session);
	        }
	        finally
	        {
	        	transaction.done();
	        }
        }

		public SipApplicationSession createApplicationSessionByKey(String key)
		{
			return getSipSessionsUtil().getApplicationSessionByKey(key, true);
		}

		public AuthInfo createAuthInfo()
		{
			return new AuthInfoImpl();
		}

		public Parameterable createParameterable(String s) throws ServletParseException
		{
			return new ParameterableImpl(s);
		} 
    }

    class SessionUtil implements SipSessionsUtil
    {
		public SipApplicationSession getApplicationSessionById(String applicationSessionId)
		{
			if (applicationSessionId == null)
				throw new NullPointerException("applicationSessionId is null");
			
			int i = applicationSessionId.indexOf(';'); // TODO id helper class
			if (i < 0) 
				return null;
			
			String callId = applicationSessionId.substring(0, i);
			
			CallSession call = ((Server) getServer()).getSessionManager().get(callId);
			if (call == null)
				return null;
			
			AppSession appSession = call.getAppSession(applicationSessionId.substring(i+1));
			if (appSession == null)
				return null;
			else
				return new AppSessionLockProxy(appSession);
		}

		public SipApplicationSession getApplicationSessionByKey(String key, boolean create)
		{
			if (key == null)
				throw new NullPointerException("key is null");
			
			String cid = getName() + ";" + key;
			
			
			return null;
		}

		public SipSession getCorrespondingSipSession(SipSession session, String headerName)
		{
			return null;
		}
    }
    
    class SContext extends Context.SContext
    {
		public RequestDispatcher getNamedDispatcher(String name)
        {
            if (_servletHandler != null)
            {
                SipServletHolder holder = ((SipServletHandler) _servletHandler).getHolder(name);
                if (holder != null)
                	return new SipDispatcher(holder);
            }
            return super.getNamedDispatcher(name);
        }
        
        public String getServerInfo()
        {
            return "cipango-2.0";
        }
    }
    
    abstract class CLFireEvent<L extends EventListener, E>
    {
    	public abstract void fireEvent(L listener, E event);
    	public abstract L[] getEventListeners();
    	
    	public boolean hasListeners()
    	{
    		L listeners[] = getEventListeners();
    		return listeners != null && listeners.length > 0;
    	}
    	
    	public void fire(E event) 
    	{
    		L listeners[] = getEventListeners();
    		
    		if (listeners != null && listeners.length > 0)
    		{
	    		ClassLoader oldClassLoader = null;
	    		Thread currentThread = null;
	    		
	    		if (getClassLoader() != null)
	    		{
	    			currentThread = Thread.currentThread();
	    			oldClassLoader = currentThread.getContextClassLoader();
	    			currentThread.setContextClassLoader(getClassLoader());
	    		}
	    		try
	    		{
	    			for (int i = 0; i < listeners.length; i++)
	    			{
	    				fireEvent(listeners[i], event);
	    			}
	    		}
	    		finally
	    		{
	    			if (getClassLoader() != null)
	    			{
	    				currentThread.setContextClassLoader(oldClassLoader);
	    			}
	    		}
	    	}
    	}
    }
}
