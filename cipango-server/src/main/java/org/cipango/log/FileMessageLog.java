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

package org.cipango.log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.cipango.SipMessage;
import org.cipango.sip.SipConnection;
import org.mortbay.io.Buffer;
import org.mortbay.log.Log;
import org.mortbay.util.RolloverFileOutputStream;
import org.mortbay.util.StringUtil;

public class FileMessageLog extends AbstractMessageLog implements AccessLog 
{

	public final static String YYYY_MM_DD="yyyy_mm_dd";
    private OutputStream _out;
    private String _filename;
    private boolean _append = true;
    private int _retainDays = 31;

    private Object _lock = new Object();
    	
	protected void doStart() throws Exception 
    {	
		try
		{		
			if (_filename != null) 
			{
				File file = new File(_filename);
				file.getParentFile().mkdirs();
				_out = new RolloverFileOutputStream(_filename, _append, _retainDays);
			}
			else 
				_out = System.out;
	        			
			super.doStart();
	        
	        Log.info("Access log started in {}", 
	                _out instanceof RolloverFileOutputStream ? 
	                        ((RolloverFileOutputStream) _out).getDatedFilename() :
	                            "stdout");
		}
		catch (Exception e) 
		{
			Log.warn("Unable to log SIP messages: " + e.getMessage());
		}
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
	
	public void doLog(SipMessage message, int direction, SipConnection connection) throws IOException 
	{
        synchronized (_lock)
		{
        	_out.write(generateInfoLine(direction, connection, System.currentTimeMillis()).getBytes()); 
            Buffer buffer = generateMessage(message);
    		_out.write(buffer.array(), 0, buffer.length());
    		_out.write(StringUtil.__LINE_SEPARATOR.getBytes());
    		_out.flush();
		}
		
	}
	
	public void setFilename(String filename) 
    {
		if (filename != null) 
        {
			filename = filename.trim();
			if (filename.length() == 0) 
				filename = null;
		}
		_filename = filename;
	}
	
    public String getFilename() 
    {
        return _filename;
    }
    
    public boolean isAppend() 
    {
    	return _append;
    }
    
    public void setAppend(boolean append) 
    {
    	_append = append;
    }
    
    public int getRetainDays() 
    {
    	return _retainDays;
    }
    
    public void setRetainDays(int days) 
    {
    	_retainDays = days;
    }
    
    public void deleteLogFiles() throws IOException
    {
		if (_filename == null)
			return;
		
    	synchronized (_lock)
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
