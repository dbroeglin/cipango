package org.cipango.jboss;

import java.util.Iterator;

import org.cipango.sipapp.SipXmlConfiguration;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;
import org.mortbay.xml.XmlParser;

public class JBossSipXmlConfiguration extends SipXmlConfiguration
{

	protected void initServlet(XmlParser.Node node)
	{	
		String servletName = node.getString("servlet-name", false, true);
		String servletClass = node.getString("servlet-class", false, true);
		
		ServletHolder holder = new ServletHolder();
		holder.setName(servletName);
		holder.setClassName(servletClass);
		
		Iterator params = node.iterator("init-param");
		
		while (params.hasNext()) 
		{
			XmlParser.Node param = (XmlParser.Node) params.next();
			String pName = param.getString("param-name", false, true);
			String pValue = param.getString("param-value", false, true);
			holder.setInitParameter(pName, pValue);
		}
		
		XmlParser.Node startup = node.get("load-on-startup");
		if (startup != null) 
		{
			String s = startup.toString(false, true);
			int order = 0; 
			if (s != null && s.trim().length() > 0) 
			{
				try 
				{
					order = Integer.parseInt(s);
				} 
				catch (NumberFormatException e) 
				{
					Log.warn("Cannot parse load-on-startup " + s);
				}
			}
			holder.setInitOrder(order);
		}
		_servlets = LazyList.add(_servlets, holder);
	}
}
