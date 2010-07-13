// ========================================================================
// $Id: Configuration.java 1598 2007-02-15 05:29:05Z janb $
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.cipango.plus.sipapp;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.cipango.plus.servlet.SipServletHandler;
import org.cipango.sipapp.SipAppContext;
import org.cipango.sipapp.SipXmlProcessor;
import org.cipango.sipapp.SipXmlProcessor.Descriptor;
import org.eclipse.jetty.plus.annotation.InjectionCollection;
import org.eclipse.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.eclipse.jetty.plus.servlet.ServletHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Configuration
 *
 *
 */
public class Configuration extends org.cipango.plus.webapp.Configuration
{
    public static final String JNDI_SIP_PREFIX = "sip/";
    public static final String JNDI_SIP_FACTORY_POSTFIX = "/SipFactory";
    public static final String JNDI_TIMER_SERVICE_POSTFIX = "/TimerService";
    public static final String JNDI_SIP_SESSIONS_UTIL_POSTFIX = "/SipSessionsUtil";
    
    @Override
    /**
     * Same as super.configure() but process sipXml instead of webXml
     */
    public void configure (WebAppContext context)
    throws Exception
    {
        bindUserTransaction(context);
        
        SipXmlProcessor sipXmlProcessor = (SipXmlProcessor)context.getAttribute(SipXmlProcessor.SIP_PROCESSOR); 
        if (sipXmlProcessor == null)
           throw new IllegalStateException ("No processor for sip xml");

        //TODO: When webdefaults.xml, web.xml, fragments and web-override.xml are merged into an effective web.xml this 
        //will change
        PlusSipXmlProcessor plusProcessor = new PlusSipXmlProcessor(context);
        plusProcessor.process(sipXmlProcessor.getSipDefaults());
        plusProcessor.process(sipXmlProcessor.getSipXml());

        //process the override-web.xml descriptor
        plusProcessor.process(sipXmlProcessor.getSipOverride());
             
        //configure injections and callbacks to be called by the FilterHolder and ServletHolder
        //when they lazily instantiate the Filter/Servlet.
        ((SipServletHandler)context.getServletHandler()).setInjections((InjectionCollection)context.getAttribute(InjectionCollection.INJECTION_COLLECTION));
        ((SipServletHandler)context.getServletHandler()).setCallbacks((LifeCycleCallbackCollection)context.getAttribute(LifeCycleCallbackCollection.LIFECYCLE_CALLBACK_COLLECTION));
        
        //do any injects on the listeners that were created and then
        //also callback any postConstruct lifecycle methods
        injectAndCallPostConstructCallbacks(context);
        
        bindSipResources((SipAppContext) context);
    }
	
	public void bindSipResources(SipAppContext appContext) throws Exception
	{
		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appContext.getClassLoader());
        Context context = new InitialContext();
        Context compCtx = (Context) context.lookup("java:comp/env");
        String name = appContext.getName();
        compCtx.createSubcontext("sip").createSubcontext(name);
        compCtx.bind(JNDI_SIP_PREFIX + name + JNDI_SIP_FACTORY_POSTFIX, appContext.getSipFactory());
        compCtx.bind(JNDI_SIP_PREFIX + name + JNDI_TIMER_SERVICE_POSTFIX, appContext.getTimerService());
        compCtx.bind(JNDI_SIP_PREFIX + name + JNDI_SIP_SESSIONS_UTIL_POSTFIX, appContext.getSipSessionsUtil());
        Log.debug("Bind SIP Resources on app " + name);
        Thread.currentThread().setContextClassLoader(oldLoader);
	}
	
	public class PlusSipXmlProcessor extends PlusWebXmlProcessor
	{

		public PlusSipXmlProcessor(WebAppContext context)
		{
			super(context);
		}
		
		public void process (Descriptor d)
        throws Exception
        {
            if (d != null)
                process(d.getRoot());
        }
		
	}
}
