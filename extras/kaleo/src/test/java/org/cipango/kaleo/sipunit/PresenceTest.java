/*
 * Created on November 22, 2005
 * 
 * Copyright 2005 CafeSip.org 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *	http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 */
package org.cipango.kaleo.sipunit;

import java.util.HashMap;

import javax.servlet.sip.SipServletResponse;
import javax.sip.RequestEvent;
import javax.sip.header.AllowEventsHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MinExpiresHeader;
import javax.sip.header.SIPETagHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.AbstractSession;
import org.cafesip.sipunit.PresenceDeviceInfo;
import org.cafesip.sipunit.PresenceSubscriber;
import org.cafesip.sipunit.PublishSession;
import org.cafesip.sipunit.SipResponse;

public class PresenceTest extends UaTestCase
{

    public void testBasicSubscription() throws Exception
    {
        // add the buddy to the buddy list - sends SUBSCRIBE, gets response
        PresenceSubscriber s = getAlicePhone().addBuddy(getBobUri(), 60, 1000);

        // check the return info
        assertNotNull(s);
        assertEquals(1, getAlicePhone().getBuddyList().size());
        assertEquals(0, getAlicePhone().getRetiredBuddies().size());
        assertEquals(getBobUri(), s.getTargetUri());
        assertNotNull(getAlicePhone().getBuddyInfo(getBobUri())); // call anytime to get
        // Subscription            
        assertEquals(SipResponse.OK, s.getReturnCode());
        assertTrue(s.format(), s.processResponse(1000));

        // check the response processing results
        assertTrue(s.isSubscriptionActive());
        
        assertTrue(s.getTimeLeft() <= 60);
        Response response = (Response) s.getLastReceivedResponse().getMessage();
        assertEquals(60, response.getExpires().getExpires());

        // wait for a NOTIFY
        RequestEvent reqevent = s.waitNotify(10000);
        assertNotNull(reqevent);
        assertNoSubscriptionErrors(s);

        // examine the request object
        Request request = reqevent.getRequest();
        assertEquals(Request.NOTIFY, request.getMethod());            
        assertBetween(55, 60, ((SubscriptionStateHeader) request
                .getHeader(SubscriptionStateHeader.NAME)).getExpires());

        // process the NOTIFY
        response = s.processNotify(reqevent);
        // reply to the NOTIFY
        assertTrue(s.format(), s.replyToNotify(reqevent, response));

        // check PRESENCE info - devices/tuples
        // -----------------------------------------------
        HashMap<String, PresenceDeviceInfo> devices = s.getPresenceDevices();
        assertEquals(1, devices.size());
        PresenceDeviceInfo dev = devices.values().iterator().next();
        assertNotNull(dev);
        assertEquals("closed", dev.getBasicStatus());
        
        Thread.sleep(200);
        
        PublishSession publishSession = new PublishSession(getBobPhone());
        Request publish = publishSession.newPublish(getClass().getResourceAsStream("publish1.txt"), 20);
        publishSession.sendRequest(publish, null, null, SipResponse.OK);

        // get the NOTIFY
        reqevent = s.waitNotify(10000);
        assertNotNull(s.format(), reqevent);
        assertNoSubscriptionErrors(s);

        // examine the request object
        request = reqevent.getRequest();
        assertEquals(Request.NOTIFY, request.getMethod());
        assertTrue(((SubscriptionStateHeader) request
                .getHeader(SubscriptionStateHeader.NAME)).getExpires() > 0);

         // process the NOTIFY
        response = s.processNotify(reqevent);
        assertNotNull(response);

        assertTrue(s.isSubscriptionActive());

        devices = s.getPresenceDevices();
        assertEquals(1, devices.size());
        dev = devices.get("bs35r9");
        assertNotNull(dev);
        assertEquals("open", dev.getBasicStatus());
        assertEquals("sip:bob@cipango.org", dev.getContactURI());
        assertEquals(0.8, dev.getContactPriority());
        assertEquals("Don't Disturb Please!", dev.getDeviceNotes().get(0).getValue());
        
        // reply to the NOTIFY
        assertTrue(s.replyToNotify(reqevent, response));

        assertNoSubscriptionErrors(s);

        // End subscription
        assertTrue(s.removeBuddy(5000));
        reqevent = s.waitNotify(10000);
        assertNotNull(s.format(), reqevent); 
        response = s.processNotify(reqevent);
        assertEquals(Response.OK, response.getStatusCode());
        assertTrue(s.replyToNotify(reqevent, response));
        
        assertTrue(s.isSubscriptionTerminated());  
    }

