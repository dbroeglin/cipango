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

package org.cipango.dar;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import javax.servlet.sip.ar.SipTargetedRequestInfo;

import org.mortbay.log.Log;

/**
 * Default Application Router. 
 * Looks for its configuration from the property javax.servlet.sip.ar.dar.configuration
 * or etc/dar.properties if not defined. 
 */
public class DefaultApplicationRouter implements SipApplicationRouter
{
	public static final String __J_S_DAR_CONFIGURATION = "javax.servlet.sip.ar.dar.configuration";
	public static final String MATCH_ON_NEW_OUTGOING_REQUESTS = "org.cipango.dar.matchOnNewOutgoingRequests";
	public static final String DEFAULT_CONFIGURATION = "etc/dar.properties";

	private Map<String, RouterInfo[]> _routerInfoMap;
	private String _configuration;
	private SortedSet<String> _applicationNames = new TreeSet<String>();
	private boolean _matchOnNewOutgoingRequests;

	public void applicationDeployed(List<String> newlyDeployedApplicationNames)
	{
		_applicationNames.addAll(newlyDeployedApplicationNames);
		init();
	}

	public void applicationUndeployed(List<String> toRemove)
	{
		_applicationNames.removeAll(toRemove);
		init();
	}

	public void destroy()
	{
	}

	public SipApplicationRouterInfo getNextApplication(SipServletRequest initialRequest,
			SipApplicationRoutingRegion region, SipApplicationRoutingDirective directive, SipTargetedRequestInfo toto, Serializable stateInfo)
	{
		if (!_matchOnNewOutgoingRequests && initialRequest.getInitialRemoteAddr() == null)
			return null;
		
		if (_routerInfoMap == null || _routerInfoMap.isEmpty())
		{
			if (stateInfo != null || _applicationNames.isEmpty() || directive != SipApplicationRoutingDirective.NEW)
				return null;
			
			return new SipApplicationRouterInfo(_applicationNames.first(), 
					SipApplicationRoutingRegion.NEUTRAL_REGION, 
					initialRequest.getFrom().getURI().toString(), 
					null,
					SipRouteModifier.NO_ROUTE, 
					1);
		}
		
		String method = initialRequest.getMethod();
		RouterInfo[] infos = _routerInfoMap.get(method.toUpperCase());

		if (infos == null)
			return null;
		
		int index = 0;
		if (stateInfo != null)
			index = (Integer) stateInfo;

		if (index >= 0 && index < infos.length)
		{
			RouterInfo info = infos[index];
			
			String identity = info.getIdentity();
			if (identity.startsWith("DAR:"))
			{
				try
				{
					identity = initialRequest.getAddressHeader(identity.substring("DAR:".length())).getURI().toString();
				}
				catch (Exception e)
				{
					Log.debug("Failed to parse router info identity: " + info.getIdentity(), e);
				}
			}
			
			return new SipApplicationRouterInfo(info.getName(), info.getRegion(), identity, null,
					SipRouteModifier.NO_ROUTE, index + 1);
		}

		return null;
	}

	public void setRouterInfos(Map<String, RouterInfo[]> infoMap)
	{
		_routerInfoMap = infoMap;
	}
	
	public Map<String, RouterInfo[]> getRouterInfos()
	{
		return _routerInfoMap;
	}

	public void init() 
	{
		
		_matchOnNewOutgoingRequests = 
			!System.getProperty(MATCH_ON_NEW_OUTGOING_REQUESTS, "true").equalsIgnoreCase("false");
		
		if (_configuration == null)
		{
			String configuration = System.getProperty(__J_S_DAR_CONFIGURATION);
		
			if (configuration != null)
			{
				_configuration = configuration;
			}
			else if (System.getProperty("jetty.home") != null)
			{
				File home = new File(System.getProperty("jetty.home"));
				_configuration = new File(home, DEFAULT_CONFIGURATION).toURI().toString();
			}
			
			if (_configuration == null)
				_configuration = DEFAULT_CONFIGURATION;
		}
		
		try
		{
			DARConfiguration config = new DARConfiguration(new URI(_configuration));
			config.configure(this);
		}
		catch (Exception e)
		{
			Log.debug("DAR configuration error: " + e);
		}
		
		if ((_routerInfoMap == null || _routerInfoMap.isEmpty()) && !_applicationNames.isEmpty())
			Log.info("No DAR configuration. Using application: " + _applicationNames.first());
	}
	
	public void setConfiguration(String configuration)
	{
		_configuration = configuration;
	}

	public void init(Properties properties)
	{
		init();
	}

	static class RouterInfo
	{
		private String _name;
		private String _identity;
		private SipApplicationRoutingRegion _region;
		private String _uri;
		private SipRouteModifier _routeModifier;

		public RouterInfo(String name, String identity, SipApplicationRoutingRegion region, String uri, SipRouteModifier routeModifier)
		{
			_name = name;
			_identity = identity;
			_region = region;
			_uri = uri;
			_routeModifier = routeModifier;
		}
		
		public String getUri()
		{
			return _uri;
		}

		public SipRouteModifier getRouteModifier()
		{
			return _routeModifier;
		}

		public String getName()
		{
			return _name;
		}

		public String getIdentity()
		{
			return _identity;
		}

		public SipApplicationRoutingRegion getRegion()
		{
			return _region;
		}
	}
}
