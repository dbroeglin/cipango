// ========================================================================
// Copyright 2011 NEXCOM Systems
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
package org.cipango.client;

import static junit.framework.Assert.assertEquals;

public abstract class UacScript
{
	private UacScript()
	{
	}
	
	public abstract static class Script
	{
		protected SipRequest _request;
		
		public Script(SipRequest request)
		{
			_request = request;
		}
		
		public abstract void doTest() throws Throwable;
		
	}
	
	public static class RingingOkBye extends Script
	{
		/**
		 * <pre>
		 * SipUnit                    Remote
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
		public RingingOkBye(SipRequest request)
		{
			super(request);
		}

		@Override
		public void doTest() throws Exception
		{
			_request.send();
			SipResponse response = _request.waitResponse();
			assertEquals(SipResponse.SC_RINGING, response.getStatus());
			response = _request.waitResponse();
			assertEquals(SipResponse.SC_OK, response.getStatus());
			response.createAck().send();
			Thread.sleep(100);
			_request = _request.getSession().createRequest(SipRequest.BYE);
			_request.send();
			assertEquals(SipResponse.SC_OK, _request.waitResponse().getStatus());
		}
		
		
	}
}
