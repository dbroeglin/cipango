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

    /* transcoding tables, from G.711 CCITT specification */
    private static byte A2U[] = {         /* A- to u-law conversions */
            1,  3,  5,  7,  9,  11, 13, 15,
            16, 17, 18, 19, 20, 21, 22, 23,
            24, 25, 26, 27, 28, 29, 30, 31,
            32, 32, 33, 33, 34, 34, 35, 35,
            36, 37, 38, 39, 40, 41, 42, 43,
            44, 45, 46, 47, 48, 48, 49, 49,
            50, 51, 52, 53, 54, 55, 56, 57,
            58, 59, 60, 61, 62, 63, 64, 64,
            65, 66, 67, 68, 69, 70, 71, 72,
        /* corrected:
            73, 74, 75, 76, 77, 78, 79, 79,
           should be: */
            73, 74, 75, 76, 77, 78, 79, 80,

            80, 81, 82, 83, 84, 85, 86, 87,
            88, 89, 90, 91, 92, 93, 94, 95,
            96, 97, 98, 99, 100,    101,    102,    103,
            104,    105,    106,    107,    108,    109,    110,    111,
            112,    113,    114,    115,    116,    117,    118,    119,
            120,    121,    122,    123,    124,    125,    126,    127};

    private static byte U2A[] = {         /* u- to A-law conversions */
            1,  1,  2,  2,  3,  3,  4,  4,
            5,  5,  6,  6,  7,  7,  8,  8,
            9,  10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24,
            25, 27, 29, 31, 33, 34, 35, 36,
            37, 38, 39, 40, 41, 42, 43, 44,
            46, 48, 49, 50, 51, 52, 53, 54,
            55, 56, 57, 58, 59, 60, 61, 62,
            64, 65, 66, 67, 68, 69, 70, 71,
            72, 73, 74, 75, 76, 77, 78, 79,
        /* corrected:
            81, 82, 83, 84, 85, 86, 87, 88, 
           should be: */
            80, 82, 83, 84, 85, 86, 87, 88,
            89, 90, 91, 92, 93, 94, 95, 96,
            97, 98, 99, 100,    101,    102,    103,    104,
            105,    106,    107,    108,    109,    110,    111,    112,
            113,    114,    115,    116,    117,    118,    119,    120,
            121,    122,    123,    124,    125,    126,    127,   -128};

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

    public static byte alaw2ulaw(byte aval)
    {
        int tmp = aval & 0xff;
        return (byte) (((tmp & 0x80) != 0 ? (0xFF ^ A2U[tmp ^ 0xD5]) :
          (0x7F ^ A2U[tmp ^ 0x55])) & 0xff);
    }

    public static byte ulaw2alaw(byte uval)
    {
        int tmp = uval & 0xff;
        return (byte) (((tmp & 0x80) != 0 ?
                (0xD5 ^ (U2A[0xFF ^ tmp] - 1)) :
            (byte) ((0x55 ^ (U2A[0x7F ^ tmp] - 1)) & 0xff)) & 0xff);
    }

}
