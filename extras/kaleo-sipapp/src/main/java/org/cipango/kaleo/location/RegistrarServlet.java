package org.cipango.kaleo.location;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.kaleo.Constants;
import org.cipango.kaleo.URIUtil;
import org.cipango.kaleo.presence.PresenceEventPackage;

public class RegistrarServlet extends SipServlet
{
	private LocationService _locationService;
	
	private int _minExpires = 60;
	private int _maxExpires = 3600;
	private int _defaultExpires = 3600;
	
	private DateFormat _dateFormat;
	private SipFactory _sipFactory;
	
	public void init()
	{
		_dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
		_dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		_sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		
		_locationService = (LocationService) getServletContext().getAttribute(LocationService.class.getName());
	}
	
	protected void doRegister(SipServletRequest register) throws IOException, ServletException
	{
		String aor = URIUtil.toCanonical(register.getRequestURI());
		
		List<Binding> bindings = _locationService.getBindings(aor);
		if (bindings == null)
			bindings = Collections.emptyList();
		
		Iterator<Address> it = register.getAddressHeaders(Constants.CONTACT);
		if (it.hasNext())
		{
			List<Address> contacts = new ArrayList<Address>();
			boolean wildcard = false;
			
			while (it.hasNext())
			{
				Address contact = it.next();
				if (contact.isWildcard())
				{
					wildcard = true;
					if (it.hasNext() || contacts.size() > 0 || register.getExpires() > 0)
					{
						register.createResponse(SipServletResponse.SC_BAD_REQUEST, "Invalid wildcard").send();
						return;
					}
				}
				contacts.add(contact);
			}
			
			String callId = register.getCallId();
			int cseq;
			try
			{
				String s = register.getHeader(Constants.CSEQ);
				cseq = Integer.parseInt(s.substring(0, s.indexOf(' ')));
			}
			catch (Exception e)
			{
				register.createResponse(SipServletResponse.SC_BAD_REQUEST).send();
				return;
			}
			
			if (wildcard)
			{
				for (Binding binding : bindings)
				{
					if (callId.equals(binding.getCallId()) && cseq < binding.getCSeq())
					{
						register.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
						return;
					}
					_locationService.removeBinding(binding);
				}
			}
			else
			{
				for (Address contact : contacts)
				{
					int expires = -1;
					expires = contact.getExpires();
					if (expires < 0)
						expires = register.getExpires();
					
					if (expires != 0)
					{
						if (expires < 0)
							expires = _defaultExpires;
						if (expires > _maxExpires)
							expires = _maxExpires;
						if (expires < _minExpires)
						{
							SipServletResponse response = register.createResponse(SipServletResponse.SC_INTERVAL_TOO_BRIEF);
							response.addHeader(Constants.MIN_EXPIRES, Integer.toString(_minExpires));
							response.send();
							return;
						}
					}
					boolean exist = false;
					
					for (Binding binding : bindings)
					{
						if (contact.getURI().equals(binding.getContact()))
						{
							exist = true;
							if (callId.equals(binding.getCallId()) && cseq < binding.getCSeq())
							{
								register.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
								return;
							}
							if (expires == 0)
							{
								_locationService.removeBinding(binding);
							}
							else
							{
								// update
							}
						}
					}
					if (!exist && expires != 0)
					{
						Binding binding = new Binding(aor, contact.getURI());
						// create
						_locationService.addBinding(binding);
					}
				}
			}
			bindings = _locationService.getBindings(aor);
		}
		SipServletResponse ok = register.createResponse(SipServletResponse.SC_OK);
		ok.addHeader(Constants.DATE, _dateFormat.format(new Date()));
		if (bindings != null)
		{
			for (Binding binding : bindings)
			{
				Address address = _sipFactory.createAddress(binding.getContact());
				//address.setExpires(binding.getExpires());
				address.setExpires(60);
				
				ok.addAddressHeader(Constants.CONTACT, address,false);
			}
		}
		ok.send();
	}
}
