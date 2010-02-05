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

/**
 * An RtpListener is invoked by {@link RtpReader} when an RTP packet is
 * received.
 * 
 * It notifies its implementing classes that a new RTP packet has been
 * received. Thus, it provides a reference to the corresponding
 * {@link RtpPacket}.
 * 
 * @author yohann
 */
public interface RtpListener
{

	/**
	 * Notifies the arrival of an incoming RTP packet. mediaBuffer is the
	 * content of the RTP packet: its corresponding data field in RTP packet.
	 * 
	 * @param mediaBuffer encoded media received in RTP packet. In mediaBuffer,
	 * RTP payload has not been decoded yet.
	 */
    public void receivedRtpPacket(RtpPacket rtpPacket);

}
