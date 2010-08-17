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

package org.cipango.diameter;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.Buffers;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;

public abstract class AbstractDiameterConnector extends AbstractLifeCycle implements DiameterConnector //, Buffers
{
	private Node _node;
	
	private int _acceptors = 1;
	private int _acceptorPriorityOffset = 0;
	
	private String _host;
	private int _port;
	
	private Thread[] _acceptorThread;
	private ArrayList<Buffer> _buffers;
	private int _messageBufferSize = 8192;
	
	protected DiameterMessageListener _listener;
	
	@Override
	protected void doStart() throws Exception
	{
		if (_node == null)
			throw new IllegalStateException("No node");
		
		open();
		
		if (_buffers != null)
			_buffers.clear();
		else
			_buffers = new ArrayList<Buffer>();
		
		super.doStart();
		
		if (_listener instanceof LifeCycle)
			((LifeCycle) _listener).start();
		
		synchronized (this)
		{
			_acceptorThread = new Thread[getAcceptors()];
			for (int i = 0; i < _acceptorThread.length; i++)
			{
				_acceptorThread[i] = new Thread(new Acceptor(i));
				_acceptorThread[i].start();
			}
		}
		Log.info("Started {}", this);
	}
	
	@Override
	protected void doStop() throws Exception
	{	
		if (_listener instanceof LifeCycle)
			((LifeCycle) _listener).stop();
		
		super.doStop();
		
		close();
		
		Thread[] acceptors = null;
        synchronized(this)
        {
            acceptors = _acceptorThread;
            _acceptorThread = null;
        }
        if (acceptors != null)
        {
            for (int i = 0; i < acceptors.length; i++)
            {
                Thread thread = acceptors[i];
                if (thread != null)
                    thread.interrupt();
            }
        }	
	}
	
	public void setMessageListener(DiameterMessageListener listener)
	{
		try
		{
			if (_listener != null  && _listener instanceof LifeCycle)
				((LifeCycle) _listener).stop();
		}
		catch (Exception e)
		{
			Log.warn(e);
		}
		
		if (_node != null && _node.getServer() != null)
			_node.getServer().getContainer().update(this, _listener, listener, "listener");
		
		_listener = listener;
		
		try
		{
			if (isStarted() && (_listener instanceof LifeCycle))
				((LifeCycle) _listener).start();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public DiameterMessageListener getMessageListener()
	{
		return _listener;
	}
	
	public Buffer getBuffer(int size)
	{
		if (size == _messageBufferSize)
		{
			synchronized(_buffers)
			{
				if (_buffers.size() > 0)
					return (Buffer) _buffers.remove(_buffers.size() - 1);
			}
			return newBuffer(size);
		}
		return newBuffer(size);
	}
	
	public void returnBuffer(Buffer buffer)
	{
		buffer.clear();
		if (!buffer.isVolatile() && !buffer.isImmutable())
        {
			int c = buffer.capacity();
            if (c == _messageBufferSize)
            {
                synchronized(_buffers)
                {
                	_buffers.add(buffer);
                }
            }
        }
	}
	
	public int getMessageBufferSize()
	{
		return _messageBufferSize;
	}
	
	protected Buffer newBuffer(int size)
	{
		return new ByteArrayBuffer(size);
	}
	
	public int getAcceptors()
	{
		return _acceptors;
	}
	
	public void setHost(String host)
	{
		_host = host;
	}
	
	public String getHost()
	{
		return _host;
	}
	
	public void setPort(int port)
	{
		_port = port;
	}
	
	public int getPort()
	{
		return _port;
	}
	
	public void setNode(Node node)
	{
		if (_node != null && _node != node && _node.getServer() != null)
			_node.getServer().getContainer().update(this, _listener, null, "listener");
				
		if (node != null && node != _node && node.getServer() != null)
			node.getServer().getContainer().update(this, null, _listener, "listener");
		
		_node = node;
	}
	
	public Node getNode()
	{
		return _node;
	}
	
	protected abstract void accept(int acceptorID) throws IOException, InterruptedException;
	
	public String toString()
    {
        String name = this.getClass().getName();
        int dot = name.lastIndexOf('.');
        if (dot>0)
            name=name.substring(dot+1);
        
        return name+"@"+(getHost()==null?"0.0.0.0":getHost())+":"+(getLocalPort()<=0?getPort():getLocalPort());
    }
	
	private class Acceptor implements Runnable
	{
		int _acceptor = 0;
		
		Acceptor(int id)
		{
			_acceptor = id;
		}
		public void run()
		{
			Thread current = Thread.currentThread();
			synchronized (AbstractDiameterConnector.this) 
			{
				if (_acceptorThread == null) 
					return;
				_acceptorThread[_acceptor] = current;
			}
			String name = _acceptorThread[_acceptor].getName();
			current.setName(name + " - Acceptor" + _acceptor + " " + AbstractDiameterConnector.this);
			int priority = current.getPriority();
			
			try
			{
				current.setPriority(priority - _acceptorPriorityOffset); 
				while (isRunning() && getTransport() != null)
				{
					try
					{
						accept(_acceptor);
					}
					catch (IOException e)
					{
						Log.ignore(e);
					}
					catch (Throwable t)
					{
						Log.warn(t);
					}
				}
			}
			finally 
			{
				current.setPriority(priority);
				current.setName(name);
				try
				{
					if (_acceptor == 0)
						close();
				}
				catch (IOException e)
				{
					Log.warn(e);
				}
				synchronized (AbstractDiameterConnector.this)
				{
					if (_acceptorThread != null)
						_acceptorThread[_acceptor] = null;
				}
			}
		}
	}
}
