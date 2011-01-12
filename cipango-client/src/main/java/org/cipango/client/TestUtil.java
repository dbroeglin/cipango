// ========================================================================
// Copyright 2011 NEXCOM Systems
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
package org.cipango.client;

import java.util.List;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import static junit.framework.Assert.fail;

public class TestUtil
{
	public static SipServletResponse waitResponse(SipServletRequest request)
	{		
		synchronized (request)
		{
			SipServletResponse response = getUncommitedResponse(request);
			if (response != null)
				return response;
			try { request.wait(5000); } catch (InterruptedException e) {}
			response = getUncommitedResponse(request);
			if (response == null)
				fail("No response received for request: " + request);
			return response;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static SipServletResponse getUncommitedResponse(SipServletRequest request)
	{
		List<CommitableMessage<SipServletResponse>> l = (List<CommitableMessage<SipServletResponse>>) request.getAttribute(CommitableMessage.class.getName());
		if (l != null)
		{
			for (CommitableMessage<SipServletResponse> cevent : l)
			{
				if (!cevent.isCommit())
				{
					cevent.setCommit(true);
					return cevent.getMessage();
				}
			}
		}
		return null;
	}

	
	public static SipServletRequest waitRequest(SipSession session)
	{		
		synchronized (session)
		{
			SipServletRequest response = getUncommitedRequest(session);
			if (response != null)
				return response;
			try { session.wait(5000); } catch (InterruptedException e) {}
			response = getUncommitedRequest(session);
			if (response == null)
				fail("No request received on session: " + session);
			return response;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static SipServletRequest getUncommitedRequest(SipSession session)
	{
		List<CommitableMessage<SipServletRequest>> l = (List<CommitableMessage<SipServletRequest>>) session.getAttribute(CommitableMessage.class.getName());
		if (l != null)
		{
			for (CommitableMessage<SipServletRequest> cevent : l)
			{
				if (!cevent.isCommit())
				{
					cevent.setCommit(true);
					return cevent.getMessage();
				}
			}
		}
		return null;
	}
}
