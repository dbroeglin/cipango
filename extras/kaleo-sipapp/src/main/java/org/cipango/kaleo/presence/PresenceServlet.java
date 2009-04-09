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

import java.io.IOException;
import java.util.List;

import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.cipango.kaleo.Constants;
import org.cipango.kaleo.URIUtil;
import org.cipango.kaleo.event.ContentHandler;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.event.Subscription;

public class PresenceServlet extends SipServlet
{
	private static final long serialVersionUID = -183362525207809670L;

	private PresenceEventPackage _presence;

	public void init()
	{
		_presence = (PresenceEventPackage) getServletContext().getAttribute(PresenceEventPackage.class.getName());
	}

	protected void doPublish(SipServletRequest publish) throws IOException
	{
		String event = publish.getHeader(Constants.EVENT);

		if (event == null || !event.equals(_presence.getName()))
		{
			SipServletResponse response = publish.createResponse(SipServletResponse.SC_BAD_EVENT);
			response.addHeader(Constants.ALLOW_EVENTS, _presence.getName());
			response.send();
			return;
		}

		String uri = URIUtil.toCanonical(publish.getRequestURI());

		int expires = publish.getExpires();
		if (expires != -1)
		{
			if(expires != 0)
			{
				if (expires < _presence.getMinExpires())
				{
					SipServletResponse response = publish.createResponse(SipServletResponse.SC_INTERVAL_TOO_BRIEF);
					response.addHeader(Constants.MIN_EXPIRES, Integer.toString(_presence.getMinExpires()));
					response.send();
					return;
				}
				else if (expires > _presence.getMaxExpires())
				{
					expires = _presence.getMaxExpires();
				}
			}
		}
		else
		{
			expires = _presence.getDefaultExpires();
		}

		String contentType = publish.getContentType();

		List<String> supported = _presence.getSupportedContentTypes();
		if (contentType != null && !(supported.contains(contentType)))
		{
			SipServletResponse response = publish.createResponse(SipServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			for (String s : supported)
			{
				response.addHeader(Constants.ACCEPT, s);
			}
			response.send();
			return;
		}

		byte[] raw = publish.getRawContent();
		Object content = null;

		if (raw != null)
		{
			if (contentType == null)
			{
				SipServletResponse response = publish.createResponse(SipServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
				for (String s : supported)
				{
					response.addHeader(Constants.ACCEPT, s);
				}
				response.send();
				return;
			}
			ContentHandler<?> handler = _presence.getContentHandler(contentType);		
			try
			{
				content = handler.getContent(raw);
			}
			catch (Exception e)
			{
				log("RAW Exception => BAD REQUEST", e);
				publish.createResponse(SipServletResponse.SC_BAD_REQUEST);
				publish.send();
				return;
			}
		}
		String etag = publish.getHeader(Constants.SIP_IF_MATCH);
		Presentity presentity = _presence.getResource(uri);

		if (etag == null)
		{
			// initial publication
			if (content == null)
			{
				publish.createResponse(SipServletResponse.SC_BAD_REQUEST).send();
				return;
			}
			State state = new State(presentity, contentType, content);
			presentity.addState(state, expires);
			SipServletResponse response = publish.createResponse(SipServletResponse.SC_OK);
			response.setExpires(expires);
			response.setHeader(Constants.SIP_ETAG, state.getETag());
			response.send();
		}
		else
		{
			/*
			 * 
	3. The ESC examines the SIP-If-Match header field of the PUBLISH
      request for the presence of a request precondition.

			 *  If the request does not contain a SIP-If-Match header field,
         the ESC MUST generate and store a locally unique entity-tag for
         identifying the publication.  This entity-tag is associated
         with the event-state carried in the body of the PUBLISH
         request.

			 *  Else, if the request has a SIP-If-Match header field, the ESC
         checks whether the header field contains a single entity-tag.
         If not, the request is invalid, and the ESC MUST return with a
         400 (Invalid Request) response and skip the remaining steps.

			 *  Else, the ESC extracts the entity-tag contained in the SIP-If-
         Match header field and matches that entity-tag against all
         locally stored entity-tags for this resource and event package.
         If no match is found, the ESC MUST reject the publication with
         a response of 412 (Conditional Request Failed), and skip the
         remaining steps.
			 */

			State state = presentity.getState(etag);
			if (state == null)
			{
				publish.createResponse(SipServletResponse.SC_CONDITIONAL_REQUEST_FAILED).send();
				return;
			}
			if (expires == 0)
			{
				presentity.removeState(etag);

			}
			else
			{
				if (content != null)
				{
					state = presentity.modifyState(state, expires, contentType, content);
				}
				else
				{
					state = presentity.refreshState(state, expires);
				}
			}
			SipServletResponse response = publish.createResponse(SipServletResponse.SC_OK);
			response.setExpires(expires);
			response.setHeader(Constants.SIP_ETAG, state.getETag());
			response.send();
		}
	}

	protected void doSubscribe(SipServletRequest subscribe) throws IOException
	{
		String eventHeader = subscribe.getHeader(Constants.EVENT);

		if (eventHeader == null || !eventHeader.equals(_presence.getName()))
		{
			SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_BAD_EVENT);
			response.addHeader(Constants.ALLOW_EVENTS, _presence.getName());
			response.send();
			return;
		}
		//TODO header EVENT with ID PARAM?
		//String id = eventParam.getParameter(Constants.ID_PARAM);

		int expires = subscribe.getExpires();
		if (expires != -1)
		{
			if(expires != 0)
			{
				if (expires < _presence.getMinExpires())
				{
					SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_INTERVAL_TOO_BRIEF);
					response.addHeader(Constants.MIN_EXPIRES, Integer.toString(_presence.getMinExpires()));
					response.send();
					return;
				}
				else if (expires > _presence.getMaxExpires())
				{
					expires = _presence.getMaxExpires();
				}
			}
		}
		else
		{
			expires = _presence.getDefaultExpires();
		}

		SipSession session = subscribe.getSession();
		Subscription subscription = (Subscription)session.getAttribute(Constants.SUBSCRIPTION_ATT);

		if (subscription == null) 
		{
			//New subscription

			String uri = URIUtil.toCanonical(subscribe.getRequestURI());
			Presentity presentity = _presence.getResource(uri);

			subscription = new Subscription(presentity, session);

			presentity.addSubscription(subscription, expires);
			//TODO Authorization
			if (subscription.getState().equals(Subscription.State.ACTIVE)) 
			{
				SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_OK);
				response.setExpires(expires);
				response.send();
			} 
			//			else if (subscription.getState().getValue() == Subscription.State.PENDING) 
			//			{
			////				subscribe.createResponse(SipServletResponse.SC_ACCEPTED).send();
			//				SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_ACCEPTED);
			//				response.setExpires(expires);
			//				response.send();
			//			}
			session.setAttribute(Constants.SUBSCRIPTION_ATT, subscription);
			presentity.startSubscription(subscription.getId());
		}
		else
		{ 
			Presentity presentity = (Presentity) subscription.getResource();

			if(!presentity.isSubscribed(subscription.getId()))
			{
				SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_CALL_LEG_DONE);
				response.setExpires(expires);
				response.send();
				return;
			}

			if(expires == 0)
			{
				presentity.removeSubscription(subscription.getId(), null);
			}
			else
			{
				presentity.refreshSubscription(subscription.getId(), expires);
			}

			if (subscription.getState().equals(Subscription.State.ACTIVE)) 
			{
				SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_OK);
				response.setExpires(expires);
				response.send();
			} 
			//			else if (subscription.getState().getValue() == Subscription.State.PENDING) 
			//			{
			//				SipServletResponse response = subscribe.createResponse(SipServletResponse.SC_ACCEPTED);
			//				response.setExpires(expires);
			//				response.send();
			//				return;
			//			}
		}
	}
}
