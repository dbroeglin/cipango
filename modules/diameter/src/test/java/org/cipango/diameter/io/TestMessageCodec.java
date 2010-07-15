package org.cipango.diameter.io;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.cipango.diameter.AVPList;
import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.Dictionary;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.IMS;
import org.cipango.diameter.util.PrettyPrinter;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;

public class TestMessageCodec extends TestCase
{
	protected void setUp()
	{
		Dictionary.getInstance().load(Common.class);
		Dictionary.getInstance().load(IMS.class);
		Dictionary.getInstance().load(Cx.class);
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
		answer.setAVPList(new AVPList());
		answer.setResultCode(Common.DIAMETER_SUCCESS);
		
		Buffer buffer = new ByteArrayBuffer(512);
		DiameterMessage message = Codecs.__message.decode(Codecs.__message.encode(buffer, answer));
		assertFalse(message.isRequest());
		assertEquals(Common.CEA, message.getCommand());
	}
	
	
	public void testCER() throws Exception
	{
		DiameterMessage message = Codecs.__message.decode(load("cer.dat"));
		System.out.println(message.getAVPs());
	}
	
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
