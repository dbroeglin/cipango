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

import org.mortbay.io.Buffer;

/**
 * Encodes and decodes RTP packets to/from Buffer.
 * <pre>
 *   0                   1                   2                   3
 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |V=2|P|X|  CC   |M|     PT      |       sequence number         |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |                           timestamp                           |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  |           synchronization source (SSRC) identifier            |
 *  +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
 *  |            contributing source (CSRC) identifiers             |
 *  |                             ....                              |
 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  </pre>
 */
public class RtpParser 
{
	public static final int PADDING_FLAG = 0x20;
	public static final int EXTENSION_FLAG = 0x10;
	public static final int MARKER_FLAG = 0x80;
	
	public static final byte DEFAULT_FIRST_BYTE = (byte) 0x80;
	
	/**
	 * Encodes an RTP packet
	 * 
	 * @param buffer	the buffer in which the packet is encoded
	 * @param packet	the packet to encode
	 */
    public void encode(Buffer buffer, RtpPacket packet)
    {
    	// assume CC=0 
    	buffer.put(DEFAULT_FIRST_BYTE);
    	
    	if (packet.getMarker())
    		buffer.put((byte) (packet.getPayloadType() | MARKER_FLAG));
    	else
    		buffer.put((byte) (packet.getPayloadType() & 0x7f));
    	
    	int seqNumber = packet.getSequenceNumber();
    	
    	buffer.put((byte) (seqNumber >> 8 & 0xff));
        buffer.put((byte) (seqNumber & 0xff));
    	
        putInt(buffer, (int) packet.getTimestamp());
        putInt(buffer, packet.getSsrc());
        
        buffer.put(packet.getData());
    }
	
    /**
     * Decodes an RTP packet
     * 
     * @param buffer	the buffer from which the packet is decodec
     * @return the decoded RTP packet
     */
	public RtpPacket decode(Buffer buffer)
	{
		byte b = buffer.get();
		
		boolean padding = ((b & PADDING_FLAG) == PADDING_FLAG);
		boolean extension = ((b & EXTENSION_FLAG) == EXTENSION_FLAG);
		
		int csrcCount = (b & 0x0f);
		
		b = buffer.get();
		
		boolean marker = ((b & MARKER_FLAG) == MARKER_FLAG);
		int payloadType = (b & 0x7f);
		
		int sequenceNumber = (buffer.get() & 0xff) << 8 | (buffer.get() & 0xff);
		long timestamp =  (getInt(buffer) & 0xffffffffl);
		
		int ssrc =  getInt(buffer);
		
		if (csrcCount > 0)
		{
			int[] csrc = new int[csrcCount];
			for (int i = 0; i < csrcCount; i++)
				csrc[i] = getInt(buffer);
		}
		
		if (extension)
		{
			int length = (int) (getInt(buffer) & 0xffff);
			for (int i = 0; i < length; i++)
				getInt(buffer); // TODO extension
		}
		
		Buffer data = buffer.slice();
		if (padding)
		{
			int nb = (data.peek(data.putIndex() - 1) & 0xff);
			data.setPutIndex(data.putIndex() - nb);
		}
		
		RtpPacket packet = new RtpPacket(ssrc, sequenceNumber, timestamp,
		        payloadType, marker);
		
		packet.setData(data);
		return packet;
	}	
	
	public int getInt(Buffer buffer)
	{
		return  (buffer.get() & 0xff) << 24 
		| (buffer.get() & 0xff) << 16 
		| (buffer.get() & 0xff) << 8
		| (buffer.get() & 0xff);
	}
	
	public void putInt(Buffer buffer, int value)
    {
        buffer.put((byte) (value >> 24 & 0xff));
        buffer.put((byte) (value >> 16 & 0xff));
        buffer.put((byte) (value >> 8 & 0xff));
        buffer.put((byte) (value & 0xff));
    }
}
