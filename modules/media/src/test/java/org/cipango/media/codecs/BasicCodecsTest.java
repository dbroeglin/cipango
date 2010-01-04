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

import static org.cipango.media.MediaTestConstants.OUTPUT_DIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import junit.framework.TestCase;

public class BasicCodecsTest extends TestCase {

    public static final String IN_S16LE = "StandardGreeting.8000.mono.s16le";
    public static final String OUT_S16LE_PCMA =
        "StandardGreeting.8000.mono.s16le.pcma.test";
    public static final String IN_S16LE_PCMA =
        "StandardGreeting.8000.mono.s16le.pcma";
    public static final String OUT_S16LE_PCMU =
        "StandardGreeting.8000.mono.s16le.pcmu.test";
    public static final String IN_S16LE_PCMU =
        "StandardGreeting.8000.mono.s16le.pcmu";

    public static final String IN_PCMA = "StandardGreeting.8000.mono.pcma";
    public static final String OUT_PCMA_S16LE =
        "StandardGreeting.8000.mono.pcma.s16le.test";
    public static final String IN_PCMA_S16LE =
        "StandardGreeting.8000.mono.pcma.s16le";
    public static final String OUT_PCMA_PCMU =
        "StandardGreeting.8000.mono.pcma.pcmu.test";
    public static final String IN_PCMA_PCMU =
        "StandardGreeting.8000.mono.pcma.pcmu";

    public static final String IN_PCMU = "StandardGreeting.8000.mono.pcmu";
    public static final String OUT_PCMU_S16LE =
        "StandardGreeting.8000.mono.pcmu.s16le.test";
    public static final String IN_PCMU_S16LE =
        "StandardGreeting.8000.mono.pcmu.s16le";
    public static final String OUT_PCMU_PCMA =
        "StandardGreeting.8000.mono.pcmu.pcma.test";
    public static final String IN_PCMU_PCMA =
        "StandardGreeting.8000.mono.pcmu.pcma";

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
        URL s16leUrl = getClass().getClassLoader().getResource(IN_S16LE);
        File s16leFile = new File(s16leUrl.toURI());
        File pcmaFile = new File(OUTPUT_DIR + "/" + OUT_S16LE_PCMA);
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
        assertFileEquals(pcmaFile, IN_S16LE_PCMA);
    }

    public void testAlaw2linear() throws Exception
    {
        URL alawUrl = getClass().getClassLoader().getResource(IN_PCMA);
        File alawFile = new File(alawUrl.toURI());
        FileInputStream alawFileInputStream = new FileInputStream(alawFile);
        File s16leFile = new File(OUTPUT_DIR + "/" + OUT_PCMA_S16LE);
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
        assertFileEquals(s16leFile, IN_PCMA_S16LE);
    }

    public void testLinear2ulaw() throws Exception
    {
        URL s16leUrl = getClass().getClassLoader().getResource(IN_S16LE);
        File s16leFile = new File(s16leUrl.toURI());
        File pcmuFile = new File(OUTPUT_DIR + "/" + OUT_S16LE_PCMU);
        FileInputStream s16leFileInputStream = new FileInputStream(s16leFile);
        FileOutputStream pcmuFileOutputStream = new FileOutputStream(pcmuFile);
        byte[] buf = new byte[BUFFER_SIZE];
        int readBytes = 0;
        while ((readBytes = s16leFileInputStream.read(buf)) > 0)
        {
            byte[] pcmuBuf = new byte[readBytes / 2];
            for (int i = 0; i < readBytes; i += 2)
            {
                int pcm_val = (buf[i + 1] << 8) | (buf[i] & 0xff);
                pcmuBuf[i / 2] = BasicCodecs.linear2ulaw(pcm_val);
            }
            pcmuFileOutputStream.write(pcmuBuf);
        }
        s16leFileInputStream.close();
        pcmuFileOutputStream.close();
        assertFileEquals(pcmuFile, IN_S16LE_PCMU);
    }

    public void testUlaw2linear() throws Exception
    {
        URL ulawUrl = getClass().getClassLoader().getResource(IN_PCMU);
        File ulawFile = new File(ulawUrl.toURI());
        FileInputStream ulawFileInputStream = new FileInputStream(ulawFile);
        File s16leFile = new File(OUTPUT_DIR + "/" + OUT_PCMU_S16LE);
        FileOutputStream s16leFileOutputStream = new FileOutputStream(s16leFile);
        byte[] buf = new byte[BUFFER_SIZE];
        int readBytes = 0;
        while ((readBytes = ulawFileInputStream.read(buf)) > 0)
        {
            byte[] linearBuf = new byte[readBytes * 2];
            for (int i = 0; i < readBytes; ++i)
            {
                int linear = BasicCodecs.ulaw2linear(buf[i]);
                linearBuf[2 * i + 1] = (byte)((linear >> 8) & 0xff);
                if (linear < 0)
                    linearBuf[2 * i] &= 0x80;
                linearBuf[2 * i] = (byte)(linear & 0xff);
            }
            s16leFileOutputStream.write(linearBuf);
        }
        ulawFileInputStream.close();
        s16leFileOutputStream.close();
        assertFileEquals(s16leFile, IN_PCMU_S16LE);
    }

    public void testAlaw2ulaw() throws Exception
    {
        URL alawUrl = getClass().getClassLoader().getResource(IN_PCMA);
        File alawFile = new File(alawUrl.toURI());
        FileInputStream alawFileInputStream = new FileInputStream(alawFile);
        File ulawFile = new File(OUTPUT_DIR + "/" + OUT_PCMA_PCMU);
        FileOutputStream ulawFileOutputStream = new FileOutputStream(ulawFile);
        byte[] buf = new byte[BUFFER_SIZE];
        int readBytes = 0;
        while ((readBytes = alawFileInputStream.read(buf)) > 0)
        {
            byte[] pcmuBuf = new byte[readBytes];
            for (int i = 0; i < readBytes; ++i)
                pcmuBuf[i] = BasicCodecs.alaw2ulaw(buf[i]);
            ulawFileOutputStream.write(pcmuBuf);
        }
        alawFileInputStream.close();
        ulawFileOutputStream.close();
        assertFileEquals(ulawFile, IN_PCMA_PCMU);
    }

    public void testUlaw2alaw() throws Exception
    {
        URL ulawUrl = getClass().getClassLoader().getResource(IN_PCMU);
        File ulawFile = new File(ulawUrl.toURI());
        FileInputStream ulawFileInputStream = new FileInputStream(ulawFile);
        File alawFile = new File(OUTPUT_DIR + "/" + OUT_PCMU_PCMA);
        FileOutputStream alawFileOutputStream = new FileOutputStream(alawFile);
        byte[] buf = new byte[BUFFER_SIZE];
        int readBytes = 0;
        while ((readBytes = ulawFileInputStream.read(buf)) > 0)
        {
            byte[] pcmaBuf = new byte[readBytes];
            for (int i = 0; i < readBytes; ++i)
                pcmaBuf[i] = BasicCodecs.ulaw2alaw(buf[i]);
            alawFileOutputStream.write(pcmaBuf);
        }
        ulawFileInputStream.close();
        alawFileOutputStream.close();
        assertFileEquals(alawFile, IN_PCMU_PCMA);
    }

}
