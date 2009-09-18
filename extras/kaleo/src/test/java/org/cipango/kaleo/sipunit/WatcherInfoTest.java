// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.kaleo.sipunit;

import java.io.ByteArrayInputStream;

import javax.sip.ServerTransaction;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.MinExpiresHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SubscribeSession;
import org.cipango.kaleo.presence.PresenceEventPackage;
import org.cipango.kaleo.presence.watcherinfo.WatcherInfoEventPackage;
import org.cipango.kaleo.presence.watcherinfo.WatcherinfoDocument;
import org.cipango.kaleo.presence.watcherinfo.WatcherDocument.Watcher;
import org.cipango.kaleo.presence.watcherinfo.WatcherDocument.Watcher.Event;
import org.cipango.kaleo.presence.watcherinfo.WatcherDocument.Watcher.Status;
import org.cipango.kaleo.presence.watcherinfo.WatcherListDocument.WatcherList;
import org.cipango.kaleo.presence.watcherinfo.WatcherinfoDocument.Watcherinfo;

public class WatcherInfoTest extends UaTestCase
{
	/**
	 * <pre>
	 *  Alice               Kaleo              SipUnit
          |                   |(1) SUBSCRIBE        |
          |                   |Event:presence.winfo |
          |                   |<--------------------|
          |                   |(2) 200 OK           |
          |                   |-------------------->|
          |                   |(3) NOTIFY           |
          |                   |-------------------->|
          |                   |(4) 200 OK           |
          |                   |<--------------------|
          |(5) SUBSCRIBE      |                     |
          |Event:presence     |                     |
          |------------------>|                     |
          |(6) 200 OK         |                     |
          |<------------------|                     |
          |                   |(7) NOTIFY           |
          |                   |-------------------->|
          |                   |(8) 200 OK           |
          |                   |<--------------------|
          |(9) NOTIFY         |                     |
          |<------------------|                     |
          |(10) 200 OK        |                     |
          |------------------>|                     |
          |(11) SUBSCRIBE     |                     |
          |Expires: 0         |                     |
          |------------------>|                     |
          |(12) 200 OK        |                     |
          |<------------------|                     |
          |                   |(13) NOTIFY          |
          |                   |-------------------->|
          |                   |(14) 200 OK          |
          |                   |<--------------------|
          |(15) NOTIFY        |                     |
          |<------------------|                     |
          |(16) 200 OK        |                     |
          |------------------>|                     |
          |                   |(17) SUBSCRIBE       |
          |                   |Expires: 0           |
          |                   |<--------------------|
          |                   |(18) 200 OK          |
          |                   |-------------------->|
          |                   |(19) NOTIFY          |
          |                   |-------------------->|
          |                   |(20) 200 OK          |
          |                   |<--------------------|
     * </pre>
	 */
	public void testSubscription()
	{		
		SubscribeSession winfoSession = new SubscribeSession(getBobPhone(), "presence.winfo"); // 1
		Request subscribe = winfoSession.newInitialSubscribe(100, getBobUri());
		winfoSession.sendRequest(subscribe, Response.OK); // 2
		
		ServerTransaction tx = winfoSession.waitForNotify(); // 3
		Request notify = tx.getRequest();
		//System.out.println(notify);
		winfoSession.sendResponse(Response.OK, tx); // 4 
		SubscriptionStateHeader subState = (SubscriptionStateHeader) notify.getHeader(SubscriptionStateHeader.NAME);
		assertEquals(SubscriptionStateHeader.ACTIVE.toLowerCase(), subState.getState().toLowerCase());
		assertBetween(95, 100, subState.getExpires());
		assertEquals(WatcherInfoEventPackage.NAME, ((EventHeader) notify.getHeader(EventHeader.NAME)).getEventType());
		Watcherinfo watcherinfo = getWatcherinfo(notify);
		assertEquals(0, watcherinfo.getVersion().intValue());
		assertEquals(Watcherinfo.State.FULL, watcherinfo.getState());
		assertEquals(1, watcherinfo.getWatcherListArray().length);
		WatcherList watcherList = watcherinfo.getWatcherListArray(0);
		assertEquals(getBobUri(), watcherList.getResource());
		assertEquals(PresenceEventPackage.NAME, watcherList.getPackage());
		assertEquals(0, watcherList.getWatcherArray().length);
		
		
		SubscribeSession presenceSession = new SubscribeSession(getAlicePhone(), "presence");
		subscribe = presenceSession.newInitialSubscribe(100, getBobUri()); // 5
		presenceSession.sendRequest(subscribe, Response.OK); // 6
		
		tx = winfoSession.waitForNotify(); // 7
		notify = tx.getRequest();
		//System.out.println(notify);
		winfoSession.sendResponse(Response.OK, tx); // 8
		watcherinfo = getWatcherinfo(notify);
		assertEquals(1, watcherinfo.getVersion().intValue());
		watcherList = watcherinfo.getWatcherListArray(0);
		assertEquals(1, watcherList.sizeOfWatcherArray());
		Watcher watcher = watcherList.getWatcherArray(0);
		assertEquals(Event.SUBSCRIBE, watcher.getEvent());
		assertEquals(getAliceUri(), watcher.getStringValue());
		assertEquals(Status.ACTIVE, watcher.getStatus());
		
		
		tx = presenceSession.waitForNotify(); // 9
		presenceSession.sendResponse(Response.OK, tx); // 10
		
		
		subscribe = presenceSession.newSubsequentSubscribe(0); // 11
		presenceSession.sendRequest(subscribe, Response.OK); // 12
		
		tx = winfoSession.waitForNotify(); // 13
		notify = tx.getRequest();
		//System.out.println(notify);
		winfoSession.sendResponse(Response.OK, tx); // 14
		watcherinfo = getWatcherinfo(notify);
		assertEquals(2, watcherinfo.getVersion().intValue());
		watcherList = watcherinfo.getWatcherListArray(0);
		assertEquals(1, watcherList.sizeOfWatcherArray());
		watcher = watcherList.getWatcherArray(0);
		assertEquals(Event.TIMEOUT, watcher.getEvent());
		assertEquals(getAliceUri(), watcher.getStringValue());
		assertEquals(Status.TERMINATED, watcher.getStatus());
		
		tx = presenceSession.waitForNotify(); // 15
		presenceSession.sendResponse(Response.OK, tx); // 16
		
		subscribe = winfoSession.newSubsequentSubscribe(0); // 17
		winfoSession.sendRequest(subscribe, Response.OK); //18
		
		tx = winfoSession.waitForNotify(); // 19
		notify = tx.getRequest();
		//System.out.println(notify);
		winfoSession.sendResponse(Response.OK, tx); // 20
		watcherinfo = getWatcherinfo(notify);
		assertEquals(3, watcherinfo.getVersion().intValue());
		watcherList = watcherinfo.getWatcherListArray(0);
		assertEquals(0, watcherList.sizeOfWatcherArray());
		
	}
	
