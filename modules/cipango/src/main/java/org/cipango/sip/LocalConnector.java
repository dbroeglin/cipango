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
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.cipango.SipMessage;
import org.cipango.SipRequest;
import org.mortbay.io.Buffer;


public class LocalConnector extends AbstractSipConnector implements SipEndpoint
{	
	public static final boolean RELIABLE = true;
	
	public static InetAddress __localAddress;
	
	static
	{
		try
		{
			__localAddress = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
		}
		catch (UnknownHostException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}

	private MessageQueue _queue;
	
	public LocalConnector()
	{
		super(SipConnectors.LOCAL_ORDINAL);
	}
	
	public void open() throws IOException
	{
		_queue = new MessageQueue();
	}
	
	public void close() throws IOException
	{
		if (_queue != null)
			_queue.close();
		_queue = null;
	}
	
	public int getLocalPort()
	{
		return getPort();
	}
	
	public Object getConnection()
	{
		return _queue;
	}

	
	public void accept(int acceptorId) throws IOException, InterruptedException
	{
		SipMessage message = _queue.receive();

		message.set5uple(getTransportOrdinal(), getAddr(), getPort(), getAddr(), getPort());
		
		if (message.isRequest())
			((SipRequest) message).setEndpoint(this);
		
		process(message);
	}
	
	public void send(SipMessage message) throws IOException
	{
		SipMessage copy = (SipMessage) message.clone();
		if (copy instanceof SipRequest)
		{
			SipRequest request = (SipRequest) copy;
			request.setInitialPoppedRoute(((SipRequest) message).getInitialPoppedRoute());
		}
		_queue.send(copy);
	}

	public InetAddress getAddr()
	{
		return __localAddress;
	}

	public int getDefaultPort()
	{
		return -1;
	}

	public int getTransportOrdinal()
	{
		return SipConnectors.LOCAL_ORDINAL;
	}

	public boolean isReliable()
	{
		return true;
	}

	public SipEndpoint send(Buffer buffer, InetAddress address, int port)
			throws IOException 
	{
		throw new UnsupportedOperationException();
	}
	
	public SipConnector getConnector()
	{
		return this;
	}
	
	class MessageQueue extends LinkedList<SipMessage>
	{
		private boolean _closed = false;
		
		public synchronized void close()
		{
			_closed = true;
			notifyAll();
		}
		
		public synchronized SipMessage receive() throws InterruptedException, IOException
		{
			while (isEmpty() && !_closed)
				wait();
			if (_closed)
				throw new IOException("closed");
			return removeFirst();
		}
		
		public synchronized void send(SipMessage message) throws IOException
		{
			if (_closed)
				throw new IOException("closed");
			
			addLast(message);
			notifyAll();
		}
	}
}
