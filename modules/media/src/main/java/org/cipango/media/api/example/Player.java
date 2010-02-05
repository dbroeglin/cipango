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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.cipango.media.api.FileReader;
import org.cipango.media.api.MediaFactory;
import org.cipango.media.api.NoObjectAvailableException;
import org.cipango.media.api.RtpSender;
import org.mortbay.io.Buffer;
import org.mortbay.log.Log;

public class Player implements Runnable
{

	public static final int DELAY = 10; // ms
	public static final int PERIOD = 20; // ms

	// input file must be encoded as PCMU
	public static final int PAYLOAD = 0;
	
    private String _filename;
    private InetAddress _destAddress;
    private int _destPort;

    private FileReader _fileReader;
    private RtpSender _rtpSender;

    public Player(String filename, InetAddress destAddress, int destPort)
    {
        _filename = filename;
        _destAddress = destAddress;
        _destPort = destPort;
    }

    public void init() throws NoObjectAvailableException
    {
        _fileReader = MediaFactory.getFileReader(_filename);
        _rtpSender = MediaFactory.getRtpSender(PAYLOAD, _destAddress,
        		_destPort);
    }

    public void start() throws NoObjectAvailableException
    {
        ScheduledExecutorService scheduledExecutorService =
        	MediaFactory.getScheduleExecutorService();
        scheduledExecutorService.scheduleAtFixedRate(this, DELAY, PERIOD,
        		TimeUnit.MILLISECONDS);
    }

    @Override
    public void run()
    {
    	Buffer buffer = _fileReader.read();
    	try
		{
    		_rtpSender.send(buffer);
		}
		catch (IOException e)
		{
			Log.warn("cannot send packet", e);
		}
    }

}
