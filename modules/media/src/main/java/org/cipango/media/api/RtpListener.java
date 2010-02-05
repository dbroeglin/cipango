package org.cipango.media.api;

/**
 * An RtpListener is invoked by {@link RtpReader} when an RTP packet is
 * received.
 * 
 * It notifies its implementing classes that a new RTP packet has been
 * received. Thus, it provides a reference to the corresponding
 * {@link RtpPacket}.
 * 
 * @author yohann
 */
public interface RtpListener
{

	/**
	 * Notifies the arrival of an incoming RTP packet. mediaBuffer is the
	 * content of the RTP packet: its corresponding data field in RTP packet.
	 * 
	 * @param mediaBuffer encoded media received in RTP packet. In mediaBuffer,
	 * RTP payload has not been decoded yet.
	 */
    public void receivedRtpPacket(RtpPacket rtpPacket);

}
