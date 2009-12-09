package org.cipango.media.rtp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RtpSession {

    public static final int PAYLOAD_TYPE_PCMU = 0;
    public static final int PACKET_SIZE_PCMU = 160;

    private int payloadType;
    private long synchronizationSourceIdentifier;
    private List<Long> contributingSourcesIdentifiers;
    private int sequenceNumber;
    private long timestamp;

    public RtpSession(int payloadType) {
        this.payloadType = payloadType;
        Random random = new Random();
        synchronizationSourceIdentifier = random.nextLong();
        contributingSourcesIdentifiers =
            Collections.synchronizedList(new ArrayList<Long>());
        sequenceNumber = random.nextInt();
        timestamp = random.nextLong();
    }

    public void nextPacket(int packetSize) {
        timestamp += packetSize;
        ++sequenceNumber;
    }

    public int getPayloadType() {
        return payloadType;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public long getSynchronizationSourceIdentifier() {
        return synchronizationSourceIdentifier;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
