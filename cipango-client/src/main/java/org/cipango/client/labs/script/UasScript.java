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
package org.cipango.client.labs.script;

import static junit.framework.Assert.assertEquals;

import javax.servlet.sip.SipServletResponse;

import org.cipango.client.labs.SipRequest;
import org.cipango.client.labs.SipSession;


public abstract class UasScript
{
	
	private UasScript()
	{
	}
	
	public static class RingingNotFound extends UaRunnable
	{
		/**
		 * <pre>
		 * Remote                    Sip unit
		 * | INVITE                     |
		 * |--------------------------->| 
		 * |                        180 | 
		 * |<---------------------------|    
		 * |                        404 | 
		 * |<---------------------------|  
		 * | ACK                        |
		 * |--------------------------->|
		 * </pre>
		 */
		public RingingNotFound(SipSession session)
		{
			super(session);
		}

		@Override
		public void doTest() throws Throwable
		{
			SipRequest request = _session.waitRequest();
			assertEquals(SipRequest.INVITE, request.getMethod());
			request.createResponse(SipServletResponse.SC_RINGING).send();
			Thread.sleep(100);
			request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
		}
	};
	
	public static class NotFound extends UaRunnable
	{

		/**
		 * <pre>
		 * Remote                    Sip unit 
		 * | INVITE                     |
		 * |--------------------------->|   
		 * |                        404 | 
		 * |<---------------------------|  
		 * | ACK                        |
		 * |--------------------------->|
		 * </pre>
		 */
		public NotFound(SipSession session)
		{
			super(session);
		}

		@Override
		public void doTest() throws Throwable
		{
			SipRequest request = _session.waitRequest();
			request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
		}
	};
	
	
	public static class RingingOkBye extends UaRunnable
	{
		/**
		 * <pre>
		 * Remote                    Sip unit 
		 * | INVITE                     |
		 * |--------------------------->| 
		 * |                        180 | 
		 * |<---------------------------|    
		 * |                        200 | 
		 * |<---------------------------|  
		 * | ACK                        |
		 * |--------------------------->|
		 * | BYE                        |
		 * |--------------------------->| 
		 * |                        200 | 
		 * |<---------------------------|    
		 * </pre>
		 */
		public RingingOkBye(SipSession session)
		{
			super(session);
		}

		@Override
		public void doTest() throws Throwable
		{
			SipRequest request = _session.waitRequest();
			assertEquals(SipRequest.INVITE, request.getMethod());
			request.createResponse(SipServletResponse.SC_RINGING).send();
			Thread.sleep(100);
			request.createResponse(SipServletResponse.SC_OK).send();

			request = _session.waitRequest();
			assertEquals(SipRequest.ACK, request.getMethod());
	        
			request = _session.waitRequest();
			assertEquals(SipRequest.BYE, request.getMethod());
			request.createResponse(SipServletResponse.SC_OK).send();
		}
	};
	
	public static class OkBye extends UaRunnable
	{

		/**
		 * <pre>
		 * Remote                    Sip unit 
		 * | INVITE                     |
		 * |--------------------------->|     
		 * |                        200 | 
		 * |<---------------------------|  
		 * | ACK                        |
		 * |--------------------------->|
		 * | BYE                        |
		 * |--------------------------->| 
		 * |                        200 | 
		 * |<---------------------------|    
		 * </pre>
		 */
		public OkBye(SipSession session)
		{
			super(session);
		}

		@Override
		public void doTest() throws Throwable
		{
			SipRequest request = _session.waitRequest();
			assertEquals(SipRequest.INVITE, request.getMethod());
			request.createResponse(SipServletResponse.SC_OK).send();

			request = _session.waitRequest();
			assertEquals(SipRequest.ACK, request.getMethod());
	        
			request = _session.waitRequest();
			assertEquals(SipRequest.BYE, request.getMethod());
			request.createResponse(SipServletResponse.SC_OK).send();
		}
	};
}
