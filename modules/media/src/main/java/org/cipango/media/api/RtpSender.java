package org.cipango.media.api;

import java.net.InetAddress;

import org.mortbay.io.Buffer;

/**
 * RtpSender is the class employed to send RTP packets over the network.
 * 
 * It takes at least a destination IP address and port as parameters, and
 * sends RTP packets from raw encoded data, for which RTP payload has already
 * been applied. In this case, {@link RtpSender} builds RTP packets filled with
 * data provided. But it can also send {@link RtpPacket}s already
 * constructed by another class. In this case, the third-party class has to
 * build {@link RtpPacket}s itself.
 * 
 * @author yohann
 */
public class RtpSender implements Managed
{

    RtpSender(InetAddress destAddress, int destPort)
    {
        
    }

    RtpSender(InetAddress destAddress, int destPort, int localPort)
    {
        
    }

    RtpSender(InetAddress destAddress, int destPort, InetAddress localAddress,
            int localPort)
    {
        
    }

    /**
     * Sends encoded data over the network. All RTP fields will be updated
     * appropriately, i.e. timestamp, sequence number, etc.
     * 
     * @param buffer encoded data to send to the remote RTP party.
     */
    public void send(Buffer buffer)
    {
        
    }

    /**
     * Sends an RtpPacket. This method allows RtpSender user to send customized
     * RtpPackets.
     * 
     * @param rtpPacket the RtpPacket that must be sent.
     */
    public void send(RtpPacket rtpPacket)
    {
    	
    }

}
