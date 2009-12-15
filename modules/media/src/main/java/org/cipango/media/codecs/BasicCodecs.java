package org.cipango.media.codecs;


public class BasicCodecs {

    private static final int QUANT_MASK = 0xf;
    private static final int SEG_SHIFT = 4;

    private static final int seg_aend[] = {0x1F, 0x3F, 0x7F, 0xFF,
        0x1FF, 0x3FF, 0x7FF, 0xFFF};

    /**
     * 
     * @param pcm_val 16 bits signed linear sample
     * @return alaw 8 bits
     */
    public static byte linear2alaw(int pcm_val)
    {
        int mask;
        int seg;
        int aval;

        int pcm_new_val = pcm_val >> 3;

        if (pcm_new_val >= 0)
            mask = 0xD5;
        else
        {
            mask = 0x55;
            pcm_new_val = (short)(-pcm_new_val - 1);
        }
        seg = search(pcm_new_val, seg_aend);
        if (seg >= 8)
            return (byte)(0x7F ^ mask);
        else
        {
            aval = seg << SEG_SHIFT;
            if (seg < 2)
                aval |= (pcm_new_val >> 1) & QUANT_MASK;
            else
                aval |= (pcm_new_val >> seg) & QUANT_MASK;
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

}
