package org.cipango.media.api;

import org.mortbay.io.Buffer;

/**
 * Write a Buffer in a File.
 * 
 * This class takes a file name as input and simply writes bytes from Buffer
 * to this File. {@link #write()} will write all bytes of its provided Buffer.
 * 
 * @author yohann
 */
public class FileWriter implements Managed
{

    FileWriter(String filename)
    {
        
    }

    /**
     * Write bytes of a Buffer to a File.
     * 
     * @param buffer jetty buffer employed to provide bytes to fill the File.
     */
    public void write(Buffer buffer)
    {
        
    }

}
