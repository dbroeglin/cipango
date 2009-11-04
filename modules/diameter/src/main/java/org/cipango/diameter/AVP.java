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

public class AVP<T>
{
	public Type<T> _type;
	public T _value;
	
	public AVP(Type<T> type)
	{
		_type = type;
	}
	
	public AVP(Type<T> type, T value)
	{
		_type = type;
		_value = value;
	}
	
	public Type<T> getType()
	{
		return _type;
	}
	
	public void setValue(T value)
	{
		_value = value;
	}
	
	public T getValue()
	{
		return _value;
	}
	
	public String toString()
	{
		return _type + " = " + _value;
	}
/*	
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
	*/
	
	/*
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
	*/
}