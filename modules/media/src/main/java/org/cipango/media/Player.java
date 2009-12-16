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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.cipango.media.rtp.RtpCodec;
import org.cipango.media.rtp.RtpPacket;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;

/**
 * Play an audio file in an RTP stream. 
 * 
 * input: mu-law wav file
 * output: rtp stream towards specified destination
 */
public class Player 
{
	public static final int DEFAULT_PTIME = 20;
	
	private String _filename;
	private String _host;
	private int _port;
	
	private SocketAddress _remoteAddress;
    private UdpEndPoint _udpEndPoint;
    
    private AudioInputStream _audioInputStream;
    private int _ptime = DEFAULT_PTIME;
    
    private int _ssrc;
    private int _seqNumber;
    private long _timestamp;
    
    private int _dataLength;
    
    private Buffer _audioBuffer;
    private Buffer _packetBuffer;
    
    public static final int BUFFER_SIZE = 160; // uncompressed bytes
    public static final int PERIOD = 20; // ms
    public static final int DELAY = 10; // ms

    
    private RtpCodec rtpCodec;

    private Timer timer = new java.util.Timer();

    public Player(String filename, String host, int port) 
    {
       _filename = filename;
       _host = host;
       _port = port;
    }
    
    public void start() throws Exception
    {
    	File file = new File(_filename);
        
        _audioInputStream = AudioSystem.getAudioInputStream(file);
        
        Log.info("Playing audio: " + file.getName() + " with format: " + _audioInputStream.getFormat());
        
        _remoteAddress = new InetSocketAddress(InetAddress.getByName(_host), _port);
        _udpEndPoint = new UdpEndPoint(new DatagramSocket());
        
        Random random = new Random();
        _ssrc = random.nextInt();
        
        _dataLength = 8000 * _ptime / 1000;
        
        _audioBuffer = new ByteArrayBuffer(_dataLength);
        _packetBuffer = new ByteArrayBuffer(12 + _dataLength);
        
        rtpCodec = new RtpCodec();    
    }

    public void play() 
    {
        timer.scheduleAtFixedRate(new PlayTimerTask(), DELAY, PERIOD);
    }

    class PlayTimerTask extends TimerTask 
    {
        public void run() 
        {
            int bytesRead;
            try 
            {
                bytesRead = _audioInputStream.read(_audioBuffer.array());
            } 
            catch (IOException e) 
            {
                Log.warn("IOException", e);
                stop();
                return;
            }
            if (bytesRead > 0) 
            {
            	_audioBuffer.setGetIndex(0); _audioBuffer.setPutIndex(bytesRead);
            	
            	RtpPacket packet = new RtpPacket(_ssrc, _seqNumber, _timestamp, RtpPacket.PAYLOAD_TYPE_PCMA);
            	
            	_seqNumber++;
            	_timestamp += bytesRead;
            	
            	packet.setData(_audioBuffer);
            	
            	_packetBuffer.clear();
            	rtpCodec.encode(_packetBuffer, packet);
            	
                try 
                {
                    _udpEndPoint.send(_packetBuffer, _remoteAddress);
                } 
                catch (IOException e) 
                {
                    Log.warn("IOException", e);
                    stop();
                    return;
                }
                
            } else {
                stop();
            }
        }
    }

    public void stop() {
        timer.cancel();
        try {
            _audioInputStream.close();
        } catch (IOException e) {
            Log.warn("IOException", e);
        }
        try {
            _udpEndPoint.close();
        } catch (IOException e) {
            Log.warn("IOException", e);
        }
    }

    public static void main(String[] args) throws Exception
    {
    	if (args.length == 0)
    	{
    		System.err.println("Usage: java org.cipango.media.Player audio_file");
    		System.exit(-1);
    	}
    	
        Player player = new Player(args[0], "127.0.0.1", 6000);
        player.start();
    }

}
