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

import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.cipango.annotations.resources.AnnotedServlet;
import org.cipango.servlet.SipServletHandler;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.plus.annotation.Injection;
import org.eclipse.jetty.plus.annotation.InjectionCollection;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class AnnotedServletTest extends TestCase
{
	private SipAppContext _context;
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_context = new SipAppContext();
		_context.setSecurityHandler(new ConstraintSecurityHandler());
	}
	
	public void testAnnotedServlet() throws Exception
	{		
        AnnotationConfiguration annotConfiguration = new AnnotationConfiguration()
        {
        	@Override
    		public void parseContainerPath(WebAppContext arg0, AnnotationParser arg1) throws Exception
    		{

    		}

    		@Override
    		public void parseWebInfClasses(WebAppContext context, AnnotationParser parser) throws Exception
    		{
    			Resource r = new FileResource(AnnotedServletTest.class.getResource("resources"));
    	        parser.parse(r , new SimpleResolver());
    		}

    		@Override
    		public void parseWebInfLib(WebAppContext arg0, AnnotationParser arg1) throws Exception
    		{
    		}
        };
        annotConfiguration.preConfigure(_context);
        annotConfiguration.configure(_context);
        _context.getSipMetaData().resolve(_context);
        
        assertEquals("org.cipango.kaleo", _context.getName());
        assertEquals("Kaleo", _context.getDisplayName());
        
        SipServletHandler handler = (SipServletHandler) _context.getServletHandler();
        SipServletHolder[] holders = handler.getSipServlets();
        assertEquals(1, holders.length);
        assertEquals("AnnotedServlet", holders[0].getName());
        assertEquals(-1, holders[0].getInitOrder());
        
        assertEquals(holders[0], handler.getMainServlet());
        
        InjectionCollection injectionCollection = (InjectionCollection) _context.getAttribute(InjectionCollection.INJECTION_COLLECTION);
        List<Injection> injections = injectionCollection.getInjections(AnnotedServlet.class.getName());
		// Value is set to 9 due to jetty issue at https://bugs.eclipse.org/bugs/show_bug.cgi?id=332796 assertEquals(3, injections.size());
		Iterator<Injection> it  = injections.iterator();
		while (it.hasNext())
		{
			Injection injection = it.next();
			String name = injection.getTarget().getName();
			if (name.equals("sipFactory"))
				assertEquals("sip/org.cipango.kaleo/SipFactory", injection.getJndiName());
			else if (name.equals("timerService"))
				assertEquals("sip/org.cipango.kaleo/TimerService", injection.getJndiName());
			else if (name.equals("sessionsUtil"))
				assertEquals("sip/org.cipango.kaleo/SipSessionsUtil", injection.getJndiName());
			else
				fail("Unexpected name: " + name);
		}
        
        EventListener[] listeners = _context.getEventListeners();
        assertEquals(1, listeners.length);
        assertEquals(AnnotedServlet.class, listeners[0].getClass());
	}

}
