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

import java.util.List;

import javax.servlet.sip.SipSession;

import org.cipango.server.Server;
import org.cipango.server.SipRequest;
import org.cipango.server.SipResponse;
import org.cipango.server.transaction.ClientTransaction;
import org.cipango.server.transaction.ServerTransaction;
import org.cipango.sipapp.SipAppContext;
import org.cipango.util.TimerTask;

/**
 * Session related to a SIP call. 
 * 
 * The call session is the first level of sessions. It acts as a holder for main primitives such 
 * as SIP transactions, application sessions and timers. 
 */
public interface CallSession
{
	/**
	 * Returns a string containing the unique identifier assigned to this session.
	 */
	String getId();
	
	/**
	 * Returns the {@link Server} instance.
	 */
	Server getServer();
	
	/**
	 * Schedule a timer
	 * 
	 * @param runnable 	the runnable to run when the timer expires
	 * @param delay	delay in ms before the timer is executed
	 */
	TimerTask schedule(Runnable runnable, long delay);
	
	/**
	 * Cancel a scheduled timer.
	 */
	void cancel(TimerTask timer);
	
	/**
	 * Creates a new AppSession
	 * 
	 * @param context	the SipAppContext to which the created AppSession belongs
	 * @param id		the AppSession id
	 * @return	a new AppSession
	 */
	AppSession createAppSession(SipAppContext context, String id);
	
	/**
	 * Retrieves an AppSession belonging to this call session
	 * 
	 * @param id	the AppSession id
	 * @return	the AppSession 
	 */
	AppSession getAppSession(String id);
	
	/**
	 * Removes an AppSession from this call session
	 * @param session
	 */
	void removeSession(AppSession session);
	
	/**
	 * Finds the Session by which the request should be handled
	 * 
	 * @param request the incoming SIP request
	 * @return the Session handling the request
	 */
	Session findSession(SipRequest request);
	
	Session findSession(SipResponse response); // TODO remove
	
	/**
	 * Indicates whether a SipSession has active transactions (i.e. transactions that are not completed)
	 * 
	 * @param session the SipSession
	 * @return true if there is at least a transaction not in the completed state, false otherwise
	 */
	boolean hasActiveTransactions(SipSession session);
	
	/**
	 * Returns a list of the server transactions belonging to the given SipSession.
	 */
	List<ServerTransaction> getServerTransactions(SipSession session);
	
	/**
	 * Adds a client transaction to this call sessions
	 */
	void addClientTransaction(ClientTransaction transaction);
	
	/**
	 * Removes a client transaction to this call session
	 */
	void removeClientTransaction(ClientTransaction transaction);
	
	/**
	 * Returns a list of the client transactions belonging to the given SipSession.
	 */
	List<ClientTransaction> getClientTransactions(SipSession session);
	
	/**
	 * Adds a server transaction to this call session
	 */
	void addServerTransaction(ServerTransaction transaction);
	
	/**
	 * Retrieves a server transaction
	 * 
	 * @param id	the transaction id 
	 */
	ServerTransaction getServerTransaction(String id);
	
	/**
	 * Retrieves a client transaction 
	 * 
	 * @param id	the transaction id
	 */
	ClientTransaction getClientTransaction(String id);
	
	/**
	 * Removes a server transaction
	 * 
	 * @param transaction	the server transaction to remove
	 */
	void removeServerTransaction(ServerTransaction transaction);
}