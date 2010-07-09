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

package org.cipango.server.transaction;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.cipango.SipRequest;
import org.cipango.SipResponse;
import org.cipango.server.SipConnection;
import org.cipango.server.SipConnectors;
import org.cipango.server.SipMethods;
import org.cipango.sip.SipVersions;
import org.cipango.sip.Via;
import org.cipango.util.ID;

import org.eclipse.jetty.util.log.Log;

public class ClientTransaction extends Transaction 
{
	private long _aDelay = __T1;
    private long _eDelay = __T1;
    
    private ClientTransactionListener _listener;
    private SipRequest _pendingCancel;
    
    private boolean _canceled = false;
    
	public ClientTransaction(SipRequest request, ClientTransactionListener listener)
    {
		this(request, listener, ID.newBranch());
	}
	
	public ClientTransaction(SipRequest request, ClientTransactionListener listener, String branch) 
    {
		super(request, branch);
        _listener = listener;
	}
	
	private void ack(SipResponse response) 
    {
		SipRequest ack = getRequest().createRequest(SipMethods.ACK);
		
		if (ack.to().getParameter("tag") == null) 
        {
			String tag = response.to().getParameter("tag");
			if (tag != null) 
				ack.to().setParameter("tag", tag);
		}
		try 
        {
			getServer().getConnectorManager().send(ack, getConnection());
		} 
        catch (IOException e) 
        {
			Log.ignore(e);
		}
	}
	
	public void cancel(SipRequest cancel)
	{
		if (_canceled) 
			return;
		
		_canceled = true;
		
		if (_state <= STATE_TRYING)
		{
			_pendingCancel = cancel;
			return;
		}
		doCancel(cancel);
	}
	
	public void cancel()
    {   
    	cancel((SipRequest) getRequest().createCancel());
    }
	
	public boolean isCanceled()
	{
		return _canceled;
	}
	
	private ClientTransaction doCancel(SipRequest cancel)
	{
		ClientTransaction cancelTx = new ClientTransaction(cancel, _listener, cancel.getTopVia().getBranch());
		cancelTx.setConnection(getConnection());
		
		cancel.getCallSession().addClientTransaction(cancelTx);
		
		try 
        {
			cancelTx.start();
		} 
        catch (IOException e) 
        {
			Log.warn(e);
		}
        return cancelTx;
	}

	private void doSend() throws IOException 
    {
		if (getConnection() != null)
		{
			getServer().getConnectorManager().send(_request, getConnection()); 
		}
		else 
		{
			// TODO check Maxforwards
			URI uri = null;
			
			Address route = _request.getTopRoute();
			
			if (route != null && !_request.isNextHopStrictRouting())
				uri = route.getURI();
			else
				uri = _request.getRequestURI();
			
			if (!uri.isSipURI()) 
				throw new IOException("Cannot route on URI: " + uri);
			
			SipURI target = (SipURI) uri;
			
			InetAddress address = InetAddress.getByName(target.getHost()); // TODO 3263
			int transport = SipConnectors.getOrdinal(target.getTransportParam()); // TODO opt
			
			if (transport == -1) 
				transport = SipConnectors.UDP_ORDINAL;
			
			int port = target.getPort();
			if (port == -1) 
				port = SipConnectors.getDefaultPort(transport);
		

			Via via = new Via(SipVersions.SIP_2_0, null, null);
			via.setBranch(getBranch());
			customizeVia(via);
			_request.pushVia(via);
			
			SipConnection connection = getServer().getConnectorManager().sendRequest(
					_request,
					transport,
					address,
					port);
			setConnection(connection);
		}
	}
	
	protected void customizeVia(Via via)
	{
		if (ID.isKey(_callSession.getId()))
		{
			via.addParameter(ID.APP_SESSION_ID_PARAMETER, _request.appSession().getAppId());
		}
	}
	
	public void start() throws IOException 
    {
        if (_state != STATE_UNDEFINED)
            throw new IllegalStateException("!undefined: " + _state);
        
        if (isInvite()) 
        {
			setState(STATE_CALLING);
			doSend();
			startTimer(TIMER_B, 64L*__T1);
			if (!isTransportReliable())
				startTimer(TIMER_A, _aDelay);
		} 
        else if (isAck()) 
        {
			setState(STATE_TRYING);
			doSend();
		} 
        else 
        {
			setState(STATE_TRYING);
			doSend();
			startTimer(TIMER_F, 64L*__T1);
			if (!isTransportReliable()) 
				startTimer(TIMER_E, _eDelay);
		}
	}
	
