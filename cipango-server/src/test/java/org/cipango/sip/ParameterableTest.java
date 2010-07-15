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
package org.cipango.sip;

import org.cipango.sip.ParameterableImpl;

import junit.framework.TestCase;

public class ParameterableTest extends TestCase
{
	public void testParse() throws Exception
	{
		String s = "<http://wwww.example.com/alice/photo.jpg> ;purpose=icon";
		ParameterableImpl p = new ParameterableImpl(s);
		
		assertEquals("<http://wwww.example.com/alice/photo.jpg>", p.getValue());
		assertEquals("icon", p.getParameter("PURPOSE"));
		
		s = "text/html    ; charset  =  ISO-8859-4";
		p = new ParameterableImpl(s);
		
		assertEquals("text/html", p.getValue());
		assertEquals("ISO-8859-4", p.getParameter("charset"));
		assertEquals("text/html;charset=ISO-8859-4", p.toString());
		
		s = "message/external-body; access-type=\"URL\";expiration=\"Tue, 24 July 2003 09:00:00 GMT\";URL=\"http://app.example.net/calingcard.xml\"";
		p = new ParameterableImpl(s);
		
		assertEquals("URL", p.getParameter("access-type"));
		assertEquals("http://app.example.net/calingcard.xml", p.getParameter("URL"));
	}
	
	public void testString() throws Exception
	{
		String s = "message/external-body; access-type=\"URL\";expiration=\"Tue, 24 July 2003 09:00:00 GMT\";URL=\"http://app.example.net/calingcard.xml\"";
		ParameterableImpl p = new ParameterableImpl(s);

		String s2 = p.toString();
		p = new ParameterableImpl(s2);
		
		String s3 = p.toString();
		
		assertEquals(s2, s3);
	}
}
