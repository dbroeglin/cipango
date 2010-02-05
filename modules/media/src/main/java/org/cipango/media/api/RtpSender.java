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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;

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
public class RtpSender implements Managed, Initializable
{

	public static final int DEFAULT_PTIME = 20;

	private int _localPort;
	private InetAddress _localAddress;
	private int _remotePort;
	private InetAddress _remoteAddress;
	private SocketAddress _remoteSocketAddress;
	private RtpParser _rtpParser;
	private UdpEndPoint _udpEndPoint;
	private Buffer _packetBuffer;

	private int _ptime = DEFAULT_PTIME;
    private int _ssrc;
    private int _seqNumber;
    private long _timestamp;
    private int _payloadType;
    private int _dataLength;

    RtpSender(int payloadType, InetAddress destAddress, int destPort)
    {
        this(payloadType, destAddress, destPort, null,
        		MediaFactory.DEFAULT_PORT);
    }
    
    RtpSender(int payloadType, InetAddress destAddress, int destPort,
    		int localPort)
    {
        this(payloadType, destAddress, destPort, null, localPort);
    }

    RtpSender(int payloadType, InetAddress destAddress, int destPort,
    		InetAddress localAddress, int localPort)
    {
    	_payloadType = payloadType;
        _remoteAddress = destAddress;
        _remotePort = destPort;
        _localAddress = localAddress;
        _localPort = localPort;
    }

	@Override
	public void init()
	{
		_rtpParser = MediaFactory.getRtpParser();
		try
		{
			_udpEndPoint = MediaFactory.getUdpEndPoint(_localPort,
					_localAddress);
		}
		catch (NoObjectAvailableException e)
		{
			Log.warn("cannot retrieve UdpEndPoint instance", e);
		}
		_ssrc = MediaFactory.getRandom().nextInt();
		_dataLength = 8000 * _ptime / 1000;
		_packetBuffer = new ByteArrayBuffer(12 + _dataLength);
        _remoteSocketAddress = new InetSocketAddress(_remoteAddress,
        		_remotePort);
	}

	/**
     * Sends encoded data over the network. All RTP fields will be updated
     * appropriately, i.e. timestamp, sequence number, etc.
     * 
     * @param buffer encoded data to send to the remote RTP party.
     * @throws IOException if underlying socket throws {@link IOException}.
     */
    public void send(Buffer buffer) throws IOException
    {
        RtpPacket rtpPacket = new RtpPacket(_ssrc, _seqNumber,
        		_timestamp, _payloadType, _seqNumber == 0);
        ++_seqNumber;
        _timestamp += buffer.length();
        rtpPacket.setData(buffer);
        send(rtpPacket);
    }

    /**
     * Sends an RtpPacket. This method allows RtpSender user to send customized
     * RtpPackets.
     * 
     * @param rtpPacket the RtpPacket that must be sent.
     * @throws IOException if underlying socket throws {@link IOException}.
     */
    public void send(RtpPacket rtpPacket) throws IOException
    {
        _packetBuffer.clear();
        _rtpParser.encode(_packetBuffer, rtpPacket);
        _udpEndPoint.send(_packetBuffer, _remoteSocketAddress);
    }

    public int getLocalPort()
	{
		return _localPort;
	}

	public InetAddress getLocalAddress()
	{
		return _localAddress;
	}

	public int getRemotePort()
	{
		return _remotePort;
	}

	public InetAddress getRemoteAddress()
	{
		return _remoteAddress;
	}

}
