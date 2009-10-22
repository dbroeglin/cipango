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

package org.cipango.diameter;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.cipango.diameter.base.Base;
import org.cipango.diameter.io.BaseDiameterCodec;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.View;
import org.mortbay.util.StringUtil;
import org.mortbay.util.Utf8StringBuffer;

public class AVP
{
	private int _vendorId;
	private int _code;
	
	private static final int TYPE_IPV4 = 1;
	private static final int TYPE_IPV6 = 2;
	
	private Buffer _buffer;
	
	public AVP(int vendorId, int code, Buffer buffer)
	{
		_vendorId = vendorId;
		_code = code;
		_buffer = buffer;
	}
	
	public int getVendorId()
	{
		return _vendorId;
	}
	
	public int getCode()
	{
		return _code;
	}
	
	public AVPList getGrouped()
	{
		try
		{
			Buffer grouped = new View(_buffer);
			AVPList list = new AVPList();
			while (grouped.hasContent())
			{
				AVP avp = BaseDiameterCodec.parseAVP(grouped);
				list.add(avp);
			}
			return list;
		}
		catch (Exception e)
		{
			return new AVPList();
		}
	}
	
	public int getInt()
	{
		return (_buffer.peek() & 0xff) << 24 
			| (_buffer.peek(_buffer.getIndex() + 1) & 0xff) << 16 
			| (_buffer.peek(_buffer.getIndex() + 2) & 0xff) << 8
			| (_buffer.peek(_buffer.getIndex() + 3) & 0xff);
	}
	
	public byte[] getBytes()
	{
		byte[] bytes = new byte[_buffer.length()];
		
		_buffer.peek(_buffer.getIndex(), bytes, 0, _buffer.length());
		return bytes;
	}
	
	public String getString()
	{
		byte[] bytes = _buffer.array();
		if (bytes != null)
			return StringUtil.toUTF8String(bytes, _buffer.getIndex(), _buffer.length());
		
		Utf8StringBuffer b = new Utf8StringBuffer(_buffer.length());
		for (int i = _buffer.getIndex(), c=0; c < _buffer.length(); i++,c++)
            b.append(_buffer.peek(i));
        return b.toString();
	}
	
	public InetAddress getAddress()
	{
		int type = (_buffer.peek() & 0xff) << 8 | (_buffer.peek(_buffer.getIndex() + 1) & 0xff);
		byte[] addr;
		switch (type)
		{
		case TYPE_IPV4:
			addr = new byte[4];
			break;
		case TYPE_IPV6:
			addr = new byte[16];
			break;
		default:
			return null;
		}
		_buffer.peek(_buffer.getIndex() + 2, addr, 0, addr.length);
		try
		{
			return InetAddress.getByAddress(addr);
		}
		catch (UnknownHostException e)
		{
			return null;
		}
	}
	
	public Buffer write(Buffer buffer)
	{
		buffer.put((Buffer) _buffer);
		return buffer;
	}
	
	public static AVP ofAddress(int code, InetAddress value)
	{
		return ofAddress(Base.IETF_VENDOR_ID, code, value);
	}
	
	public static AVP ofAddress(int vendorId, int code, InetAddress address)
	{
		
		Buffer buffer;
		if (address instanceof Inet4Address)
			buffer = new ByteArrayBuffer(6);
		else
			buffer = new ByteArrayBuffer(18);
		buffer.put((byte) 0);
		buffer.put((byte) (address instanceof Inet4Address ? TYPE_IPV4 : TYPE_IPV6));
		buffer.put(address.getAddress());
		
		return new AVP(vendorId, code, buffer);
	}
	
	public static AVP ofInt(int code, int value)
	{
		return ofInt(Base.IETF_VENDOR_ID, code, value);
	}
	
	public static AVP ofInt(int vendorId, int code, int value)
	{
		Buffer buffer = new ByteArrayBuffer(4);
		buffer.put((byte)  (value >> 24 & 0xff));
		buffer.put((byte)  (value >> 16 & 0xff));
		buffer.put((byte)  (value >> 8 & 0xff));
		buffer.put((byte)  (value & 0xff));
		
		return new AVP(vendorId, code, buffer);
	}

	public static AVP ofBytes(int code, byte[] b)
	{
		return ofBytes(Base.IETF_VENDOR_ID, code, b);
	}
	
	public static AVP ofBytes(int vendorId, int code, byte[] b)
	{
		ByteArrayBuffer buffer = new ByteArrayBuffer(b.length);
		buffer.put(b);
		return new AVP(vendorId, code, buffer);
	}

	public static AVP ofString(int code, String value)
	{
		return ofString(Base.IETF_VENDOR_ID, code, value);
	}
	
	public static AVP ofString(int vendorId, int code, String value)
	{
		byte[] b;
		try
		{
			b = value.getBytes(StringUtil.__UTF8);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
		return new AVP(vendorId, code, new ByteArrayBuffer(b));
	}
	
	public static AVP ofAVPs(int code, AVP... avps)
	{
		return ofAVPs(Base.IETF_VENDOR_ID, code, avps);
	}
	
	public static AVP ofAVPs(int vendorId, int code, AVP...avps)
	{
		Buffer buffer = new ByteArrayBuffer(256); // TODO
		for (AVP avp : avps)
		{
			buffer = BaseDiameterCodec.writeAVP(avp, buffer);
		}
		return new AVP(vendorId, code, buffer);
	}
	
	public static AVP ofAVPs(int code, AVPList avps)
	{
		return ofAVPs(Base.IETF_VENDOR_ID, code, avps);
	}
	
	public static AVP ofAVPs(int vendorId, int code, AVPList avps)
	{
		return ofAVPs(vendorId, code, (AVP[]) avps.toArray(new AVP[avps.size()]));
	}
	
}