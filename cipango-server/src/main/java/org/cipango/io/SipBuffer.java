// ========================================================================
// Copyright 2007-2008 NEXCOM Systems
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

package org.cipango.io;

import java.io.UnsupportedEncodingException;

import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.StringUtil;

public class SipBuffer extends ByteArrayBuffer
{
	public SipBuffer(String value) throws UnsupportedEncodingException
	{
		super(value, StringUtil.__UTF8);
	}
	
	public SipBuffer(byte[] bytes)
	{
		super(bytes, 0, bytes.length, IMMUTABLE);
	}
	
	public SipBuffer(byte[] bytes, int index, int length)
	{
		super(bytes, index, length, IMMUTABLE);
	}
	
	@Override
	public String toString()
    {
		if (_string == null)
			_string = StringUtil.toUTF8String(array(), getIndex(), length());
        return _string;
    }
	
	public SipBuffer clone()
	{
		return this;
	}
}
