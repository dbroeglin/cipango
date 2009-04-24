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
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.cipango.diameter.base.Base;
import org.mortbay.log.Log;

/**
 *  A Diameter Peer is a Diameter Node to which a given Diameter Node 
 *  has a direct transport connection.
 *
 */
public class Peer
{
	private Node _node;
	
	private String _host;
	private InetAddress _address;
	private int _port;
	
	private State _state;
	
	private DiameterConnection _rConnection;
	private DiameterConnection _iConnection;
	
	private Map<Integer, DiameterRequest> _pendingRequests = new HashMap<Integer, DiameterRequest>();
	
	public Peer()
	{
		_state = CLOSED;
	}
	
	public Peer(String host)
	{
		_host = host;
		_state = CLOSED;
	}
	
	public void setHost(String host)
	{
		if (_host != null)
			throw new IllegalArgumentException("Host already set");
		_host = host;
	}
	
	public String getHost()
	{
		return _host;
	}
	
	public int getPort()
	{
		return _port;
	}
	
	public void setPort(int port)
	{
		_port = port;
	}
	
	public void setAddress(InetAddress address)
	{
		_address = address;
	}
	
	public InetAddress getAddress()
	{
		return _address;
	}
	
	public Node getNode()
	{
		return _node;
	}
	
	public void setNode(Node node)
	{
		_node = node;
	}
	
	public boolean isOpen()
	{
		return _state == OPEN;
	}
	
	public DiameterConnection getConnection()
	{
		return _iConnection != null ? _iConnection : _rConnection;
	}
	
	public void send(DiameterRequest request) throws IOException
	{
		if (!isOpen())
			throw new IOException("peer not open");
		
		DiameterConnection connection = getConnection();
		if (connection == null || !connection.isOpen())
			throw new IOException("connection not open");
		
		synchronized (_pendingRequests)
		{
			_pendingRequests.put(request.getHopByHopId(), request);
		}
		connection.write(request);
	}
	
	public void receive(DiameterMessage message) throws IOException
	{
		if (message.isRequest())
			receiveRequest((DiameterRequest) message);
		else
			receiveAnswer((DiameterAnswer) message);
	}
	
	protected void receiveRequest(DiameterRequest request) throws IOException
	{
		switch (request.getCommand()) 
		{
		case Base.DWR:
			receiveDWR(request);
			return;

		default:
			break;
		}
		getNode().handle(request);
	}
	
	protected void receiveAnswer(DiameterAnswer answer) throws IOException
	{
		switch (answer.getCommand()) 
		{
		case Base.CEA:
			_state.rcvCEA(answer);
			return;
		}
		System.out.println("receive answer " + answer);
		 
		int rc = -1;
		AVP avp = answer.getAVP(Base.RESULT_CODE);
		if (avp == null)
		{
			
			avp = answer.getAVP(Base.EXPERIMENTAL_RESULT);
			if (avp != null )
				rc = avp.getGrouped().getAVP(Base.EXPERIMENTAL_RESULT_CODE).getInt();
			
		}
		else
		{
			rc = avp.getInt();
		}
		DiameterRequest request;
		synchronized (_pendingRequests)
		{
			request = _pendingRequests.remove(answer.getHopByHopId());
		}
		answer.setRequest(request);
		
		getNode().handle(answer);
	}
	
	protected void receiveDWR(DiameterRequest dwr)
	{
		DiameterAnswer dwa = dwr.createAnswer(Base.DIAMETER_SUCCESS);
		try
		{
			dwr.getConnection().write(dwa);
		}
		catch (Exception e)
		{
			Log.ignore(e);
		}
	}
	// --
	
	protected void setState(State state)
	{
		Log.debug(this + " " + _state + " > " + state);
		_state = state;
	}
	
	public String toString()
	{
		return _host + " (" + _state + ")";
	}
	
	// ==================== Events ====================
	
	public void start()
	{
		if (_host == null)
			throw new IllegalArgumentException("host not set");
		_state.start();
	}
	
