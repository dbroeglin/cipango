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

import static junit.framework.Assert.assertEquals;

import org.cipango.annotations.resources.AnnotedServlet;
import org.cipango.servlet.SipServletHandler;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.annotations.AbstractDiscoverableAnnotationHandler;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.junit.Before;
import org.junit.Test;

public class SipServletAnnotationHandlerTest
{
	private SipAppContext _sac;
	private AnnotationParser _parser;

	@Before
	public void setUp() throws Exception
	{
		_sac = new SipAppContext();
		_parser = new AnnotationParser();
        _parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipServlet",
        		new SipServletAnnotationHandler(_sac));
	}
	
	@Test
	public void testAnnotedServlet() throws Exception
	{	
        _parser.parse(AnnotedServlet.class.getName(), new SimpleResolver());
        AbstractDiscoverableAnnotationHandler annotHandler = (AbstractDiscoverableAnnotationHandler) _parser.getAnnotationHandlers().get(0);
        _sac.getSipMetaData().addDiscoveredAnnotations(annotHandler.getAnnotationList());    
        _sac.getSipMetaData().resolve(_sac);
        SipServletHandler handler = (SipServletHandler) _sac.getServletHandler();
        SipServletHolder[] holders = handler.getSipServlets();
        assertEquals(1, holders.length);
        assertEquals("AnnotedServlet", holders[0].getName());
        assertEquals(-1, holders[0].getInitOrder());
	}
	
}

