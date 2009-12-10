// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.cipango.media.codecs.Encoder;
import org.cipango.media.codecs.PcmuEncoder;
import org.cipango.media.rtp.RtpCodec;
import org.cipango.media.rtp.RtpPacket;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;

/**
 * input: pcm wave file, mono-channel, 8000 Hz, 16 bits samples, signed,
 *     big endian
 * output: rtp stream towards specified destination
 * 
 * @author yohann
 *
 */
public class Player {

    public static final int BUFFER_SIZE = 320; // uncompressed bytes
    public static final int PERIOD = 20; // ms
    public static final int DELAY = 10; // ms

    private File file;
    private SocketAddress socketAddress;
    private UdpEndPoint udpEndPoint;
    private RtpCodec rtpCodec;
    private AudioInputStream audioInputStream;
    private byte[] byteBuffer;
    private Buffer buffer;
    private Encoder encoder;
    private RtpPacket rtpPacket;
    private Timer timer;

    public Player(String filename, String ipAddress, int port) {
        file = new File(filename);
       
        // TODO check format is supported
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException e) {
            Log.warn("UnsupportedAudioFileException", e);
            return;
        } catch (IOException e) {
            Log.warn("IOException", e);
            return;
        }
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            Log.warn("unknown host: " + ipAddress, e);
            return;
        }
        socketAddress = new InetSocketAddress(inetAddress, port);
        DatagramSocket datagramSocket;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            Log.warn("SocketException", e);
            return;
        }
        udpEndPoint = new UdpEndPoint(datagramSocket);
        rtpCodec = new RtpCodec();
        byteBuffer = new byte[BUFFER_SIZE];
        buffer = new ByteArrayBuffer(BUFFER_SIZE);
        encoder = new PcmuEncoder();
        Random random = new Random();
        rtpPacket = new RtpPacket(random.nextInt(), random.nextInt(),
                random.nextInt() & 0xffffffffl, RtpPacket.PAYLOAD_TYPE_PCMU);
        timer = new Timer();
    }

    public void play() {
        timer.scheduleAtFixedRate(new PlayTimerTask(), DELAY, PERIOD);
    }

    class PlayTimerTask extends TimerTask {
        @Override
        public void run() {
            int bytesRead;
            try {
                bytesRead = audioInputStream.read(byteBuffer);
            } catch (IOException e) {
                Log.warn("IOException", e);
                stop();
                return;
            }
            if (bytesRead > 0) {
                buffer.clear();
                buffer.put(byteBuffer, 0, bytesRead);
                encoder.encode(buffer);
                rtpPacket.setData(buffer);
                Buffer rtpBuffer = new ByteArrayBuffer(buffer.length() + 12);
                rtpCodec.encode(rtpBuffer, rtpPacket);
                try {
                    udpEndPoint.send(rtpBuffer, socketAddress);
                } catch (IOException e) {
                    Log.warn("IOException", e);
                    stop();
                    return;
                }
                int sequenceNumber = rtpPacket.getSequenceNumber() + 1;
                long timestamp = rtpPacket.getTimestamp() + buffer.length();
                rtpPacket.setSequenceNumber(sequenceNumber);
                rtpPacket.setTimestamp(timestamp);
            } else {
                stop();
            }
        }
    }

    public void stop() {
        timer.cancel();
        try {
            audioInputStream.close();
        } catch (IOException e) {
            Log.warn("IOException", e);
        }
        try {
            udpEndPoint.close();
        } catch (IOException e) {
            Log.warn("IOException", e);
        }
    }

    public static void main(String[] args) {
        Player player = new Player("D:/workspace/cipango-googlecode/modules/" +
        		"media/src/test/resources/test.wav", "127.0.0.1", 6000);
        player.play();
    }

}
