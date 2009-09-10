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
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SubscribeSession;
import org.cipango.kaleo.location.event.ReginfoDocument;
import org.cipango.kaleo.location.event.ContactDocument.Contact;
import org.cipango.kaleo.location.event.ContactDocument.Contact.Event;
import org.cipango.kaleo.location.event.ReginfoDocument.Reginfo;
import org.cipango.kaleo.location.event.RegistrationDocument.Registration;
import org.cipango.kaleo.location.event.RegistrationDocument.Registration.State;

public class RegEventTest extends UaTestCase
{
	/**
	 * <pre>
	 *  Alice               Kaleo              SipUnit
          |                   |(1) SUBSCRIBE      |
          |                   |Event:reg          |
          |                   |<------------------|
          |                   |(2) 200 OK         |
          |                   |------------------>|
          |                   |(3) NOTIFY         |
          |                   |------------------>|
          |                   |(4) 200 OK         |
          |                   |<------------------|
          |(5) REGISTER       |                   |
          |Expires: 1800      |                   |
          |------------------>|                   |
          |(6) 200 OK         |                   |
          |<------------------|                   |
          |                   |(7) NOTIFY         |
          |                   |------------------>|
          |                   |(8) 200 OK         |
          |                   |<------------------|
          |(9) REGISTER       |                   |
          |Expires: 0         |                   |
          |------------------>|                   |
          |(10) 200 OK        |                   |
          |<------------------|                   |
          |                   |(11) NOTIFY        |
          |                   |------------------>|
          |                   |(12) 200 OK        |
          |                   |<------------------|
          |                   |(13) SUBSCRIBE     |
          |                   |Expires: 0         |
          |                   |<------------------|
          |                   |(14) 200 OK        |
          |                   |------------------>|
          |                   |(15) NOTIFY        |
          |                   |------------------>|
          |                   |(16) 200 OK        |
          |                   |<------------------|
     * </pre>
	 */
	public void testSubscription()
	{
		SubscribeSession session = new SubscribeSession(getAlicePhone(), "reg");
		Request subscribe = session.newInitialSubscribe(100, getAliceUri());
		Response response = session.sendRequest(subscribe, null, null, Response.OK);
		
		ServerTransaction tx = session.waitForNotify(2000);
		Request notify = tx.getRequest();
		System.out.println(notify);
		session.sendResponse(Response.OK, tx);
		SubscriptionStateHeader subState = (SubscriptionStateHeader) notify.getHeader(SubscriptionStateHeader.NAME);
		assertEquals(SubscriptionStateHeader.ACTIVE.toLowerCase(), subState.getState().toLowerCase());
		assertBetween(95, 100, subState.getExpires());
		assertEquals("reg", ((EventHeader) notify.getHeader(EventHeader.NAME)).getEventType());
		Reginfo regInfo = getRegInfo(notify);
		int version = regInfo.getVersion().intValue();
		Registration registration = regInfo.getRegistrationArray(0);
		assertEquals(State.INIT, registration.getState());
		assertEquals(getAliceUri(), registration.getAor());
		assertEquals(0, registration.getContactArray().length);
		
		getAlicePhone().register(null, 1800);
		assertLastOperationSuccess(getAlicePhone());
		
		tx = session.waitForNotify(2000);
		notify = tx.getRequest();
		System.out.println(notify);
		session.sendResponse(Response.OK, tx);
		regInfo = getRegInfo(notify);
		registration = regInfo.getRegistrationArray(0);
		assertEquals(1, registration.getContactArray().length);
		assertEquals(version + 1, regInfo.getVersion().intValue());
		assertEquals(State.ACTIVE, registration.getState());
		Contact contact = registration.getContactArray(0);
		assertBetween(1795, 1800, contact.getExpires().intValue());
		assertEquals(Event.REGISTERED, contact.getEvent());
		
		getAlicePhone().unregister(null, 2000);
		assertLastOperationSuccess(getAlicePhone());
		tx = session.waitForNotify(2000);
		notify = tx.getRequest();
		System.out.println(notify);
		session.sendResponse(Response.OK, tx);
		regInfo = getRegInfo(notify);
		registration = regInfo.getRegistrationArray(0);
		assertEquals(1, registration.getContactArray().length);
		assertEquals(version + 2, regInfo.getVersion().intValue());
		assertEquals(State.TERMINATED, registration.getState());
		contact = registration.getContactArray(0);
		assertEquals(0, contact.getExpires().intValue());
		assertEquals(Event.UNREGISTERED, contact.getEvent());
		
		subscribe = session.newSubsequentSubscribe(0);
		response  = session.sendRequest(subscribe, null, null, Response.OK);
		
		tx = session.waitForNotify(2000);
		notify = tx.getRequest();
		System.out.println(notify);
		session.sendResponse(Response.OK, tx);
		subState = (SubscriptionStateHeader) notify.getHeader(SubscriptionStateHeader.NAME);
		assertEquals(SubscriptionStateHeader.TERMINATED.toLowerCase(), 
				subState.getState());
		regInfo = getRegInfo(notify);
		registration = regInfo.getRegistrationArray(0);
		assertEquals(State.TERMINATED, registration.getState());
		assertEquals(0, registration.getContactArray().length);
		assertEquals(version + 3, regInfo.getVersion().intValue());
	}
	
	private Reginfo getRegInfo(Request request)
	{
		ContentTypeHeader contentType = (ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME);
		assertEquals("application", contentType.getContentType());
		assertEquals("reginfo+xml", contentType.getContentSubType());
		try
		{
			return ReginfoDocument.Factory.parse(new ByteArrayInputStream(request.getRawContent())).getReginfo();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
