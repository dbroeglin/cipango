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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;

import org.cipango.server.ID;
import org.cipango.server.Server;
import org.cipango.server.SipConnectors;
import org.cipango.server.SipMessage;
import org.cipango.server.SipRequest;
import org.cipango.server.SipResponse;
import org.cipango.server.session.scope.ScopedAppSession;
import org.cipango.server.transaction.ClientTransaction;
import org.cipango.server.transaction.ClientTransactionListener;
import org.cipango.server.transaction.ServerTransaction;
import org.cipango.server.transaction.ServerTransactionListener;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sip.NameAddr;
import org.cipango.sip.SipException;
import org.cipango.sip.SipFields;
import org.cipango.sip.SipHeaders;
import org.cipango.sip.SipMethods;
import org.cipango.sip.SipParams;
import org.cipango.sipapp.SipAppContext;
import org.cipango.util.ReadOnlyAddress;
import org.eclipse.jetty.util.log.Log;

public class Session implements SessionIf
{
	protected String _id;
	private AppSession _appSession;
	protected boolean _invalidateWhenReady = true;
	
	protected State _state = State.INITIAL;
	private boolean _valid = true;
	
	protected long _created = System.currentTimeMillis();
	protected long _lastAccessed;
	
	private SipServletHolder _handler;
	protected SipApplicationRoutingRegion _region;
	protected URI _subscriberURI;
	
	protected Map<String, Object> _attributes;
	
	protected String _callId;
	protected NameAddr _localParty;
	protected NameAddr _remoteParty;
	
	private UA _ua;
	private boolean _proxy;
	
	public Session(AppSession appSession, String id)
	{
		_appSession = appSession;
		_id = id;
	}
	
	public Session(AppSession appSession, String id, String callId, NameAddr local, NameAddr remote)
	{
		this(appSession, id);
		
		_callId = callId;
		_localParty = local;
		_remoteParty = remote;
	}
		
	public Session(String id, Session other)
	{
		this(other._appSession, id);
		_invalidateWhenReady = other._invalidateWhenReady;
		_handler = other._handler;
		
		_callId = other._callId;
		_localParty = (NameAddr) other._localParty.clone();
		_remoteParty = (NameAddr) other._remoteParty.clone();
		_remoteParty.setParameter(SipParams.TAG, null);
	}
	
	/**
	 * @see SessionIf#getSession()
	 */
	public Session getSession() 
	{
		return this;
	}

	/**
	 * @see SipSession#createRequest(java.lang.String)
	 */
	public SipServletRequest createRequest(String method) 
	{
		checkValid();
		
		return getUA(true).createRequest(method);
	}

	/**
	 * @see SipSession#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() 
	{
		return new ScopedAppSession(_appSession);
	}

	/**
	 * @see SipSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) 
	{
		checkValid();
		if (name == null)
			throw new NullPointerException("Attribute name is null");
		if (_attributes == null)
			return null;
		return _attributes.get(name);
	}

	/**
	 * @see SipSession#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() 
	{
		checkValid();
		List<String> names;
		if (_attributes == null)
			names = Collections.emptyList();
		else
			names = new ArrayList<String>(_attributes.keySet());
		return Collections.enumeration(names);
	}

	/**
	 * @see SipSession#getCallId()
	 */
	public String getCallId() 
	{
		return _callId;
	}

	/**
	 * @see SipSession#getCreationTime()
	 */
	public long getCreationTime() 
	{
		checkValid();
		return _created;
	}

	/**
	 * @see SipSession#getId()
	 */
	public String getId() 
	{
		return _id;
	}

	/**
	 * @see SipSession#getInvalidateWhenReady()
	 */
	public boolean getInvalidateWhenReady() 
	{
		checkValid();
		return _invalidateWhenReady;
	}

	/**
	 * @see SipSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() 
	{
		return _lastAccessed;
	}

	/**
	 * @see SipSession#getLocalParty()
	 */
	public Address getLocalParty() 
	{
		return new ReadOnlyAddress(_localParty);
	}

