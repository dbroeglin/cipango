package org.cipango.diameter.io;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import org.cipango.diameter.AVP;
import org.cipango.diameter.ims.Cx;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.io.View;
import org.junit.Test;

public class AVPCodecTest
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testAVPCodec() throws IOException
	{
		AVP avp = new AVP(Cx.PUBLIC_IDENTITY, "sip:alice@cipango.org");
		Buffer buffer = new ByteArrayBuffer(64);
		Codecs.__avp.encode(buffer, avp);
	
		AVP decoded = Codecs.__avp.decode(buffer);
		
		assertEquals(avp.getType().getCode(), decoded.getType().getCode());
		assertEquals(avp.getType().getVendorId(), decoded.getType().getVendorId());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPadding() throws IOException
	{
		byte[] b = { 13 };
		AVP<byte[]> avp = new AVP<byte[]>(Cx.INTEGRITY_KEY, b);
		Buffer buffer = new ByteArrayBuffer(64);
		for (int i = 0; i < 64; i++)
			buffer.put((byte) 44);
		buffer.setPutIndex(0);
		Codecs.__avp.encode(buffer, avp);
		View view = new View(buffer);
		view.setGetIndex(view.putIndex() - 3);
		for (int i = 0; i < 3; i++)
			assertEquals(0, view.get());
		
		AVP<byte[]> decoded = (AVP<byte[]>) Codecs.__avp.decode(buffer);
		
		assertEquals(avp.getType().getCode(), decoded.getType().getCode());
		assertEquals(avp.getType().getVendorId(), decoded.getType().getVendorId());
		assertEquals(avp.getValue()[0], decoded.getValue()[0]);
		
	}
}
