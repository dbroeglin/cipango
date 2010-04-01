package org.cipango.diameter.log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.TimeZone;

import org.cipango.diameter.DiameterConnection;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.DiameterMessageListener;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.log.Log;
import org.mortbay.util.DateCache;
import org.mortbay.util.RolloverFileOutputStream;
import org.mortbay.util.StringUtil;

public class FileMessageLogger extends AbstractLifeCycle implements DiameterMessageListener
{
	private enum Direction { IN, OUT }
	public final static String YYYY_MM_DD="yyyy_mm_dd";
	private OutputStream _out;
    private String _filename;
    private boolean _append = true;
    private int _retainDays = 31;
    
    private DateCache _logDateCache;
    private String _logDateFormat = "yyyy-MM-dd HH:mm:ss"; //"yyyy-MM-dd HH:mm:ss.SSS ZZZ";
    private Locale _logLocale     = Locale.getDefault();
    private String _logTimeZone   = TimeZone.getDefault().getID();
    
    private StringBuilder _buf = new StringBuilder();

	
	protected void doStart() throws Exception 
    {	
		_logDateCache = new DateCache(_logDateFormat, _logLocale);
		_logDateCache.setTimeZoneID(_logTimeZone);
		
		if (_filename != null) 
			_out = new RolloverFileOutputStream(_filename, _append, _retainDays);
		else 
			_out = System.out;
		
		super.doStart();
        
        Log.info("Diameter Access log started in {}", 
                _out instanceof RolloverFileOutputStream ? 
                        ((RolloverFileOutputStream) _out).getDatedFilename() :
                            "stdout");
	}
	
	protected void doStop() throws Exception 
    {
		super.doStop();
		if (_out != null) 
        {
			try 
            {
				_out.close();
			} 
            catch (IOException e) 
            {
				Log.ignore(e);
			}
			_out = null;
		}
	}
	
	protected void doLog(Direction direction, DiameterMessage message, DiameterConnection connection)
	{
		if (!isStarted()) 
			return;

		try 
        {
			synchronized (_out) 
            {
				_out.write(StringUtil.__LINE_SEPARATOR.getBytes());
				_buf.setLength(0);
                _buf.append(_logDateCache.format(System.currentTimeMillis()));
                if (direction == Direction.IN)
        			_buf.append(" IN  ");
        		else
                    _buf.append(" OUT ");
                _buf.append(connection.getLocalAddr());
                _buf.append(':');
                _buf.append(connection.getLocalPort());
                _buf.append((direction == Direction.IN ? " < " : " > "));
                _buf.append(connection.getRemoteAddr());
                _buf.append(':');
                _buf.append(connection.getRemotePort());
                _buf.append('\n');
                _buf.append(message);
                _out.write(_buf.toString().getBytes());	
				_out.flush();
			}
		} 
        catch (Exception e) 
        {
			Log.warn("Failed to log message", e);
		}
	}
	
	public void setFilename(String filename) 
    {
		if (filename != null) 
        {
			filename=filename.trim();
			if (filename.length() == 0) 
				filename = null;
		}
		this._filename = filename;
	}
	
    public String getFilename() 
    {
        return _filename;
    }
    
    public boolean isAppend() {
    	return _append;
    }
    
    public void setAppend(boolean append) {
    	_append = append;
    }
    
    public int getRetainDays() {
    	return _retainDays;
    }
    
    public void setRetainDays(int days) {
    	_retainDays = days;
    }
    
    public void messageReceived(DiameterMessage message, DiameterConnection connection) 
	{
		doLog(Direction.IN, message, connection);
	}

	public void messageSent(DiameterMessage message, DiameterConnection connection) 
	{
		doLog(Direction.OUT, message, connection);
	}
	
    public void deleteLogFiles() throws IOException
    {
		if (_filename == null)
			return;
		
    	synchronized (_out)
		{
    		_out.close();
			File file= new File(_filename);
            File dir = new File(file.getParent());
            String fn=file.getName();
            int s=fn.toLowerCase().indexOf(YYYY_MM_DD);
            if (s<0)
                file.delete();
            else
            {
	            String prefix=fn.substring(0,s);
	            String suffix=fn.substring(s+YYYY_MM_DD.length());
	            String[] logList=dir.list();
	            for (int i=0;i<logList.length;i++)
	            {
	                fn = logList[i];
	                if(fn.startsWith(prefix) && fn.indexOf(suffix,prefix.length())>=0)
	                {        
	                    new File(dir,fn).delete();   
	                }
	            }
            }
            _out = new RolloverFileOutputStream(_filename, _append, _retainDays);
		}
    }
}
