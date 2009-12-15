package org.cipango.media.codecs;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import junit.framework.TestCase;

public class BasicCodecsTest extends TestCase {

    
    public static final String S16LE_FILENAME =
        "StandardGreeting.8000.mono.s16le";
    public static final String SEMS_PCMA_FILENAME =
        "StandardGreeting.sems.pcma";

    public static final int BUFFER_SIZE = 256;


    public void testLinear2alaw() throws Exception
    {
        URL s16leUrl = getClass().getClassLoader().getResource(S16LE_FILENAME);
        File s16leFile = new File(s16leUrl.toURI());
        URL semsPcmaUrl = getClass().getClassLoader().getResource(
                SEMS_PCMA_FILENAME);
        File semsPcmaFile = new File(semsPcmaUrl.toURI());
        FileInputStream s16leFileInputStream = new FileInputStream(s16leFile);
        FileInputStream semsPcmaInputStream = new FileInputStream(semsPcmaFile);
        byte[] buf = new byte[BUFFER_SIZE];
        int readBytes = 0;
        while ((readBytes = s16leFileInputStream.read(buf)) > 0)
        {
            byte[] pcmaBuf = new byte[readBytes / 2];
            for (int i = 0; i < readBytes; i += 2)
            {
                int pcm_val = (buf[i + 1] << 8) | (buf[i] & 0xff);
                pcmaBuf[i / 2] = BasicCodecs.linear2alaw(pcm_val);
            }
            byte[] semsBuf = new byte[readBytes / 2];
            if (semsPcmaInputStream.read(semsBuf) != semsBuf.length)
            {
                fail();
                return;
            }
            boolean similar = true;
            for (int i = 0; i < semsBuf.length; ++i)
                if (pcmaBuf[i] != semsBuf[i])
                {
                    similar = false;
                    break;
                }
            assertTrue(similar);
        }
        s16leFileInputStream.close();
        semsPcmaInputStream.close();
    }

}
