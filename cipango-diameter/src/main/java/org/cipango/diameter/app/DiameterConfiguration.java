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

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.cipango.diameter.DiameterFactory;
import org.cipango.diameter.DiameterFactoryImpl;
import org.cipango.diameter.Node;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.Configuration;

public class DiameterConfiguration implements Configuration
{
	public void preConfigure(org.eclipse.jetty.webapp.WebAppContext arg0) throws Exception
	{
	}


	public void configure(org.eclipse.jetty.webapp.WebAppContext context) throws Exception
	{
		if (context.isStarted())
        {
           	Log.debug("Cannot configure webapp after it is started");
            return;
        } 
		EventListener[] listeners = context.getEventListeners();
		if (listeners == null)
			return;
		
		List<DiameterListener> diameterListeners = new ArrayList<DiameterListener>();
		
		for (int i = 0; i < listeners.length; i++)
		{
			EventListener listener = listeners[i];
			if (listener instanceof DiameterListener)
				diameterListeners.add((DiameterListener) listener);
		}
		
		Log.debug("Using " + diameterListeners + " as diameter listeners");
		
		DiameterFactoryImpl factory = new DiameterFactoryImpl();
		Node node = (Node) context.getServer().getAttribute(Node.class.getName());
		factory.setNode(node);
		
		context.getServletContext().setAttribute(DiameterFactory.class.getName(), factory);
		
		if (!diameterListeners.isEmpty())
			((DiameterContext) node.getHandler()).addListeners(diameterListeners.toArray(new DiameterListener[0]), context);
		
	}
	

	public void postConfigure(org.eclipse.jetty.webapp.WebAppContext context) throws Exception
	{
		
	}

	public void deconfigure(org.eclipse.jetty.webapp.WebAppContext context) throws Exception
	{
		Node node = (Node) context.getServer().getAttribute(Node.class.getName());
		((DiameterContext) node.getHandler()).removeListeners(context);
	}


	
}
