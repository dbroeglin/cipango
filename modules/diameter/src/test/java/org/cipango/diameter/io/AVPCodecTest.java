package org.cipango.diameter.io;

import java.io.IOException;

import org.cipango.diameter.AVP;
import org.cipango.diameter.ims.Cx;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;

public class AVPCodecTest extends TestCase
{
	@SuppressWarnings("unchecked")
	public void testAVPCodec() throws IOException
	{
		AVP avp = new AVP(Cx.PUBLIC_IDENTITY, "sip:alice@cipango.org");
		Buffer buffer = new ByteArrayBuffer(64);
		Codecs.__avp.encode(buffer, avp);
	
		AVP decoded = Codecs.__avp.decode(buffer);
		
		assertEquals(avp.getType().getCode(), decoded.getType().getCode());
		assertEquals(avp.getType().getVendorId(), decoded.getType().getVendorId());
		
	}
}
