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

package org.cipango.media;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cipango.media.codecs.TelephoneEventCodec;
import org.cipango.media.rtp.RtpCodec;
import org.cipango.media.rtp.RtpPacket;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;

/**
 * This class implements DTMF detection. It analyzes incoming RTP DTMF packets
 * and invokes a callback on its list of DTMF listeners. To detect a new DTMF,
 * at least two packets must be received: one without end marker bit set in
 * telephone-event payload, and one with end marker bit set. Generally, several
 * packets are sent without end marker bit set: at least one with RTP marker
 * and several intermediate packets without any marker; and three packets with
 * end marker bit set in telephone-event payload. Thus, even if several packets
 * are lost during DTMF transmission, the probability that at least one packet
 * without end marker and one packet with marker is recveived is very high.
 * <p>
 * To receive DTMF notifications, create a class that implements DtmfListener
 * interface, and add this listener to your DtmfHandler instance with method
 * addDtmfListener.
 * 
 * @author yohann
 */
public class DtmfHandler implements Runnable
{

    public static final int RTP_PACKET_SIZE = 172; // bytes

    private static final int DEFAULT_PORT = -1;

    private int _payloadType;
    private int _port = DEFAULT_PORT; 
    private UdpEndPoint _udpEndPoint;
    private RtpCodec _rtpCodec;
    private TelephoneEventCodec _telephoneEventCodec;
    private boolean _running;
    private ByteArrayBuffer _buffer;
    private boolean _active;

    private List<DtmfListener> _dtmfListeners;

    public DtmfHandler(int payloadType)
    {
        this(payloadType, DEFAULT_PORT);
    }

    public DtmfHandler(int payloadType, int port)
    {
        _payloadType = payloadType;
        _port = port;
        _running = false;
    }

    public DtmfHandler(int payloadType, UdpEndPoint udpEndPoint)
    {
        _payloadType = payloadType;
        _udpEndPoint = udpEndPoint;
    }

    public void init()
    {
        if (_udpEndPoint == null)
        {
            DatagramSocket datagramSocket;
            try
            {
                if (_port == DEFAULT_PORT)
                {
                    datagramSocket = new DatagramSocket();
                    _port = datagramSocket.getLocalPort();
                }
                else
                    datagramSocket = new DatagramSocket(_port);
            }
            catch (SocketException e)
            {
                Log.warn("SocketException", e);
                return;
            }
            _udpEndPoint = new UdpEndPoint(datagramSocket);
        }
        _running = true;
        _buffer = new ByteArrayBuffer(RTP_PACKET_SIZE);
        _rtpCodec = new RtpCodec();
        _telephoneEventCodec = new TelephoneEventCodec();
        _dtmfListeners = Collections.synchronizedList(
                new ArrayList<DtmfListener>());
        _active = false;
    }

    @Override
    public void run() {
        while (_running)
        {
            try
            {
                _udpEndPoint.read(_buffer);
            }
            catch (IOException e)
            {
                Log.ignore(e);
                break;
            }
            
            RtpPacket rtpPacket = _rtpCodec.decode(_buffer);
            if (rtpPacket.getPayloadType() != _payloadType)
            {
                _buffer.clear();
                continue;
            }
            Buffer buffer = rtpPacket.getData();
            char event = _telephoneEventCodec.decode(buffer);
            if (!_active)
            {
                if ((byte)(buffer.get() & -128) != -128) // no end bit
                    _active = true;
            }
            else
                if ((byte)(buffer.get() & -128) == -128) // end bit
                {
                    _active = false;
                    for (DtmfListener dtmfListener: _dtmfListeners)
                        dtmfListener.telephoneEvent(event);
                }
            _buffer.clear();
        }
        try
        {
            _udpEndPoint.close();
        }
        catch (IOException e)
        {
            Log.warn("IOException", e);
        }
    }

    public void stop()
    {
        _running = false;
        try {
            _udpEndPoint.close();
        }
        catch (IOException e)
        {
            Log.ignore(e);
        }
    }

    public int getPort() {
        return _port;
    }

    /**
     * Adds a DTMF listener to this DTMF handler.
     * 
     * @param dtmfListener
     */
    public void addDtmfListener(DtmfListener dtmfListener)
    {
        _dtmfListeners.add(dtmfListener);
    }

    public void removeDtmfListener(DtmfListener dtmfListener)
    {
        _dtmfListeners.remove(dtmfListener);
    }

}
