// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.kaleo.presence.policy;

import java.util.Date;

import org.apache.xmlbeans.XmlCursor;
import org.cipango.kaleo.Resource;
import org.cipango.kaleo.event.Subscription;
import org.cipango.kaleo.policy.ConditionsType;
import org.cipango.kaleo.policy.ExtensibleType;
import org.cipango.kaleo.policy.IdentityType;
import org.cipango.kaleo.policy.ManyType;
import org.cipango.kaleo.policy.RuleType;
import org.cipango.kaleo.policy.RulesetDocument;
import org.cipango.kaleo.policy.ValidityType;
import org.cipango.kaleo.policy.RulesetDocument.Ruleset;
import org.cipango.kaleo.policy.oma.ExternalListDocument.ExternalList;
import org.cipango.kaleo.policy.presence.SubHandlingDocument;
import org.cipango.kaleo.xcap.XcapException;
import org.cipango.kaleo.xcap.XcapResource;
import org.cipango.kaleo.xcap.XcapService;
import org.cipango.kaleo.xcap.XcapUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XcapPolicyManager implements PolicyManager
{
	private final Logger _log = LoggerFactory.getLogger(XcapPolicyManager.class);
	private XcapService _xcapService;
	private static final String OMA_COMMON_POLICY = "urn:oma:xml:xdm:common-policy";
	private static final String PRES_RULES = "urn:ietf:params:xml:ns:pres-rules";
	
	
	public XcapPolicyManager(XcapService xcapService)
	{
		_xcapService = xcapService;
	}
	
	public SubHandling getPolicy(Subscription subscription)
	{
		return getPolicy(subscription.getUri(), subscription.getResource());
	}
	
	
	public SubHandling getPolicy(String subscriberUri, Resource resource)
	{
		try
		{
			XcapUri xcapUri = new XcapUri("org.openmobilealliance.pres-rules", resource.getUri(), "pres-rules", null);
			XcapResource xcapResource = _xcapService.getResource(xcapUri, false, null, null);
			Ruleset ruleset = RulesetDocument.Factory.parse(xcapResource.getSelectedResource().getDom()).getRuleset();
			SubHandling best = null;
			String domain = null;
			if (subscriberUri.indexOf('@') != -1)
				domain = subscriberUri.substring(subscriberUri.indexOf('@') + 1);
			
			for (int i = 0; i < ruleset.getRuleArray().length; i++)
			{
				RuleType rule = ruleset.getRuleArray(i);
				if (match(rule.getConditions(), subscriberUri, domain))
				{
					SubHandling subHandling = getSubHandling(rule.getActions());
					if (best == null || subHandling.getValue() > best.getValue())
						best = subHandling;
				}
			}
			if (best == null)
			{
				for (int i = 0; i < ruleset.getRuleArray().length; i++)
				{
					RuleType rule = ruleset.getRuleArray(i);
					if (matchOma(rule.getConditions(), subscriberUri))
					{
						SubHandling subHandling = getSubHandling(rule.getActions());
						if (best == null || subHandling.getValue() > best.getValue())
							best = subHandling;
					}
				}
			}
			if (best == null)
			{
				for (int i = 0; i < ruleset.getRuleArray().length; i++)
				{
					RuleType rule = ruleset.getRuleArray(i);
					if (matchOmaOtherIdentity(rule.getConditions()))
					{
						SubHandling subHandling = getSubHandling(rule.getActions());
						if (best == null || subHandling.getValue() > best.getValue())
							best = subHandling;
					}
				}
			}
			_log.debug("Got policy " + best + " for subscriber {} and resource {}", subscriberUri, resource);
			if (best == null)
				return SubHandling.BLOCK;
			return best;
		}
		catch (XcapException e) 
		{
			_log.debug("Unable to find policy for subcription: "  + subscriberUri, e);
			return SubHandling.BLOCK;
		}
		catch (Exception e) 
		{
			_log.warn("Unable to find policy for subcription: "  + subscriberUri, e);
			return SubHandling.BLOCK;
		}	
	}
	
	private SubHandling getSubHandling(ExtensibleType actions)
	{
		XmlCursor cursor = actions.newCursor();
		cursor.toChild(PRES_RULES, "sub-handling");
		SubHandlingDocument.SubHandling subHandling = (SubHandlingDocument.SubHandling) cursor.getObject();
		
		if (subHandling.enumValue().equals(SubHandlingDocument.SubHandling.ALLOW))
			return SubHandling.ALLOW;
		else if (subHandling.enumValue().equals(SubHandlingDocument.SubHandling.CONFIRM))
			return SubHandling.CONFIRM;
		else if (subHandling.enumValue().equals(SubHandlingDocument.SubHandling.BLOCK))
			return SubHandling.BLOCK;
		else if (subHandling.enumValue().equals(SubHandlingDocument.SubHandling.POLITE_BLOCK))
			return SubHandling.POLITE_BLOCK;
		
		throw new IllegalArgumentException("No sub-handling block");
	}

	private boolean match(ConditionsType conditions, String subscriberAor, String domain)
	{
		boolean matchIdentity = conditions.getIdentityArray().length == 0;
		for (int i = 0; i < conditions.getIdentityArray().length; i++)
		{
			IdentityType identity = conditions.getIdentityArray(i);
			for (int j = 0; j < identity.getOneArray().length; j++)
				if (identity.getOneArray(j).getId().equals(subscriberAor))
					matchIdentity = true;
			for (int j = 0; j < identity.getManyArray().length; j++)
			{
				ManyType manyType = identity.getManyArray(j);
				if (manyType.getDomain().equals(domain))
				{
					boolean match = true;
					for (int k = 0; k < manyType.getExceptArray().length; k++)
						if (manyType.getExceptArray(k).getId().equals(subscriberAor))
							match = false;
					if (match)
						matchIdentity = true;
				}
			}
		}
		
		if (!matchIdentity)
			return false;
		
		boolean matchValidity = conditions.getValidityArray().length == 0;
		Date now = new Date();
		for (int i = 0; i < conditions.getValidityArray().length; i++)
		{
			ValidityType validity = conditions.getValidityArray(i);
			for (int j = 0; j < validity.getFromArray().length; j++)
				if (validity.getFromArray(j).after(now) && validity.getUntilArray(j).before(now))
					matchValidity = true;
		}
		if (!matchValidity)
			return false;
		
		// TODO check sphere
		
		// Ensure that at least one condition has match
		return conditions.getIdentityArray().length != 0 || conditions.getValidityArray().length != 0;
	}
	
	private boolean matchOma(ConditionsType conditions, String subscriberAor)
	{
		try
		{
			XmlCursor cursor = conditions.newCursor();
			cursor.push();
			if (cursor.toChild(OMA_COMMON_POLICY, "external-list"))
			{
				ExternalList list = (ExternalList) cursor.getObject();
				for (int i = 0; i < list.getEntryArray().length; i++)
				{
					String anchor = list.getEntryArray(i).getAnc();
					int index = anchor.indexOf("://");
					// Assume it is the same XCAP server
					String uri = anchor.substring(anchor.indexOf(_xcapService.getRootName(), index + 3));
					XcapUri xcapUri = new XcapUri(uri, _xcapService.getRootName());
					XcapResource xcapResource = _xcapService.getResource(xcapUri, false, null, null);
					NodeList nodes = xcapResource.getSelectedResource().getDom().getChildNodes();
					for (int j = 0; j < nodes.getLength(); j++)
					{
						Node node = nodes.item(j);
						if ("entry".equals(node.getLocalName()))
						{
							Element element = (Element) node;
							if (subscriberAor.equals(element.getAttribute("uri")))
								return true;
						}
					}
				}
			}
			cursor.pop();
			if (cursor.toChild(OMA_COMMON_POLICY, "anonymous-request"))
			{
				return subscriberAor.equals("sip:anonymous@anonymous.invalid");
				// TODO add better support to anonymous-request
			}
			
		}
		catch (Throwable e) {
			_log.warn("Unable to check OMA conditions for subscriber " + subscriberAor, e);
		}
		return false;
	}
	private boolean matchOmaOtherIdentity(ConditionsType conditions)
	{
		XmlCursor cursor = conditions.newCursor();
		return cursor.toChild(OMA_COMMON_POLICY, "other-identity");
	}
}
