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

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.cipango.annotations.resources.AnnotedServlet;
import org.cipango.plus.webapp.Configuration;
import org.cipango.servlet.SipServletHandler;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.cipango.sipapp.SipXmlProcessor;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.DeclareRolesAnnotationHandler;
import org.eclipse.jetty.annotations.PostConstructAnnotationHandler;
import org.eclipse.jetty.annotations.PreDestroyAnnotationHandler;
import org.eclipse.jetty.annotations.ResourcesAnnotationHandler;
import org.eclipse.jetty.annotations.RunAsAnnotationHandler;
import org.eclipse.jetty.plus.annotation.Injection;
import org.eclipse.jetty.plus.annotation.InjectionCollection;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;

public class AnnotedServletTest extends TestCase
{
	private SipAppContext _sac;
	private AnnotationParser _parser;
	private SipXmlProcessor _processor;
	private ResourceAnnotationHandler _resourceAnnotationHandler;
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_sac = new SipAppContext();
		_sac.setSecurityHandler(new ConstraintSecurityHandler());
		_sac.setServletHandler(new org.cipango.plus.servlet.SipServletHandler());
		_processor = new SipXmlProcessor(_sac);
		_sac.setAttribute(SipXmlProcessor.SIP_PROCESSOR, _processor);
		
		Configuration plusConfig = new Configuration();
		plusConfig.preConfigure(_sac);
		_parser = new AnnotationParser();
		_resourceAnnotationHandler = new ResourceAnnotationHandler(_sac);
		_parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipApplication", new SipApplicationAnnotationHandler(_sac));
		_parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipApplicationKey", new SipApplicationKeyAnnotationHandler(_sac));
		_parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipListener", new SipListenerAnnotationHandler(_sac, _processor));
		_parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipServlet", new SipServletAnnotationHandler(_sac));
        
		_parser.registerAnnotationHandler("javax.annotation.Resource", _resourceAnnotationHandler);
		_parser.registerAnnotationHandler("javax.annotation.Resources", new ResourcesAnnotationHandler (_sac));
		_parser.registerAnnotationHandler("javax.annotation.PostConstruct", new PostConstructAnnotationHandler(_sac));
		_parser.registerAnnotationHandler("javax.annotation.PreDestroy", new PreDestroyAnnotationHandler(_sac));
		_parser.registerAnnotationHandler("javax.annotation.security.RunAs", new RunAsAnnotationHandler(_sac));
		_parser.registerAnnotationHandler("javax.annotation.security.DeclareRoles", new DeclareRolesAnnotationHandler(_sac));
	}
	
	public void testAnnotedServlet() throws Exception
	{		
		Resource r = new FileResource(AnnotedServletTest.class.getResource("resources"));
        _parser.parse(r , new SimpleResolver());
        _resourceAnnotationHandler.normalizeSipInjections();
        
        assertEquals("org.cipango.kaleo", _sac.getName());
        assertEquals("Kaleo", _sac.getDisplayName());
        
        SipServletHandler handler = (SipServletHandler) _sac.getServletHandler();
        SipServletHolder[] holders = handler.getSipServlets();
        assertEquals(1, holders.length);
        assertEquals("AnnotedServlet", holders[0].getName());
        assertFalse(holders[0].isInitOnStartup());
        
        assertEquals(holders[0], handler.getMainServlet());
        
        InjectionCollection injectionCollection = (InjectionCollection) _sac.getAttribute(InjectionCollection.INJECTION_COLLECTION);
        List<Injection> injections = injectionCollection.getInjections(AnnotedServlet.class.getName());
		assertEquals(3, injections.size());
		Iterator<Injection> it  = injections.iterator();
		while (it.hasNext())
		{
			Injection injection = it.next();
			String name = injection.getFieldName();
			if (name.equals("sipFactory"))
				assertEquals("sip/org.cipango.kaleo/SipFactory", injection.getJndiName());
			else if (name.equals("timerService"))
				assertEquals("sip/org.cipango.kaleo/TimerService", injection.getJndiName());
			else if (name.equals("sessionsUtil"))
				assertEquals("sip/org.cipango.kaleo/SipSessionsUtil", injection.getJndiName());
			else
				fail("Unexpected name: " + name);
		}
        
        
        
	}

	
	
}
