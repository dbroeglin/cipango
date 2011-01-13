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

import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;

/**
 * Represents SIP request messages. When receiving an incoming SIP request the container creates a SipServletRequest and passes it to the handling servlet. For outgoing, locally initiated requests, applications call SipFactory.createRequest to obtain a SipServletRequest that can then be modified and sent.
 */
public interface SipRequest extends SipMessage 
{
	public static final String 
	ACK = "ACK",
	BYE = "BYE",
 	CANCEL = "CANCEL",
 	INFO = "INFO",
 	INVITE = "INVITE",
 	MESSAGE = "MESSAGE",
 	NOTIFY = "NOTIFY",
 	OPTIONS = "OPTIONS",
 	PRACK = "PRACK",
 	PUBLISH = "PUBLISH",
 	REFER = "REFER",
 	REGISTER = "REGISTER",
 	SUBSCRIBE = "SUBSCRIBE",
 	UPDATE = "UPDATE";	
	
	SipResponse waitResponse();
	
	/**
	 * This method allows the addition of the appropriate authentication header(s) 
	 * to the request that was challenged with a challenge response.
	 * @param challengeResponse The challenge response (401/407) receieved from a UAS/Proxy.
	 * @param authInfo The AuthInfo object that will add the Authentication headers to the request.
	 */
	void addAuthHeader(SipResponse challengeResponse,
            AuthInfo authInfo);
	
	/**
	 * This method allows the addition of the appropriate authentication header(s) 
	 * to the request that was challenged with a challenge response without needing 
	 * the creation and/or maintenance of the AuthInfo object.
	 * @param challengeResponse the challenge response (401/407) receieved from a UAS/Proxy.
	 * @param username
	 * @param password
	 */
	void addAuthHeader(SipResponse challengeResponse,
            java.lang.String username,
            java.lang.String password);
    /**
     * Returns a CANCEL request object. This method is used by applications to cancel outstanding transactions for which they act as a user agent client (UAC). The CANCEL request is sent when the application invokes
     * on it.
     * Note that proxy applications MUST use Proxy.cancel() to cancel outstanding branches.
     */
    SipRequest createCancel();

    /**
     * Creates a response for this request with the specifies status code.
     */
    SipResponse createResponse(int statuscode);

    /**
     * Creates a response for this request with the specifies status code and reason phrase.
     */
    SipResponse createResponse(int statusCode, java.lang.String reasonPhrase);

    /**
     * Returns the value of the Max-Forwards header.
     */
    int getMaxForwards();

    /**
     * If a top route header had been removed by the container upon receiving this request, then this method can be used to retrieve it. Otherwise, if no route header had been popped then this method will return null.
     */
    javax.servlet.sip.Address getPoppedRoute();
    
    /**
     * Returns the request URI of this request.
     */
    javax.servlet.sip.URI getRequestURI();

    /**
     * Returns true if this is an initial request. An initial request is one that is dispatched to applications based on the containers configured rule set, as opposed to subsequent requests which are routed based on the application path established by a previous initial request.
     */
    boolean isInitial();

    /**
     * Adds a Path header field value to this request. The new value is added ahead of any existing Path header fields. If this request does not already container a Path header, one is added with the value specified in the argument. This method allows a UAC or a proxy to add Path on a REGISTER Request.
     */
    void pushPath(javax.servlet.sip.Address uri);

    /**
     * Adds a Route header field value to this request with Address argument. The new value is added ahead of any existing Route header fields. If this request does not already contains a Route header, one is added with the value as specified in the argument.
     * This method allows a UAC or a proxy to specify that the request should visit one or more proxies before being delivered to the destination.
     */
    void pushRoute(javax.servlet.sip.Address uri);

    /**
     * Adds a Route header field value to this request. The new value is added ahead of any existing Route header fields. If this request does not already contains a Route header, one is added with the value as specified in the argument.
     * This method allows a UAC or a proxy to specify that the request should visit one or more proxies before being delivered to the destination.
     */
    void pushRoute(javax.servlet.sip.SipURI uri);

    /**
     * Causes this request to be sent. This method is used by SIP servlets acting as user agent clients (UACs) only. Proxying applications use
     * instead.
     */
    void send() throws java.io.IOException;

    /**
     * Sets the value of the Max-Forwards header. Max-Forwards serves to limit the number of hops a request can make on the way to its destination. It consists of an integer that is decremented by one at each hop.
     * This method is equivalent to: setHeader("Max-Forwards", String.valueOf(n));
     */
    void setMaxForwards(int n);

    /**
     * Sets the request URI of this request. This then becomes the destination used in a subsequent invocation of
     * .
     */
    void setRequestURI(javax.servlet.sip.URI uri);

}