	public boolean isTransportReliable()
	{
		return getConnection().getConnector().isReliable();
	}
	
	public void handleResponse(SipResponse response) 
    {
        if (Log.isDebugEnabled())
            Log.debug("Response {} for tx {}", response, this);
        
		int status = response.getStatus();
        
		if (response.isInvite()) 
        {
			switch (_state) 
            {
			case STATE_CALLING:
				cancelTimer(TIMER_A); cancelTimer(TIMER_B);
				if (status < 200) 
                {
					setState(STATE_PROCEEDING);
					if (_pendingCancel != null)
						doCancel(_pendingCancel);
				} 
                else if (200 <= status && status < 300) 
                {
					terminated();
				} 
                else 
                {
					setState(STATE_COMPLETED);
					ack(response);
					if (isTransportReliable()) 
						terminated();
					else 
						startTimer(TIMER_D, __TD);
				}
                if (!_cancel)
                    _listener.handleResponse(response);
				break;
				
			case STATE_PROCEEDING:
				if (200 <= status && status < 300) 
                {
					terminated();
				} 
                else if (status >= 300) 
                {
					setState(STATE_COMPLETED);
					ack(response);
					if (isTransportReliable()) 
						terminated();
					else 
						startTimer(TIMER_D, __TD);
				}
                if (!_cancel)
                    _listener.handleResponse(response);
				break;
                
			case STATE_COMPLETED:
				ack(response);
				break;
			default:
				Log.warn("handleResponse (invite) && state ==" + _state);
			}
		} 
        else 
        {
			switch (_state) 
            {
			case STATE_TRYING:
				if (status < 200) 
                {
					setState(STATE_PROCEEDING);
				} 
                else 
                {
					cancelTimer(TIMER_E); cancelTimer(TIMER_F);
					setState(STATE_COMPLETED);
					if (isTransportReliable()) 
						terminated();
					else 
						startTimer(TIMER_K, __T4);
				}
                if (!_cancel)
                    _listener.handleResponse(response);
				break;
                
			case STATE_PROCEEDING:
				if (status >= 200) 
                {
                    cancelTimer(TIMER_E); cancelTimer(TIMER_F);
					setState(STATE_COMPLETED);
					if (isTransportReliable())
						terminated();
					else 
						startTimer(TIMER_K, __T4);
                    if (!_cancel)
                        _listener.handleResponse(response);
				}
				break;
				
			case STATE_COMPLETED:
				break;
				
			default:
				Log.warn("handleResponse (non-invite) && state ==" + _state);
			}
		}
	}
	
	public boolean isServer() 
    {
		return false;
	}
	
	protected void terminated() 
    {
		setState(STATE_TERMINATED);
		getCallSession().removeClientTransaction(this); 
    }
	
	public void timeout(int id) 
    {
        if (Log.isDebugEnabled())
            Log.debug("Timeout {} for tx {}", Integer.toString(id), this);
        
		switch (id) 
        {
		case TIMER_A:
			try 
            {
            	doSend();
			} 
            catch (IOException e) 
            {
				Log.debug("Failed to (re)send request " + _request);
			}
			_aDelay = _aDelay * 2;
			startTimer(TIMER_A, _aDelay);
			break;
		case TIMER_B:
			cancelTimer(TIMER_A);
			SipResponse responseB = create408();
			// TODO send to ??
            if (!_cancel)
                _listener.handleResponse(responseB);
			terminated();
            break;
        case TIMER_D:
            terminated();
            break;
            
        case TIMER_E:
            try 
            {
                doSend();
            }
            catch (IOException e)
            {
                Log.debug("Failed to (re)send request " + _request);
            }
            if (_state == STATE_TRYING)
                _eDelay = Math.min(_eDelay * 2, __T2);
            else
                _eDelay = __T2;
            startTimer(TIMER_E, _eDelay);
            break;
        case TIMER_F:
            cancelTimer(TIMER_E);
            SipResponse responseF = create408();
            // TODO send to ??
            if (!_cancel)
                _listener.handleResponse(responseF);
            terminated();
            break;
        case TIMER_K:
            terminated();
            break;
        default:
            throw new RuntimeException("!(a || b || d || e || f || k)");
		}
	}

	public SipResponse create408()
	{
		// could not use request.createResponse() because the request is committed. 
		SipResponse responseB = new SipResponse(_request, SipServletResponse.SC_REQUEST_TIMEOUT, null);
		responseB.setToTag(ID.newTag());
		return responseB;
	}
}
