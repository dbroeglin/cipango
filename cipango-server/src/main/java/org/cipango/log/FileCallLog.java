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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.DateCache;

public class FileCallLog extends AbstractLifeCycle implements CallLog
{	
	private String _logDirName;
	private File _logDir;
	
	private DateCache _dateCache;
	
	public void setLogDir(String logDirName)
	{
		_logDirName = logDirName;
	}
	
	@Override
	protected void doStart() throws Exception
	{
		_logDir = new File(_logDirName);
		if (!_logDir.exists())
		{
			if (!_logDir.mkdirs())
				throw new IOException("Failed to create call log directory " + _logDir.getCanonicalPath());
		}
		
		_dateCache = new DateCache("yyyy-MM-dd HH:mm:ss");
	}
	
	public CallLogger getLogger(String callId) 
	{
		try
		{
			return new Logger(callId);
		}
		catch (IOException e)
		{
			Log.warn(e);
		}
		return null;
	}

	class Logger implements CallLogger
	{	
		private PrintWriter _writer;

		public Logger(String id) throws IOException
		{
			String name = URLEncoder.encode(id, "UTF-8");
			File log = new File(_logDir, name + ".log");
			
			_writer = new PrintWriter(new FileOutputStream(log));
		}
		
		public void log(String message, Object arg0, Object arg1)
		{
			String d = _dateCache.now();
	        int ms = _dateCache.lastMs();
	        
			_writer.println(d+(ms>99?".":(ms>0?".0":".00"))+ms + "   " + format(message, arg0, arg1));
			_writer.flush();
		}
		
		private String format(String msg, Object arg0, Object arg1)
	    {
	        int i0=msg.indexOf("{}");
	        int i1=i0<0?-1:msg.indexOf("{}",i0+2);
	        
	        if (arg1!=null && i1>=0)
	            msg=msg.substring(0,i1)+arg1+msg.substring(i1+2);
	        if (arg0!=null && i0>=0)
	            msg=msg.substring(0,i0)+arg0+msg.substring(i0+2);
	        return msg;
	    }
		
		public void close()
		{
			_writer.close();
		}
	}
}
