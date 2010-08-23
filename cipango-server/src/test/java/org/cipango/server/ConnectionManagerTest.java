// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.cipango.sip.SipURIImpl;

public class ConnectionManagerTest extends TestCase
{

	private static final String[] MATCHING_LOCAL_URI = 
	{ 
			"sip:cipango.org;lr",
			"sip:cipango.org:5060;lr",
			"sip:as.cipango.org;lr",
			"sip:as.cipango.org:5070;lr",
			"sip:192.168.1.1;lr",
			"sip:192.168.1.1:5060;lr",
			"sip:192.168.2.2:5070;lr",
			"sip:[1fff:0:a88:85a3::ac1f:8001]:5070;lr",
			"sip:[1fff::a88:85a3:0:0:ac1f:8001]:5070;lr",
			"sip:[1fff:0:a88:85a3::172.31.128.1]:5070;lr",
			"sip:[2000:0:0:0:0:0:0:1];lr",
			"sip:[2000:0:0:0:0:0:0:1]:5060;lr"
	};
	
	private static final String[] NO_MATCHING_LOCAL_URI = 
	{ 
			"sip:other.cipango.org;lr",
			"sip:badIp@10.10.10.10;lr",
			"sip:missingLr@cipango.org",
			"sip:badPort@192.168.1.1:5080;lr",
			"sip:noPort@192.168.2.2;lr",
			"sip:noPort@[1fff:0:a88:85a3::172.31.128.1];lr"
	};
	
	public void testIsLocalUri() throws Exception
	{
		ConnectorManager connectorManager = new ConnectorManager();
		connectorManager.addConnector(new TestConnector("cipango.org", "192.168.1.1", 5060));
		connectorManager.addConnector(new TestConnector("ipv6.cipango.org", "[2000::1]", 5060));
		connectorManager.addConnector(new TestConnector("as.cipango.org", "192.168.2.2", 5070));
		connectorManager.addConnector(new TestConnector("ipv6.as.cipango.org", "[1fff:0:a88:85a3::ac1f:8001]", 5070));
		
		for (int i = 0; i < MATCHING_LOCAL_URI.length; i++)
			assertTrue("Not match on " + MATCHING_LOCAL_URI[i], 
					connectorManager.isLocalUri(new SipURIImpl(MATCHING_LOCAL_URI[i])));
		
		for (int i = 0; i < NO_MATCHING_LOCAL_URI.length; i++)
			assertFalse("Match on " + NO_MATCHING_LOCAL_URI[i], 
					connectorManager.isLocalUri(new SipURIImpl(NO_MATCHING_LOCAL_URI[i])));
	}
	
	class TestConnector extends AbstractSipConnector
	{
		private InetAddress _addr;
		
		public TestConnector(String host, String addr, int port) throws UnknownHostException
		{
			_addr = InetAddress.getByName(addr);
			setPort(port);
			setHost(host);
		}

		public void close() throws IOException
		{
		}

		public InetAddress getAddr()
		{
			return _addr;
		}

		public int getDefaultPort()
		{
			return 5060;
		}

		public int getTransportOrdinal()
		{
			return 0;
		}

		public boolean isReliable()
		{
			return false;
		}

		public void open() throws IOException
		{	
		}

		@Override
		public void accept(int acceptorID) throws IOException,
				InterruptedException
		{
		}
		
		public SipConnection getConnection(InetAddress address, int port)
		{
			return null;
		}

		public Object getConnection()
		{
			return null;
		}

		public int getLocalPort()
		{
			return 0;
		}

		public boolean isSecure()
		{
			return false;
		}
	}
}