	/**
	 * @see SipSession#getRegion()
	 */
	public SipApplicationRoutingRegion getRegion() 
	{
		checkValid();
		return _region;
	}

	/**
	 * @see SipSession#getRemoteParty()
	 */
	public Address getRemoteParty() 
	{
		return new ReadOnlyAddress(_remoteParty);
	}

	/**
	 * @see SipSession#getServletContext()
	 */
	public ServletContext getServletContext() 
	{
		return _appSession.getContext().getServletContext();
	}

	/**
	 * @see SipSession#getState()
	 */
	public State getState() 
	{
		checkValid();
		return _state;
	}

	/**
	 * @see SipSession#getSubscriberURI()
	 */
	public URI getSubscriberURI() 
	{
		checkValid();
		return _subscriberURI;
	}

	/**
	 * @see SipSession#invalidate()
	 */
	public void invalidate() 
	{
		checkValid();
		
		if (Log.isDebugEnabled())
			Log.debug("invalidating SipSession " + this);
		
		_valid = false;
		_appSession.removeSession(this);
	}

	/**
	 * @see SipSession#isReadyToInvalidate()
	 */
	public boolean isReadyToInvalidate() 
	{
		checkValid();
		
		if (_lastAccessed == 0)	
			return false;
		
		if (_state == State.TERMINATED)
			return true;
		else if (isUA() && _state == State.INITIAL)
			return !hasTransactions();
		
		return false;
	}

	/**
	 * @see SipSession#isValid()
	 */
	public boolean isValid() 
	{
		return _valid;
	}

	/**
	 * @see SipSession#removeAttribute(String)
	 */
	public void removeAttribute(String name) 
	{
		checkValid();
		
		if (_attributes == null)
			return;
		
		Object oldValue = _attributes.remove(name);
		if (oldValue != null)
		{
			unbindValue(name, oldValue);
			
			SipSessionAttributeListener[] listeners = _appSession.getContext().getSessionAttributeListeners();
			if (listeners.length > 0)
			{
				SipSessionBindingEvent e = new SipSessionBindingEvent(this, name);
				for (SipSessionAttributeListener listener : listeners)
					listener.attributeRemoved(e);
			}
		}
	}

	/**
	 * @see SipSession#setAttribute(String, Object)
	 */
	public void setAttribute(String name, Object value) 
	{
		checkValid();
		
		if (name == null || value == null)
			throw new NullPointerException("name or value is null");
		
		if (_attributes == null)
			_attributes = newAttributeMap();
		
		Object oldValue = _attributes.put(name, value);
		
		if (oldValue == null || !value.equals(oldValue))
		{
			unbindValue(name, oldValue);
			bindValue(name, value);
			
			SipSessionAttributeListener[] listeners = _appSession.getContext().getSessionAttributeListeners();
			if (listeners.length > 0)
			{
				SipSessionBindingEvent e = new SipSessionBindingEvent(this, name);
				for (SipSessionAttributeListener listener : listeners)
				{
					if (oldValue == null)
						listener.attributeAdded(e);
					else
						listener.attributeReplaced(e);
				}
			}
		}
	}

	/**
	 * @see SipSession#setHandler(String)
	 */
	public void setHandler(String name) throws ServletException 
	{
		checkValid();
		
		SipAppContext context = _appSession.getContext();
		SipServletHolder handler = context.getSipServletHandler().getHolder(name);
		
		if (handler == null)
			throw new ServletException("No handler named " + name);
		
		setHandler(handler);
	}

	/**
	 * @see SipSession#setInvalidateWhenReady(boolean)
	 */
	public void setInvalidateWhenReady(boolean b) 
	{
		checkValid();
		_invalidateWhenReady = b;
	}

