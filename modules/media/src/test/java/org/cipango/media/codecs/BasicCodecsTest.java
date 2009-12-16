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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import junit.framework.TestCase;

public class BasicCodecsTest extends TestCase {

    public static final String OUTPUT_DIR = "target/test-classes";

    public static final String S16LE_FILENAME =
        "StandardGreeting.8000.mono.s16le";
    public static final String PCMA_FILENAME = "StandardGreeting.pcma";

    public static final String SEMS_PCMA_FILENAME =
        "StandardGreeting.sems.pcma";
    public static final String SEMS_S16LE_FILENAME =
        "StandardGreeting.sems.8000.mono.s16le";
    

    public static final int BUFFER_SIZE = 256;

    public void assertFileEquals(File file, String filename)
        throws Exception
    {
        if (file == null)
            fail("file is null");
        if (filename == null)
            fail("filename is null");
        URL url = getClass().getClassLoader().getResource(filename);
        File file2 = new File(url.toURI());
        FileInputStream in1 = new FileInputStream(file);
        FileInputStream in2 = new FileInputStream(file2);
        byte[] buf1 = new byte[BUFFER_SIZE];
        byte[] buf2 = new byte[BUFFER_SIZE];
        int readBytes1 = 0;
        int index = 0;
        boolean equals = true;
        while ((readBytes1 = in1.read(buf1)) > 0 && equals)
        {
            int readBytes2 = in2.read(buf2);
            assertEquals(readBytes1, readBytes2);
            int i = 0;
            while (i < readBytes1 && equals)
            {
                equals = (buf1[i] == buf2[i]);
                ++i;
                ++index;
            }
        }
        
        in1.close();
        in2.close();
        assertTrue("diff between " + file.getName() + " and " + filename
                + " at index " + --index, equals);
    }

    public void testLinear2alaw() throws Exception
    {
        URL s16leUrl = getClass().getClassLoader().getResource(S16LE_FILENAME);
        File s16leFile = new File(s16leUrl.toURI());
        File pcmaFile = new File(OUTPUT_DIR + "/StandardGreeting.media.pcma");
        FileInputStream s16leFileInputStream = new FileInputStream(s16leFile);
        FileOutputStream pcmaFileOutputStream = new FileOutputStream(pcmaFile);
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
            pcmaFileOutputStream.write(pcmaBuf);
        }
        s16leFileInputStream.close();
        pcmaFileOutputStream.close();
        assertFileEquals(pcmaFile, SEMS_PCMA_FILENAME);
    }

    public void testAlaw2linear() throws Exception
    {
        URL alawUrl = getClass().getClassLoader().getResource(PCMA_FILENAME);
        File alawFile = new File(alawUrl.toURI());
        FileInputStream alawFileInputStream = new FileInputStream(alawFile);
        File s16leFile = new File(OUTPUT_DIR + "/StandardGreeting.media.s16le");
        FileOutputStream s16leFileOutputStream = new FileOutputStream(s16leFile);
        byte[] buf = new byte[BUFFER_SIZE];
        int readBytes = 0;
        while ((readBytes = alawFileInputStream.read(buf)) > 0)
        {
            byte[] linearBuf = new byte[readBytes * 2];
            for (int i = 0; i < readBytes; ++i)
            {
                int linear = BasicCodecs.alaw2linear(buf[i]);
                linearBuf[2 * i + 1] = (byte)((linear >> 8) & 0xff);
                if (linear < 0)
                    linearBuf[2 * i] &= 0x80;
                linearBuf[2 * i] = (byte)(linear & 0xff);
            }
            s16leFileOutputStream.write(linearBuf);
        }
        alawFileInputStream.close();
        s16leFileOutputStream.close();
        assertFileEquals(s16leFile, SEMS_S16LE_FILENAME);
    }

}
