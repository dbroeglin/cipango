package org.cipango.callflow.diameter;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.cipango.callflow.diameter.JmxMessageLogger.Direction;
import org.cipango.diameter.DiameterConnection;
import org.cipango.diameter.DiameterMessage;

public class MessageInfo
{
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private DiameterMessage _message;
	private Direction _direction;
	private DiameterConnection _connection;
	
	private long _date;
	
	public MessageInfo(DiameterMessage message, Direction direction, DiameterConnection connection)
	{
		_message = message;
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
	public DiameterMessage getMessage()
	{
		return _message;
	}

	public Direction getDirection()
	{
		return _direction;
	}

	public DiameterConnection getConnection()
	{
		return _connection;
	}
	public String getLocal()
	{
		return _connection.getLocalAddr() + ":" + _connection.getLocalPort();
	}
	public String getRemote()
	{
		return _connection.getRemoteAddr() + ":" + _connection.getRemotePort();
	}

	/**
	 * Time since the message has been received or sent in seconds
	 */
	public long getRelativeTime()
	{
		return (System.currentTimeMillis() - _date)/1000;
	}
}