	/**
	 * @see SipSession#setOutboundInterface(InetSocketAddress)
	 */
	public void setOutboundInterface(InetSocketAddress address) 
	{
		checkValid();
		if (address == null)
			throw new NullPointerException("Null address");
	}

	/**
	 * @see SipSession#setOutboundInterface(InetAddress)
	 */
	public void setOutboundInterface(InetAddress address) 
	{
		checkValid();
		if (address == null)
			throw new NullPointerException("Null address");
	}
	
	// =====
	
	public void sendResponse(SipResponse response, ServerTransaction tx, boolean reliable) throws IOException
    {
		getUA(true).sendResponse(response, reliable);
    }
	// =====
	
	public void handleRequest(SipRequest request) throws SipException
	{
		accessed();
		
		Proxy proxy = null;
		
		if (request.isInitial())
		{
			if (Log.isDebugEnabled())
				Log.debug("initial request {} for session {}", request.getRequestLine(), this);
			
			_localParty = (NameAddr) request.to().clone();
			_remoteParty = (NameAddr) request.from().clone();
			_callId = request.getCallId();
		}
		else
		{
			if (Log.isDebugEnabled())
				Log.debug("subsequent request {} for session {}", request.getRequestLine(), this);
			
			if (isUA())
			{
				_ua.handleRequest(request);
			}
			else if (isProxy())
			{
				try
				{
					proxy = request.getProxy();
				}
				catch (TooManyHopsException e)
				{
					throw new SipException(SipServletResponse.SC_TOO_MANY_HOPS);
				}
			}
		}
		invokeServlet(request);
		
		if (proxy != null && !request.isCancel())
			proxy.proxyTo(request.getRequestURI());
	}
	
	public ClientTransaction sendRequest(SipRequest request, ClientTransactionListener listener) throws IOException
	{
		accessed();
		
		Server server = getServer();
		server.customizeRequest(request);
		
		request.setCommitted(true);
		if (isUA())
			_ua.sendingRequest(request);
		
		return server.getTransactionManager().sendRequest(request, listener);
	}
	
	public ClientTransaction sendRequest(SipRequest request) throws IOException
	{
		if (!isUA())
			throw new IllegalStateException("Session is not UA");
		
		return sendRequest(request, _ua);
	}
	