	/**
	 * <pre>
	 *  Alice               Kaleo              SipUnit
          |(1) SUBSCRIBE      |                     |
          |Event:presence     |                     |
          |------------------>|                     |
          |(2) 200 OK         |                     |
          |<------------------|                     |
          |(3) NOTIFY         |                     |
          |<------------------|                     |
          |(4) 200 OK         |                     |
          |------------------>|                     |
          |                   |(5) SUBSCRIBE        |
          |                   |Event:presence.winfo |
          |                   |<--------------------|
          |                   |(6) 200 OK           |
          |                   |-------------------->|
          |                   |(7) NOTIFY           |
          |                   |-------------------->|
          |                   |(8) 200 OK           |
          |                   |<--------------------|
          |(9) SUBSCRIBE      |                     |
          |Expires: 0         |                     |
          |------------------>|                     |
          |(10) 200 OK        |                     |
          |<------------------|                     |
          |(11) NOTIFY        |                     |
          |<------------------|                     |
          |(12) 200 OK        |                     |
          |------------------>|                     |

     * </pre>
	 */
	public void testSubscription2()
	{		
		SubscribeSession presenceSession = new SubscribeSession(getAlicePhone(), "presence");
		Request subscribe = presenceSession.newInitialSubscribe(100, getBobUri()); // 1
		presenceSession.sendRequest(subscribe, Response.OK); // 2

		ServerTransaction tx = presenceSession.waitForNotify(); // 3
		presenceSession.sendResponse(Response.OK, tx); // 4
		
		SubscribeSession winfoSession = new SubscribeSession(getBobPhone(), "presence.winfo"); // 5
		 subscribe = winfoSession.newInitialSubscribe(0, getBobUri());
		winfoSession.sendRequest(subscribe, Response.OK); // 6
		
		tx = winfoSession.waitForNotify(); // 7
		Request notify = tx.getRequest();
		//System.out.println(notify);
		winfoSession.sendResponse(Response.OK, tx); // 8 
		SubscriptionStateHeader subState = (SubscriptionStateHeader) notify.getHeader(SubscriptionStateHeader.NAME);
		assertEquals(SubscriptionStateHeader.TERMINATED.toLowerCase(), subState.getState().toLowerCase());
		assertEquals(WatcherInfoEventPackage.NAME, ((EventHeader) notify.getHeader(EventHeader.NAME)).getEventType());
		Watcherinfo watcherinfo = getWatcherinfo(notify);
		assertEquals(0, watcherinfo.getVersion().intValue());
		assertEquals(Watcherinfo.State.FULL, watcherinfo.getState());
		assertEquals(1, watcherinfo.getWatcherListArray().length);
		WatcherList watcherList = watcherinfo.getWatcherListArray(0);
		assertEquals(getBobUri(), watcherList.getResource());
		assertEquals(PresenceEventPackage.NAME, watcherList.getPackage());
		assertEquals(1, watcherList.getWatcherArray().length);
		Watcher watcher = watcherList.getWatcherArray(0);
		assertEquals(Event.SUBSCRIBE, watcher.getEvent());
		assertEquals(getAliceUri(), watcher.getStringValue());
		assertEquals(Status.ACTIVE, watcher.getStatus());
		
		
		subscribe = presenceSession.newSubsequentSubscribe(0); // 9
		presenceSession.sendRequest(subscribe, Response.OK); // 10
				
		tx = presenceSession.waitForNotify(); // 11
		presenceSession.sendResponse(Response.OK, tx); // 12
			
	}
	
	private Watcherinfo getWatcherinfo(Request request)
	{
		ContentTypeHeader contentType = (ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME);
		assertEquals("application", contentType.getContentType());
		assertEquals("watcherinfo+xml", contentType.getContentSubType());
		try
		{
			return WatcherinfoDocument.Factory.parse(new ByteArrayInputStream(request.getRawContent())).getWatcherinfo();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
    public void testMinExpires() throws Exception
    {       
        SubscribeSession session = new SubscribeSession(getAlicePhone(), "presence.winfo");
        Request request = session.newInitialSubscribe(1, getAliceUri());
        Response response = session.sendRequest(request, SipResponse.INTERVAL_TOO_BRIEF);
        MinExpiresHeader minExpiresHeader = (MinExpiresHeader) response.getHeader(MinExpiresHeader.NAME);
        assertNotNull(minExpiresHeader);
    }
    

}
