// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.annotations;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.cipango.servlet.SipServletHandler;
import org.cipango.sipapp.SipAppContext;
import org.cipango.sipapp.SipXmlProcessor;
import org.eclipse.jetty.annotations.AbstractConfiguration;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.ClassNameResolver;
import org.eclipse.jetty.annotations.DeclareRolesAnnotationHandler;
import org.eclipse.jetty.annotations.PostConstructAnnotationHandler;
import org.eclipse.jetty.annotations.PreDestroyAnnotationHandler;
import org.eclipse.jetty.annotations.ResourcesAnnotationHandler;
import org.eclipse.jetty.annotations.RunAsAnnotationHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Configuration
 */
public class AnnotationConfiguration extends AbstractConfiguration
{
 
    public void preConfigure(final WebAppContext context) throws Exception
    {
    }
    
    public void configure(WebAppContext context) throws Exception
    {
    	
        
        AnnotationParser parser = new AnnotationParser();

        SipXmlProcessor sipXmlProcessor = (SipXmlProcessor)context.getAttribute(SipXmlProcessor.SIP_PROCESSOR); 
        if (sipXmlProcessor == null)
           throw new IllegalStateException ("No processor for sip xml");
        

        SipAppContext sac = (SipAppContext) context;
        if (sac.getSpecVersion() == SipAppContext.VERSION_10)
        	return;
        
        if (Log.isDebugEnabled()) 
        	Log.debug("parsing annotations");
        
        SipApplicationAnnotationHandler sipApplicationAnnotationHandler = new SipApplicationAnnotationHandler(sac);
        parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipApplication", sipApplicationAnnotationHandler);
        parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipApplicationKey", new SipApplicationKeyAnnotationHandler(sac));
        parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipListener", new SipListenerAnnotationHandler(sac, sipXmlProcessor));
        parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipServlet", new SipServletAnnotationHandler(sac, sipXmlProcessor));
        
        ResourceAnnotationHandler resourceAnnotationHandler = new ResourceAnnotationHandler(sac);
        parser.registerAnnotationHandler("javax.annotation.Resource", resourceAnnotationHandler);
        parser.registerAnnotationHandler("javax.annotation.Resources", new ResourcesAnnotationHandler(context));
        parser.registerAnnotationHandler("javax.annotation.PostConstruct", new PostConstructAnnotationHandler(context));
        parser.registerAnnotationHandler("javax.annotation.PreDestroy", new PreDestroyAnnotationHandler(context));
        parser.registerAnnotationHandler("javax.annotation.security.RunAs", new RunAsAnnotationHandler(context));
        parser.registerAnnotationHandler("javax.annotation.security.DeclareRoles", new DeclareRolesAnnotationHandler(context));
        
        parseContainerPath(context, parser);
        parseWebInfLib (context, parser);
        parseWebInfClasses(context, parser);
       
        resourceAnnotationHandler.normalizeSipInjections();
        sipXmlProcessor.setListeners(context.getEventListeners());
        sipXmlProcessor.initListeners();
        sac.setEventListeners(sipXmlProcessor.getListeners());
        if (sipApplicationAnnotationHandler.getMainServletName() != null)
        	((SipServletHandler) sac.getServletHandler()).setMainServletName(sipApplicationAnnotationHandler.getMainServletName());
    }
    
    public void deconfigure(WebAppContext context) throws Exception
    {
        
    }




    public void postConfigure(WebAppContext context) throws Exception
    {

    }

    @Override
	public void parseWebInfLib(final WebAppContext context, final AnnotationParser parser) throws Exception
	{
		ArrayList<URI> webInfUris = new ArrayList<URI>();
		@SuppressWarnings("unchecked")
		List<Resource> jarResources = (List<Resource>) context.getAttribute(WEB_INF_JAR_RESOURCES);

		for (Resource r : jarResources)
			webInfUris.add(r.getURI());

		parser.parse(webInfUris.toArray(new URI[webInfUris.size()]), new ClassNameResolver()
		{
			public boolean isExcluded(String name)
			{
				if (context.isSystemClass(name))
					return true;
				if (context.isServerClass(name))
					return false;
				return false;
			}

			public boolean shouldOverride(String name)
			{
				// looking at webapp classpath, found already-parsed class of
				// same name - did it come from system or duplicate in webapp?
				if (context.isParentLoaderPriority())
					return false;
				return true;
			}
		});
	}
}
