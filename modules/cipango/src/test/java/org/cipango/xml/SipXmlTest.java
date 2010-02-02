// ========================================================================
// Copyright 2007-2008 NEXCOM Systems
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

package org.cipango.xml;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.cipango.servlet.SipServletHandler;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.cipango.sipapp.SipServletMapping;
import org.cipango.sipapp.SipXmlConfiguration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.xml.XmlParser;
import org.xml.sax.SAXParseException;

public class SipXmlTest extends TestCase
{
	XmlParser getParser(boolean validating)
	{
		String value = (validating ? "true" : "false");
		System.setProperty("org.mortbay.xml.XmlParser.Validating", value);
		
		return SipXmlConfiguration.sipXmlParser();
	}
	
	public void testXml() throws Exception
	{
		XmlParser parser = getParser(true);
		try
		{
			parser.parse(getClass().getResource("/org/cipango/xml/sip.xml").toString());
			fail("expected SAXParseException");
		}
		catch (SAXParseException e)
		{
		}
		
		parser = getParser(false);
		parser.parse(getClass().getResource("/org/cipango/xml/sip.xml").toString());
	}
	
	public void testXmlDtd() throws Exception
	{
		XmlParser parser = getParser(true);
		parser.parse(getClass().getResource("/org/cipango/xml/sip-dtd.xml").toString());
	}
	
	public void testSipXml() throws Exception
	{
		System.setProperty("org.mortbay.xml.XmlParser.Validating", "false");
		
		SipXmlConfiguration conf = new SipXmlConfiguration();
		conf.setWebAppContext(new SipAppContext());
		conf.configure(getClass().getResource("/org/cipango/xml/sip-xsd.xml").toString());
	}
	
	@SuppressWarnings("unchecked")
	public void testSipXml10() throws Exception
	{
		System.setProperty("org.mortbay.xml.XmlParser.Validating", "false");
		
		SipAppContext context = new SipAppContext();
		SipXmlConfiguration conf = new SipXmlConfiguration();
		conf.setWebAppContext(context);
		conf.configure(getClass().getResource("/org/cipango/xml/sip-sample-1.0.xml").toString());

		SipServletHandler servletHandler = (SipServletHandler) context.getServletHandler();
		
		assertEquals(SipAppContext.VERSION_10, context.getSpecVersion());
		assertEquals("SIP Servlet based Registrar", context.getDisplayName());
		
		Enumeration<String> e = context.getInitParameterNames();
		String name = (String) e.nextElement();
		assertEquals("contextConfigLocation", name);
		assertEquals("/WEB-INF/kaleo.xml", context.getInitParameter(name));
		assertFalse(e.hasMoreElements());	
		
		assertEquals(TestListener.class, context.getTimerListeners()[0].getClass());
	
		// servlets
		SipServletHolder[] holders = servletHandler.getSipServlets();
		assertEquals(2, holders.length);
		
		assertEquals("main", holders[0].getName());
		// TODO assertEquals("PBX Servlet", holders[0].getDisplayName());
		assertEquals("org.cipango.kaleo.PbxServlet", holders[0].getClassName());
		e = holders[0].getInitParameterNames();
		name = (String) e.nextElement();
		assertEquals("value", holders[0].getInitParameter(name));
		assertFalse(e.hasMoreElements());	
		assertEquals(10, holders[0].getInitOrder());
		
		assertEquals("presence", holders[1].getName());
		assertEquals("org.cipango.kaleo.presence.PresenceServlet", holders[1].getClassName());
		assertFalse(holders[1].getInitParameterNames().hasMoreElements());	
		assertEquals(0, holders[1].getInitOrder());
		
		// servlet-mapping
		SipServletMapping[] mappings = servletHandler.getSipServletMappings();
		assertEquals(1, mappings.length);
		assertEquals("main", mappings[0].getServletName());
		assertEquals("((request.method == REGISTER) or (request.method == PUBLISH) or (request.method == SUBSCRIBE) or (request.method == INVITE))", 
				mappings[0].getMatchingRuleExpression());
		assertEquals(60, context.getSessionTimeout());
		
		assertNull(servletHandler.getMainServlet());
	}
	
