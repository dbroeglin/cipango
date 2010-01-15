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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import junit.framework.TestCase;

import org.cipango.media.codecs.TelephoneEventCodec;
import org.cipango.media.rtp.RtpCodec;
import org.cipango.media.rtp.RtpPacket;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

public class DtmfHandlerTest extends TestCase
{

    public void testDtmfHandler() throws Exception
    {
        final DtmfHandler dtmfHandler = new DtmfHandler(101);

        dtmfHandler.init();
        DtmfTestListener dtmfTestListener = new DtmfTestListener();
        dtmfHandler.addDtmfListener(dtmfTestListener);
        
        char c = '0';

        RtpCodec rtpCodec = new RtpCodec();
        // first packet with marker
        RtpPacket rtpPacket = new RtpPacket(0, 0, 0, 101, true);
        Buffer rtpPacketBuffer = new ByteArrayBuffer(16);
        Buffer eventBuffer = new ByteArrayBuffer(4);
        TelephoneEventCodec telephoneEventCodec =
            new TelephoneEventCodec();
        // first packet without end bit
        telephoneEventCodec.encode(eventBuffer, c, false);
        rtpPacket.setData(eventBuffer);
        rtpCodec.encode(rtpPacketBuffer, rtpPacket);
        int port = dtmfHandler.getPort();
        DatagramSocket datagramSocket = new DatagramSocket();
        UdpEndPoint udpEndPoint = new UdpEndPoint(datagramSocket);
        SocketAddress socketAddress = new InetSocketAddress(
                InetAddress.getLocalHost(), port);
        Thread thread = new Thread(dtmfHandler);
        thread.start();
        udpEndPoint.send(rtpPacketBuffer, socketAddress);


        // three end bit packets (without marker)
        rtpPacket.setMarker(false);
        eventBuffer.clear();
        rtpPacketBuffer.clear();
        telephoneEventCodec.encode(eventBuffer, c, true);
        rtpCodec.encode(rtpPacketBuffer, rtpPacket);
        for (int i = 0; i < 3; ++i)
            udpEndPoint.send(rtpPacketBuffer, socketAddress);
        int maxCount = 10;
        int count = 0;
        while (dtmfTestListener.getEvent() == 0 && count < maxCount)
        {
            Thread.sleep(50);
            ++count;
        }
        if (count == maxCount)
            fail("timeout");
        assertEquals('0', dtmfTestListener.getEvent());
        assertEquals(1, dtmfTestListener.getInvocations());
    }

    class DtmfTestListener implements DtmfListener
    {

        private char event = 0;
        private int invocations = 0;

        @Override
        public void telephoneEvent(char event)
        {
            this.event = event;
            ++invocations;
        }

        public char getEvent()
        {
            return event;
        }

        public int getInvocations()
        {
            return invocations;
        }

    }
}
