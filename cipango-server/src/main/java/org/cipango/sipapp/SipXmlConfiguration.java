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

package org.cipango.sipapp;

import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;

public class SipXmlConfiguration implements Configuration 
{	
	public void preConfigure(WebAppContext context) throws Exception
	{
		if (!(context instanceof SipAppContext))
			throw new IllegalArgumentException("!sip context: " + context.getClass());
		
		SipAppContext sipContext = (SipAppContext) context;
		
		if (sipContext.isStarted())
		{
			if (Log.isDebugEnabled()) 
				Log.debug("Cannot configure sipapp after it is started");
			return;
		}
		
		SipXmlProcessor processor = (SipXmlProcessor) sipContext.getAttribute(SipXmlProcessor.SIP_PROCESSOR);
		if (processor == null)
		{
			processor = new SipXmlProcessor(sipContext);
			sipContext.setAttribute(SipXmlProcessor.SIP_PROCESSOR, processor);
		}
		
		String defaultsSipDescriptor = sipContext.getDefaultsSipDescriptor();
		if (defaultsSipDescriptor != null && defaultsSipDescriptor.length() > 0)
		{
			Resource dftSipResource = Resource.newSystemResource(defaultsSipDescriptor);
			if (dftSipResource == null)
				dftSipResource = context.newResource(defaultsSipDescriptor);
			processor.parseDefaults(dftSipResource);
			processor.processDefaults();
		}
		Resource sipXml = findSipXml(context);
		if (sipXml != null)
			processor.parseSipXml(sipXml);
	}
	
	public void configure(WebAppContext context) throws Exception
	{
		if (!(context instanceof SipAppContext))
			throw new IllegalArgumentException("! sip context");
		
		SipAppContext sipContext = (SipAppContext) context;
		
		if (sipContext.isStarted())
		{
			if (Log.isDebugEnabled())
				Log.debug("Cannot configure sipapp after it is started");
		}
		
		SipXmlProcessor processor = (SipXmlProcessor) sipContext.getAttribute(SipXmlProcessor.SIP_PROCESSOR);
		if (processor == null)
		{
			processor = new SipXmlProcessor(sipContext);
			context.setAttribute(SipXmlProcessor.SIP_PROCESSOR, processor);
		}
		
		processor.processSipXml();
		
		String overrideSipDescriptor = sipContext.getOverrideSipDescriptor();
		if (overrideSipDescriptor != null && overrideSipDescriptor.length() > 0)
		{
			Resource overrideSipResource = Resource.newSystemResource(overrideSipDescriptor);
			if (overrideSipResource == null)
				overrideSipResource = sipContext.newResource(overrideSipDescriptor);
			processor.parseSipOverride(overrideSipResource);
			processor.processSipOverride();
		}
	}
	
	public void deconfigure(WebAppContext context) throws Exception 
	{
		// TODO Auto-generated method stub
		
	}

	public void postConfigure(WebAppContext context) throws Exception 
	{
		// TODO Auto-generated method stub
		
	}
	
	protected Resource findSipXml(WebAppContext context) throws IOException, MalformedURLException 
	{
		// TODO sip descriptor
		Resource webInf = context.getWebInf();
		if (webInf != null && webInf.isDirectory()) 
		{
			Resource sip = webInf.addPath("sip.xml");
			if (sip.exists()) 
				return sip;
			Log.debug("No WEB-INF/sip.xml in " + context.getWar());
		}
		return null;
	}
}
