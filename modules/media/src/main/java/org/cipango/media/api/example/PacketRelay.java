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

package org.cipango.media.api.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.cipango.media.api.LifeCycle;
import org.cipango.media.api.MediaFactory;
import org.cipango.media.api.NoObjectAvailableException;
import org.cipango.media.api.RtpListener;
import org.cipango.media.api.RtpPacket;
import org.cipango.media.api.RtpReader;
import org.cipango.media.api.RtpSender;
import org.mortbay.log.Log;

public class PacketRelay implements RtpListener, LifeCycle
{

	public static final int PAYLOAD_TYPE = 0;

    private int _sourcePort;
    private InetAddress _destAddress;
    private int _destPort;

    private RtpReader _rtpReader;
    private RtpSender _rtpSender;

    public PacketRelay(int sourcePort, InetAddress destAddress, int destPort)
    {
        _sourcePort = sourcePort;
        _destAddress = destAddress;
        _destPort = destPort;
    }

    public void init() throws NoObjectAvailableException
    {
        _rtpReader = MediaFactory.getRtpReader(_sourcePort);
        _rtpReader.addRtpListener(this);
        // here payload type won't be used as we send rtp packets directly
        // but it has to be provided to media factory.
        _rtpSender = MediaFactory.getRtpSender(PAYLOAD_TYPE, _destAddress,
        		_destPort);
    }

    @Override
    public void start()
    {
        _rtpReader.start();
    }

    @Override
    public void stop()
    {
        _rtpReader.stop();
    }

    @Override
    public void receivedRtpPacket(RtpPacket rtpPacket)
    {
    	try
		{
            _rtpSender.send(rtpPacket.getData());
		}
		catch (IOException e)
		{
			Log.warn("cannot send packet", e);
		}
    }

    public static void main(String[] args) {
        if (args.length != 3)
        {
            System.err.println(
                    "usage: java source_port dest_address dest_port");
            System.exit(1);
        }
        int sourcePort = Integer.parseInt(args[0]);
        InetAddress destAddress;
        try {
            destAddress = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        int destPort = Integer.parseInt(args[2]);
        PacketRelay packetRelay = new PacketRelay(sourcePort, destAddress,
                destPort);
        try
		{
			packetRelay.init();
		}
		catch (NoObjectAvailableException e)
		{
			e.printStackTrace();
			return;
		}
        packetRelay.start();
        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            packetRelay.stop();
        }
    }

}
