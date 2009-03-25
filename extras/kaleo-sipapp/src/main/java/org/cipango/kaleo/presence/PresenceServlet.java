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

import org.cipango.kaleo.Constants;
import org.cipango.kaleo.URIUtil;
import org.cipango.kaleo.event.ContentHandler;
import org.cipango.kaleo.event.State;

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
		if (expires != 0)
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
		else
		{
			expires = _presence.getDefaultExpires();
		}
		String contentType = publish.getContentType();
		
		List<String> supported = _presence.getSupportedContentTypes();
		
		if (contentType == null || !(supported.contains(contentType)))
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
			ContentHandler<?> handler = _presence.getContentHandler(contentType);		
			try
			{
				content = handler.getContent(raw);
			}
			catch (Exception e)
			{
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
			State state = new State(contentType, content);
			presentity.addState(state);
			SipServletResponse response = publish.createResponse(200);
			response.setHeader(Constants.SIP_ETAG, state.getETag());
			response.send();
		}
		else
		{
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
					// update
				}
				else
				{
					// refresh
				}
			}
		}
	}
}
