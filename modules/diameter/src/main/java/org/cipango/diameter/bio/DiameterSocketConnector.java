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

package org.cipango.diameter.bio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.cipango.diameter.AbstractDiameterConnector;
import org.cipango.diameter.DiameterConnection;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.Peer;
import org.cipango.diameter.io.Codecs;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.bio.SocketEndPoint;
import org.mortbay.jetty.EofException;
import org.mortbay.log.Log;

/**
 * TCP Diameter Connector using BIO
 */
public class DiameterSocketConnector extends AbstractDiameterConnector
{
	public static final int DEFAULT_PORT = 3868;
	
	private ServerSocket _serverSocket;
	
	public void open() throws IOException
	{
		if (_serverSocket == null || _serverSocket.isClosed())
			_serverSocket = newServerSocket(getPort()); 
	}
	
	public void close() throws IOException
	{
		if (_serverSocket != null)
			_serverSocket.close();
		_serverSocket = null;
	}
	
	public Object getTransport()
	{
		return _serverSocket;
	}
	
	protected ServerSocket newServerSocket(int port) throws IOException
	{
		ServerSocket ss = new ServerSocket();
		ss.bind(new InetSocketAddress(getHost(), getPort()));
		return ss;
	}
	
	public void accept(int acceptorID) throws IOException, InterruptedException
	{
		Socket socket = _serverSocket.accept();
		Connection connection = new Connection(socket);
		new Thread(connection, "Connection-" + acceptorID).start();
	}
	
	public DiameterConnection getConnection(Peer peer) throws IOException
	{
		int port = peer.getPort();
		if (port == 0)
			port = DEFAULT_PORT;
		
		Socket socket;
		
		if (peer.getAddress() != null)
			socket = new Socket(peer.getAddress(), port);
		else 
			socket = new Socket(peer.getHost(), port);
		
		Connection connection = new Connection(socket);
		connection.setPeer(peer);
		
		new Thread(connection, "Connection-" + peer.getHost()).start();

		return connection;
	}
	
	public int getLocalPort()
	{
		if (_serverSocket == null || _serverSocket.isClosed())
			return -1;
		return _serverSocket.getLocalPort();
	}
	
	public InetAddress getLocalAddress()
	{
		if (_serverSocket == null || _serverSocket.isClosed())
			return null;
		return _serverSocket.getInetAddress();
	}
	
	class Connection extends SocketEndPoint implements Runnable, DiameterConnection
	{
		private Peer _peer;
		
		public Connection(Socket socket) throws IOException
		{
			super(socket);
		}
		
		public void setPeer(Peer peer)
		{
			_peer = peer;
		}
		
		public Peer getPeer()
		{
			return _peer;
		}
		
		public void stop()
		{
			try { close(); } catch (IOException e) { Log.ignore(e); }
		}
		
		public void write(DiameterMessage message) throws IOException
		{
			Buffer buffer = getBuffer(getMessageBufferSize());
			buffer = Codecs.__message.encode(buffer, message);
			
			flush(buffer);
			returnBuffer(buffer);
			
			if (_listener != null)
				_listener.messageSent(message, this);
		}
		
		public void run()
		{
			try
			{
				Buffer fb = new ByteArrayBuffer(4);
				
				while (isStarted() && !isClosed())
				{
					fb.clear();
					int read = fill(fb);
					
					if (read == -1)
						throw new EofException();
					
					int length = 
						(fb.peek(1) & 0xff) << 16
						| (fb.peek(2) & 0xff) << 8
						| (fb.peek(3) & 0xff);
					
					Buffer b = new ByteArrayBuffer(length);

					b.put(fb);
					read = fill(b);

					if (read == -1)
						throw new EofException();
					
					DiameterMessage message = Codecs.__message.decode(b);
					message.setConnection(this);
					message.setNode(getNode());
					
					if (_listener != null)
						_listener.messageReceived(message, this);
					
					getNode().receive(message);
				}
			}
			catch (EofException e)
			{
				Log.debug("EOF", e);
				try { close(); } catch (IOException e2) { Log.ignore(e2); }
			}
			catch (IOException e)
			{
				Log.debug("IO", e); // TODO
				try { close(); } catch (IOException e2) { Log.ignore(e2); }
			}
			catch (Throwable t)
			{
				Log.warn("handle failed", t);
				try { close(); } catch (IOException e2) { Log.ignore(e2); }
			}
			finally
			{
				if (_peer != null)
					_peer.peerDisc(this);
			}
		}
	}
}
