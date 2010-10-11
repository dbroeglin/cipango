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
package org.cipango.server.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.cipango.server.session.Session.UA;
import org.cipango.server.session.SessionManager.CSession;
import org.cipango.server.transaction.ClientTransaction;
import org.cipango.server.transaction.ServerTransaction;
import org.cipango.util.TimerTask;
import org.eclipse.jetty.util.log.Log;

public class CallSessionViewer
{
	private SessionManager _sessionManager;
	
	public CallSessionViewer(SessionManager sessionManager)
	{
		_sessionManager = sessionManager;
	}
	
	public String viewCall(String callId) 
	{
		CallSession callSession = _sessionManager.get(callId);
		if (callSession == null)
			return "No call with ID " + callId + " found";
		
		StringBuilder sb = new StringBuilder();
		try
		{			
			sb.append("+ ").append(callId).append('\n');
			CSession cSession = (CSession) callSession;
			Iterator<AppSession> it = cSession._appSessions.iterator();
			sb.append("\t+ [appSessions]\n");
			while (it.hasNext())
				printAppSession(sb, it.next());
			
			Iterator<ClientTransaction> it2 = cSession._clientTransactions.iterator();
			if (it2.hasNext())
				sb.append("\t+ [clientTransaction]\n");
			while (it2.hasNext())
				sb.append("\t\t+ ").append(it2.next()).append('\n');
			
			Iterator<ServerTransaction> it3 = cSession._serverTransactions.iterator();
			if (it3.hasNext())
				sb.append("\t+ [serverTransaction]\n");
			while (it3.hasNext())
				sb.append("\t\t+ ").append(it3.next()).append('\n');

			Iterator<TimerTask> it4 = cSession._timers.iterator();
			if (it4.hasNext())
				sb.append("\t+ [Timers]\n");
			while (it4.hasNext())
			{
				TimerTask task = it4.next();
				sb.append("\t\t+ ").append(task).append('\n');
				printAttr(sb, "class", task.getRunnable().getClass().getName());
				printAttr(sb, "executionTime", new Date(task.getExecutionTime()));
			}
		}
		catch (Exception e)
		{
			sb.append("\n\n").append(e);
			Log.warn(e);
		}
		
		return sb.toString();
	}
	
	private void printAppSession(StringBuilder sb, AppSession appSession)
	{
		sb.append("\t\t+ ").append(appSession.getAppId()).append('\n');
		printAttr(sb, "created", new Date(appSession.getCreationTime()));
		printAttr(sb, "accessed", new Date(appSession.getLastAccessedTime()));
		printAttr(sb, "expirationTime", new Date(appSession.getExpirationTime()));
		printAttr(sb, "context", appSession.getContext().getName());
		printAttr(sb, "invalidateWhenReady", appSession.getInvalidateWhenReady());
		printAttr(sb, "attributes", appSession._attributes);
		
		Iterator<Session> it = appSession._sessions.iterator();
		if (it.hasNext())
			sb.append("\t\t\t+ [sipSessions]\n");
		while (it.hasNext())
		{
			Session session = it.next();
			sb.append("\t\t\t\t+ ").append(session.getId()).append('\n');
			printAttr(sb, "created", new Date(session.getCreationTime()), 5);
			printAttr(sb, "accessed", new Date(session.getLastAccessedTime()), 5);
			printAttr(sb, "role", session._role, 5);
			printAttr(sb, "state", session._state, 5);
			printAttr(sb, "invalidateWhenReady", session.getInvalidateWhenReady(), 5);
			printAttr(sb, "attributes", session._attributes, 5);
			printAttr(sb, "localParty", session._localParty, 5);
			printAttr(sb, "remoteParty", session._remoteParty, 5);
			printAttr(sb, "region", session._region, 5);
			printAttr(sb, "Call-ID", session._callId, 5);
			printAttr(sb, "linkedSessionId", session._linkedSessionId, 5);
			printAttr(sb, "subscriberURI", session._subscriberURI, 5);
			UA ua = session._ua;
			if (ua != null)
			{
				printAttr(sb, "local CSeq", ua._localCSeq, 5);
				printAttr(sb, "Remote CSeq", ua._remoteCSeq, 5);
				printAttr(sb, "Remote Target", ua._remoteTarget, 5);
				printAttr(sb, "route Set", ua._routeSet, 5);
				printAttr(sb, "Secure", ua._secure, 5);
				printAttr(sb, "local RSeq", ua._localRSeq, 5);
				printAttr(sb, "Remote RSeq", ua._remoteRSeq, 5);
			}
			
		}
	}
	
	private void printAttr(StringBuilder sb, String name, Object value)
	{
		printAttr(sb, name, value, 3);
	}
	
	private void printAttr(StringBuilder sb, String name, Object value, int index)
	{
		for (int i =0; i < index; i++)
			sb.append('\t');
		sb.append("- ").append(name).append(": ").append(value).append('\n');
	}
	
	public List<String> getCallIds()
	{
		synchronized (_sessionManager._callSessions)
		{
			return new ArrayList<String>(_sessionManager._callSessions.keySet());
		}
	}
}