	public void invokeServlet(SipRequest request) throws SipException
	{
		try
		{
			_appSession.getContext().handle(request);
		}
		catch (TooManyHopsException e)
		{
			throw new SipException(SipServletResponse.SC_TOO_MANY_HOPS);
		}
		catch (Throwable t)
		{
			throw new SipException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, t);
		}
	}
	
	public void invokeServlet(SipResponse response)
	{
		try
		{
			_appSession.getContext().handle(response);
		}
		catch (Throwable t)
		{
			Log.debug(t);
		}
	}
	
	private void accessed()
	{
		_lastAccessed = System.currentTimeMillis();
		_appSession.access(_lastAccessed);
	}
	
	public void setState(State newState) 
    {
        if (Log.isDebugEnabled())
            Log.debug("{} -> {}", this, newState);
		_state = newState;
	}
	
	public void updateState(SipResponse response, boolean uac)
	{
		SipRequest request = (SipRequest) response.getRequest();
		int status = response.getStatus();
				
		if (request.isInitial() && (request.isInvite() || request.isSubscribe()))
		{
			switch (_state)
			{
			case INITIAL:
				if (status < 300)
				{
					if (_ua != null)
						_ua.createDialog(response, uac);
					if (status < 200)
						setState(State.EARLY);
					else
						setState(State.CONFIRMED);
				}
				else
				{
					if (uac)
					{
						_ua.resetDialog();
						setState(State.INITIAL);
					}
					else
					{
						setState(State.TERMINATED);
					}
				}
				break;
			case EARLY:
				if (200 <= status && status < 300)
					setState(State.CONFIRMED);
				else if (status >= 300)
					setState(State.TERMINATED);
				break;
			}
		}
		else if (request.isBye())
		{
			setState(State.TERMINATED);
		}
	}
	
	public void invalidateIfReady()
	{
		if (isValid() && getInvalidateWhenReady() && isReadyToInvalidate())
		{
			SipAppContext context = _appSession.getContext();
			SipSessionListener[] listeners = context.getSipSessionListeners();
			if (listeners.length > 0)
				context.fire(listeners, AppSession.__sessionReadyToInvalidate, new SipSessionEvent(this));
			
			if (isValid() && getInvalidateWhenReady())
				invalidate();
		}
	}
	
	private void checkValid()
	{
		if (!_valid)
			throw new IllegalStateException("Session has been invalidated");
	}
	
	public boolean isUA()
	{
		return _ua != null;
	}
	
	public UA getUA(boolean create)
	{
		if (_ua == null && create)
		{
			if (isProxy())
				throw new IllegalStateException("session is proxy");
			_ua = new UA();
		}
		return _ua;
	}
	
	public boolean isProxy()
	{
		return _proxy;
	}
	
	public void setProxy()
	{
		if (_ua != null)
			throw new IllegalStateException("session is UA");
		_proxy = true;
	}
	
	public boolean isDialog(String fromTag, String toTag)
	{
		String localTag = _localParty.getParameter(SipParams.TAG);
		String remoteTag = _remoteParty.getParameter(SipParams.TAG);
		
		if (fromTag.equals(localTag) && toTag.equals(remoteTag))
			return true;
		if (toTag.equals(localTag) && fromTag.equals(remoteTag))
			return true;
		return false;
	}
		
	public SipServletHolder getHandler()
	{
		return _handler;
	}
	
	public void setHandler(SipServletHolder handler)
	{
		_handler = handler;
	}
	
	public void setSubscriberURI(URI uri)
	{
		_subscriberURI = uri;
	}
	
	public void setRegion(SipApplicationRoutingRegion region)
	{
		_region = region;
	}
	
	protected HashMap<String, Object> newAttributeMap()
	{
		return new HashMap<String, Object>(3);
	}
	
	private void bindValue(String name, Object value)
	{
		if (value != null && value instanceof SipSessionBindingListener)
			((SipSessionBindingListener) value).valueBound(new SipSessionBindingEvent(this, name));
	}
	
	private void unbindValue(String name, Object value)
	{
		if (value != null && value instanceof SipSessionBindingListener)
			((SipSessionBindingListener) value).valueUnbound(new SipSessionBindingEvent(this, name));
	}
	
	public AppSession appSession()
	{
		return _appSession;
	}
	
	public CallSession getCallSession()
	{
		return _appSession.getCallSession();
	}
	
	public Server getServer()
	{
		return _appSession.getCallSession().getServer();
	}
	
	private boolean hasTransactions()
	{
		return true; // TODO
	}
	
	public Address getContact()
	{
		Address address = getServer().getConnectorManager().getContact(SipConnectors.TCP_ORDINAL);
		address.getURI().setParameter(ID.APP_SESSION_ID_PARAMETER, _appSession.getAppId());
		return address;
	}
	
	@Override
	public Session clone()
	{
		return this; // TODO
	}
	
	public String toString()
	{
		return "[" + _id + ",state=" + _state + ",ua=" + (_ua != null) + ",proxy=" + _proxy + "]";
	}
	
	public void setLinkedSession(Session session) { }
	public Session getLinkedSession() { return null; }
	public List<SipServletResponse> getUncommitted200(UAMode mode) { return null; }
	
	public class UA implements ClientTransactionListener, ServerTransactionListener
	{
		private long _localCSeq = 1;
		private long _remoteCSeq = -1;
		private URI _remoteTarget;
		private LinkedList<String> _routeSet;
		private boolean _secure = false;
		
		public UA()
		{
			
		}
		
		public SipRequest createRequest(SipRequest srcRequest, boolean sameCallId)
		{
			SipRequest request = (SipRequest) srcRequest.clone();
			_localParty = (NameAddr) srcRequest.from().clone();
			_localParty.setParameter(SipParams.TAG, ID.newTag());
			
			_remoteParty = (NameAddr) srcRequest.to().clone();
			_remoteParty.removeParameter(SipParams.TAG);
			
            if (sameCallId)
                _callId = srcRequest.getCallId();
            else 
            	_callId = ID.newCallId(srcRequest.getCallId());
            
			SipFields fields = request.getFields();
			fields.setAddress(SipHeaders.FROM_BUFFER, _localParty);
			fields.setAddress(SipHeaders.TO_BUFFER, _remoteParty);
			fields.remove(SipHeaders.RECORD_ROUTE_BUFFER);
			fields.remove(SipHeaders.VIA_BUFFER);
			
			if (request.isRegister())
				fields.remove(SipHeaders.CONTACT_BUFFER);
			
			fields.setString(SipHeaders.CALL_ID_BUFFER, _callId);
			
			fields.setString(SipHeaders.CSEQ_BUFFER, _localCSeq++ + " " + request.getMethod());
			
			if (request.needsContact())
				fields.setAddress(SipHeaders.CONTACT_BUFFER, getContact());
			
			request.setInitial(true);
			request.setSession(Session.this);
			
			return request;
		}
		
		public SipServletRequest createRequest(String method)
		{
			if (method.equalsIgnoreCase(SipMethods.ACK) || method.equalsIgnoreCase(SipMethods.CANCEL))
				throw new IllegalArgumentException("Forbidden request method " + method);
		
			if (_state == State.TERMINATED)
				throw new IllegalStateException("Cannot create request in TERMINATED state");
			
			return createRequest(method, _localCSeq++);
		}
		
		public SipServletRequest createAck()
		{
			return createRequest(SipMethods.ACK, _localCSeq);
		}
		
		public SipServletRequest createRequest(String method, long cseq)
		{
			SipRequest request = new SipRequest();
			request.setSession(Session.this);
			request.setMethod(method.toUpperCase());
			
			request.getFields().setAddress(SipHeaders.FROM_BUFFER, (NameAddr) _localParty.clone()); 
			request.getFields().setAddress(SipHeaders.TO_BUFFER, (NameAddr) _remoteParty.clone());
			
			if (_remoteTarget != null)
				request.setRequestURI((URI) _remoteTarget.clone());
			else
				request.setRequestURI(request.to().getURI());
			
			if (_routeSet != null)
			{
				for (String route: _routeSet)
				{
					request.getFields().addString(SipHeaders.ROUTE_BUFFER, route);
				}
			}
			request.getFields().setString(SipHeaders.CALL_ID_BUFFER, _callId);
			request.getFields().setString(SipHeaders.CSEQ_BUFFER, cseq + " " + method);
			request.getFields().setString(SipHeaders.MAX_FORWARDS_BUFFER, "70");
			
			if (request.needsContact())
				request.getFields().setAddress(SipHeaders.CONTACT_BUFFER, getContact());
			
			return request;
		}
		
		public void handleRequest(SipRequest request) throws SipException
		{
			if (request.getCSeq().getNumber() <= _remoteCSeq && !request.isAck() && !request.isCancel())
				throw new SipException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Out of order request");
			
			_remoteCSeq = request.getCSeq().getNumber();
			if (request.isInvite())
				setRemoteTarget(request);
			
			// TODO ACK / PRACK
		}
		
		public void handleCancel(ServerTransaction transaction, SipRequest cancel) throws IOException 
		{
			cancel.setSession(Session.this);
			if (transaction.isCompleted())
			{
				Log.debug("ignoring late cancel {}", transaction);
			}
			else
			{
				try
				{
					transaction.getRequest().createResponse(SipServletResponse.SC_REQUEST_TERMINATED).send();
					setState(State.TERMINATED);
				}
				catch (Exception e)
				{
					Log.debug("failed to cancel request", e);
				}
			}
			invokeServlet(cancel);
		}
		
		public void handleResponse(SipResponse response)
		{
			String remoteTag = _remoteParty.getParameter(SipParams.TAG);
			if (remoteTag != null)
			{
				String responseTag = response.to().getParameter(SipParams.TAG);
				if (responseTag != null && !remoteTag.equals(responseTag))
				{
					Session derived = _appSession.getSession(response);
					if (derived == null)
						derived = _appSession.createDerivedSession(Session.this);
					derived.getUA(true).handleResponse(response);
					return;
				}
			}
			
			response.setSession(Session.this); 
			
			accessed();
			
			updateState(response, true);
			
			if (response.getStatus() < 300 && (response.isInvite() || response.isSubscribe()))
				setRemoteTarget(response);
			
			if (response.isInvite())
			{
				// TODO ack
			}
			
			if (isValid())
				invokeServlet(response);
		}
		
		public void sendResponse(SipResponse response, boolean reliable)
		{
			ServerTransaction tx = (ServerTransaction) response.getTransaction();
			
			if (tx.isCompleted())
				throw new IllegalStateException("transaction terminated for response" + response.getRequestLine());
			
			tx.setListener(this);
            
			updateState(response, false);
			
			SipRequest request = (SipRequest) response.getRequest();

			if (request.isInitial() && (response.to().getParameter(SipParams.TAG) == null))
			{
				String tag = _localParty.getParameter(SipParams.TAG);
				if (tag == null)
					tag = ID.newTag();
				response.to().setParameter(SipParams.TAG, tag);
			}
			
			if (request.isInvite() || request.isSubscribe())
				setRemoteTarget(request);
			
			if (request.isInvite())
			{
				// TODO reliable && retrans
			}
			tx.send(response);
		}
		
		public void sendingRequest(SipRequest request)
		{
			
		}
		
		protected void resetDialog()
		{
			_remoteTarget = _remoteParty.getURI();
			_remoteParty.setParameter(SipParams.TAG, null);
			_remoteCSeq = -1;
			_routeSet = null;
			_secure = false;
		}
		
		protected void createDialog(SipResponse response, boolean uac)
		{
			if (uac)
			{
				
				String tag = response.to().getParameter(SipParams.TAG);
                _remoteParty.setParameter(SipParams.TAG, tag);
                
                System.out.println("Created dialog: " + tag);
                setRoute(response, true);
			}
			else
			{
				String tag = ID.newTag();
				_localParty.setParameter(SipParams.TAG, tag);
				
				/*String rtag = response.to().getParameter(SipParams.TAG);
                if (rtag == null) 
                {
                    String tag = _localParty.getParameter(SipParams.TAG);
                    if (tag == null) 
                    {
                        tag = ID.newTag();
                        _localParty.setParameter(SipParams.TAG, tag);
                    }
                    response.to().setParameter(SipParams.TAG, tag);
                }*/
                
                SipRequest request = (SipRequest) response.getRequest();
    			
				_remoteCSeq = request.getCSeq().getNumber();
				_secure = request.isSecure() && request.getRequestURI().getScheme().equals("sips");
				
				setRoute(request, false);
			}
		}
		
		protected void setRemoteTarget(SipMessage message) 
		{
			_remoteTarget = message.getFields().getAddress(SipHeaders.CONTACT_BUFFER).getURI();
		}
		
		protected void setRoute(SipMessage message, boolean reverse)
		{
			ListIterator<String> routes = message.getFields().getValues(SipHeaders.RECORD_ROUTE_BUFFER);
			_routeSet = new LinkedList<String>();
			while (routes.hasNext())
			{
				if (reverse)
					_routeSet.addFirst(routes.next());
				else
					_routeSet.addLast(routes.next());
			}
		}
		
		public boolean isSecure()
		{
			return _secure;
		}
	}
}