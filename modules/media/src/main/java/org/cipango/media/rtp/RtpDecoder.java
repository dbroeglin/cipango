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

package org.cipango.media.rtp;

import org.mortbay.io.Buffer;

public class RtpDecoder 
{
	public static final int PADDING_FLAG = 0x20;
	public static final int EXTENSION_FLAG = 0x10;
	public static final int MARKER_FLAG = 0x80;
	
	public long getLong(Buffer buffer)
	{
		return  (buffer.get() & 0xff) << 24 
		| (buffer.get() & 0xff) << 16 
		| (buffer.get() & 0xff) << 8
		| (buffer.get() & 0xff);
	}
	
	public RtpPacket decode(Buffer buffer)
	{
		RtpPacket packet = new RtpPacket();
		
		byte b = buffer.get();
		
		boolean padding = ((b & PADDING_FLAG) == PADDING_FLAG);
		boolean extension = ((b & EXTENSION_FLAG) == EXTENSION_FLAG);
		
		int csrcCount = (b & 0x0f);
		
		b = buffer.get();
		
		boolean marker = ((b & MARKER_FLAG) == MARKER_FLAG);
		int payloadType = (b & 0x7f);
		
		int sequenceNumber = (buffer.get() & 0xff) << 8 | (buffer.get() & 0xff);
		long timestamp =  getLong(buffer);
		
		long ssrc =  getLong(buffer);
		
		if (csrcCount > 0)
		{
			long[] csrc = new long[csrcCount];
			for (int i = 0; i < csrcCount; i++)
				csrc[i] = getLong(buffer);
		}
		
		if (extension)
		{
			int length = (int) (getLong(buffer) & 0xffff);
			for (int i = 0; i < length; i++)
				getLong(buffer); // TODO extension
		}
		
		Buffer data = buffer.slice();
		if (padding)
		{
			int nb = (data.peek(data.putIndex() - 1) & 0xff);
			data.setPutIndex(data.putIndex() - nb);
		}
		
		packet.setData(data);
		return packet;
	}	
}
