package org.cipango.kaleo.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cipango.kaleo.event.Resource;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.location.Binding;
import org.cipango.kaleo.location.LocationService;
import org.cipango.kaleo.presence.PresenceEventPackage;
import org.cipango.kaleo.presence.Presentity;
import org.cipango.kaleo.presence.pidf.PresenceDocument;
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
		JSON.getDefault().addConvertor(Binding.class, new Convertor()
		{
			public void toJSON(Object obj, Output out) 
			{
				Binding binding = (Binding) obj;
				out.add("aor", binding.getAOR());
				out.add("contact", binding.getContact());
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
		
		List<Presentity> presentities = new ArrayList<Presentity>();
		
		Iterator<String> it = _presence.getResourceUris();
		while (it.hasNext())
		{
			presentities.add(_presence.getResource(it.next()));
		}
		
		
		String json = JSON.getDefault().toJSON(presentities);
		
		response.getOutputStream().println(json);
	}
}
