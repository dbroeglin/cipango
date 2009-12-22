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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;

public class Recorder
{

    public static final int RTP_PACKET_SIZE = 172;
    private static final int DEFAULT_PORT = -1;

    private int _port = DEFAULT_PORT;
    private UdpEndPoint _udpEndPoint;
    private FileOutputStream _fileOutputStream;
    private boolean _running;
    private ByteArrayBuffer _buffer;

    public Recorder()
    {
        this(DEFAULT_PORT);
    }

    public Recorder(int port)
    {
        _port = port;
        _running = false;
    }

    public void init()
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss,SSS");
        String filename = dateFormat.format(new Date()) + " " + _port
            + " 8000.mono.g711";
        try
        {
            _fileOutputStream = new FileOutputStream(filename);
        }
        catch (FileNotFoundException e)
        {
            Log.warn("FileNotFoundException", e);
            return;
        }
        _running = true;
        _buffer = new ByteArrayBuffer(RTP_PACKET_SIZE);
    }

    public void record()
    {
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
            
            if (_buffer.length() < 12)
                continue; // not an rtp packet
            _buffer.get(12);
            
            if (_buffer.hasContent())
            {
                byte[] buf = new byte[160];
                int read = _buffer.get(buf, 0, buf.length);
                try
                {
                    _fileOutputStream.write(buf, 0, read);
                }
                catch (IOException e)
                {
                    Log.warn("IOException", e);
                    break;
                }
                _buffer.clear();
            }
        }
        try
        {
            _fileOutputStream.close();
        }
        catch (IOException e)
        {
            Log.warn("IOException");
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
        } catch (IOException e) {
            Log.ignore(e);
        }
    }

    public int getPort() {
        return _port;
    }

    public static void main(String[] args)
    {
        final Recorder recorder = new Recorder(6000);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                recorder.stop();
                timer.cancel();
            }
        }, 10000);
        recorder.init();
        recorder.record();
    }

}
