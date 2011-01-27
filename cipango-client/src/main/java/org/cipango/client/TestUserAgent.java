package org.cipango.client;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

public class TestUserAgent extends UserAgent 
{
	public static final int DEFAULT_TIMEOUT_MS = 5000;
	
	private List<SipServletResponse> _responses = new ArrayList<SipServletResponse>();
	private int _timeout = DEFAULT_TIMEOUT_MS;
	
	public TestUserAgent(SipURI aor) 
	{
		super(aor);
	}
	
	public void handleResponse(SipServletResponse response)
	{
		synchronized (_responses)
		{
			_responses.add(response);
			_responses.notifyAll();
		}
	}
	
	public SipServletResponse getResponse(SipServletRequest request) throws InterruptedException
	{
		long start = System.currentTimeMillis();
		
		synchronized (_responses)
		{
			for (long remaining = _timeout; remaining > 0; remaining = _timeout - (System.currentTimeMillis() - start))
			{
				for (SipServletResponse response : _responses)
				{
					if (response.getRequest() == request)
						return response;
				}
				_responses.wait(remaining);	
			}
		}
		return null;
	}
}