    public void testMinExpires() throws Exception
    {       
        AbstractSession session = new AbstractSession(getAlicePhone());
        HeaderFactory hf = session.getHeaderFactory();
        Request request = session.newRequest(Request.SUBSCRIBE, 1, getBobUri());
        request.setHeader(hf.createExpiresHeader(1));
        request.setHeader(hf.createEventHeader("presence"));
        request.setHeader(getAlicePhone().getContactInfo().getContactHeader());
        Response response = session.sendRequest(request, null, null, SipResponse.INTERVAL_TOO_BRIEF);
        MinExpiresHeader minExpiresHeader = (MinExpiresHeader) response.getHeader(MinExpiresHeader.NAME);
        assertNotNull(minExpiresHeader);
    }
    
    public void testBadEvent() throws Exception
    {       
        AbstractSession session = new AbstractSession(getAlicePhone());
        HeaderFactory hf = session.getHeaderFactory();
        Request request = session.newRequest(Request.SUBSCRIBE, 1, getBobUri());
        request.setHeader(hf.createExpiresHeader(60));
        request.setHeader(hf.createEventHeader("unknown"));
        request.setHeader(getAlicePhone().getContactInfo().getContactHeader());
        Response response = session.sendRequest(request, null, null, SipResponse.BAD_EVENT);
        AllowEventsHeader allowEvents = (AllowEventsHeader) response.getHeader(AllowEventsHeader.NAME);
        assertNotNull(allowEvents);
        assertEquals("presence", allowEvents.getEventType());
    }
    
    public void testBadAcceptHeader() throws Exception
    {       
        AbstractSession session = new AbstractSession(getAlicePhone());
        HeaderFactory hf = session.getHeaderFactory();
        Request request = session.newRequest(Request.SUBSCRIBE, 1, getBobUri());
        request.setHeader(hf.createExpiresHeader(60));
        request.setHeader(hf.createEventHeader("presence"));
        request.setHeader(getAlicePhone().getContactInfo().getContactHeader());
        request.setHeader(hf.createAcceptHeader("application", "unknown"));
        session.sendRequest(request, null, null, SipResponse.UNSUPPORTED_MEDIA_TYPE);
    }
    
    
    public void testEtags() throws Exception
    {       
    	 PublishSession publishSession = new PublishSession(getBobPhone());
         Request publish = publishSession.newPublish(getClass().getResourceAsStream("publish1.txt"), 20);
         Response response = publishSession.sendRequest(publish, null, null, SipResponse.OK);
         
         SIPETagHeader etagHeader = (SIPETagHeader) response.getHeader(SIPETagHeader.NAME);
         assertNotNull(etagHeader);
         String etag = etagHeader.getETag();
         
         publish = publishSession.newPublish(getClass().getResourceAsStream("publish2.txt"), 20);
         HeaderFactory hf = publishSession.getHeaderFactory();
         publish.setHeader(hf.createSIPIfMatchHeader(etag));
         response = publishSession.sendRequest(publish, null, null, SipResponse.OK);
         etagHeader = (SIPETagHeader) response.getHeader(SIPETagHeader.NAME);
         assertNotNull(etagHeader);
         
         publish = publishSession.newPublish(getClass().getResourceAsStream("publish1.txt"), 20);
         publish.setHeader(hf.createSIPIfMatchHeader(etag)); //Use old etag
         response = publishSession.sendRequest(publish, null, null, SipServletResponse.SC_CONDITIONAL_REQUEST_FAILED);
    }

}