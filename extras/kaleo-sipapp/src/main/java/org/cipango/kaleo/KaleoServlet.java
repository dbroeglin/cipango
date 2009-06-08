package org.cipango.kaleo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;

public class KaleoServlet extends SipServlet
{
	private static final long serialVersionUID = 1L;

	protected void doRequest(SipServletRequest request) // throws ServletException, IOException
	{
		//System.out.println(request);
		try
		{
		super.doRequest(request);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected void doRegister(SipServletRequest register) throws ServletException, IOException
	{
		getServletContext().getNamedDispatcher("registrar").forward(register, null);
	}
	
	protected void doPublish(SipServletRequest publish) throws ServletException, IOException
	{
		getServletContext().getNamedDispatcher("presence").forward(publish, null);
	}
}

