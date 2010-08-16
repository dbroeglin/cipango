// ========================================================================
// Copyright 2004-2006 Mort Bay Consulting Pty. Ltd.
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

package org.cipango.sip;

import java.io.IOException;

import javax.servlet.sip.SipServletResponse;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache.CachedBuffer;
import org.eclipse.jetty.io.BufferUtil;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.io.View;
import org.eclipse.jetty.util.log.Log;

/**
 * @author gregw
 * @author thomasl
 */
public class SipParser 
{
	private static final int STATE_START = -9;
	private static final int STATE_FIELD0 = -8;
	private static final int STATE_SPACE0 = -7;
	private static final int STATE_FIELD1 = -6;
	private static final int STATE_SPACE1 = -5;
	private static final int STATE_FIELD2 = -4;
	
	private static final int STATE_HEADER = -3;
	private static final int STATE_HEADER_NAME = -2;
	private static final int STATE_HEADER_VALUE	= -1;
	private static final int STATE_END =  0;
	
	private static final int STATE_EOF_CONTENT = 1;
	private static final int STATE_CONTENT = 2;
	
	private static final int UNKNOWN_CONTENT = -1;
	private static final int NO_CONTENT = 0;
	
	private Buffer _buffer;
	
	private Buffer _header;
	private Buffer _body;
	
	private CachedBuffer _cached;
	
	private EndPoint _endpoint;
	
	private int _state = STATE_START;
	private byte _eol;
	
	private EventHandler _handler;
	
	private View _token0;
	private View _token1;
	
	private View contentView = new View();
	private int _length;
	private boolean _response;
	
	private int headerBufferSize = 10 * 1024;
	private int contentBufferSize = 10 * 1024;
	
	private String _multiline;
	private int _contentLength;
	private int _contentPosition;
	
	private int _streamDefaultHeaderSize = 5 * 1024;
	private int _streamMaxHeaderSize = 500 * 1024;
	
	public SipParser(Buffer buffer, EventHandler handler) 
	{
		_buffer = buffer;
		_handler = handler;
		if (buffer != null) 
		{
			_token0 = new View(buffer);
			_token1 = new View(buffer);
			_token0.setPutIndex(_token0.getIndex());
			_token1.setPutIndex(_token1.getIndex());
		}
	}
	
	public SipParser(Buffer buffer, EndPoint endpoint, EventHandler handler) 
	{
		_buffer = buffer;
		_token0 = new View(_buffer);
		_token1 = new View(_buffer);
		_token0.setPutIndex(_token0.getIndex());
		_token1.setPutIndex(_token1.getIndex());
		
		_endpoint = endpoint;
		_handler = handler;
	}
	
	public int getState() 
	{
		return _state;
	}	
	
	public void parse() throws IOException 
	{
		if (_state == STATE_END)
			setBuffer(_buffer);
		
		if (_state != STATE_START) 
			throw new IllegalStateException("!STATE_START (" + _state + ")");
		
		while (_state != STATE_END) 
			parseNext();
	}
	
