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

package org.cipango.diameter.app;

import java.util.EventListener;

import org.cipango.diameter.DiameterFactory;
import org.cipango.diameter.Node;
import org.cipango.diameter.util.DiameterHolder;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.xml.XmlParser;

public class DiameterConfiguration implements Configuration
{
	private WebAppContext _context;
	private XmlParser _xmlParser;
	
	public DiameterConfiguration()
	{
		_xmlParser = diameterXmlParser();
	}
	
	public static XmlParser diameterXmlParser()
	{
		return new XmlParser();
	}
	
	public void configureClassLoader() throws Exception { }

	public void configureDefaults() throws Exception 
	{
	}

	public void configureWebApp() throws Exception 
	{
		if (_context.isStarted())
        {
           	Log.debug("Cannot configure webapp after it is started");
            return;
        } 
		EventListener[] listeners = getWebAppContext().getEventListeners();
		if (listeners == null)
			return;
		
		DiameterListener diameterListener = null;
		
		for (int i = 0; i < listeners.length; i++)
		{
			EventListener listener = listeners[i];
			if (listener instanceof DiameterListener)
				diameterListener = (DiameterListener) listener;
		}
		
		Log.debug("Using " + diameterListener + " as diameter listener");
		
		DiameterFactory factory = new DiameterFactory();
		Node node = (Node) getWebAppContext().getServer().getAttribute(Node.class.getName());
		factory.setNode(node);
		
		getWebAppContext().getServletContext().setAttribute(DiameterFactory.class.getName(), factory);
		
		node.setHandler(new DiameterHolder(diameterListener, getWebAppContext().getClassLoader()));
	}
	
	public void deconfigureWebApp() throws Exception 
	{
	}

	public WebAppContext getWebAppContext() 
	{
		return _context;
	}

	public void setWebAppContext(WebAppContext context) 
	{
		_context = context;
	}
}
