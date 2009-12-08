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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.mortbay.io.Buffer;

public class UdpEndPoint 
{
	private DatagramSocket _socket;
	
	public UdpEndPoint(DatagramSocket socket)
	{
		_socket = socket; 
	}
	
	public SocketAddress getLocalAddress()
	{
		if (_socket != null)
			return _socket.getLocalSocketAddress();
		return null;
	}
	
	public void close() throws IOException
	{
		_socket.close();
	}
	
	public boolean isClosed()
	{
		return _socket == null || _socket.isClosed();
	}
	
	public void send(Buffer buffer, SocketAddress remoteAddress) throws IOException
	{
		DatagramPacket packet;
		
		byte[] b = buffer.array();
		
		if (b != null)
		{
			packet = new DatagramPacket(b, buffer.getIndex(), buffer.length());
		}
		else
		{
			b = buffer.asArray();
			packet = new DatagramPacket(b, b.length);
		}
		packet.setSocketAddress(remoteAddress);
		
		_socket.send(packet);
	}
	
	public SocketAddress read(Buffer buffer) throws IOException
	{
		byte[] b = buffer.array(); // TODO b == null
		DatagramPacket packet = new DatagramPacket(b, buffer.putIndex(), buffer.space());
		
		_socket.receive(packet);
		
		buffer.setPutIndex(buffer.putIndex() + packet.getLength());
		
		return packet.getSocketAddress();
	}
}
