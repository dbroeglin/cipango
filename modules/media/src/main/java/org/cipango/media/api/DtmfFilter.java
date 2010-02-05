// ========================================================================
// Copyright 2008-2010 NEXCOM Systems
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

package org.cipango.media.api;

import java.util.ArrayList;
import java.util.List;

import org.mortbay.io.Buffer;


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
public class DtmfFilter implements RtpListener, Initializable, Managed
{

    public static final char[] EVENTS = { '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', '*', '#', 'A', 'B', 'C', 'D' };

	private List<DtmfListener> _dtmfListeners;
	private int _payloadType;
	private boolean _activeDtmf;

	@Override
	public void init()
	{
		_dtmfListeners = new ArrayList<DtmfListener>();
		_activeDtmf = false;
	}

	/**
	 * Adds a DtmfListener to this DtmfFilter. This DtmfListener will be
	 * invoked when a new key is pressed with the corresponding character.
	 * 
	 * @param DtmfListener the DtmfListener that will be invoked when a key
	 * is pressed.
	 */
	public void addDtmfListener(DtmfListener dtmfListener)
	{
		_dtmfListeners.add(dtmfListener);
	}

	/**
	 * Removes a DtmfListener from the list of DtmfListeners of this
	 * DtmfFilter. Once removed, this DtmfListener will not be invoked anymore
	 * on telephone events detection.
	 * 
	 * @param DtmfListener the DtmfListener that does not need notifications
	 * anymore.
	 */
	public void removeDtmfListener(DtmfListener dtmfListener)
	{
		_dtmfListeners.remove(dtmfListener);
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
		_payloadType = payloadType;
	}

	@Override
	public void receivedRtpPacket(RtpPacket rtpPacket)
	{
		if (rtpPacket.getPayloadType() != _payloadType)
			return;
		Buffer buffer = rtpPacket.getData();
        byte event = buffer.get();
        char c;
        if (event > -1 && event < 16)
            c = EVENTS[event];
        else
        	c = (char)event;
        if (!_activeDtmf)
        {
            if ((byte)(buffer.get() & -128) != -128) // no end bit
                _activeDtmf = true;
        }
        else
            if ((byte)(buffer.get() & -128) == -128) // end bit
            {
                _activeDtmf = false;
                for (DtmfListener dtmfListener: _dtmfListeners)
                    dtmfListener.dtmfReceived(c);
            }
	}

}
