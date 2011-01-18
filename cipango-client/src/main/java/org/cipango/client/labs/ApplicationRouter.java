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

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import javax.servlet.sip.ar.SipTargetedRequestInfo;

public class ApplicationRouter implements SipApplicationRouter
{
	/**
	 * @see SipApplicationRouter#init
	 */
	public void init() { }

	/**
	 * @see SipApplicationRouter#init(Properties)
	 */
	public void init(Properties properties) { }

	/**
	 * @see SipApplicationRouter#destroy()
	 */
	public void destroy() { }
	
	/**
	 * @see SipApplicationRouter#applicationDeployed(List)
	 */
	public void applicationDeployed(List<String> deployedApplications) { }

	/**
	 * @see SipApplicationRouter#applicationUndeployed(List)
	 */
	public void applicationUndeployed(List<String> undeployedApplications) { }

	/**
	 * @see SipApplicationRouter#getNextApplication(SipServletRequest, SipApplicationRoutingRegion, 
	 * 			SipApplicationRoutingDirective, SipTargetedRequestInfo, Serializable)
	 */
	public SipApplicationRouterInfo getNextApplication(SipServletRequest request,
			SipApplicationRoutingRegion region, SipApplicationRoutingDirective directive,
			SipTargetedRequestInfo requestedInfo, Serializable info)
	{
		if (request.getRemoteAddr() == null)
			return null;
		return new SipApplicationRouterInfo(UaManager.class.getName(),
				SipApplicationRoutingRegion.NEUTRAL_REGION, 
				request.getFrom().getURI().toString(), 
				null,
				SipRouteModifier.NO_ROUTE, 
				1);
	}
}
