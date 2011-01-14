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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletMessage.HeaderForm;

public abstract class SipMessageImpl implements SipMessage
{
	protected SipServletMessage _message;
	protected boolean _hasBeenRead = false;
	
	public SipMessageImpl(SipServletMessage message)
	{
		_message = message;
		_message.setAttribute(SipMessage.class.getName(), this);
	}
	
	public void addAcceptLanguage(Locale locale)
	{
		_message.addAcceptLanguage(locale);
	}

	public void addAddressHeader(String name, Address addr, boolean first)
	{
		_message.addAddressHeader(name, addr, first);
	}

	public void addHeader(String name, String value)
	{
		_message.addHeader(name, value);
	}

	public void addParameterableHeader(String name, Parameterable param, boolean first)
	{
		_message.addParameterableHeader(name, param, first);
	}

	public Locale getAcceptLanguage()
	{
		return _message.getAcceptLanguage();
	}

	public Iterator<Locale> getAcceptLanguages()
	{
		return _message.getAcceptLanguages();
	}

	public Address getAddressHeader(String name) throws ServletParseException
	{
		return _message.getAddressHeader(name);
	}

	public ListIterator<Address> getAddressHeaders(String name) throws ServletParseException
	{
		return _message.getAddressHeaders(name);
	}

	public Object getAttribute(String name)
	{
		return _message.getAttribute(name);
	}

	public Enumeration<String> getAttributeNames()
	{
		return _message.getAttributeNames();
	}

	public String getCallId()
	{
		return _message.getCallId();
	}

	public String getCharacterEncoding()
	{
		return _message.getCharacterEncoding();
	}

	public Object getContent() throws IOException, UnsupportedEncodingException
	{
		return _message.getContent();
	}

	public Locale getContentLanguage()
	{
		return _message.getContentLanguage();
	}

	public int getContentLength()
	{
		return _message.getContentLength();
	}

	public String getContentType()
	{
		return _message.getContentType();
	}

	public int getExpires()
	{
		return _message.getExpires();
	}

	public Address getFrom()
	{
		return _message.getFrom();
	}

	public String getHeader(String name)
	{
		return _message.getHeader(name);
	}

	public HeaderForm getHeaderForm()
	{
		return _message.getHeaderForm();
	}

	public Iterator<String> getHeaderNames()
	{
		return _message.getHeaderNames();
	}

	public ListIterator<String> getHeaders(String name)
	{
		return _message.getHeaders(name);
	}

	public String getLocalAddr()
	{
		return _message.getLocalAddr();
	}

	public int getLocalPort()
	{
		return _message.getLocalPort();
	}

	public String getMethod()
	{
		return _message.getMethod();
	}

	public Parameterable getParameterableHeader(String name) throws ServletParseException
	{
		return _message.getParameterableHeader(name);
	}

	public ListIterator<? extends Parameterable> getParameterableHeaders(String name)
			throws ServletParseException
	{
		return _message.getParameterableHeaders(name);
	}

	public String getProtocol()
	{
		return _message.getProtocol();
	}

	public byte[] getRawContent() throws IOException
	{
		return _message.getRawContent();
	}

	public String getRemoteAddr()
	{
		return _message.getRemoteAddr();
	}

	public int getRemotePort()
	{
		return _message.getRemotePort();
	}

	public String getRemoteUser()
	{
		return _message.getRemoteUser();
	}

	public SipSession getSession()
	{
		return (SipSession) _message.getSession().getAttribute(SipSession.class.getName());
	}

	public Address getTo()
	{
		return _message.getTo();
	}

	public String getTransport()
	{
		return _message.getTransport();
	}

	public boolean isCommitted()
	{
		return _message.isCommitted();
	}

	public boolean isSecure()
	{
		return _message.isSecure();
	}

	public void removeAttribute(String name)
	{
		_message.removeAttribute(name);
	}

	public void removeHeader(String name)
	{
		_message.removeHeader(name);
	}

	public void send() throws IOException
	{
		_message.send();
	}

	public void setAcceptLanguage(Locale locale)
	{
		_message.setAcceptLanguage(locale);
	}

	public void setAddressHeader(String name, Address addr)
	{
		_message.setAddressHeader(name, addr);
	}

	public void setAttribute(String name, Object o)
	{
		_message.setAttribute(name, o);
	}

	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException
	{
		_message.setCharacterEncoding(enc);
	}

	public void setContent(Object content, String contentType) throws UnsupportedEncodingException
	{
		_message.setContent(content, contentType);
	}

	public void setContentLanguage(Locale locale)
	{
		_message.setContentLanguage(locale);
	}

	public void setContentLength(int len)
	{
		_message.setContentLength(len);
	}

	public void setContentType(String type)
	{
		_message.setContentType(type);
	}

	public void setExpires(int seconds)
	{
		_message.setExpires(seconds);
	}

	public void setHeader(String name, String value)
	{
		_message.setHeader(name, value);
	}

	public void setHeaderForm(HeaderForm form)
	{
		_message.setHeaderForm(form);
	}

	public void setParameterableHeader(String name, Parameterable param)
	{
		_message.setParameterableHeader(name, param);
	}

	@Override
	public int hashCode()
	{
		return _message.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return _message.equals(obj);
	}

	@Override
	public String toString()
	{
		return _message.toString();
	}

	public boolean hasBeenRead()
	{
		return _hasBeenRead;
	}

	public void setHasBeenRead(boolean hasBeenRead)
	{
		_hasBeenRead = hasBeenRead;
	}
	
	public Session session()
	{
		return (Session) getSession();
	}
}
