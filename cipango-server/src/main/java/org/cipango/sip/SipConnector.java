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

package org.cipango.sip;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.sip.SipURI;

import org.cipango.SipHandler;
import org.cipango.Server;
import org.cipango.Via;

import org.eclipse.jetty.util.component.LifeCycle;

public interface SipConnector extends LifeCycle
{    
	void open() throws IOException;
	void close() throws IOException;

    String getHost();
	InetAddress getAddr();
	int getPort();

	Object getConnection();
	int getLocalPort();
	
    String getTransport(); 
    int getTransportOrdinal();
	int getDefaultPort();
	boolean isReliable();
	boolean isSecure();
	
    SipURI getSipUri();
    Via getVia(); // TODO buffer

    void setTransportParam(boolean b);
    
    SipConnection getConnection(InetAddress addr, int port) throws IOException;
     
    void setServer(Server server);
    void setHandler(SipHandler handler);
    
    //SipEndpoint send(Buffer buffer, InetAddress address, int port) throws IOException;
    //void send(Buffer buffer, SipEndpoint endpoint) throws IOException;
    
    long getNbParseError();
    void setStatsOn(boolean on);
    void statsReset();
}
