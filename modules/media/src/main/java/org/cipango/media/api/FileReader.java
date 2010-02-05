package org.cipango.media.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.log.Log;

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

	private String _filename;
	private File _file;
	private FileInputStream _fileInputStream;

    FileReader(String filename)
    {
        _filename = filename;
    }

    @Override
    public void init()
    {
    	_file = new File(_filename);
    }

    @Override
    public void open()
    {
    	try
		{
			_fileInputStream = new FileInputStream(_file);
		}
		catch (FileNotFoundException e)
		{
			Log.warn("file not found: " + _filename, e);
		}
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
    	return read(DEFAULT_BUFFER_SIZE);
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
    	byte[] buf = new byte[size];
    	try
		{
			_fileInputStream.read(buf);
		}
		catch (IOException e)
		{
			Log.warn("cannot read from file: " + _filename, e);
			return null;
		}
		return new ByteArrayBuffer(buf);
    }

    @Override
    public void close()
    {
    	try
		{
			_fileInputStream.close();
		}
		catch (IOException e)
		{
			Log.warn("cannot close file: " + _filename, e);
		}
    }

	public String getFilename()
	{
		return _filename;
	}

}
