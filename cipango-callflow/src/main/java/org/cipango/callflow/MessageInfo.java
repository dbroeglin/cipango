package org.cipango.callflow;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.sip.SipServletRequest;

import org.cipango.server.SipConnection;
import org.cipango.server.SipMessage;
import org.cipango.server.SipResponse;
import org.cipango.server.log.AbstractMessageLog;

public class MessageInfo
{
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SipMessage _sipMessage;
	private int _direction;
	private SipConnection _connection;
	private long _date;
	
	public MessageInfo(SipMessage message, int direction, SipConnection connection)
	{
		if (direction == AbstractMessageLog.IN)
			_sipMessage = (SipMessage) message.clone();
		else
			_sipMessage = message;
		_direction = direction;
		_connection = connection;
		_date = System.currentTimeMillis();
		
	}
	
	public long getDate()
	{
		return _date;
	}
	public String getFormatedDate()
	{
		return DATE_FORMAT.format(new Date(_date));
	}
	public SipMessage getMessage()
	{
		return _sipMessage;
	}

	public int getDirection()
	{
		return _direction;
	}

	public SipConnection getConnection()
	{
		return _connection;
	}
	public String getLocal()
	{
		return _connection.getLocalAddress() + ":" + _connection.getLocalPort();
	}
	
	public String getLocalKey()
	{
		return _connection.getLocalAddress().getHostAddress() + ":" + _connection.getLocalPort();
	}
	
	public String getRemote()
	{
		return _connection.getRemoteAddress() + ":" + _connection.getRemotePort();
	}
	
	public String getRemoteKey()
	{
		return _connection.getRemoteAddress().getHostAddress() + ":" + _connection.getRemotePort();
	}
	
	public String getShortName()
	{
		if (_sipMessage.isRequest())
			return _sipMessage.getMethod() + " " + ((SipServletRequest) _sipMessage).getRequestURI();
		else
		{
			SipResponse response = (SipResponse) _sipMessage;
			return response.getStatus() + " " + response.getReasonPhrase();
		}
	}
	/**
	 * Time since the message has been received or sent in seconds
	 */
	public long getRelativeTime()
	{
		return (System.currentTimeMillis() - _date)/1000;
	}
}