	public int parseNext() throws IOException 
	{
		int totalFilled = -1;
		
		if (_buffer == null)
			throw new IOException("No buffer");
		/*
		if (_buffer == null) 
		{
			if (_header == null)
				_header = _buffers.getBuffer(headerBufferSize);
			
			_buffer = _header;
			_token0 = new View(_buffer);
			_token1 = new View(_buffer);
			_token0.setPutIndex(_token0.getIndex());
			_token1.setPutIndex(_token1.getIndex());
		}
		*/
		
		if (_state == STATE_END) 
			throw new IllegalStateException("STATE_END");
		
		if (_state == STATE_CONTENT && _contentPosition == _contentLength) 
		{
			_state = STATE_END;
			// TODO FIXME content(...) 
			return totalFilled;
		}
		
		int length = _buffer.length();

		if (length == 0) 
		{
			int filled = -1;
		
			if (_buffer.markIndex() == 0 && _buffer.putIndex() == _buffer.capacity()) 
				throw new BufferOverflowException("FULL");
			
			if (_endpoint != null && filled <= 0) 
			{
				// TODO compact ??
				
				if (_buffer.space() == 0) 
					throw new BufferOverflowException("FULL");
	
				try 
				{
					if (totalFilled < 0) 
						totalFilled = 0;
					
					filled = _endpoint.fill(_buffer);
					
					if (filled > 0)
						totalFilled += filled;
				} 
				catch (IOException e) 
				{
					Log.debug(e);
					throw (e instanceof EofException) ? e:new EofException(e);			
				}
			}
			
			if (filled < 0)
			{
				if (_state == STATE_EOF_CONTENT) 
				{
					_state = STATE_END;
					_handler.content(_buffer.sliceFromMark(_contentPosition));
					return totalFilled;
				}
				//reset();
				throw new EofException();
			}
			length = _buffer.length();
		}
		
		byte b;
		byte[] array = _buffer.array();
		
		while (_state < STATE_END && length --> 0) 
		{
			b = _buffer.get();
			if (_eol == SipGrammar.CR && b == SipGrammar.LF) 
			{
				_eol = SipGrammar.LF;
				continue;
			}
			_eol = 0;
			
			switch (_state) 
			{
			case STATE_START:
				_contentLength = UNKNOWN_CONTENT;
				_cached = null;
				if (b < 0 || b > SipGrammar.SPACE) 
				{
					_buffer.mark();
					_state = STATE_FIELD0;
				}
				break;
				
			case STATE_FIELD0:
				if (b == SipGrammar.SPACE) 
				{
					_token0.update(_buffer.markIndex(), _buffer.getIndex() - 1);
					_state = STATE_SPACE0;
				} 
				else if (b >= 0 && b < SipGrammar.SPACE) // TODO token only
				{ 
					throw new SipException(SipServletResponse.SC_BAD_REQUEST);
				}
				break;
				
			case STATE_SPACE0:
				if (b < 0 || b > SipGrammar.SPACE) 
				{
					_buffer.mark();
					_state = STATE_FIELD1;
					_response = (b >= '1' && b <= '6');
				} 
				else if (b < SipGrammar.SPACE) 
				{
					throw new SipException(SipServletResponse.SC_BAD_REQUEST);
				}
				break;
				
			case STATE_FIELD1: 
				if (b == SipGrammar.SPACE) 
				{
					_token1.update(_buffer.markIndex(), _buffer.getIndex() - 1);
					_state = STATE_SPACE1;
				}
				break;
				
			case STATE_SPACE1: 
				if (b < 0 || b > SipGrammar.SPACE) 
				{
					_buffer.mark();
					_state = STATE_FIELD2;
				}
				else if (b < SipGrammar.SPACE) 
				{
					// Case no reason phrase
					_state = STATE_FIELD2;
					_buffer.mark(0);
				}
				break;
				
			case STATE_FIELD2: 
				if (b == SipGrammar.CR || b == SipGrammar.LF) 
				{
					if (_response) 
					{
						_handler.startResponse(
								SipVersions.CACHE.lookup(_token0),
								BufferUtil.toInt(_token1), 
								_buffer.sliceFromMark());
					} 
					else 
					{
						_handler.startRequest(
								SipMethods.CACHE.lookup(_token0),
								_token1, 
								SipVersions.CACHE.lookup(_buffer.sliceFromMark()));
					}
					
					_eol = b;
					_state = STATE_HEADER;
					_token0.setPutIndex(_token0.getIndex());
					_token1.setPutIndex(_token1.getIndex());
					_multiline = null;
					return totalFilled;
				}
				break;
					
			case STATE_HEADER:
				if (b== SipGrammar.COLON || b == SipGrammar.SPACE || b == SipGrammar.TAB) 
				{
					_length = -1;
					_state = STATE_HEADER_VALUE;
				} 
				else 
				{	
					if (_cached != null || _token0.length() > 0 || _token1.length() > 0 || _multiline != null) // TODO cached
					{ 
                        Buffer name = null;
                        if (_cached != null)
                        {
                        	//System.out.println("Cached: " + _cached);
                        	name = _cached;
                        	_cached = null;
                        }
                        else 
                        {
                        	//System.out.println("Not Cached: " + _token0);
	                        if (_token0.length() == 1)
	                        {
	                            name = SipHeaders.getCompact(_token0.peek());
	
	                            if (name == null)
	                                name = SipHeaders.CACHE.lookup(_token0);
	                        }
	                        else 
	                        {
	                            name = SipHeaders.CACHE.lookup(_token0);
	                        }
                        }
                        
                        
						Buffer value = _multiline == null ? (Buffer) _token1 : new ByteArrayBuffer(_multiline); // FIXME UTF8
						
						int ho = SipHeaders.CACHE.getOrdinal(name);
						
						if (ho == SipHeaders.CONTENT_LENGTH_ORDINAL) 
						{
							_contentLength = BufferUtil.toInt(value);
							if (_contentLength <= 0) 
								_contentLength = NO_CONTENT;					
						}
						
						_handler.header(name, value);
						_token0.setPutIndex(_token0.getIndex());
						_token1.setPutIndex(_token1.getIndex());
						_multiline = null;
					}
					
					if (b == SipGrammar.CR || b == SipGrammar.LF) 
					{
						_eol = b;
						_contentPosition = 0;
						_buffer.mark(0);
						
						if (_contentLength == UNKNOWN_CONTENT) 
						{
							if (_endpoint != null) 
								throw new IOException("No Content-Length");
							else 
								_state = STATE_EOF_CONTENT;	
						} 
						else if (_contentLength == 0) 
						{
							_state = STATE_END;
							// TODO callback
						} 
						else 
						{
							_state = STATE_CONTENT; // TODO header complete ??
						}
						return totalFilled;
					} 
					else 
					{
						
						_buffer.mark();
						_length = 1;
						_state = STATE_HEADER_NAME;	
						
						if (array != null)
							_cached = SipHeaders.CACHE.getBest(array, _buffer.markIndex(), length + 1);
						
						//System.out.println(new String(array, _buffer.markIndex(), length + 1));
						if (_cached != null)
						{
							//System.out.println("Found in cache: " + _cached);
							_length = _cached.length();
							_buffer.setGetIndex(_buffer.markIndex() + _length);
							length = _buffer.length();
						}
					}
				}
				break;
				
			case STATE_HEADER_NAME:
				if (b == SipGrammar.CR || b == SipGrammar.LF) 
				{
					if (_length > 0) 
						_token0.update(_buffer.markIndex(), _buffer.markIndex() + _length);
						
					_eol = b;
					_state = STATE_HEADER;
				}
				if (b == SipGrammar.COLON) 
				{
					if (_length > 0) 
						_token0.update(_buffer.markIndex(), _buffer.markIndex() + _length);
					
					_length = -1;
					_state = STATE_HEADER_VALUE;
				} 
				else if (b != SipGrammar.SPACE && b != SipGrammar.TAB) 
				{
					if (_length == -1) 
						_buffer.mark();
					
					_length = _buffer.getIndex() - _buffer.markIndex();
				}
				break;
				
			case STATE_HEADER_VALUE:
				if (b == SipGrammar.CR || b == SipGrammar.LF) 
				{
					if (_length > 0) 
					{
						if (_token1.length() == 0) 
						{
							_token1.update(_buffer.markIndex(), _buffer.markIndex() + _length);
						} 
						else 
						{
							if (_multiline == null) 
								_multiline = _token1.toString(); // TODO UTF-8
							
							_token1.update(_buffer.markIndex(), _buffer.markIndex() + _length);
							_multiline += " " + _token1.toString();
						}
					}
					_eol = b; 
					_state = STATE_HEADER;
				} 
				else if (b != SipGrammar.SPACE && b!= SipGrammar.TAB) 
				{
					if (_length == -1) 
						_buffer.mark();
					
					_length = _buffer.getIndex() - _buffer.markIndex();
				}
				break;
			}
		}
		
		// header done
		
		Buffer chunk;
		length = _buffer.length();
		while (_state > STATE_END && length > 0) 
		{
			if (_eol == SipGrammar.CR && _buffer.peek() == SipGrammar.LF) 
			{
				_eol = _buffer.get();
				length = _buffer.length();
				_contentPosition = 0;
				_buffer.mark(0);
				continue;
			}
			_eol = 0;
			switch (_state) 
			{
			case STATE_EOF_CONTENT:
				chunk = _buffer.get(_buffer.length());
				_contentPosition += chunk.length();
				return totalFilled;
				
			case STATE_CONTENT:
				int remaining = _contentLength - _contentPosition;
				if (remaining == 0) 
				{
					_state = STATE_END;
				} 
				else if (length >= remaining) 
				{
					length = remaining;
					_state = STATE_END;
				}
				_contentPosition += length;
				_buffer.get(length);
					//System.out.println("Read " + length);
					
				if (_state == STATE_END) 
				{
					_handler.content(_buffer.sliceFromMark(_contentPosition));
					//System.out.println("Buffer: " + buffer.sliceFromMark(contentPosition));
				}
				return totalFilled;
			}
		}
		return totalFilled;
	}
	
