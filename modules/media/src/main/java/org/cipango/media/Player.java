package org.cipango.media;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.cipango.media.codecs.Encoder;
import org.cipango.media.codecs.PcmuEncoder;
import org.cipango.media.rtp.RtpEncoder;
import org.cipango.media.rtp.RtpPacket;
import org.cipango.media.rtp.RtpSession;
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
    private RtpEncoder rtpEncoder;
    private RtpSession rtpSession;
    private AudioInputStream audioInputStream;
    private byte[] byteBuffer;
    private Buffer buffer;
    private Encoder encoder;
    private RtpPacket rtpPacket;
    private Timer timer;

    public Player(String filename, String ipAddress, int port) {
        file = new File(filename);
        AudioFileFormat audioFileFormat;
        try {
            audioFileFormat = AudioSystem.getAudioFileFormat(file);
        } catch (UnsupportedAudioFileException e) {
            Log.warn("UnsupportedAudioFileException " + filename, e);
            return;
        } catch (IOException e) {
            Log.warn("IOException", e);
            return;
        }
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
        rtpSession = new RtpSession(RtpSession.PAYLOAD_TYPE_PCMU);
        rtpEncoder = new RtpEncoder();
        byteBuffer = new byte[BUFFER_SIZE];
        buffer = new ByteArrayBuffer(BUFFER_SIZE);
        encoder = new PcmuEncoder();
        rtpPacket = new RtpPacket();
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
                return;
            }
            if (bytesRead > 0) {
                buffer.clear();
                buffer.put(byteBuffer, 0, bytesRead);
                encoder.encode(buffer);
                rtpPacket.setData(buffer);
                Buffer rtpBuffer = rtpEncoder.encode(rtpPacket, rtpSession);
                try {
                    udpEndPoint.send(rtpBuffer, socketAddress);
                } catch (IOException e) {
                    Log.warn("IOException", e);
                    return;
                }
            } else {
                try {
                    audioInputStream.close();
                } catch (IOException e) {
                    Log.warn("IOException", e);
                    return;
                }
                timer.cancel();
            }
        }
    }

    public static void main(String[] args) {
        Player player = new Player("D:/workspace/cipango-googlecode/modules/" +
        		"media/src/test/resources/test.wav", "192.168.2.1", 8000);
        player.play();
        Log.debug("ok");
    }

}
