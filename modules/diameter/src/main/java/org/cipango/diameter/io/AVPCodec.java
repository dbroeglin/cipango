package org.cipango.diameter.io;

import java.io.IOException;

import org.cipango.diameter.AVP;
import org.cipango.diameter.Dictionary;
import org.cipango.diameter.Factory;
import org.cipango.diameter.Type;
import org.cipango.diameter.base.Common;
import org.mortbay.io.Buffer;
import org.mortbay.io.View;

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
public class AVPCodec extends AbstractCodec<AVP<?>>
{
	private static final int AVP_VENDOR_FLAG = 0x80;
	private static final int AVP_MANDATORY_FLAG = 0x40;
	
	@SuppressWarnings("unchecked")
	public AVP<?> decode(Buffer buffer) throws IOException
	{
		int code = getInt(buffer);
		int i = getInt(buffer);
		
		int flags = i >> 24 & 0xff;
		int length = i & 0xffffff;
		
		int dataLength = length - 8;
		int vendorId = 0;
		if ((flags & AVP_VENDOR_FLAG) == AVP_VENDOR_FLAG)
		{
			vendorId = getInt(buffer);
			dataLength -= 4;
		}
		
		//Buffer data = buffer.slice();
		Buffer data = new View(buffer);
		
		data.setGetIndex(buffer.getIndex());
		data.setPutIndex(data.getIndex() + dataLength);
		
		buffer.setGetIndex(buffer.getIndex() + (dataLength + 3 & -4));
		
		Type type = Dictionary.getInstance().getType(vendorId, code);
		
		if (type == null)
			type = Factory.newType("Unknown", vendorId, code, Common.__octetString);
		
		AVP avp = new AVP(type);
		// TODO flags
		avp.setValue(type.getDataFormat().decode(data));
		
		return avp;	
	}

	@SuppressWarnings("unchecked")
	public Buffer encode(Buffer buffer, AVP avp) throws IOException
	{
		int flags = AVP_MANDATORY_FLAG;
		
		int start = buffer.putIndex();
		putInt(buffer, avp.getType().getCode());
		buffer.setPutIndex(start+8);
		if (avp.getType().isVendorSpecific())
		{
			flags |= AVP_VENDOR_FLAG;
			putInt(buffer, avp.getType().getVendorId());
		}
		
		avp.getType().getDataFormat().encode(buffer, avp.getValue());
		
		
		pokeInt(buffer, start+4, flags << 24 | (buffer.putIndex() - start) & 0xffffff);
		buffer.setPutIndex(buffer.putIndex() + 3 & -4);
		return buffer;
	}
}
