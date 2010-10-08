package org.cipango.diameter.io;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.Dictionary;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.IMS;
import org.cipango.diameter.ims.Sh;
import org.cipango.diameter.node.DiameterAnswer;
import org.cipango.diameter.node.DiameterMessage;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

public class TestMessageCodec extends TestCase
{
	protected void setUp()
	{
		Dictionary.getInstance().load(Common.class);
		Dictionary.getInstance().load(IMS.class);
		Dictionary.getInstance().load(Cx.class);
		Dictionary.getInstance().load(Sh.class);
		Dictionary.getInstance().load(IMS.class);
	}
	
	protected Buffer load(String name) throws Exception
	{
		URL url = getClass().getClassLoader().getResource(name);
		File file = new File(url.toURI());
		FileInputStream fin = new FileInputStream(file);
		byte[] b = new byte[(int) file.length()];
		fin.read(b);
		
		return new ByteArrayBuffer(b);
	}
	
	public void testDecodeSAR() throws Exception
	{
		DiameterMessage message = Codecs.__message.decode(load("sar.dat"));

		assertTrue(message.isRequest());
		assertEquals(Cx.SAR, message.getCommand());
		assertEquals("scscf1.home1.net", message.get(Common.ORIGIN_HOST));
		assertEquals("home1.net", message.get(Common.ORIGIN_REALM));
		
		AVPList vsai = message.get(Common.VENDOR_SPECIFIC_APPLICATION_ID);
		assertEquals(IMS.IMS_VENDOR_ID, (int) vsai.getValue(Common.VENDOR_ID));
		assertEquals(Cx.CX_APPLICATION_ID.getId(), (int) vsai.getValue(Common.AUTH_APPLICATION_ID));	
	}
	
	public void testDecodeLIA() throws Exception
	{
		DiameterMessage message = Codecs.__message.decode(load("lia.dat"));
		assertFalse(message.isRequest());
	}
	
	public void testEncodeCEA() throws Exception
	{
		DiameterAnswer answer = new DiameterAnswer();
		answer.setCommand(Common.CEA);
		AVPList l = new AVPList();
		answer.setAVPList(l);
		answer.setResultCode(Common.DIAMETER_SUCCESS);

		Buffer buffer = new ByteArrayBuffer(512);
		buffer = Codecs.__message.encode(buffer, answer);
		DiameterMessage message = Codecs.__message.decode(buffer);
		assertFalse(message.isRequest());
		assertEquals(Common.CEA, message.getCommand());
	}
	
	public void testEncodeSmallBuffer() throws Exception
	{
		DiameterAnswer answer = new DiameterAnswer();
		answer.setCommand(Sh.UDA);
		AVPList l = new AVPList();
		answer.setAVPList(l);
		answer.setResultCode(Common.DIAMETER_SUCCESS);
		answer.setEndToEndId(33);
		answer.setHopByHopId(51648);
		l.add(Common.DIAMETER_SUCCESS.getAVP());
		l.add(new AVP<String>(Common.ORIGIN_HOST, "cipango.org"));
		l.add(new AVP<InetAddress>(Common.HOST_IP_ADDRESS, InetAddress.getLocalHost()));
		l.add(new AVP<Integer>(Common.FIRMWARE_REVISION, 2));
		l.add(new AVP<byte[]>(Sh.USER_DATA, "<shData>dasaiTag<shData>".getBytes()));

		for (int i = 24; i < 256; i++)
		{
			Buffer buffer = new ByteArrayBuffer(i);
			buffer = Codecs.__message.encode(buffer, answer);
			// System.out.println(buffer.putIndex() + " / " + i);
			DiameterMessage message = Codecs.__message.decode(buffer);
			// System.out.println(message);
			assertFalse(message.isRequest());
			assertEquals(answer.getEndToEndId(), message.getEndToEndId());
			assertEquals(answer.getHopByHopId(), message.getHopByHopId());
			assertEquals(Sh.UDA, message.getCommand());
			assertEquals(Common.DIAMETER_SUCCESS.getCode(), message.get(Common.RESULT_CODE).intValue());
			assertEquals(InetAddress.getLocalHost(), message.get(Common.HOST_IP_ADDRESS));
			assertEquals("<shData>dasaiTag<shData>", new String(message.get(Sh.USER_DATA)));
		}
	}
	
	
	/*public void testCER() throws Exception
	{
		DiameterMessage message = Codecs.__message.decode(load("cer.dat"));
		System.out.println(message.getAVPs());
	}*/
	
	/*
	public void testPerf() throws Exception
	{
		Buffer buffer = load("sar.dat");
		
		long nb = 100000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < nb; i++)
		{
			buffer.mark(buffer.getIndex());
			Codecs.__message.decode(buffer);
			buffer.reset();
		}
		System.out.println((nb * 1000 / (System.currentTimeMillis() - start)) + " msg / s");
	*/
}
