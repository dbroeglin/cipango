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

package org.cipango.diameter.io;

import java.io.IOException;

import org.cipango.diameter.AVP;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.util.BufferUtil;
import org.mortbay.io.Buffer;
import org.mortbay.io.View;

/**
 * <pre>
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |    Version    |                 Message Length                |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * | command flags |                  Command-Code                 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |                         Application-ID                        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |                      Hop-by-Hop Identifier                    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |                      End-to-End Identifier                    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |  AVPs ... 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-
 * </pre>
 */
public class BaseDiameterCodec 
{
	private static final int DIAMETER_VERSION_1 = 1;
	public static final int REQUEST_FLAG = 0x80;
	
	private static final int AVP_VENDOR_FLAG = 0x80;
	private static final int AVP_MANDATORY_FLAG = 0x40;
	
	
	public static DiameterMessage parse(Buffer buffer) throws IOException
	{
		int i = BufferUtil.getInt(buffer);
		
		int version = i >> 24 & 0xff;
		if (version != DIAMETER_VERSION_1)
			throw new IOException("Invalid version");
		
		i = BufferUtil.getInt(buffer);
		
		int flags = i >> 24 & 0xff;
		boolean isRequest = ((flags & REQUEST_FLAG) == REQUEST_FLAG);
		
		int command = i & 0xffffff;
		
		DiameterMessage message;
		
		if (isRequest)
			message = new DiameterRequest();
		else
			message = new DiameterAnswer();
		
		message.setApplicationId(BufferUtil.getInt(buffer));
		message.setHopByHopId(BufferUtil.getInt(buffer));
		message.setEndToEndId(BufferUtil.getInt(buffer));
		message.setCommand(command);
		
		while (buffer.hasContent())
		{
			AVP avp = parseAVP(buffer);
			message.getAVPs().add(avp);
		}
		
		return message;
	}
	
	/**
	 * <pre>
	 * 0                   1                   2                   3
	 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * |                           AVP Code                            |
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * |V M P r r r r r|                  AVP Length                   |
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * |                        Vendor-ID (opt)                        |
	 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * |    Data ...
	 * +-+-+-+-+-+-+-+-+
	 * </pre>
	 */
	public static AVP parseAVP(Buffer buffer) throws IOException
	{
		int code = BufferUtil.getInt(buffer);
		int i = BufferUtil.getInt(buffer);
		
		int flags = i >> 24 & 0xff;
		int length = i & 0xffffff;
		
		int dataLength = length - 8;
		int vendorId = 0;
		if ((flags & AVP_VENDOR_FLAG) == AVP_VENDOR_FLAG)
		{
			vendorId = BufferUtil.getInt(buffer);
			dataLength -= 4;
		}
		
		Buffer data = new View(buffer, -1, buffer.getIndex(), buffer.getIndex() + dataLength, Buffer.READONLY);
		buffer.setGetIndex(buffer.getIndex() + (dataLength + 3 & -4));
		
		return new AVP(vendorId, code, data);
	}
	
	public static Buffer write(DiameterMessage message, Buffer buffer)
	{
		int start = buffer.putIndex();
		buffer.setPutIndex(start + 4);
		
		int flags = 0;
		if (message.isRequest())
			flags |= REQUEST_FLAG;
		else
			flags &= ~REQUEST_FLAG;
		
		BufferUtil.putInt(buffer, flags << 24 | message.getCommand() & 0xffffff);
		BufferUtil.putInt(buffer, message.getApplicationId());
		BufferUtil.putInt(buffer, message.getHopByHopId());
		BufferUtil.putInt(buffer, message.getEndToEndId());
		
		for (int i = 0; i < message.size(); i++)
		{
			buffer = writeAVP(message.get(i), buffer);
		}
		
		BufferUtil.pokeInt(buffer, start, DIAMETER_VERSION_1 << 24 | (buffer.putIndex() - start) & 0xffffff);
		
		return buffer;
	}
	
	public static Buffer writeAVP(AVP avp, Buffer buffer)
	{
		int start = buffer.putIndex();
		BufferUtil.putInt(buffer, avp.getCode());
		buffer.setPutIndex(start+8);
		if (avp.getVendorId() != Base.IETF_VENDOR_ID)
			BufferUtil.putInt(buffer, avp.getVendorId());
		
		buffer = avp.write(buffer);
		//avp.getType().getDataFormat().encode(buffer, avp.getValue());
		
		int flags = AVP_MANDATORY_FLAG;
		if (avp.getVendorId() != Base.IETF_VENDOR_ID)
			flags |= AVP_VENDOR_FLAG;
		
		BufferUtil.pokeInt(buffer, start+4, flags << 24 | (buffer.putIndex() - start) & 0xffffff);
		buffer.setPutIndex(buffer.putIndex() + 3 & -4);
		return buffer;
	}
}
