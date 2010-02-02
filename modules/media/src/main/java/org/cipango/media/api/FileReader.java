package org.cipango.media.api;

import org.mortbay.io.Buffer;

/**
 * Reads a File into a Buffer.
 * 
 * This class takes a file name as input and simply reads bytes from this
 * file into a new Buffer. {@link #read(int)} will put at most the number of
 * bytes provided in parameter in the buffer and {@link #read()} will put at
 * most {@link #DEFAULT_BUFFER_SIZE} bytes into the buffer. To know the number
 * of bytes really read from file, you can use {@link Buffer#getIndex()}.
 * If no more data can be read from file, read functions will return null.
 * 
 * @author yohann
 */
public class FileReader implements Initializable, Door, Managed
{

	public static final int DEFAULT_BUFFER_SIZE = 256;

    FileReader(String filename)
    {
        
    }

    @Override
    public void init()
    {
    	// TODO Auto-generated method stub
    	
    }

    @Override
    public void open()
    {
    	// TODO Auto-generated method stub
    	
    }

    /**
     * Read at most {@link #DEFAULT_BUFFER_SIZE} bytes from file into a Buffer.
     * 
     * The number of bytes really read from file can be retrieved from Buffer.
     * 
     * @return a Buffer containing file data or null if end of file reached.
     */
    public Buffer read()
    {
        return null;
    }

    /**
     * Read at most <code>size</code> bytes from file into a Buffer.
     * 
     * The number of bytes really read from file can be retrieved from Buffer.
     * 
     * @param size the number of bytes to read from file.
     * @return a Buffer containing file data or null if end of file reached.
     */
    public Buffer read(int size)
    {
    	return null;
    }

    @Override
    public void close()
    {
    	// TODO Auto-generated method stub
    	
    }

}
