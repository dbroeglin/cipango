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

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipApplicationKey;

import junit.framework.TestCase;

import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.ClassNameResolver;
import org.eclipse.jetty.webapp.DiscoveredAnnotation;

public class SipApplicationKeyAnnotationHandlerTest extends TestCase
{
	private SipAppContext _context;
	private AnnotationParser _parser;
	private SipApplicationKeyAnnotationHandler _handler;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_context = new SipAppContext();
		_parser = new AnnotationParser();
		_handler = new SipApplicationKeyAnnotationHandler(_context);
        _parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipApplicationKey",
        		_handler);
	}
	
	public void testApplicationKey() throws Exception
	{	
       parse(GoodApplicationKey.class);
       assertNotNull(_context.getSipApplicationKeyMethod());
	}
	
	@SuppressWarnings("rawtypes")
	private void parse(Class clazz) throws Exception
	{
		 _parser.parse(clazz.getName(), new SimpleResolver());
		 for (DiscoveredAnnotation annotation : _handler.getAnnotationList())
			 annotation.apply();
	}

	public void testNotPublic() throws Exception
	{	
        try { parse(BadApplicationKey.class); fail();} catch (IllegalStateException e) {}
	}
	
	public void testBadReturnType() throws Exception
	{	
		 try { parse(BadApplicationKey2.class); fail();} catch (IllegalStateException e) {}
	}
	
	public void testBadArgument() throws Exception
	{	
		 try { parse(BadApplicationKey3.class); fail();} catch (IllegalStateException e) {}
	}
	

}

class SimpleResolver implements ClassNameResolver
{
	public boolean isExcluded(String name)
    {
        return false;
    }

    public boolean shouldOverride(String name)
    {
        return false;
    }
}


class GoodApplicationKey
{
	@SipApplicationKey
	public static String getSessionKey(SipServletRequest request)
	{
		return request.getCallId();
	}
}

class BadApplicationKey
{
	@SipApplicationKey
	protected static String getSessionKey(SipServletRequest request)
	{
		return request.getCallId();
	}
}

class BadApplicationKey2
{
	@SipApplicationKey
	public static Object getSessionKey(SipServletRequest request)
	{
		return request.getCallId();
	}
}

class BadApplicationKey3
{
	@SipApplicationKey
	public static String getSessionKey(SipServletResponse response)
	{
		return response.getCallId();
	}
}
