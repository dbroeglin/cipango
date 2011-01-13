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

import java.util.Iterator;

import javax.servlet.sip.Rel100Exception;
import javax.servlet.sip.SipServletResponse;

public class SipResponseImpl extends SipMessageImpl implements SipResponse
{

	public SipResponseImpl(javax.servlet.sip.SipServletResponse response)
	{
		super(response);
	}
	
	protected SipServletResponse response()
	{
		return (SipServletResponse) _message;
	}


	public SipRequest createAck()
	{
		SipRequestImpl request = new SipRequestImpl(response().createAck());
		((Session) getSession()).addHeaders(request);
		return request;
	}

	public SipRequest createPrack() throws Rel100Exception
	{
		SipRequestImpl request = new SipRequestImpl(response().createPrack());
		((Session) getSession()).addHeaders(request);
		return request;
	}

	public Iterator<String> getChallengeRealms()
	{
		return response().getChallengeRealms();
	}

	public String getReasonPhrase()
	{
		return response().getReasonPhrase();
	}

	public SipRequest getRequest()
	{
		return new SipRequestImpl(response().getRequest());
	}

	public int getStatus()
	{
		return response().getStatus();
	}

	public void sendReliably() throws Rel100Exception
	{
		response().sendReliably();
	}

	public void setStatus(int statusCode)
	{
		response().setStatus(statusCode);
	}

	public void setStatus(int statusCode, String reasonPhrase)
	{
		response().setStatus(statusCode, reasonPhrase);
	}

}
