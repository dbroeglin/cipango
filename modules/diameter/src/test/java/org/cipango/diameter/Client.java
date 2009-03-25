package org.cipango.diameter;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class Client 
{
	public static void main(String[] args) throws Exception
	{
		URL url = Client.class.getClassLoader().getResource("sar.dat");
		File file = new File(url.toURI());
		FileInputStream fin = new FileInputStream(file);
		byte[] b = new byte[(int) file.length()];
		fin.read(b);
		
		Socket s = new Socket(InetAddress.getLocalHost(), 3868);
		s.getOutputStream().write(b);
		s.close();
		
	}
}
