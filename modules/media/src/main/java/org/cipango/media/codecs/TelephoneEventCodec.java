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

package org.cipango.media.codecs;

import org.mortbay.io.Buffer;

public class TelephoneEventCodec
{

    public static final byte[] CODES = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15 };
    public static final char[] EVENTS = { '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', '*', '#', 'A', 'B', 'C', 'D' };

    /*
        0                   1                   2                   3
        0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
       |     event     |E|R| volume    |          duration             |
       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    
                     Figure 1: Payload Format for Named Events
     */
    // TODO E, R, volume, duration
    public char decode(Buffer buffer)
    {
        byte event = buffer.get();
        if (event > -1 && event < 16)
            return EVENTS[event];
        return (char)event;
    }

    /**
     * Encodes a telephone event
     * 
     * @param buffer    the buffer in which the packet is encoded
     * @param event     the event to encode
     */
    public void encode(Buffer buffer, char event, boolean end)
    {
        // event
        if (event >= '0' && event <= '9')
            buffer.put((byte)(event - '0'));
        else
            switch (event) {
            case '*':
                buffer.put((byte)10);
                break;
            case '#':
                buffer.put((byte)11);
                break;
            case 'A':
                buffer.put((byte)12);
                break;
            case 'B':
                buffer.put((byte)13);
                break;
            case 'C':
                buffer.put((byte)14);
                break;
            case 'D':
                buffer.put((byte)15);
                break;

            default:
                buffer.put((byte)event);
                break;
            }

        // TODO E, R, volume, duration

        // reserved = 0
        // volume = 10
        buffer.put(end ? (byte)-118 : 10);
        // duration = 160
        buffer.put((byte)0);
        buffer.put((byte)-96);
    }
}
