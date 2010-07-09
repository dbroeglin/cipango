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

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletResponse;

import org.cipango.SipProxy;
import org.cipango.SipHandler;
import org.cipango.SipMessage;
import org.cipango.SipRequest;
import org.cipango.SipResponse;
import org.cipango.server.session.Session;
import org.cipango.sip.SipGrammar;

import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.log.Log;

public class TransactionManager extends HandlerWrapper implements SipHandler
{   
	private long _statsStartedAt = -1;	
	private Object _statsLock = new Object();	
	private long _nbRetransmission;
	private long _nbUnknownTransaction;
	
	public void handle(SipServletMessage message) throws ServletException, IOException 
    {
		if (((SipMessage) message).isRequest())
			handleRequest((SipRequest) message);
		else
			handleResponse((SipResponse) message);
	}
	
	public void handleRequest(SipRequest request) throws ServletException, IOException 
    {
		String branch = request.getTopVia().getBranch();
		
		if (branch == null || !branch.startsWith(SipGrammar.MAGIC_COOKIE)) 
        {
			if (!("0".equals(branch) && request.isAck()))
			{
				Log.debug("Not 3261 branch: {}. Dropping request", branch);
				return;
			}
		}
		if (request.isCancel()) 
			branch = "cancel-" + branch;
		
		ServerTransaction tx = request.getCallSession().getServerTransaction(branch);
		
		if (tx != null) 
        {
            if (Log.isDebugEnabled()) 
                Log.debug("request {} in transaction {}", request.getRequestLine(), tx);
			
            request.setTransaction(tx);
			boolean handled = tx.handleRequest(request);
            
            if (handled)
                return;
		} 
        
		tx = newServerTransaction(request);

		if (request.isCancel())
        {
            String txBranch = request.getTopVia().getBranch();
            ServerTransaction stx = request.getCallSession().getServerTransaction(txBranch);
            if (stx == null)
            {
                if (Log.isDebugEnabled())
                    Log.debug("No transaction for cancelled branch {}", txBranch, null);
                SipResponse unknown = (SipResponse) request.createResponse(SipServletResponse.SC_CALL_LEG_DONE);
                tx.send(unknown);
            }
            else 
            {
                SipResponse ok = (SipResponse) request.createResponse(SipServletResponse.SC_OK);
                tx.send(ok);
                
                stx.cancel(request);
            }
        }
        else 
        	((SipHandler) getHandler()).handle(request);
	}
	
	public ServerTransaction newServerTransaction(SipRequest request)
	{
		ServerTransaction tx = new ServerTransaction(request);

		if (!request.isAck()) 
			request.getCallSession().addServerTransaction(tx);
		 
        if (Log.isDebugEnabled())
            Log.debug("new transaction {} for request {}", tx, request.getRequestLine());

        return tx;
	}
	
	public void handleResponse(SipResponse response) 
    {
		String branch = response.getTopVia().getBranch();
        
		if (response.isCancel()) 
			branch = "cancel-" + branch;
		
		ClientTransaction ctx = response.getCallSession().getClientTransaction(branch);

		if (ctx == null) 
        {
			// TODO fork or error
            if (Log.isDebugEnabled())
                Log.debug("Response {} with no transaction ", response, branch);

        	boolean invite2xx = response.isInvite() 
        							&& response.getStatus() >= 200
									&& response.getStatus() < 300;
			if (invite2xx)
			{
				incrementNbRetransmission();
				Session session = (Session) response.getCallSession().findSession(response);
				if (session != null)
				{
					try
					{
						session.handleResponse(response);
					}
					catch (Exception e)
					{
						Log.warn(e);
					}
				}
			}
			else
			{
				if (_statsStartedAt != -1) 
				{
					synchronized (_statsLock) 
					{
						_nbUnknownTransaction++;
					}
				}
			}
		} 
        else 
        {
            if (Log.isDebugEnabled())
                Log.debug("Response {} in transaction {}", response, ctx);
			response.setTransaction(ctx);
			ctx.handleResponse(response);
		}
    }
	
	public ClientTransaction sendRequest(SipRequest request, ClientTransactionListener listener) 
    {
		ClientTransaction ctx = new ClientTransaction(request, listener);
		
		if (!request.isAck())
			request.getCallSession().addClientTransaction(ctx);
		
		try 
        {
			ctx.start();
		} 
        catch (IOException e)
        {
			Log.warn(e);
		}
		return ctx;
	}
	
	public int getT1() { return Transaction.__T1; }
	public int getT2() { return Transaction.__T2; }
	public int getT4() { return Transaction.__T4; }
	public int getTD() { return Transaction.__TD; }
	public int getTimerC() { return SipProxy.__timerC; }
	
	public void setT1(int millis)
	{ 
		if (millis < 0)
			throw new IllegalArgumentException("SIP Timers must be positive");
		Transaction.__T1 = millis;
	}
	
	public void setT2(int millis) 
	{
		if (millis < 0)
			throw new IllegalArgumentException("SIP Timers must be positive");
		Transaction.__T2 = millis;
	}
	
	public void setT4(int millis) 
	{
		if (millis < 0)
			throw new IllegalArgumentException("SIP Timers must be positive");
		Transaction.__T4 = millis;
	}
	
	public void setTD(int millis) 
	{
		if (millis < 0)
			throw new IllegalArgumentException("SIP Timers must be positive");
		Transaction.__TD = millis;
	}
	
	public void setTimerC(int millis) 
	{
		if (millis < 0)
			throw new IllegalArgumentException("SIP Timers must be positive");
		SipProxy.__timerC = millis;
	}
	
	protected void incrementNbRetransmission() {
		if (_statsStartedAt != -1) 
		{
			synchronized (_statsLock) 
			{
				_nbRetransmission++;
			}
		}
	}
	
	public long getNbRetransmission()
	{
		return _nbRetransmission;
	}
	
	public long getNbUnknownTransaction()
	{
		return _nbUnknownTransaction;
	}
	
	public void statsReset() 
	{
		synchronized (_statsLock) 
		{
			_statsStartedAt = _statsStartedAt == -1 ? -1 : System.currentTimeMillis();
			_nbRetransmission = 0;
			_nbUnknownTransaction = 0;
		}
	}
	
	public void setStatsOn(boolean on) 
	{
        if (on && _statsStartedAt != -1) 
        	return;

        statsReset();
        _statsStartedAt = on ? System.currentTimeMillis() : -1;
    }
	
	public boolean isStatsOn() 
	{
		return  _statsStartedAt != -1;
	}
}
