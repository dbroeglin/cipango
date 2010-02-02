package org.cipango.media.api;


/**
 * Analyzes incoming DTMF RTP packets to trigger real telephone events.
 * 
 * DTMF format for RTP packets is described in
 * <a href="http://www.ietf.org/rfc/rfc4733.txt">RFC 4733</a>. This class
 * decodes out of band telephone-events. It uses event codes only.
 * Telephony tones are not supported. In-bound events are not detected.
 * DtmfFilter implements the algorithm that will understand when a new key
 * has been pressed on user phone keyboard. Actually, it will detect an event
 * when it receives two consecutive packets: one without End bit marker and
 * one with End bit marker. To trigger this key pressed event,
 * {@link DtmfListener}s must be added to DtmfFilter. Those listeners have
 * only one method, this method will be invoked when a new key is detected.
 * 
 * @author yohann
 */
public class DtmfFilter implements RtpListener
{

	/**
	 * Adds a DtmfListener to this DtmfFilter. This DtmfListener will be
	 * invoked when a new key is pressed with the corresponding character.
	 * 
	 * @param DtmfListener the DtmfListener that will be invoked when a key
	 * is pressed.
	 */
	public void addDtmfListener(DtmfListener DtmfListener)
	{
		
	}

	/**
	 * Removes a DtmfListener from the list of DtmfListeners of this
	 * DtmfFilter. Once removed, this DtmfListener will not be invoked anymore
	 * on telephone events detection.
	 * 
	 * @param DtmfListener the DtmfListener that does not need notifications
	 * anymore.
	 */
	public void removeDtmfListener(DtmfListener DtmfListener)
	{
		
	}

	/**
	 * Sets the payload type of this DtmfFilter. The payload type will be
	 * necessary to filter only telephone-events RTP packets amongst all RTP
	 * packets. DtmfFilter is an {@link RtpListener}, thus it will receive
	 * notifications for all incoming RTP packets. That is why it needs to
	 * filter only telephone-events.
	 * 
	 * @param payloadType this integer represents the payload type as defined
	 * in SDP.
	 */
	public void setPayloadType(int payloadType)
	{
		
	}

	@Override
	public void receivedRtpPacket(RtpPacket rtpPacket)
	{
		// TODO Auto-generated method stub
		
	}

}
