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

package org.cipango.log;

import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import org.cipango.SipGenerator;
import org.cipango.SipMessage;
import org.cipango.sip.SipConnectors;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;
import org.mortbay.util.DateCache;
import org.mortbay.util.StringUtil;

public abstract class AbstractMessageLog extends AbstractLifeCycle implements AccessLog
{
	private DateCache _logDateCache;
    private String _logDateFormat = "yyyy-MM-dd HH:mm:ss"; //"yyyy-MM-dd HH:mm:ss.SSS ZZZ";
    private Locale _logLocale     = Locale.getDefault();
    private String _logTimeZone   = TimeZone.getDefault().getID();
    private SipGenerator _generator;
    private StringBuffer _buf = new StringBuffer();
    private Buffer _buffer;
    
	private static final int IN = 0;
	private static final int OUT = 1;
    
    protected void doStart() throws Exception 
    {	
		try
		{
			_logDateCache = new DateCache(_logDateFormat, _logLocale);
			_logDateCache.setTimeZoneID(_logTimeZone);
			
			_generator = new SipGenerator();
			_buffer = new ByteArrayBuffer(64000);
			
			super.doStart();
	        
		}
		catch (Exception e) 
		{
			Log.warn("Unable to log SIP messages: " + e.getMessage());
		}
	}
    
    public void messageReceived(SipMessage message)
	{
    	if (!isStarted()) return;
    	
    	try
    	{
    		doLog(message, IN, message.getTransport(), 
    				message.getLocalAddr(), message.getLocalPort(),
    				message.getRemoteAddr(), message.getRemotePort());
    	}
    	catch (Exception e)
    	{
    		Log.warn("Failed to log message", e);
    	}
	}
    
	public void messageSent(SipMessage message, int transport, String localAddr, int localPort, String remoteAddr, int remotePort)
	{
    	if (!isStarted()) return;
    	
    	try
    	{
    		doLog(message, OUT, SipConnectors.getName(transport), localAddr, localPort, remoteAddr, remotePort);
    	}
		catch (Exception e)
		{
			Log.warn("Failed to log message", e);
		}
	}
	
	public abstract void doLog(SipMessage message, int direction, String transport, String localAddr, int localPort, String remoteAddr, int remotePort) throws IOException;
	
	protected String generateInfoLine(int direction, String transport, String localAddr, int localPort, String remoteAddr, int remotePort, long date)
	{
		_buf.setLength(0);
        _buf.append(_logDateCache.format(date));
        if (direction == IN)
			_buf.append(" IN  ");
		else
            _buf.append(" OUT ");
		
		_buf.append(transport);
        _buf.append(" ");
        _buf.append(localAddr);
        _buf.append(':');
        _buf.append(localPort);
        
        if (direction == IN)
        	_buf.append(" < ");
        else
        	_buf.append(" > ");
        
        _buf.append(remoteAddr);
        _buf.append(':');
        _buf.append(remotePort);
        _buf.append(StringUtil.__LINE_SEPARATOR);
        _buf.append(StringUtil.__LINE_SEPARATOR);
        return _buf.toString();
	}
	
	protected Buffer generateMessage(SipMessage message)
	{
		_buffer.clear();
		_generator.generate(_buffer, message);
		return _buffer;
	}
}
