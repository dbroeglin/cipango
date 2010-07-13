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

import java.util.ArrayList;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipApplicationKey;

import junit.framework.TestCase;

import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.ClassNameResolver;

public class SipApplicationKeyAnnotationHandlerTest extends TestCase
{
	private SipAppContext _sac;
	private AnnotationParser _parser;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_sac = new SipAppContext();
		_parser = new AnnotationParser();
        _parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipApplicationKey",
        		new SipApplicationKeyAnnotationHandler(_sac));
	}
	
	public void testApplicationKey() throws Exception
	{	
        _parser.parse(GoodApplicationKey.class.getName(), new SimpleResolver());
        assertNotNull(_sac.getSipApplicationKeyMethod());
	}

	public void testNotPublic() throws Exception
	{	
        try { _parser.parse(BadApplicationKey.class.getName(), new SimpleResolver()); fail();} catch (IllegalStateException e) {}
	}
	
	public void testBadReturnType() throws Exception
	{	
		 try { _parser.parse(BadApplicationKey2.class.getName(), new SimpleResolver()); fail();} catch (IllegalStateException e) {}
	}
	
	public void testBadArgument() throws Exception
	{	
		 try { _parser.parse(BadApplicationKey3.class.getName(), new SimpleResolver()); fail();} catch (IllegalStateException e) {}
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
