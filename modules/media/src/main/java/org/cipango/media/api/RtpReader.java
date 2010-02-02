package org.cipango.media.api;

import java.net.InetAddress;

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
public class RtpReader implements Managed, LifeCycle
{

    RtpReader(int port)
    {
        
    }

    RtpReader(InetAddress inetAddress, int port)
    {
        
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
        
    }

    /**
     * Starts listening for incoming RTP packets. This method is non-blocking.
     */
	@Override
	public void start()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Stops listening for incoming RTP packets.
	 */
	@Override
	public void stop()
	{
		// TODO Auto-generated method stub
		
	}

}
