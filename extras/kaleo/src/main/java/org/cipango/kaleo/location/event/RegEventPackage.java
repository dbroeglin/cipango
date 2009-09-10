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

package org.cipango.kaleo.location.event;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import javax.servlet.sip.SipSession;

import org.cipango.kaleo.event.AbstractEventPackage;
import org.cipango.kaleo.event.ContentHandler;
import org.cipango.kaleo.event.EventResource;
import org.cipango.kaleo.event.EventResourceListener;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.event.Subscription;
import org.cipango.kaleo.location.event.ReginfoDocument.Reginfo;

public class RegEventPackage extends AbstractEventPackage<RegResource>
{
	public static final String NAME = "reg";
	public static final String REGINFO = "application/reginfo+xml";
	private static final String VERSION = "version";
	
	private ReginfoHandler _handler = new ReginfoHandler();
	private EventResourceListener _listener = new RegEventListener();
	
	public ContentHandler<?> getContentHandler(String contentType) 
	{
		if (REGINFO.equals(contentType))
			return _handler;
		else
			return null;
	}

	public String getName() 
	{
		return NAME;
	}
	
	protected RegResource newResource(String uri)
	{
		RegResource regResource = new RegResource(uri);
		regResource.addListener(_listener);
		return regResource;
	}

	public List<String> getSupportedContentTypes() 
	{
		return Collections.singletonList(REGINFO);
	}
	
	
	class RegEventListener implements EventResourceListener
	{

		public void stateChanged(EventResource resource)
		{
			if (_log.isDebugEnabled())
				_log.debug("State changed for resource {}", resource);
			
			for (Subscription subscription : resource.getSubscriptions())
			{
				RegEventPackage.this.notify(subscription);
			}
		}
		
		public void subscriptionExpired(Subscription subscription)
		{
			subscription.setState(Subscription.State.TERMINATED);
			RegEventPackage.this.notify(subscription);
		}
		
	}
	
	protected void preprocessState(SipSession session, State state)
	{
		BigInteger version = (BigInteger) session.getAttribute(VERSION);
		if (version == null)
			version = BigInteger.ZERO;
		else
			version = version.add(BigInteger.ONE);
		Reginfo reginfo = ((ReginfoDocument) state.getContent()).getReginfo();
		
		reginfo.setVersion(version);
		session.setAttribute(VERSION, version);
	}
}
