// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.console.printer.logs;

import java.io.Writer;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.cipango.console.Action;
import org.cipango.console.Action.StartAction;
import org.cipango.console.Action.StopAction;
import org.cipango.console.Page;
import org.cipango.console.printer.generic.HtmlPrinter;

public class FileLogPrinter implements HtmlPrinter
{

	protected MBeanServerConnection _connection;
	private Page _page;
	private boolean _deleteSupported;
	private ObjectName _objectName;
		
	public FileLogPrinter(MBeanServerConnection connection, Page page, ObjectName objectName, boolean deleteSupported) throws Exception
	{
		_connection = connection;
		_page = page;
		_deleteSupported = deleteSupported;
		_objectName = objectName;
	}


	public void print(Writer out) throws Exception
	{
		if (!_connection.isRegistered(_objectName))
		{
			out.write("File logger is not registered");
			return;
		}
		
		boolean on = ((Boolean) _connection.getAttribute(_objectName, "running")).booleanValue();
		out.write("All incoming and outgoing messages ");
		if (on)
			out.write("are logged ");
		else
			out.write("can be logged ");

		String filename = (String) _connection.getAttribute(_objectName, "filename");
		if (filename == null)
			out.write("on the console");
		else
			out.write("in the file " + filename);

		out.write(". Traces are kept during at most ");
		out.write(_connection.getAttribute(_objectName, "retainDays")
						.toString());
		out.write(" days.<br/>");

		if (on)
			new StopFileLoggerAction(getPage(), null).print(out);
		else
			new StartFileLoggerAction(getPage(), null).print(out);
		if (_deleteSupported)
		{
			out.write("&nbsp;&nbsp;&nbsp;");
			new DeleteLogsFilesAction(getPage()).print(out);
		}
	}
	
	public Page getPage()
	{
		return _page;
	}
	
	public static class StartFileLoggerAction extends StartAction
	{
		public StartFileLoggerAction(Page page, ObjectName objectName)
		{
			super(page, "activate-file-message-log", objectName);
		}
	}
	
	public static class StopFileLoggerAction extends StopAction
	{
		public StopFileLoggerAction(Page page, ObjectName objectName)
		{
			super(page, "deactivate-file-message-log", objectName);
		}
	}
	
	public static class DeleteLogsFilesAction extends Action
	{
		public DeleteLogsFilesAction(Page page)
		{
			super(page, "delete-logs-files");
		}

		@Override
		protected void doProcess(HttpServletRequest request) throws Exception
		{
			throw new UnsupportedOperationException();
		}
	}
}
