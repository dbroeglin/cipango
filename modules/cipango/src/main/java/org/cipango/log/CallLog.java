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

public class CallLog
{
	private static String __suffix = ".log";
	
	private PrintWriter _writer;

	public CallLog(String dir, String id) throws IOException
	{
		File logDir = new File(dir);
		if (!logDir.exists())
		{
			if (!logDir.mkdirs())
				throw new IOException("Failed to create call log dir: " + logDir.getCanonicalPath());
		}
		String name = URLEncoder.encode(id, "UTF-8");
		File log = new File(logDir, name + __suffix);
		
		_writer = new PrintWriter(new FileOutputStream(log));
	}
	
	public void log(String message)
	{
		_writer.println(message);
		_writer.flush();
	}
	
	public void close()
	{
		_writer.close();
	}
}
