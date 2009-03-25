// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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

package org.cipango.kaleo;

public abstract class Constants
{
	public static final String ACCEPT 				= "Accept";
	public static final String ALLOW_EVENTS 		= "Allow-Events";
	public static final String AUTHORIZATION 		= "Authorization";
	public static final String CONTACT 				= "Contact";
	public static final String CSEQ 				= "CSeq";
	public static final String DATE 				= "Date";
	public static final String EVENT 				= "Event";
	public static final String MIN_EXPIRES 			= "Min-Expires";
	public static final String REQUIRE				= "Require";
	public static final String PROXY_AUTHORIZATION 	= "Proxy-Authorization";
	public static final String PROXY_AUTHENTICATE 	= "Proxy-Authenticate";
	public static final String SIP_IF_MATCH 		= "SIP-If-Match";
	public static final String SIP_ETAG 			= "SIP-ETag";
	public static final String UNSUPPORTED 			= "Unsupported";
	public static final String WWW_AUTHENTICATE 	= "WWW-Authenticate";
	public static final String ID_PARAM 			= "id";
	public static final String SUBSCRIPTION_ATT		= "Subscription";
	
	/* 
	 * TODO:
	 * substate-value       = "active" / "pending" / "terminated"
                          / extension-substate
   extension-substate   = token
   subexp-params        =   ("reason" EQUAL event-reason-value)
                          / ("expires" EQUAL delta-seconds)
                          / ("retry-after" EQUAL delta-seconds)
                          / generic-param
   event-reason-value   =   "deactivated"
                          / "probation"
                          / "rejected"
                          / "timeout"
                          / "giveup"
                          / "noresource"
                          / event-reason-extension
   event-reason-extension = token
*/
	public static final String SUBSCRIPTION_STATE	= "Subscription-State";
	public static final String ACTIVE				= "active";
	public static final String PENDING				= "pending";
	public static final String TERMINATED			= "terminated";
	public static final String EXPIRES				= "expires";
	public static final String SIP_FACTORY			= "javax.servlet.sip.SipFactory";
	
	private Constants() { }	
}