	public void setBuffer(Buffer buffer)
	{
		setBuffer(buffer, false);
	}
	
	public void setBuffer(Buffer buffer, boolean all)
	{
		_state = STATE_START;
		_contentLength = -1;
		_contentPosition = 0;
		_length = 0;
		_response = false;
		
		if (_buffer!= null && _buffer.length() > 0 && _eol == SipGrammar.CR && _buffer.peek() == SipGrammar.LF) 
		{
			_buffer.skip(1);
			_eol = SipGrammar.LF;
		}
		
		if (_endpoint != null && _buffer != null)
		{
			if (all)
				_buffer.setGetIndex(0);
			
			buffer.put(_buffer);
		}
		
		_buffer = buffer;
		
		if (_token0 == null)
			_token0 = new View(_buffer);
		
		_token0.update(_buffer);
		_token0.update(0, 0);
		
		if (_token1 == null)
			_token1 = new View(_buffer);
		
		_token1.update(_buffer);
		_token1.update(0, 0);
	}
	
	public static class EventHandler 
	{
		public void startRequest(Buffer method, Buffer uri, Buffer version) throws IOException {}	
		public void startResponse(Buffer version, int status, Buffer reason) throws IOException {}
		public void header(Buffer name, Buffer value) throws IOException {}
		public void content(Buffer content) throws IOException {}
	}
}
