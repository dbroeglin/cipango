package org.cipango.sip;

import java.io.IOException;
import java.net.InetAddress;

import org.mortbay.io.Buffer;

public interface SipConnection 
{
	SipConnector getConnector();
	
	InetAddress getLocalAddress();
	int getLocalPort();
	
	InetAddress getRemoteAddress();
	int getRemotePort();
		
	void write(Buffer buffer) throws IOException;
}
