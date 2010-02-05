package org.cipango.media.api;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;

/**
 * Listens to incoming RTP packets.
 * 
 * A user of this class must implement {@link RtpListener} so that when
 * an RTP packet is received, {@link RtpReader} will invoke its
 * {@link RtpListener#receivedRtpPacket(MediaBuffer)} method. To be notified of
 * incoming packets, {@link #addRtpListener(RtpListener)} must be invoked
 * with the corresponding {@link RtpListener}. This RtpReader opens a socket,
 * listens on a local port, receives RTP packets, and invokes its listeners
 * when a packet is received.
 *  
 * @author yohann
 */
public class RtpReader implements Managed, LifeCycle, Initializable
{

	private InetAddress _inetAddress;
	private int _port;
	private List<RtpListener> _rtpListeners;
	private ExecutorService _executorService;
	private boolean _running = false;
	private Buffer _buffer;
	private UdpEndPoint _udpEndPoint;
	private RtpParser _rtpParser;

    RtpReader(int port)
    {
    	this(null, port);
    }

    RtpReader(InetAddress inetAddress, int port)
    {
        _inetAddress = inetAddress;
        _port = port;
    }

    @Override
    public void init()
    {
    	_rtpListeners = new ArrayList<RtpListener>();
    	_executorService = MediaFactory.getExecutorService();
    	try
		{
			_udpEndPoint = MediaFactory.getUdpEndPoint(_port, _inetAddress);
		}
		catch (NoObjectAvailableException e)
		{
			Log.warn("cannot retrieve UdpEndPoint instance", e);
		}
    	_buffer = new ByteArrayBuffer(_udpEndPoint.getReceiveBufferSize());
    	_rtpParser = MediaFactory.getRtpParser();
    }

    /**
     * Adds an {@link RtpListener} to this {@link RtpReader}.
     * This listener will be invoked when an RTP packet is received.
     * Several {@link RtpListener}s can listen to the same {@link RtpReader}.
     * 
     * @param rtpListener the call back
     */
    public void addRtpListener(RtpListener rtpListener)
    {
        _rtpListeners.add(rtpListener);
    }

    /**
     * Removes an {@link RtpListener} from the list of listeners.
     * After this method invocation, {@link RtpListener} will not be notified
     * of incoming RTP packets from this {@link RtpReader} anymore.
     * 
     * @param rtpListener the {@link RtpListener} to remove from this
     * {@link RtpReader} listeners list.
     */
    public void removeRtpListener(RtpListener rtpListener)
    {
        _rtpListeners.remove(rtpListener);
    }

    /**
     * Starts listening for incoming RTP packets. This method is non-blocking.
     */
	@Override
	public void start()
	{
		_executorService.execute(new RtpReaderTask());
		_running = true;
	}

	/**
	 * Stops listening for incoming RTP packets.
	 */
	@Override
	public void stop()
	{
		_running = false;
	}

	class RtpReaderTask implements Runnable
	{

		@Override
		public void run()
		{
			if(!_running)
				return;
			try
			{
				_udpEndPoint.read(_buffer);
			}
			catch (IOException e)
			{
				Log.warn("cannot read on datagram socket " + _inetAddress
						+ ":" + _port, e);
				return;
			}
        	RtpPacket rtpPacket = _rtpParser.decode(_buffer);
        	// maybe rtpListener.receivedRtpPacket should be invoked using
        	// executorService.
        	for (RtpListener rtpListener: _rtpListeners)
        		rtpListener.receivedRtpPacket(rtpPacket);
			if(_running)
				_executorService.execute(this);
		}

	}

	public InetAddress getInetAddress()
	{
		return _inetAddress;
	}

	public int getPort()
	{
		return _port;
	}

}
