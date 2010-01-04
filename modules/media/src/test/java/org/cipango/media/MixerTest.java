package org.cipango.media;

import static org.cipango.media.MediaTestConstants.OUTPUT_DIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.TestCase;

public class MixerTest extends TestCase {

    public static final String IN_VOICE_1 = "voice-1.8000.mono.s16le";
    public static final String IN_VOICE_2 = "voice-2.8000.mono.s16le";
    public static final String OUT_MIXED = "two-voices-mixed.8000.mono.s16le";

    private boolean _timerExpired;

    public void test2Files() throws Exception
    {
        _timerExpired = false;
        URL voice1Url = getClass().getClassLoader().getResource(IN_VOICE_1);
        File voice1File = new File(voice1Url.toURI());
        FileInputStream voice1InputStream = new FileInputStream(voice1File);
        URL voice2Url = getClass().getClassLoader().getResource(IN_VOICE_2);
        File voice2File = new File(voice2Url.toURI());
        FileInputStream voice2InputStream = new FileInputStream(voice2File);
        File mixedFile = new File(OUTPUT_DIR + "/" + OUT_MIXED);
        FileOutputStream mixedOutputStream = new FileOutputStream(mixedFile);
        Mixer mixer = new Mixer(mixedOutputStream);
        mixer.addInputStream(voice1InputStream);
        mixer.addInputStream(voice2InputStream);
        Thread thread = new Thread(mixer);
        Timer timer = new Timer();
        timer.schedule(new TestTerminated(), 5000);
        thread.start();
        while (!_timerExpired)
            Thread.sleep(50);
        if (mixer.isRunning())
            fail("mixer should not be running anymore");
    }

    class TestTerminated extends TimerTask
    {

        @Override
        public void run() {
            _timerExpired = true;
        }

    }

}