	@SuppressWarnings("unchecked")
	public void testSipXml11() throws Exception
	{
		System.setProperty("org.mortbay.xml.XmlParser.Validating", "false");
		SipAppContext context = new SipAppContext();
		SipXmlConfiguration conf = new SipXmlConfiguration();
		conf.setWebAppContext(context);
		conf.configure(getClass().getResource("/org/cipango/xml/sip-sample-1.1.xml").toString());
		
		assertEquals(SipAppContext.VERSION_11, context.getSpecVersion());
		assertEquals("SIP Servlet based Registrar", context.getDisplayName());
		
		Enumeration<String> e = context.getInitParameterNames();
		String name = (String) e.nextElement();
		assertEquals("contextConfigLocation", name);
		assertEquals("/WEB-INF/kaleo.xml", context.getInitParameter(name));
		assertFalse(e.hasMoreElements());	
		
		assertEquals(TestListener.class, context.getTimerListeners()[0].getClass());
	
		// servlets
		SipServletHandler servletHandler = (SipServletHandler) context.getServletHandler();
		SipServletHolder[] holders = servletHandler.getSipServlets();
		assertEquals(2, holders.length);
		
		assertEquals("main", holders[0].getName());
		// TODO assertEquals("PBX Servlet", holders[0].getDisplayName());
		assertEquals("org.cipango.kaleo.PbxServlet", holders[0].getClassName());
		e = holders[0].getInitParameterNames();
		name = (String) e.nextElement();
		assertEquals("value", holders[0].getInitParameter(name));
		assertFalse(e.hasMoreElements());	
		assertEquals(10, holders[0].getInitOrder());
		
		assertEquals("presence", holders[1].getName());
		assertEquals("org.cipango.kaleo.presence.PresenceServlet", holders[1].getClassName());
		assertFalse(holders[1].getInitParameterNames().hasMoreElements());	
		assertEquals(0, holders[1].getInitOrder());
		
		// servlet-mapping
		SipServletMapping[] mappings = servletHandler.getSipServletMappings();
		assertEquals(0, mappings.length);
		assertEquals(60, context.getSessionTimeout());
		
		assertNotNull(servletHandler.getMainServlet());
		assertEquals("main", servletHandler.getMainServlet().getName());
		assertEquals("org.cipango.kaleo", context.getName());
	}
	
	public void testMappings11() throws Exception 
	{
		System.setProperty("org.mortbay.xml.XmlParser.Validating", "false");

		SipAppContext context = new SipAppContext();
		SipXmlConfiguration conf = new SipXmlConfiguration();
		conf.setWebAppContext(context);
		conf.configure(getClass().getResource("/org/cipango/xml/sip-mappings-1.1.xml").toString());
		
		assertEquals(SipAppContext.VERSION_11, context.getSpecVersion());
		
		SipServletHandler servletHandler = (SipServletHandler) context.getServletHandler();
		SipServletMapping[] mappings = servletHandler.getSipServletMappings();
		assertEquals(2, mappings.length);
	}
	
	public void testNamespace() throws Exception
	{
		System.setProperty("org.mortbay.xml.XmlParser.Validating", "false");
		
		SipAppContext context = new SipAppContext();
		SipXmlConfiguration conf = new SipXmlConfiguration();
		conf.setWebAppContext(context);
		
		conf.configure(getClass().getResource("/org/cipango/xml/sip-namespace.xml").toString());
		
		assertEquals(SipAppContext.VERSION_11, context.getSpecVersion());
		
		Enumeration<String> e = context.getInitParameterNames();
		String name = (String) e.nextElement();
		assertNotNull(name);
		assertEquals("contextConfigLocation", name);
		assertEquals("/WEB-INF/kaleo.xml", context.getInitParameter(name));
		assertFalse(e.hasMoreElements());	
		
		// servlets
		SipServletHandler servletHandler = (SipServletHandler) context.getServletHandler();
		SipServletHolder[] holders = servletHandler.getSipServlets();
		assertEquals(1, holders.length);
		
		assertEquals("main", holders[0].getName());
		// TODO assertEquals("PBX Servlet", holders[0].getDisplayName());
		assertEquals("org.cipango.kaleo.PbxServlet", holders[0].getClassName());
		e = holders[0].getInitParameterNames();
		name = (String) e.nextElement();
		assertEquals("value", holders[0].getInitParameter(name));
		assertFalse(e.hasMoreElements());			
		
		// servlet-mapping
		SipServletMapping[] mappings = servletHandler.getSipServletMappings();
		assertEquals(0, mappings.length);
		
		assertEquals("main", servletHandler.getMainServlet().getName());
		
		assertEquals("org.cipango.kaleo", context.getName());	
	}
	
	public void testValidateSip() throws Exception
	{
		System.setProperty("org.mortbay.xml.XmlParser.Validating", "true");
		
		SipAppContext context = new SipAppContext();
		SipXmlConfiguration conf = new SipXmlConfiguration();
		conf.setWebAppContext(context);
		
		conf.configure(getClass().getResource("/org/cipango/xml/sip-validated-1.1.xml").toString());
	}
	
	public void testXmlXsd() throws Exception
	{
		System.out.println(WebAppContext.class.getResource("/javax/servlet/resources/javaee_5.xsd"));
		XmlParser parser = getParser(true);
		parser.parse(getClass().getResource("/org/cipango/xml/sip-xsd.xml").toString());
	}
	
	public void testWeb() throws Exception 
	{
		XmlParser parser = getParser(true);
		parser.parse(getClass().getResource("/org/cipango/xml/web.xml").toString());
	}
	/**/
}
