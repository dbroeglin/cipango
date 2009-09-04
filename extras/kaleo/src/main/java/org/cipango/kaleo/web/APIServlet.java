package org.cipango.kaleo.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cipango.kaleo.Resource;
import org.cipango.kaleo.location.Binding;
import org.cipango.kaleo.location.LocationService;
import org.cipango.kaleo.location.Registration;
import org.cipango.kaleo.presence.PresenceEventPackage;
import org.cipango.kaleo.presence.Presentity;
import org.mortbay.util.ajax.JSON;
import org.mortbay.util.ajax.JSON.Convertor;
import org.mortbay.util.ajax.JSON.Output;

public class APIServlet extends HttpServlet
{
	private PresenceEventPackage _presence;
	private LocationService _locationService;
	
	public void init()
	{
		_presence = (PresenceEventPackage) getServletContext().getAttribute(PresenceEventPackage.class.getName());
		_locationService = (LocationService) getServletContext().getAttribute(LocationService.class.getName());
		JSON.getDefault().addConvertor(Resource.class, new Convertor()
		{
			public void toJSON(Object obj, Output out) 
			{
				Resource resource = (Resource) obj;
				out.add("uri", resource.getUri());
			}
			public Object fromJSON(Map object)  { return null; }
		});
		JSON.getDefault().addConvertor(Registration.class, new Convertor()
		{
			public void toJSON(Object obj, Output out) 
			{
				Registration record = (Registration) obj;
				out.add("aor", record.getUri());
				out.add("bindings", record.getBindings());
			}
			public Object fromJSON(Map object)  { return null; }
		});
		JSON.getDefault().addConvertor(Binding.class, new Convertor()
		{
			public void toJSON(Object obj, Output out) 
			{
				Binding binding = (Binding) obj;
				out.add("contact", binding.getContact());
				out.add("expiration", new Date(binding.getExpirationTime()));
			}
			public Object fromJSON(Map object)  { return null; }
		});
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String pathInfo = request.getPathInfo();
		
		if (pathInfo == null)
			pathInfo = "";
		
		if (pathInfo.startsWith("/"))
			pathInfo = pathInfo.substring(1);
		
		String[] path = pathInfo.split("/");
		
		if ("registrations".equals(path[0]))
		{
			List<Registration> records = _locationService.getResources();
			response.getOutputStream().println(JSON.getDefault().toJSON(records));
		}
		else if ("presentities".equals(path[0]))
		{
			List<Presentity> presentities = _presence.getResources(); 
			String json = JSON.getDefault().toJSON(presentities);
			
			response.getOutputStream().println(json);
		}
	}
}
