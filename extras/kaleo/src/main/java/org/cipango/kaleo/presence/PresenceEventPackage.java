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

package org.cipango.kaleo.presence;

import java.util.Collections;
import java.util.List;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.cipango.kaleo.AbstractResourceManager;
import org.cipango.kaleo.Constants;
import org.cipango.kaleo.Resource;
import org.cipango.kaleo.event.AbstractEventPackage;
import org.cipango.kaleo.event.ContentHandler;
import org.cipango.kaleo.event.EventResource;
import org.cipango.kaleo.event.EventResourceListener;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.event.Subscription;
import org.cipango.kaleo.presence.pidf.PidfHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presence Event Package
 */
public class PresenceEventPackage extends AbstractEventPackage<Presentity>
{
	public Logger _log = LoggerFactory.getLogger(PresenceEventPackage.class);
	
	public static final String NAME = "presence";
	public static final String PIDF = "application/pidf+xml";
	
	private PidfHandler _pidfHandler = new PidfHandler();
	private EventResourceListener _presenceListener = new PresentityListener();
	
	public int _minStateExpires = 1;
	public int _maxStateExpires = 3600;
	public int _defaultStateExpires = 3600;
	
	public PresenceEventPackage()
	{
	}

	public String getName()
	{
		return NAME;
	}

	public int getMinStateExpires()
	{
		return _minStateExpires;
	}
	
	public int getMaxStateExpires()
	{
		return _maxStateExpires;
	}
	
	public int getDefaultStateExpires()
	{
		return _defaultStateExpires;
	}
	
	protected Presentity newResource(String uri)
	{
		Presentity presentity = new Presentity(uri);
		presentity.addListener(_presenceListener);
		return presentity;
	}
	
	public List<String> getSupportedContentTypes()
	{
		return Collections.singletonList(PIDF);
	}
	
	public ContentHandler<?> getContentHandler(String contentType)
	{
		if (PIDF.equals(contentType))
			return _pidfHandler;
		else
			return null;
	}
		
	class PresentityListener implements EventResourceListener
	{
		public void stateChanged(EventResource resource)
		{
			if (_log.isDebugEnabled())
				_log.debug("State changed for resource {}", resource);
			
			for (Subscription subscription : resource.getSubscriptions())
			{
				PresenceEventPackage.this.notify(subscription);
			}
		}
		
		public void subscriptionExpired(Subscription subscription)
		{
			subscription.setState(Subscription.State.TERMINATED);
			PresenceEventPackage.this.notify(subscription);
		}
	}
}
