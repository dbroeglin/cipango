package org.cipango.client.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.cipango.client.MessageHandler;
import org.cipango.client.UserAgent;

public class SipTester extends UserAgent 
{
	public static final int DEFAULT_TIMEOUT_MS = 5000;
	
	private List<SipServletResponse> _responses = new ArrayList<SipServletResponse>();
	private int _timeout = DEFAULT_TIMEOUT_MS;
	
	public SipTester(SipURI aor) 
	{
		super(aor);
		setDefaultHandler(new TestHandler());
	}
	
	public SipServletRequest createRequest(String method, UserAgent destination)
	{
		return createRequest(method, destination.getLocalAddress());
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
	
	class TestHandler implements MessageHandler
	{	
		public void handleRequest(SipServletRequest request) throws IOException
		{ 
			request.createResponse(SipServletResponse.SC_OK).send();
		}

		public void handleResponse(SipServletResponse response) 
		{
			synchronized (_responses)
			{
				_responses.add(response);
				_responses.notifyAll();
			}
		}
	}
}
