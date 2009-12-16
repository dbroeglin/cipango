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

package org.cipango.media.codecs;

public class BasicCodecs {

    /* Sign bit for a A-law byte. */
    private static final int SIGN_BIT = 0x80;

    /* Quantization field mask. */
    private static final int QUANT_MASK = 0xf;

    /* Left shift for segment number. */
    private static final int SEG_SHIFT = 4;

    /* Segment field mask. */
    private static final int SEG_MASK = 0x70;

    private static final int seg_aend[] = {0x1F, 0x3F, 0x7F, 0xFF,
        0x1FF, 0x3FF, 0x7FF, 0xFFF};

    /**
     * Convert a pcm sample to a alaw value.
     * 
     * @param pcm_val 16 bits signed linear sample
     * @return alaw 8 bits
     */
    public static byte linear2alaw(int pcm_val)
    {
        pcm_val = pcm_val >> 3;

        int mask;
        if (pcm_val >= 0)
            mask = 0xD5;
        else
        {
            mask = 0x55;
            pcm_val = (short)(-pcm_val - 1);
        }
        int seg = search(pcm_val, seg_aend);
        if (seg >= 8)
            return (byte)(0x7F ^ mask);
        else
        {
            int aval = seg << SEG_SHIFT;
            if (seg < 2)
                aval |= (pcm_val >> 1) & QUANT_MASK;
            else
                aval |= (pcm_val >> seg) & QUANT_MASK;
            return (byte)(aval ^ mask);
        }
    }

    private static int search(int val, int[] table)
    {
        for (int i = 0; i < table.length; ++i)
            if (val <= table[i])
                return i;
        return table.length;
    }

    /**
     * Convert a alaw value to a pcm sample.
     * 
     * @param a_val alaw value
     * @return 16 bits signed linear sample
     */
    public static int alaw2linear(byte a_val)
    {
        a_val ^= 0x55;
        int t = (a_val & QUANT_MASK) << 4;
        int seg = ((a_val & 0xff) & SEG_MASK) >> SEG_SHIFT;
        switch (seg) {
        case 0:
            t += 8;
            break;
        case 1:
            t += 0x108;
            break;
        default:
            t += 0x108;
            t <<= seg - 1;
        }
        return ((a_val & SIGN_BIT) != 0) ? t : -t;
    }

}
