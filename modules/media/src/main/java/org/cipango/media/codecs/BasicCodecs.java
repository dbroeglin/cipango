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

    private static final int seg_uend[] = {0x3F, 0x7F, 0xFF, 0x1FF,
        0x3FF, 0x7FF, 0xFFF, 0x1FFF};

    /* Bias for linear code. */
    private static final int BIAS = 0x84;
    private static final int CLIP = 8159;

    private static int search(int val, int[] table)
    {
        for (int i = 0; i < table.length; ++i)
            if (val <= table[i])
                return i;
        return table.length;
    }

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

    /**
     * Convert a pcm sample to a ulaw value.
     * 
     * @param pcm_val 16 bits signed linear sample
     * @return ulaw 8 bits
     */
    public static byte linear2ulaw(int pcm_val)
    {

        /* Get the sign and the magnitude of the value. */
        pcm_val = pcm_val >> 2;
        int mask;
        if (pcm_val < 0)
        {
            pcm_val = -pcm_val;
            mask = 0x7F;
        }
        else
            mask = 0xFF;
        if (pcm_val > CLIP)
            pcm_val = CLIP;       /* clip the magnitude */
        pcm_val += (BIAS >> 2);

        /* Convert the scaled magnitude to segment number. */
        int seg = search(pcm_val, seg_uend);

        /*
         * Combine the sign, segment, quantization bits;
         * and complement the code word.
         */
        if (seg >= 8)       /* out of range, return maximum value. */
            return (byte)(0x7F ^ mask);
        int uval = (byte) (seg << 4) | ((pcm_val >> (seg + 1)) & 0xF);
        return (byte)(uval ^ mask);
    }

    /**
     * Convert a ulaw value to a pcm sample.
     * 
     * @param u_val ulaw value
     * @return 16 bits signed linear sample
     */
    public static int ulaw2linear(byte u_val)
    {
        /* Complement to obtain normal u-law value. */
        u_val = (byte)~(u_val & 0xff);

        /*
         * Extract and bias the quantization bits. Then
         * shift up by the segment number and subtract out the bias.
         */
        int t = ((u_val & QUANT_MASK) << 3) + BIAS;
        t <<= ((u_val & 0xff) & SEG_MASK) >> SEG_SHIFT;

        return ((u_val & SIGN_BIT) != 0 ? (BIAS - t) : (t - BIAS));
    }

}
