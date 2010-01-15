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

import junit.framework.TestCase;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

public class TelephoneEventCodecTest extends TestCase
{

    public static final byte[] CODES = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15 };
    public static final char[] EVENTS = { '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', '*', '#', 'A', 'B', 'C', 'D' };

    public void testDecode()
    {
        Buffer buffer = new ByteArrayBuffer(CODES.length);
        TelephoneEventCodec telephoneEventCodec =
            new TelephoneEventCodec();
        buffer.put(CODES);
        for (int i = 0; i < CODES.length; ++i)
            assertEquals(EVENTS[i], telephoneEventCodec.decode(buffer));
    }

    public void testEncode()
    {
        TelephoneEventCodec telephoneEventCodec =
            new TelephoneEventCodec();
        for (int i = 0; i < 16; ++i)
        {
            Buffer buffer = new ByteArrayBuffer(4);
            telephoneEventCodec.encode(buffer, EVENTS[i], true);
            assertEquals(CODES[i], buffer.get());
            assertEquals(-118, buffer.get());
            assertEquals(0, buffer.get());
            assertEquals(-96, buffer.get());
        }
    }

}
