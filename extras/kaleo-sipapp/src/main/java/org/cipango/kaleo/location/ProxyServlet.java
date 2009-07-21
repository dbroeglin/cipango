package org.cipango.kaleo.location;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.kaleo.URIUtil;

public class ProxyServlet extends SipServlet
{
	private LocationService _locationService;
	
	@Override
	public void init()
	{
		_locationService = (LocationService) getServletContext().getAttribute(LocationService.class.getName());
	}
	
	@Override
	protected void doInvite(SipServletRequest invite) throws ServletException, IOException
	{
		String target = URIUtil.toCanonical(invite.getRequestURI());
		
		List<Binding> bindings = _locationService.getBindings(target);
		
		if (bindings.size() == 0)
		{
			invite.createResponse(SipServletResponse.SC_NOT_FOUND).send();
			return;
		}
		
		Binding binding = bindings.get(0);
		
		invite.getSession().setHandler(getServletName());
		invite.getProxy().proxyTo(binding.getContact());
	}
}
