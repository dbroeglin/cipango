package org.cipango.media.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.mortbay.io.Buffer;
import org.mortbay.log.Log;

/**
 * Write a Buffer in a File.
 * 
 * This class takes a file name as input and simply writes bytes from Buffer
 * to this File. {@link #write()} will write all bytes of its provided Buffer.
 * 
 * @author yohann
 */
public class FileWriter implements Initializable, Door, Managed
{

	private String _filename;
	private File _file;
	private FileOutputStream _fileOutputStream;

    FileWriter(String filename)
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
			_fileOutputStream = new FileOutputStream(_file);
		}
		catch (FileNotFoundException e)
		{
			Log.warn("file not found: " + _filename, e);
		}
    }

    /**
     * Write bytes of a Buffer to a File.
     * 
     * @param buffer jetty buffer employed to provide bytes to fill the File.
     */
    public void write(Buffer buffer)
    {
        try
		{
			_fileOutputStream.write(buffer.asArray());
		}
		catch (IOException e)
		{
			Log.warn("cannot write to file: " + _filename);
		}
    }

    @Override
    public void close()
    {
    	try
		{
			_fileOutputStream.close();
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
