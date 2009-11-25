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
import java.util.TimerTask;

import org.cipango.diameter.base.Base;
import org.cipango.diameter.base.Base.DisconnectCause;
import org.mortbay.log.Log;

/**
 *  A Diameter Peer is a Diameter Node to which a given Diameter Node
 *  has a direct transport connection.
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
	
	private long _lastReceivedTime;
	private boolean _waitForDwa = false;
	
	// indicate whether the peer has been explicitly stopped
	private boolean _stopped;
	
	public Peer()
	{
		_state = CLOSED;
	}
	
	public Peer(String host)
	{
		this();
		_host = host;
	}
	
	public String getHost()
	{
		return _host;
	}
	
	public void setHost(String host)
	{
		if (_host != null)
			throw new IllegalArgumentException("host already set");
		_host = host;
	}
	
	public int getPort()
	{
		return _port;
	}
	
	public void setPort(int port)
	{
		_port = port;
	}
	
	public InetAddress getAddress()
	{
		return _address;
	}
	
	public void setAddress(InetAddress address)
	{
		_address = address;
	}
	
	public Node getNode()
	{
		return _node;
	}
	
	public void setNode(Node node)
	{
		_node = node;
	}
	
	public State getState()
	{
		return _state;
	}
	
	public String getStateAsString()
	{
		return _state.toString();
	}
	
	public boolean isOpen()
	{
		return _state == OPEN;
	}
	
	public boolean isClosed()
	{
		return _state == CLOSED;
	}
		
	public DiameterConnection getConnection()
	{
		return _iConnection != null ? _iConnection : _rConnection;
	}
	
	public void send(DiameterRequest request) throws IOException
	{
		// FIXME find a solution when peer is starting.
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
		_lastReceivedTime = System.currentTimeMillis();
		
		if (message.isRequest())
			receiveRequest((DiameterRequest) message);
		else
			receiveAnswer((DiameterAnswer) message);
	}
	
	protected void receiveRequest(DiameterRequest request) throws IOException
	{
		synchronized (this)
		{
			switch (request.getCommand().getCode()) 
			{
			case Base.DWR_ORDINAL:
				receiveDWR(request);
				return;
			case Base.CER_ORDINAL:
				rConnCER(request);
				return;
			case Base.DPR_ORDINAL:
				_state.rcvDPR(request);
				return;
			default:
				break;
			}
		}
		getNode().handle(request);
	}
	
	protected void receiveAnswer(DiameterAnswer answer) throws IOException
	{
		synchronized (this)
		{
			switch (answer.getCommand().getCode()) 
			{
			case Base.CEA_ORDINAL:
				_state.rcvCEA(answer);
				return;
			case Base.DWA_ORDINAL:
				_waitForDwa = false;
				return;
			case Base.DPA_ORDINAL:
				_state.rcvDPA(answer);
				return;
			}
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
	
    
    
    public void sendDWRIfNeeded(long tw)
    {
    	if (isOpen() && _lastReceivedTime + tw  <= System.currentTimeMillis())
    	{
	    	if (_waitForDwa)
			{
				Log.warn("Close " + this + " as no DWA received");
				close();
			}
	    	else
			{
				try
				{
					DiameterRequest request = new DiameterRequest(_node, Base.DWR, 0, null);
			    	DiameterConnection connection = getConnection();
					if (connection == null || !connection.isOpen())
						throw new IOException("connection not open");
					
					connection.write(request);
				} 
				catch (IOException e)
				{
					// Ignore exception as 
					Log.ignore(e);
				}
			}
    	}
    }
    	
	public String toString()
	{
		return _host + " (" + _state + ")";
	}
	
	// ==================== Events ====================
	
	/**
	 * Starts the peers
	 */
	public synchronized void start()
	{
		if (_host == null)
			throw new IllegalStateException("host not set");
		_state.start();
	}
	
	/**
	 * Handles an incoming connection request (CER)
	 * 
	 * @param cer	the incoming CER
	 */
	public synchronized void rConnCER(DiameterRequest cer)
	{
		_state.rConnCER(cer);
	}
	
	/**
	 * The transport connection has been closed
	 * 
	 * @param connection the closed connection
	 */
	public synchronized void peerDisc(DiameterConnection connection)
	{
		_state.disc(connection);
	}
	
	/**
	 * The Diameter application has signaled that a connection should be 
	 * terminated (e.g., on system shutdown).
	 */
	public synchronized void stop() 
	{
		_stopped = true;
		if (_state == OPEN)
		{
			try 
			{
				setState(CLOSING);
				
				DiameterRequest dpr = new DiameterRequest(_node, Base.DPR, 0, null);
				dpr.add(Base.DISCONNECT_CAUSE, DisconnectCause.REBOOTING);
				getConnection().write(dpr);	
			} 
			catch (IOException e) 
			{
				Log.warn("Unable to send DPR on shutdown", e);
			}
		} 
		else if (_state != CLOSING)
			setState(CLOSED);
	}
	
	protected void close() 
	{
		_rConnection = _iConnection = null;
		setState(CLOSED);
		if (!_stopped)
			_node.scheduleReconnect(new ReconnectTask());
	}
	
	/**
	 * An application-defined timer has expired while waiting for some event. 
	 */
	protected void timeout()
	{
		close();
	}
	
	// ==================== Actions ====================

	protected void iSndConnReq()
	{
		// cnx request is blocking so start in a new thread
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
				synchronized (Peer.this)
				{
					if (_iConnection != null)
						_state.rcvConnAck();
					else
						_state.rcvConnNack();
				}
			}
		}).start();
		setState(WAIT_CONN_ACK);
	}
	
	protected boolean elect()
    {
        String other = getHost();
        String local = _node.getIdentity();
        
        boolean won = (local.compareTo(other) > 0);
        if (won)
            Log.debug("Won election (" + local + ">" + other + ")");
        return won;      
    }
    
    protected void sendCER()
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
    }
    
    protected void iDisc()
    {
        if (_iConnection != null) 
        {
            try { _iConnection.close(); } 
            catch (IOException e) { Log.debug("Failed to disconnect " + _iConnection + " on peer " + this + ": " + e); }
            _iConnection = null;
        }
    }
    
    protected void rDisc()
    {
        if (_rConnection != null) 
        {
        	try { _rConnection.close(); } 
        	catch (IOException e) { Log.debug("Failed to disconnect " + _rConnection + " on peer " + this + ": " + e); }
        	_rConnection = null;
        }
    }
    
    protected void sendCEA(DiameterRequest cer)
    {
    	DiameterAnswer cea = cer.createAnswer(Base.DIAMETER_SUCCESS);
		try
		{
			cea.send();
		}
		catch (IOException e)
		{
			Log.debug(e);
		}
    	
    }
	
    // peer states 
    
	private abstract class State
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
		public void rcvDPR(DiameterRequest dpr) { throw new IllegalStateException("rcvDPR() in state " + _name); }
		public void rcvDPA(DiameterAnswer dpa) { throw new IllegalStateException("rcvDPA() in state " + _name); }
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
			_rConnection = _iConnection = null;
			iSndConnReq();
		}
		
		public synchronized void rConnCER(DiameterRequest cer)
		{
			_rConnection = cer.getConnection();
			
			sendCEA(cer);
			setState(OPEN);
		}
		
		public synchronized void disc(DiameterConnection connection)
		{
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
			sendCER();
			setState(WAIT_CEA);
		}

		@Override
		public synchronized void rcvConnNack()
		{
			_iConnection = null;
			close();
		}

		@Override
		public synchronized void rConnCER(DiameterRequest cer)
		{
			_rConnection = cer.getConnection();
			if (elect())
            {
                iDisc();
                sendCEA(cer);
                setState(OPEN);
            }
            else 
                setState(WAIT_RETURNS);
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

		public synchronized void disc(DiameterConnection connection)
		{
			iDisc();
			close();
		}

		public synchronized void rConnCER(DiameterRequest cer)
		{
			_rConnection = cer.getConnection();
			if (elect())
            {
                iDisc();
                sendCEA(cer);
                setState(OPEN);
            }
            else 
                setState(WAIT_RETURNS);
		}
		
	};
	
	/**
	 * <pre>
	 * Wait-Conn-Ack/   I-Rcv-Conn-Ack   I-Snd-CER,Elect  Wait-Returns
     * Elect            I-Rcv-Conn-Nack  R-Snd-CEA        R-Open
     *                  R-Peer-Disc      R-Disc           Wait-Conn-Ack
     *                  R-Conn-CER       R-Reject         Wait-Conn-Ack/
     *                                                    Elect
     *                  Timeout          Error            Closed
     *</pre>
	 */
	State WAIT_CONN_ACK_ELECT = new State("Wait-Conn-Ack-Elect")
	{

		@Override
		public synchronized void disc(DiameterConnection connection)
		{
			iDisc();
			close();
		}

		@Override
		public synchronized void rConnCER(DiameterRequest cer)
		{
			_rConnection = null;
		}

		@Override
		public synchronized void rcvConnAck()
		{
			sendCER();
			if (elect())
			{
				iDisc();
				// TODO send CEA
				setState(OPEN);
			}
			else
				setState(WAIT_RETURNS);
		}

		@Override
		public synchronized void rcvConnNack()
		{
			// TODO send CEA
			setState(OPEN);
		}
		
	};
	
	/**
	 * <pre>
	 *  Wait-Returns     Win-Election     I-Disc,R-Snd-CEA R-Open
	 *                   I-Peer-Disc      I-Disc,          R-Open
	 *                                    R-Snd-CEA
	 *                   I-Rcv-CEA        R-Disc           I-Open
	 *                   R-Peer-Disc      R-Disc           Wait-I-CEA
	 *                   R-Conn-CER       R-Reject         Wait-Returns
	 *                   Timeout          Error            Closed
	 *</pre>
	 */
	State WAIT_RETURNS = new State("Wait-Returns")
	{

		@Override
		public synchronized void disc(DiameterConnection connection)
		{
			if (connection == _iConnection)
			{
				iDisc();
				// TODO sendCea
				setState(OPEN);
			}
			else if (connection == _rConnection)
			{
				rDisc();
				setState(WAIT_CEA);
			}
		}

		@Override
		public synchronized void rConnCER(DiameterRequest cer)
		{
			_rConnection = null;
		}

		@Override
		public synchronized void rcvCEA(DiameterAnswer cea)
		{
			rDisc();
			setState(OPEN);
		}
		
	};
	
	/**
	 * <pre>
	 *  I-Open      Send-Message     I-Snd-Message    I-Open
	 *              I-Rcv-Message    Process          I-Open
	 *              I-Rcv-DWR        Process-DWR,     I-Open
	 *                               I-Snd-DWA
	 *              I-Rcv-DWA        Process-DWA      I-Open
	 *              R-Conn-CER       R-Reject         I-Open
	 *              Stop             I-Snd-DPR        Closing
	 *              I-Rcv-DPR        I-Snd-DPA,       Closed
	 *                                 I-Disc
	 *              I-Peer-Disc      I-Disc           Closed
	 *              I-Rcv-CER        I-Snd-CEA        I-Open
	 *              I-Rcv-CEA        Process-CEA      I-Open
	 *</pre>
	 */
	State OPEN = new State("Open")
	{
		public synchronized void disc(DiameterConnection connection)
		{
			if (connection == getConnection())
			{
				close();
			}
		}

		@Override
		public synchronized void rcvDPR(DiameterRequest dpr)
		{
			try {
				DiameterAnswer dpa = dpr.createAnswer(Base.DIAMETER_SUCCESS);
				dpr.getConnection().write(dpa);
			} catch (IOException e) {
				Log.warn("Unable to send DPA");
			}
			
			setState(CLOSED);
			_rConnection = _iConnection = null;
			
			// Start reconnect task if Disconnect cause is rebooting
			if (dpr.get(Base.DISCONNECT_CAUSE) == DisconnectCause.REBOOTING)
			{
				_node.scheduleReconnect(new ReconnectTask());
			}
		}	
	};
	
	/**
	 * <pre>
	 *  Closing     I-Rcv-DPA        I-Disc           Closed
	 *              R-Rcv-DPA        R-Disc           Closed
	 *              Timeout          Error            Closed
	 *              I-Peer-Disc      I-Disc           Closed
	 *              R-Peer-Disc      R-Disc           Closed
	 * </pre>
	 */
	State CLOSING = new State("Closing")
	{
		@Override
		public void disc(DiameterConnection connection)
		{
			setState(CLOSED);
		}

		@Override
		public void rcvDPA(DiameterAnswer dpa)
		{
			setState(CLOSED);
		}
		
	};
	
	class ReconnectTask extends TimerTask
	{
        public ReconnectTask()
        {
            Log.debug("Create new reconnect Task for " + Peer.this);
        }
        
        public void run()
        {
            try
            {
            	if (_node.isStarted())
            	{
            		Log.debug("Restarting peer: " + this);
            		 if (!_stopped)
            			 start();
            	}
            }
            catch (Exception e)
            {
                Log.warn("Failed to reconnect to peer: " + Peer.this);
            }
        }
    }

}
