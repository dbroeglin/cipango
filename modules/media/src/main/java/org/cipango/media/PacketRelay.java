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

package org.cipango.media; 

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;

public class PacketRelay extends AbstractLifeCycle
{
	private Connection[] _connections = new Connection[2];
	
	protected void doStart() throws Exception
	{
		InetAddress localhost = InetAddress.getLocalHost();
		
		_connections[0] = new Connection(0, new DatagramSocket(0, localhost));
		_connections[1] = new Connection(1, new DatagramSocket(0, localhost));
		
		new Thread(_connections[0]).start();
		new Thread(_connections[1]).start();
	}
	
	public SocketAddress getLocalAddress(int index)
	{
		if (_connections[index] != null)
			return _connections[index].getLocalAddress();
		return null;
	}
	
	class Connection extends UdpEndPoint implements Runnable
	{
		private int _index;
		private SocketAddress _remoteAddress;
		
		public Connection(int index, DatagramSocket socket)
		{
			super(socket);
		}
		
		public SocketAddress getRemoteAddress()
		{
			return _remoteAddress;
		}
		
		public void setRemoteAddress(SocketAddress remoteAddress)
		{
			_remoteAddress = remoteAddress;
		}
		
		public void send(Buffer buffer) throws IOException
		{
			if (_remoteAddress != null)
				send(buffer, _remoteAddress);
		}
		
		public void run()
		{
			Buffer buffer = new ByteArrayBuffer(65535);
			
			try
			{
				while (!isClosed())
				{
					SocketAddress address = read(buffer);
					if (_remoteAddress == null)
						_remoteAddress = address;
					
					Connection connection = _connections[_index^1];
					connection.send(buffer);
					
					buffer.clear();
				}
			} 
			catch (IOException e)
			{
				Log.debug(e);
				try { close(); } catch (Exception e2) { Log.ignore(e2); }
			}
		}
	}
}