	public void rConnCER(DiameterRequest cer)
	{
		_state.rConnCER(cer);
	}
	
	public void disc(DiameterConnection connection)
	{
		_state.disc(connection);
	}
	
	// ==================== Actions ====================

	protected void iSndConnReq()
	{
		new Thread(new Runnable() 
		{
			public void run() 
			{
				try
				{
					_iConnection = _node.getConnection(Peer.this);
				}
				catch (IOException e)
				{
					Log.debug("Failed to connect to peer " + Peer.this);
				}
				if (_iConnection != null)
					_state.rcvConnAck();
			}
		}).start();
		setState(WAIT_CONN_ACK);
	}
	
	abstract class State
	{
		private String _name;
		
		public State(String name)
		{
			_name = name;
		}
		
		public void start() { throw new IllegalStateException("start() in state " + _name); }
		public void rConnCER(DiameterRequest cer) { throw new IllegalStateException("rConnCER() in state " + _name); }
		public void rcvConnAck() { throw new IllegalStateException("rcvConnAck() in state " + _name); }
		public void rcvConnNack() { throw new IllegalStateException("rcvConnNack() in state " + _name); }
		public void rcvCEA(DiameterAnswer cea) { throw new IllegalStateException("rcvCEA() in state " + _name); }
		public void disc(DiameterConnection connection) { throw new IllegalStateException("disc() in state " + _name); } 
		public String toString() { return _name; }
	}
	
	/**
	 * <pre>
	 *    state            event              action         next state
	 *    -----------------------------------------------------------------
	 *    Closed           Start            I-Snd-Conn-Req   Wait-Conn-Ack
	 *                     R-Conn-CER       R-Accept,        R-Open
	 *                                      Process-CER,
	 *                                      R-Snd-CEA
	 * </pre>
	 */    
	State CLOSED = new State("Closed")
	{
		public synchronized void start()
		{
			iSndConnReq();
		}
		
		public synchronized void rConnCER(DiameterRequest cer)
		{
			_rConnection = cer.getConnection();
			
			DiameterAnswer cea = cer.createAnswer(Base.DIAMETER_SUCCESS);
			try
			{
				cea.send();
			}
			catch (IOException e)
			{
				Log.debug(e);
			}
			setState(OPEN);
		}
	};
	
	/**
	 * <pre>
	 * state            event              action         next state
	 * -----------------------------------------------------------------
	 * Wait-Conn-Ack    I-Rcv-Conn-Ack   I-Snd-CER        Wait-I-CEA
	 *                  I-Rcv-Conn-Nack  Cleanup          Closed
	 *                  R-Conn-CER       R-Accept,        Wait-Conn-Ack/
	 *                                   Process-CER      Elect
	 *                  Timeout          Error            Closed
	 * </pre>
	 */
	State WAIT_CONN_ACK = new State("Wait-Conn-Ack")
	{
		public synchronized void rcvConnAck()
		{
			DiameterRequest cer = new DiameterRequest(getNode(), Base.CER, 0, null);
			getNode().addCapabilities(cer);
			
			try
			{
				getConnection().write(cer);
			}
			catch (IOException e)
			{
				Log.debug(e);
			}
			setState(WAIT_CEA);
		}
	};
	
	/**
	 * <pre>
	 *    Wait-I-CEA       I-Rcv-CEA        Process-CEA      I-Open
	 *                     R-Conn-CER       R-Accept,        Wait-Returns
	 *                                      Process-CER,
	 *                                      Elect
	 *                     I-Peer-Disc      I-Disc           Closed
	 *                     I-Rcv-Non-CEA    Error            Closed
	 *                     Timeout          Error            Closed
	 * </pre>
	 */
	State WAIT_CEA = new State("Wait-CEA")
	{
		public synchronized void rcvCEA(DiameterAnswer cea)
		{
			setState(OPEN);
		}
	};
	
	State OPEN = new State("Open")
	{
		public synchronized void disc(DiameterConnection connection)
		{
			if (connection == getConnection())
			{
				setState(CLOSED);
			}
		}
	};
}
