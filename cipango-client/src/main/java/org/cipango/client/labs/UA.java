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
package org.cipango.client.labs;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

public class UA
{
	private UaManager _uaManager;
	private Address _aor;
	private SipURI _proxy;
	private Session _uasSession;
	
	public UA(UaManager uaManager, String aor) throws Exception
	{
		_uaManager = uaManager;
		_aor = _uaManager.getSipFactory().createAddress(aor);
		_uaManager.addUserAgent(this);
	}
	
	public Address getContact()
	{
		return _uaManager.getContact();
	}
	
	public Address getAor()
	{
		return _aor;
	}
	
	public void setProxy(String uri) throws ServletParseException
	{
		URI proxyUri = _uaManager.getSipFactory().createURI(uri);
		if (!proxyUri.isSipURI())
			throw new ServletParseException("Proxy URI: " + uri + " is not a SIP URI");
		_proxy = (SipURI) proxyUri;
		_proxy.setLrParam(true);
	}
	
	public void setProxy(SipURI uri) throws ServletParseException
	{
		_proxy = uri;
		_proxy.setLrParam(true);
	}
	
	public SipURI getProxy()
	{
		return _proxy;
	}
	
	public SipRequest createRequest(String method, String to) throws ServletParseException
	{
		Address toAddr = _uaManager.getSipFactory().createAddress(to);
		return createRequest(method, toAddr);
	}
	
	public SipRequest createRequest(String method, Address to) throws ServletParseException
	{
		SipApplicationSession appSession = _uaManager.getSipFactory().createApplicationSession();
		SipServletRequest request = _uaManager.getSipFactory().createRequest(appSession, method, _aor, to);
		if (_proxy != null)
			request.pushRoute(_proxy);
		return new SipRequestImpl(request);
	}
	
	public SipFactory getSipFactory()
	{
		return _uaManager.getSipFactory();
	}
	
	
	public SipSession createUasSession()
	{
		if (_uasSession != null)
			throw new IllegalStateException("A UAS session is already created");
		
		_uasSession = new Session(null);
		return _uasSession;
	}
	
	protected Session getUasSession()
	{
		return _uasSession;
	}
}
