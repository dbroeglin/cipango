package org.cipango.media.rtp;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;


public class RtpEncoder {

    public void putLong(long value, Buffer buffer){
        buffer.put((byte)(value >> 24 & 0xff));
        buffer.put((byte)(value >> 16 & 0xff));
        buffer.put((byte)(value >> 8 & 0xff));
        buffer.put((byte)(value & 0xff));
    }

    public Buffer encode(RtpPacket rtpPacket, RtpSession rtpSession) {
        Buffer data = rtpPacket.getData();
        data.setGetIndex(0);
        // TODO csrc count
        Buffer buffer = new ByteArrayBuffer(data.length() + 12);
        // V = 2, P = 0, X = 0, CC = 0
        buffer.put((byte)-128);
        // M = 0, PT
        buffer.put((byte)(rtpSession.getPayloadType() & 0xff));
        int sequenceNumber = rtpSession.getSequenceNumber();
        buffer.put((byte)(sequenceNumber >> 8 & 0xff));
        buffer.put((byte)(sequenceNumber & 0xff));
        long timestamp = rtpSession.getTimestamp();
        putLong(timestamp, buffer);
        long synchronizationSourceIdentifier =
            rtpSession.getSynchronizationSourceIdentifier();
        putLong(synchronizationSourceIdentifier, buffer);
        // TODO csrc
        buffer.put(data);
        return buffer;
    }

}
