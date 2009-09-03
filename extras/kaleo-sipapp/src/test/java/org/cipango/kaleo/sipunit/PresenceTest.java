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

import javax.sip.RequestEvent;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

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

    /*
    public void testEndSubscription()
    {
        // This method tests terminating a subscription from the client side
        // (by removing a buddy from the SipPhone buddy list).

        String buddy = "sip:becky@"
                + properties.getProperty("sipunit.test.domain"); // URI of
        // buddy

        // test steps SEQUENCE:
        // 1) prepare the far end (I will use the PresenceNotifySender
        // utility class to simulate the Presence Server)
        // 2) establish an active subscription (SUBSCRIBE, NOTIFY)
        // 3) remove buddy from buddy list - sends SUBSCRIBE, gets response
        // 4) check the return code, process the received response
        // 5) tell far end to send a NOTIFY
        // 6) get the NOTIFY
        // 7) process the NOTIFY
        // check the processing results
        // check PRESENCE info - devices/tuples
        // check PRESENCE info - top-level extensions
        // check PRESENCE info - top-level notes
        // 8) reply to the NOTIFY

        try
        {
            // (1) prepare the far end - a presence server and a buddy somewhere
            // create the far end, register buddy w/server
            PresenceNotifySender pserver = new PresenceNotifySender(sipStack
                    .createSipPhone(properties
                            .getProperty("sipunit.proxy.host"), testProtocol,
                            proxyPort, buddy));
            boolean registered = pserver.register(new Credential(properties
                    .getProperty("sipunit.test.domain"), "becky", "a1b2c3d4"));
            assertTrue(pserver.getErrorMessage(), registered);

            // prepare far end to receive SUBSCRIBE within 2 sec, respond w/OK
            assertTrue(pserver.processSubscribe(2000, SipResponse.OK, "OK"));

            // (2) establish a subscription, get the buddy in the buddy list
            PresenceSubscriber s = getAlicePhone().addBuddy(buddy, 1000);
            assertNotNull(s);
            boolean status = s.processResponse(1000);
            assertTrue(s.format(), status);
            assertTrue(s.isSubscriptionActive());

            // do the initial NOTIFY sequence - still establishing
            String notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence entity=\"sip:becky@"
                    + properties.getProperty("sipunit.test.domain")
                    + "\" xmlns=\"urn:ietf:params:xml:ns:pidf\"><tuple id=\"1\"><status><basic>closed</basic></status></tuple></presence>";
            assertTrue(pserver.sendNotify(SubscriptionStateHeader.ACTIVE, null,
                    notify_body, 2400, true));
            Thread.sleep(100);
            RequestEvent reqevent = s.waitNotify(1000); // client receives
            // notify
            assertNotNull(reqevent);
            assertNoSubscriptionErrors(s);
            Response response = s.processNotify(reqevent); // client processes
            // it
            assertNotNull(response); // this is the response that should be
            // sent
            // back
            assertTrue(s.isSubscriptionActive());

            // verify upcated client PRESENCE info - still part of establishment
            HashMap<String, PresenceDeviceInfo> devices = s
                    .getPresenceDevices();
            assertEquals(1, devices.size());
            PresenceDeviceInfo dev = devices.get("1");
            assertNotNull(dev);
            assertEquals("closed", dev.getBasicStatus());
            assertEquals(0, s.getPresenceExtensions().size());
            assertEquals(0, s.getPresenceNotes().size());

            // reply to the NOTIFY
            assertTrue(s.replyToNotify(reqevent, response));
            Thread.sleep(200); // !IF YOU DON't HAVE THIS, THINGS WILL FAIL
            // BELOW.

            // verify the buddy lists look correct
            assertEquals(1, getAlicePhone().getBuddyList().size());
            assertEquals(0, getAlicePhone().getRetiredBuddies().size());
            assertNoSubscriptionErrors(s);

            // (3) Now, end the subscription from our (client) side

            // prepare far end to receive SUBSCRIBE within 2 sec, reply with OK
            assertTrue(pserver.processSubscribe(2000, SipResponse.OK,
                    "OK Ended"));

            // remove buddy from contacts list, terminating SUBSCRIBE sequence
            s = getAlicePhone().getBuddyInfo(buddy);
            assertTrue(s.removeBuddy(2000));

            // check immediate impacts - buddy lists, subscription state
            assertEquals(0, getAlicePhone().getBuddyList().size());
            assertEquals(1, getAlicePhone().getRetiredBuddies().size());
            assertNoSubscriptionErrors(s);
            assertNotNull(getAlicePhone().getBuddyInfo(buddy)); // check buddy can still be
            // found
            assertEquals(s.getTargetUri(), getAlicePhone().getBuddyInfo(buddy)
                    .getTargetUri());
            assertFalse(s.isSubscriptionActive());
            assertFalse(s.isSubscriptionPending());
            assertTrue(s.isSubscriptionTerminated());
            String reason = s.getTerminationReason();
            assertNotNull(reason);

            // (4) check the SUBSCRIBE response code, process the response
            assertEquals(SipResponse.OK, s.getReturnCode());

            ResponseEvent resp_event = s.getCurrentResponse();
            response = resp_event.getResponse(); // check out the response
            // details
            assertEquals("OK Ended", response.getReasonPhrase());
            assertEquals(0, response.getExpires().getExpires());
            assertEquals(response.toString(), s.getLastReceivedResponse()
                    .getMessage().toString());
            ArrayList<SipResponse> received_responses = s
                    .getAllReceivedResponses();
            assertEquals(3, received_responses.size());
            assertEquals(response.toString(), received_responses.get(2)
                    .toString());

            // process the received response
            assertTrue(s.processResponse(300));

            // check the response processing results
            assertFalse(s.isSubscriptionActive());
            assertFalse(s.isSubscriptionPending());
            assertTrue(s.isSubscriptionTerminated());
            assertEquals(reason, s.getTerminationReason());
            assertEquals(0, s.getTimeLeft());
            assertEquals(0, getAlicePhone().getBuddyList().size());
            assertEquals(1, getAlicePhone().getRetiredBuddies().size());
            assertNoSubscriptionErrors(s);

            // (5) tell far end to send a last NOTIFY - use different tuple info
            notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence entity=\"sip:becky@"
                    + properties.getProperty("sipunit.test.domain")
                    + "\" xmlns=\"urn:ietf:params:xml:ns:pidf\"> <tuple id=\"3\"> <status> <basic>open</basic> </status> </tuple> </presence>";
            assertTrue(pserver.sendNotify(SubscriptionStateHeader.TERMINATED,
                    "done", notify_body, 0, true));

            // (6) get the NOTIFY
            reqevent = s.waitNotify(1000);
            assertNotNull(reqevent);
            assertNoSubscriptionErrors(s);

            // (7) process the NOTIFY
            response = s.processNotify(reqevent);
            assertNotNull(response);

            assertEquals(0, getAlicePhone().getBuddyList().size());
            assertEquals(1, getAlicePhone().getRetiredBuddies().size());
            assertNoSubscriptionErrors(s);

            // check the processing results
            assertTrue(s.isSubscriptionTerminated());
            assertNotNull(s.getTerminationReason());
            assertFalse(reason.equals(s.getTerminationReason())); // updated
            assertEquals(0, s.getTimeLeft());
            assertEquals(SipResponse.OK, s.getReturnCode()); // response code

            // check PRESENCE info got updated w/last NOTIFY - devices/tuples
            // -----------------------------------------------
            devices = s.getPresenceDevices();
            assertEquals(1, devices.size());
            dev = devices.get("3");
            assertNotNull(dev);
            assertEquals("open", dev.getBasicStatus());
            assertEquals(-1.0, dev.getContactPriority(), 0.001);
            assertNull(dev.getContactURI());
            assertEquals(0, dev.getDeviceExtensions().size());
            assertEquals(0, dev.getDeviceNotes().size());
            assertEquals("3", dev.getId());
            assertEquals(0, dev.getStatusExtensions().size());
            assertNull(dev.getTimestamp());

            // check PRESENCE info - top-level extensions
            // -----------------------------------------------
            assertEquals(0, s.getPresenceExtensions().size());

            // check PRESENCE info - top-level notes
            // -----------------------------------------------
            assertEquals(0, s.getPresenceNotes().size());

            // (8) reply to the NOTIFY
            assertTrue(s.replyToNotify(reqevent, response));

            Thread.sleep(30);
            pserver.dispose();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void testFetch()
    {
        String buddy = "sip:becky@"
                + properties.getProperty("sipunit.test.domain"); // I am amit

        try
        {
            // create far end (presence server simulator, fictitious buddy)
            PresenceNotifySender ub = new PresenceNotifySender(sipStack
                    .createSipPhone(properties
                            .getProperty("sipunit.proxy.host"), testProtocol,
                            proxyPort, buddy));
            boolean registered = ub.register(new Credential(properties
                    .getProperty("sipunit.test.domain"), "becky", "a1b2c3d4"));
            assertTrue(ub.getErrorMessage(), registered);

            // SEQUENCE OF EVENTS
            // prepare far end to receive SUBSCRIBE
            // do something with a buddy - sends SUBSCRIBE, gets response
            // check the return info
            // process the received response
            // check the response processing results
            // tell far end to send a NOTIFY
            // get the NOTIFY
            // process the NOTIFY
            // check the processing results
            // check PRESENCE info - devices/tuples
            // check PRESENCE info - top-level extensions
            // check PRESENCE info - top-level notes
            // reply to the NOTIFY

            // prepare far end to receive SUBSCRIBE
            assertTrue(ub.processSubscribe(5000, SipResponse.OK, "OK"));
            Thread.sleep(500);

            // do something with a buddy - sends SUBSCRIBE, gets response
            PresenceSubscriber s = getAlicePhone().fetchPresenceInfo(buddy, 2000);

            // check the return info
            assertNotNull(s);
            assertEquals(0, getAlicePhone().getBuddyList().size());
            assertEquals(1, getAlicePhone().getRetiredBuddies().size());
            assertEquals(buddy, s.getTargetUri());
            assertNotNull(getAlicePhone().getBuddyInfo(buddy));
            assertEquals(s.getTargetUri(), getAlicePhone().getBuddyInfo(buddy)
                    .getTargetUri());
            assertFalse(s.isSubscriptionPending());
            assertFalse(s.isSubscriptionActive());
            assertTrue(s.isSubscriptionTerminated());

            assertTrue(s.getReturnCode() == SipResponse.PROXY_AUTHENTICATION_REQUIRED
                    || s.getReturnCode() == SipResponse.UNAUTHORIZED);

            // process the received response and any remaining ones
            assertTrue(s.processResponse(1000));

            assertEquals(SipResponse.OK, s.getReturnCode());
            ResponseEvent resp_event = s.getCurrentResponse();
            Response response = resp_event.getResponse();
            assertEquals("OK", response.getReasonPhrase());
            assertEquals(0, response.getExpires().getExpires());
            assertEquals(response.toString(), s.getLastReceivedResponse()
                    .getMessage().toString());
            ArrayList<SipResponse> received_responses = s
                    .getAllReceivedResponses();
            assertEquals(2, received_responses.size());
            assertEquals(response.toString(), received_responses.get(1)
                    .toString());
            assertEquals(0, s.getTimeLeft());
            assertEquals("Fetch", s.getTerminationReason());
            assertTrue(s.isSubscriptionTerminated());

            // tell far end to send a NOTIFY
            String notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence entity=\"sip:becky@"
                    + properties.getProperty("sipunit.test.domain")
                    + "\" xmlns=\"urn:ietf:params:xml:ns:pidf\"><tuple id=\"1\"><status><basic>closed</basic></status></tuple></presence>";
            assertTrue(ub.sendNotify(SubscriptionStateHeader.TERMINATED,
                    "fetch", notify_body, 0, true));

            // get the NOTIFY
            RequestEvent reqevent = s.waitNotify(1000);
            assertNotNull(reqevent);
            assertNoSubscriptionErrors(s);
            HashMap<String, PresenceDeviceInfo> devices = s
                    .getPresenceDevices();
            assertTrue(devices.isEmpty());

            // process the NOTIFY
            response = s.processNotify(reqevent);
            assertNotNull(response);

            // check the processing results
            assertTrue(s.isSubscriptionTerminated());
            assertEquals(0, s.getTimeLeft());
            assertEquals("fetch", s.getTerminationReason());
            assertEquals(SipResponse.OK, s.getReturnCode()); // response code

            // check PRESENCE info - devices/tuples
            // -----------------------------------------------
            devices = s.getPresenceDevices();
            assertEquals(1, devices.size());
            PresenceDeviceInfo dev = devices.get("1");
            assertNotNull(dev);
            assertEquals("closed", dev.getBasicStatus());
            assertEquals(-1.0, dev.getContactPriority(), 0.001);
            assertNull(dev.getContactURI());
            assertEquals(0, dev.getDeviceExtensions().size());
            assertEquals(0, dev.getDeviceNotes().size());
            assertEquals("1", dev.getId());
            assertEquals(0, dev.getStatusExtensions().size());
            assertNull(dev.getTimestamp());

            // check PRESENCE info - top-level extensions
            // -----------------------------------------------
            assertEquals(0, s.getPresenceExtensions().size());

            // check PRESENCE info - top-level notes
            // -----------------------------------------------
            assertEquals(0, s.getPresenceNotes().size());

            // reply to the NOTIFY
            assertTrue(s.replyToNotify(reqevent, response));

            // check misc again
            assertEquals(0, getAlicePhone().getBuddyList().size());
            assertEquals(1, getAlicePhone().getRetiredBuddies().size());
            assertNotNull(getAlicePhone().getBuddyInfo(buddy));
            assertEquals(buddy, getAlicePhone().getBuddyInfo(buddy).getTargetUri());
            assertFalse(s.isSubscriptionPending());
            assertFalse(s.isSubscriptionActive());
            assertTrue(s.isSubscriptionTerminated());
            assertEquals(0, s.getTimeLeft());
            assertEquals("fetch", s.getTerminationReason());

            // do another fetch

            Thread.sleep(100);
            assertTrue(ub.processSubscribe(5000, SipResponse.OK, "OKay"));
            Thread.sleep(500);

            s = getAlicePhone().fetchPresenceInfo(buddy, 2000);

            // check the return info
            assertNotNull(s);
            assertEquals(0, getAlicePhone().getBuddyList().size());
            assertEquals(1, getAlicePhone().getRetiredBuddies().size());
            assertEquals(buddy, s.getTargetUri());
            assertNotNull(getAlicePhone().getBuddyInfo(buddy));
            assertEquals(s.getTargetUri(), getAlicePhone().getBuddyInfo(buddy)
                    .getTargetUri());
            assertFalse(s.isSubscriptionPending());
            assertFalse(s.isSubscriptionActive());
            assertTrue(s.isSubscriptionTerminated());

            // process the received response(s)
            assertTrue(s.processResponse(1000));

            assertEquals(SipResponse.OK, s.getReturnCode());
            resp_event = s.getCurrentResponse();
            response = resp_event.getResponse();
            assertEquals("OKay", response.getReasonPhrase());
            assertEquals(0, response.getExpires().getExpires());
            assertEquals(response.toString(), s.getLastReceivedResponse()
                    .getMessage().toString());
            received_responses = s.getAllReceivedResponses();
            assertEquals(2, received_responses.size());
            assertEquals(response.toString(), received_responses.get(1)
                    .toString());
            assertEquals(0, s.getTimeLeft());
            assertEquals("Fetch", s.getTerminationReason());
            assertTrue(s.isSubscriptionTerminated());

            // tell far end to send a NOTIFY
            notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence entity=\"sip:becky@"
                    + properties.getProperty("sipunit.test.domain")
                    + "\" xmlns=\"urn:ietf:params:xml:ns:pidf\"><tuple id=\"1\"><status><basic>open</basic></status></tuple></presence>";
            boolean notify_sent = ub.sendNotify(
                    SubscriptionStateHeader.TERMINATED, "refetch", notify_body,
                    0, true);
            assertTrue(ub.getErrorMessage(), notify_sent);

            // get the NOTIFY
            reqevent = s.waitNotify(5000);
            assertNotNull(reqevent);
            assertNoSubscriptionErrors(s);

            // process the NOTIFY
            response = s.processNotify(reqevent);
            assertNotNull(response);

            // check the processing results
            assertTrue(s.isSubscriptionTerminated());
            assertEquals(0, s.getTimeLeft());
            assertEquals("refetch", s.getTerminationReason());
            assertEquals(SipResponse.OK, s.getReturnCode()); // response code

            // check PRESENCE info - devices/tuples
            // -----------------------------------------------
            devices = s.getPresenceDevices();
            assertEquals(1, devices.size());
            dev = devices.get("1");
            assertNotNull(dev);
            assertEquals("open", dev.getBasicStatus());
            assertEquals(-1.0, dev.getContactPriority(), 0.001);
            assertNull(dev.getContactURI());
            assertEquals(0, dev.getDeviceExtensions().size());
            assertEquals(0, dev.getDeviceNotes().size());
            assertEquals("1", dev.getId());
            assertEquals(0, dev.getStatusExtensions().size());
            assertNull(dev.getTimestamp());

            // check PRESENCE info - top-level extensions
            // -----------------------------------------------
            assertEquals(0, s.getPresenceExtensions().size());

            // check PRESENCE info - top-level notes
            // -----------------------------------------------
            assertEquals(0, s.getPresenceNotes().size());

            // reply to the NOTIFY
            assertTrue(s.replyToNotify(reqevent, response));

            // check misc again
            assertEquals(0, getAlicePhone().getBuddyList().size());
            assertEquals(1, getAlicePhone().getRetiredBuddies().size());
            assertNotNull(getAlicePhone().getBuddyInfo(buddy));
            assertEquals(buddy, getAlicePhone().getBuddyInfo(buddy).getTargetUri());
            assertFalse(s.isSubscriptionPending());
            assertFalse(s.isSubscriptionActive());
            assertTrue(s.isSubscriptionTerminated());
            assertEquals(0, s.getTimeLeft());
            assertEquals("refetch", s.getTerminationReason());

            Thread.sleep(30);
            ub.dispose();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void testNotifyPresenceDataDetail()
    {
        String buddy = "sip:becky@"
                + properties.getProperty("sipunit.test.domain"); // I am amit

        try
        {
            PresenceNotifySender ub = new PresenceNotifySender(sipStack
                    .createSipPhone(properties
                            .getProperty("sipunit.proxy.host"), testProtocol,
                            proxyPort, buddy));
            boolean registered = ub.register(new Credential(properties
                    .getProperty("sipunit.test.domain"), "becky", "a1b2c3d4"));
            assertTrue(ub.getErrorMessage(), registered);

            // prepare far end to receive SUBSCRIBE
            assertTrue(ub.processSubscribe(5000, SipResponse.OK, "OKee"));
            Thread.sleep(500);

            // do something with a buddy - sends SUBSCRIBE, gets fist response
            PresenceSubscriber s = getAlicePhone().addBuddy(buddy, 2000);

            // check initial success
            assertNotNull(s);

            // process the received response and any remaining ones for the
            // transaction
            assertTrue(s.processResponse(1000));

            // check final result of the SUBSCRIBE operation
            assertEquals(SipResponse.OK, s.getReturnCode());

            // check the response processing results
            assertTrue(s.isSubscriptionActive());
            assertTrue(s.getTimeLeft() <= 3600);

            // (1) send notify with everything possible

            String notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" entity=\"sip:becky@"
                    + properties.getProperty("sipunit.test.domain")
                    + "\">"
                    + " <tuple id=\"bs35r9\">"
                    + " <status><basic>open</basic>"
                    // here + " status extention 1"
                    // here + " status extension 2"
                    + " </status>"
                    // here + " device(tuple) extension 1"
                    // here + " device extension 2"
                    + " <contact priority=\"0.8\">im:someone@mobilecarrier.net</contact>"
                    + " <note xml:lang=\"en\">Don't Disturb Please!</note>"
                    + " <note xml:lang=\"fr\">Ne derangez pas, s'il vous plait</note>"
                    + " <timestamp>2001-10-27T16:49:29Z</timestamp>"
                    + " </tuple>"
                    + " <tuple id=\"doodah\">"
                    + " <status><basic>closed</basic>"
                    + " <xs:string>Status extension 1</xs:string>"
                    + " </status>"
                    + " <contact priority=\"1.0\">me@mobilecarrier.net</contact>"
                    + " <note xml:lang=\"fr\">Ne derangez pas, s'il vous plait</note>"
                    + " <timestamp>2002-10-27T16:48:29Z</timestamp>"
                    + " </tuple>"
                    + " <tuple id=\"eg92n8\">"
                    + " <status>"
                    + " </status>"
                    + " </tuple>"
                    + " <note>I'll be in Tokyo next week</note>"
                    + " <note xml:lang=\"en\">I'll be in Tahiti after that</note>"
                    // here + " top level extension 1"
                    // here + " top level extension 2"
                    // here + mustUnderstand
                    + " </presence>";

            assertTrue(ub.sendNotify(SubscriptionStateHeader.ACTIVE, null,
                    notify_body, 3600, true));

            // get the NOTIFY
            RequestEvent reqevent = s.waitNotify(1000);
            assertNotNull(reqevent);
            assertNoSubscriptionErrors(s);

            // process the NOTIFY
            Response response = s.processNotify(reqevent);
            assertNotNull(response);

            // check the processing results
            assertTrue(s.isSubscriptionActive());
            assertEquals(SipResponse.OK, s.getReturnCode()); // response
            // code

            // reply to the NOTIFY
            assertTrue(s.replyToNotify(reqevent, response));

            // check PRESENCE info - devices
            // -----------------------------------------------
            HashMap<String, PresenceDeviceInfo> devices = s
                    .getPresenceDevices();
            assertEquals(3, devices.size());
            assertNull(devices.get("dummy"));

            PresenceDeviceInfo dev = devices.get("bs35r9");
            assertNotNull(dev);
            assertEquals("open", dev.getBasicStatus());
            List<Object> statusext = dev.getStatusExtensions();
            assertEquals(0, statusext.size());
            assertEquals(0.8, dev.getContactPriority(), 0.001);
            assertEquals("im:someone@mobilecarrier.net", dev.getContactURI());
            assertEquals(0, dev.getDeviceExtensions().size());
            List<PresenceNote> notes = dev.getDeviceNotes();
            assertEquals(2, notes.size());
            assertEquals("Don't Disturb Please!", ((PresenceNote) notes.get(0))
                    .getValue());
            assertEquals("en", ((PresenceNote) notes.get(0)).getLanguage());
            assertEquals("Ne derangez pas, s'il vous plait",
                    ((PresenceNote) notes.get(1)).getValue());
            assertEquals("fr", ((PresenceNote) notes.get(1)).getLanguage());
            assertEquals("bs35r9", dev.getId());
            Calendar timestamp = dev.getTimestamp();
            assertEquals(2001, timestamp.get(Calendar.YEAR));
            assertEquals(49, timestamp.get(Calendar.MINUTE));

            dev = devices.get("doodah");
            assertNotNull(dev);
            assertEquals("closed", dev.getBasicStatus());
            statusext = dev.getStatusExtensions();
            assertEquals(0, statusext.size());
            assertEquals(1.0, dev.getContactPriority(), 0.001);
            assertEquals("me@mobilecarrier.net", dev.getContactURI());
            assertEquals(0, dev.getDeviceExtensions().size());
            notes = dev.getDeviceNotes();
            assertEquals(1, notes.size());
            assertEquals("Ne derangez pas, s'il vous plait",
                    ((PresenceNote) notes.get(0)).getValue());
            assertEquals("fr", ((PresenceNote) notes.get(0)).getLanguage());
            assertEquals("doodah", dev.getId());
            timestamp = dev.getTimestamp();
            assertEquals(2002, timestamp.get(Calendar.YEAR));
            assertEquals(48, timestamp.get(Calendar.MINUTE));

            dev = devices.get("eg92n8");
            assertNotNull(dev);
            assertEquals(null, dev.getBasicStatus());
            assertEquals(0, dev.getStatusExtensions().size());
            assertEquals(-1.0, dev.getContactPriority(), 0.001);
            assertEquals(null, dev.getContactURI());
            assertEquals(0, dev.getDeviceExtensions().size());
            assertEquals(0, dev.getDeviceNotes().size());
            assertNotSame("bs35r9", dev.getId());
            assertEquals("eg92n8", dev.getId());
            assertNull(dev.getTimestamp());

            // check PRESENCE info - top-level notes
            // ---------------------------------------
            assertEquals(2, s.getPresenceNotes().size());
            PresenceNote note = (PresenceNote) s.getPresenceNotes().get(0);
            assertEquals("I'll be in Tokyo next week", note.getValue());
            assertNull(note.getLanguage());
            note = (PresenceNote) s.getPresenceNotes().get(1);
            assertEquals("I'll be in Tahiti after that", note.getValue());
            assertEquals("en", note.getLanguage());

            // check PRESENCE info - top-level extensions
            // ----------------------------------
            assertEquals(0, s.getPresenceExtensions().size());
            // check mustUnderstand

            // (2) send notify with minimal possible

            notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\""
                    + " entity=\"sip:becky@"
                    + properties.getProperty("sipunit.test.domain") + "\">"
                    + " </presence>";

            assertTrue(ub.sendNotify(SubscriptionStateHeader.ACTIVE, null,
                    notify_body, 3600, true));

            // get the NOTIFY
            reqevent = s.waitNotify(1000);
            assertNotNull(reqevent);
            assertNoSubscriptionErrors(s);

            // process the NOTIFY
            response = s.processNotify(reqevent);
            assertNotNull(response);

            // check the processing results
            assertTrue(s.isSubscriptionActive());
            assertEquals(SipResponse.OK, s.getReturnCode()); // response code

            // reply to the NOTIFY
            assertTrue(s.replyToNotify(reqevent, response));

            // check PRESENCE info - devices
            // -----------------------------------------------
            devices = s.getPresenceDevices();
            assertEquals(0, devices.size());

            // check PRESENCE info - top-level notes
            // ---------------------------------------
            assertEquals(0, s.getPresenceNotes().size());

            // check PRESENCE info - top-level extensions
            // ----------------------------------
            assertEquals(0, s.getPresenceExtensions().size());

            // (3) send badly formed data

            assertNoSubscriptionErrors(s);
            notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n "
                    + " <presence entity=\"sip:becky@"
                    + properties.getProperty("sipunit.test.domain") + "\""
                    + " xmlns=\"urn:ietf:params:xml:ns:pidf\"> "
                    + " <tuple id=\"1\"> "
                    + " <status><basic>open</basic></status>" + " </tuple> "
                    + " </presencee>";

            assertTrue(ub.sendNotify(SubscriptionStateHeader.ACTIVE, null,
                    notify_body, 3600, true));

            // get the NOTIFY
            reqevent = s.waitNotify(1000);
            assertNotNull(reqevent);
            assertNoSubscriptionErrors(s);

            // process the NOTIFY

            System.out
                    .println("The following validation FATAL_ERROR is SUPPOSED TO HAPPEN");
            response = s.processNotify(reqevent);
            assertNotNull(response);

            // check the processing results
            assertTrue(s.isSubscriptionActive());
            assertEquals(SipResponse.BAD_REQUEST, s.getReturnCode());

            String err = (String) s.getErrorMessage();
            assertTrue(err.indexOf("parsing error") != -1);
            devices = s.getPresenceDevices();
            assertEquals(0, devices.size());
            assertEquals(0, s.getEventErrors().size());

            // reply to the NOTIFY
            assertTrue(s.replyToNotify(reqevent, response));

            Thread.sleep(30);
            ub.dispose();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Exception: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void testStrayNotify() // with no matching Subscription
    {
        String buddy = "sip:becky@"
                + properties.getProperty("sipunit.test.domain");
        String buddy2 = "sip:vidya@"
                + properties.getProperty("sipunit.test.domain");

        try
        {
            // create object to send a NOTIFY
            PresenceNotifySender sender = new PresenceNotifySender(
                    sipStack
                            .createSipPhone(
                                    properties
                                            .getProperty("sipunit.proxy.host"),
                                    testProtocol,
                                    proxyPort,
                                    "sip:tom@"
                                            + properties
                                                    .getProperty("sipunit.test.domain")));
            boolean registered = sender.register(new Credential(properties
                    .getProperty("sipunit.test.domain"), "tom", "a1b2c3d4"));
            assertTrue(sender.getErrorMessage(), registered);

            // create and send NOTIFY out of the blue
            Request request = sipStack.getMessageFactory().createRequest(
                    "NOTIFY sip:amit@"
                            + properties.getProperty("javax.sip.IP_ADDRESS")
                            + ':' + myPort + ";transport=udp SIP/2.0\n");
            String notify_body = "<?xml version='1.0' encoding='UTF-8'?> "
                    + " <presence entity='sip:anyone@"
                    + properties.getProperty("sipunit.test.domain") + "' "
                    + "xmlns='urn:ietf:params:xml:ns:pidf'>" + "<tuple id='1'>"
                    + "<status><basic>closed</basic>" + "</status>"
                    + "</tuple>" + "</presence>";

            sender.addNotifyHeaders(request, "amit", properties
                    .getProperty("sipunit.test.domain"),
                    SubscriptionStateHeader.TERMINATED, "late", notify_body, 0);

            SipTransaction trans = sender.sendStatefulNotify(request, true);
            assertNotNull(sender.getErrorMessage(), trans);

            // get the response
            EventObject event = sender.waitResponse(trans, 2000);
            assertNotNull(sender.getErrorMessage(), event);

            if (event instanceof TimeoutEvent)
            {
                fail("Event Timeout received by far end while waiting for NOTIFY response");
            }

            assertTrue("Expected auth challenge", sender
                    .needAuthorization((ResponseEvent) event));
            trans = sender.resendWithAuthorization((ResponseEvent) event);
            assertNotNull(sender.getErrorMessage(), trans);

            // get the next response
            event = sender.waitResponse(trans, 2000);
            assertNotNull(sender.getErrorMessage(), event);

            if (event instanceof TimeoutEvent)
            {
                fail("Event Timeout received by far end while waiting for NOTIFY response");
            }

            assertFalse("Didn't expect auth challenge", sender
                    .needAuthorization((ResponseEvent) event));
            // should have a do-while loop here, handle multiple challenges

            Response response = ((ResponseEvent) event).getResponse();
            assertEquals("Should have gotten 481 response for stray NOTIFY",
                    SipResponse.CALL_OR_TRANSACTION_DOES_NOT_EXIST, response
                            .getStatusCode());

            // //////////////////////////////////////////////////////////
            // repeat w/2 buddies using wrong presentity. Verify
            // presence event on both

            // set up the two buddies

            PresenceNotifySender buddy1sim = new PresenceNotifySender(sipStack
                    .createSipPhone(properties
                            .getProperty("sipunit.proxy.host"), testProtocol,
                            proxyPort, buddy));
            PresenceNotifySender buddy2sim = new PresenceNotifySender(sipStack
                    .createSipPhone(properties
                            .getProperty("sipunit.proxy.host"), testProtocol,
                            proxyPort, buddy2));

            // register the buddies with the server
            registered = buddy1sim.register(new Credential(properties
                    .getProperty("sipunit.test.domain"), "becky", "a1b2c3d4"));
            assertTrue(buddy1sim.getErrorMessage(), registered);
            registered = buddy2sim.register(new Credential(properties
                    .getProperty("sipunit.test.domain"), "vidya", "a1b2c3d4"));
            assertTrue(buddy2sim.getErrorMessage(), registered);

            assertTrue(buddy1sim.processSubscribe(5000, SipResponse.OK, "OK")); // prepare
            assertTrue(buddy2sim.processSubscribe(5000, SipResponse.OK, "OK")); // prepare
            Thread.sleep(500);
            PresenceSubscriber s1 = getAlicePhone().addBuddy(buddy, 2000);
            PresenceSubscriber s2 = getAlicePhone().addBuddy(buddy2, 2000);
            assertNotNull(s1);
            assertNotNull(s2);
            boolean status = s1.processResponse(1000);
            assertTrue(s1.format(), status);
            status = s2.processResponse(1000);
            assertTrue(s2.format(), status);
            assertEquals(SipResponse.OK, s1.getReturnCode());
            assertEquals(SipResponse.OK, s2.getReturnCode());

            notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence entity=\""
                    + buddy
                    + "\" xmlns=\"urn:ietf:params:xml:ns:pidf\"><tuple id=\"1\"><status><basic>"
                    + "open" + "</basic></status></tuple></presence>";
            assertTrue(buddy1sim.sendNotify(SubscriptionStateHeader.ACTIVE,
                    null, notify_body, 3600, true));
            notify_body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence entity=\""
                    + buddy2
                    + "\" xmlns=\"urn:ietf:params:xml:ns:pidf\"><tuple id=\"2\"><status><basic>"
                    + "open" + "</basic></status></tuple></presence>";
            assertTrue(buddy2sim.sendNotify(SubscriptionStateHeader.ACTIVE,
                    null, notify_body, 3600, true));

            RequestEvent reqevent1 = s1.waitNotify(1000); // wait for notify
            // on 1
            RequestEvent reqevent2 = s2.waitNotify(1000); // wait for notify
            // on 2

            response = s1.processNotify(reqevent1); // process
            // notify
            // on 1
            assertTrue(s1.replyToNotify(reqevent1, response)); // send reply

            response = s2.processNotify(reqevent2); // process notify on 2
            assertTrue(s2.replyToNotify(reqevent2, response)); // send reply

            assertNoSubscriptionErrors(s1);
            assertNoSubscriptionErrors(s2);

            // end setting up two buddies

            s1 = getAlicePhone().getBuddyInfo(buddy);
            assertNotNull(s1);
            assertEquals(0, s1.getEventErrors().size());
            s2 = getAlicePhone().getBuddyInfo(buddy2);
            assertNotNull(s2);
            assertEquals(0, s2.getEventErrors().size());

            request = (Request) request.clone();
            trans = sender.sendStatefulNotify(request, true); // resend last
            // notify
            assertNotNull(sender.getErrorMessage(), trans);

            // get the response
            event = sender.waitResponse(trans, 2000);
            assertNotNull(sender.getErrorMessage(), event);
            assertTrue("Expected challenge", sender
                    .needAuthorization((ResponseEvent) event));
            trans = sender.resendWithAuthorization((ResponseEvent) event);
            assertNotNull(sender.getErrorMessage(), trans);
            event = sender.waitResponse(trans, 2000);
            assertNotNull(sender.getErrorMessage(), event);
            response = ((ResponseEvent) event).getResponse();
            assertEquals("Should have gotten 481 response for stray NOTIFY",
                    SipResponse.CALL_OR_TRANSACTION_DOES_NOT_EXIST, response
                            .getStatusCode());

            // check presence errors on subscriptions - these won't be seen in
            // nist sip 1.2 because
            // the stack now sends the 481 automatically unless the automatic
            // dialog support flag is false
            // assertEquals(1, s1.getEventErrors().size());
            // assertEquals(1, s2.getEventErrors().size());
            // assertEquals(s1.getEventErrors().getFirst(), s2.getEventErrors()
            // .getFirst());
            // s1.clearEventErrors();
            // s2.clearEventErrors();

            // //////////////////////////////////////////////////////////
            // repeat w/2 buddies using correct presentity but wrong event ID.
            // Verify
            // presence event on both

            request = sipStack.getMessageFactory().createRequest(
                    "NOTIFY sip:amit@"
                            + properties.getProperty("javax.sip.IP_ADDRESS")
                            + ':' + myPort + ";transport=udp SIP/2.0");
            notify_body = "<?xml version='1.0' encoding='UTF-8'?> "
                    + " <presence entity='" + buddy
                    + "' xmlns='urn:ietf:params:xml:ns:pidf'><tuple id='1'>"
                    + "<status><basic>closed</basic></status>"
                    + "</tuple></presence>";

            sender.addNotifyHeaders(request, "amit", properties
                    .getProperty("sipunit.test.domain"),
                    SubscriptionStateHeader.ACTIVE, "late", notify_body, 1000);

            EventHeader ehdr = (EventHeader) request
                    .getHeader(EventHeader.NAME);
            ehdr.setEventId("unmatched-eventid");
            trans = sender.sendStatefulNotify(request, true);
            assertNotNull(sender.getErrorMessage(), trans);

            // get the response
            event = sender.waitResponse(trans, 2000);
            assertNotNull(sender.getErrorMessage(), event);
            assertTrue("Expected challenge", sender
                    .needAuthorization((ResponseEvent) event));
            trans = sender.resendWithAuthorization((ResponseEvent) event);
            assertNotNull(sender.getErrorMessage(), trans);
            event = sender.waitResponse(trans, 2000);
            assertNotNull(sender.getErrorMessage(), event);
            response = ((ResponseEvent) event).getResponse();
            assertEquals("Should have gotten 481 response for stray NOTIFY",
                    SipResponse.CALL_OR_TRANSACTION_DOES_NOT_EXIST, response
                            .getStatusCode());

            // check presence errors on subscriptions - these won't be seen in
            // nist sip 1.2 because
            // the stack now sends the 481 automatically unless the automatic
            // dialog support flag is false
            // assertEquals(1, s1.getEventErrors().size());
            // assertEquals(1, s2.getEventErrors().size());
            // assertEquals(s1.getEventErrors().getFirst(), s2.getEventErrors()
            // .getFirst());
            // s1.clearEventErrors();
            // s2.clearEventErrors();

            sender.dispose();
            buddy1sim.dispose();
            buddy2sim.dispose();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Exception: " + e.getClass().getName() + ": " + e.getMessage());
        }

    }
*/

}