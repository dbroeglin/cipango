package org.cipango.diameter.io;

import java.io.IOException;

import junit.framework.TestCase;

import org.cipango.diameter.AVP;
import org.cipango.diameter.ims.Cx;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

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
