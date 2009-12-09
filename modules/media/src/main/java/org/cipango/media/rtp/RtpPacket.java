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

package org.cipango.media.rtp;

import org.mortbay.io.Buffer;

public class RtpPacket
{
	private int _ssrc;
	private int[] _csrc;
	
	private int _sequenceNumber;
	private long _timestamp;
	private int _payloadType;
	
	private Buffer _data;
	
	public RtpPacket(int ssrc, int sequenceNumber, long timestamp)
	{
		_ssrc = ssrc;
		_sequenceNumber = sequenceNumber;
		_timestamp = timestamp;
	}
	
	public int getSsrc()
	{
		return _ssrc;
	}
	
	public int getSequenceNumber()
	{
		return _sequenceNumber;
	}
	
	public long getTimestamp()
	{
		return _timestamp;
	}
	
	public int getPayloadType()
	{
		return _payloadType;
	}
	
	public void setData(Buffer data)
	{
		_data = data;
	}
	
	public Buffer getData()
	{
		return _data;
	}
	
	public String toString()
	{
		return "[ssrc=" + _ssrc + ",seq=" + _sequenceNumber + ",ts=" + _timestamp + "]";
	}
}
