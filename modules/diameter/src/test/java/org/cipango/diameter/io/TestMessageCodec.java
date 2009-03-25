package org.cipango.diameter.io;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.Client;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.ims.IMS;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;

public class TestMessageCodec extends TestCase
{
	protected byte[] load(String name) throws Exception
	{
		URL url = Client.class.getClassLoader().getResource(name);
		File file = new File(url.toURI());
		FileInputStream fin = new FileInputStream(file);
		byte[] b = new byte[(int) file.length()];
		fin.read(b);
		
		return b;
	}
	
	public void testDecode() throws Exception
	{
		DiameterMessage message = DiameterCodec.parse(new ByteArrayBuffer(load("sar.dat")));
		//assertEquals(Base.SAR, message.getCommand());
		AVP avp = message.getAVP(Base.VENDOR_SPECIFIC_APPLICATION_ID);
		AVPList list = avp.getGrouped();
		System.out.println(list.getAVP(Base.AUTH_APPLICATION_ID).getInt());
		System.out.println(list.getAVP(Base.VENDOR_ID).getInt());
		
		System.out.println(message.getAVP(Base.ORIGIN_HOST).getString());
		System.out.println(message.getAVP(Base.ORIGIN_REALM));	
		
		ByteArrayBuffer out = new ByteArrayBuffer(1000);
		ByteArrayBuffer out3 = new ByteArrayBuffer(1000);
		
		DiameterCodec.write(message, out);
		DiameterCodec.write(message, out3);
		
		System.out.println(out.length());
		
		message = DiameterCodec.parse(out);
		System.out.println(message.getAVP(Base.ORIGIN_HOST).getString());
		System.out.println(message.getAVP(Base.ORIGIN_REALM));	
		
		ByteArrayBuffer out2 = new ByteArrayBuffer(1000);
		DiameterCodec.write(message, out2);
		
		assertEquals(out3, out2);
		
		AVP app = AVP.ofAVPs(Base.VENDOR_SPECIFIC_APPLICATION_ID, 
				AVP.ofInt(Base.VENDOR_ID, IMS.IMS_VENDOR_ID),
				AVP.ofInt(Base.AUTH_APPLICATION_ID, IMS.CX_APPLICATION_ID));
		list = app.getGrouped();
		System.out.println(list.getAVP(Base.AUTH_APPLICATION_ID).getInt());
	}
	
}
