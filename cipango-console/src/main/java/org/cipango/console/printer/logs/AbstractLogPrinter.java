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

import java.io.IOException;
import java.io.Writer;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.cipango.console.Action;
import org.cipango.console.Action.StartAction;
import org.cipango.console.Action.StopAction;
import org.cipango.console.Page;
import org.cipango.console.Parameters;
import org.cipango.console.printer.generic.HtmlPrinter;
import org.cipango.console.printer.generic.PrinterUtil;

public abstract class AbstractLogPrinter implements HtmlPrinter
{
	protected static final int[] NB_MESSAGES =
	{ 4, 10, 20, 50, 200 };

	public static final int DEFAULT_MAX_MESSAGES = 20;

	public static enum Output
	{
		TEXT, HTML;
	}

	protected MBeanServerConnection _connection;
	protected int _maxMessages;
	protected String _msgFilter;

	public AbstractLogPrinter(MBeanServerConnection connection, HttpServletRequest request) throws Exception
	{
		_connection = connection;
		String maxMsg = request.getParameter(Parameters.MAX_MESSAGES);
		if (maxMsg == null)
			maxMsg = (String) request.getSession().getAttribute(Parameters.MAX_MESSAGES);
		else
			request.getSession().setAttribute(Parameters.MAX_MESSAGES, maxMsg);
		
		_maxMessages = PrinterUtil.getInt(maxMsg, DEFAULT_MAX_MESSAGES);
	}
	
	public void printHeaders(Writer out) throws Exception
	{
		if (!_connection.isRegistered(getObjectName()))
		{
			out.write("Console logger is not registered");
			return;
		}
		
		boolean on = ((Boolean) _connection.getAttribute(getObjectName(), "running")).booleanValue();
		if (on)
		{
			out.write("<div class=\"inline\">");
			out.write("<form method=\"get\" action=\"" + getPage().getName() + "\">" + "Show &nbsp;"
					+ "<SELECT name=\"" + Parameters.MAX_MESSAGES + "\">");
			int maxSavedMsg = (Integer) _connection.getAttribute(getObjectName(), "maxMessages");
			for (int i = 0; i < NB_MESSAGES.length; i++)
			{
				if (NB_MESSAGES[i] <= maxSavedMsg)
				{
					out.write("<OPTION ");
					if (_maxMessages == NB_MESSAGES[i])
						out.write(" selected");
					out.write(">" + NB_MESSAGES[i] + "</OPTION>");
				}
			}
			out.write("</SELECT>");
			out.write("&nbsp;last filtered messages.&nbsp;|&nbsp;");

			printCommonFilters(out);

			out.write("&nbsp;&nbsp;<input type=\"submit\" name=\"submit\" value=\"Apply\"/></FORM>");

			new ClearConsoleLoggerAction(getPage()).print(out);
			out.write("&nbsp;|&nbsp;");
			new StopConsoleLoggerAction(getPage(), getObjectName()).print(out);
			
			out.write("&nbsp;|&nbsp;<A href=\"" + getLogFile() + "\">Display as log file</A>\n");
			
			new MessageInMemoryAction(getPage(), getObjectName()).print(out, maxSavedMsg);
		}
		else
		{
			new StartConsoleLoggerAction(getPage(), getObjectName()).print(out);
		}
	}
	

	public abstract Page getPage();
	
	public abstract String getLogFile();
	
	protected abstract void printCommonFilters(Writer out) throws Exception;
	
	public abstract ObjectName getObjectName();
	
	public boolean isLoggerRunning()
	{
		try
		{
			return _connection.isRegistered(getObjectName()) && ((Boolean) _connection.getAttribute(getObjectName(), "running"));
		}
		catch (Exception e)
		{
			return false;
		}
	}

	protected void replaceAll(StringBuilder sb, String toFind, Object toSet)
	{
		int index = 0;
		while ((index = sb.indexOf(toFind)) != -1)
			sb.replace(index, index + toFind.length(), toSet.toString());
	}

	protected void replaceOnce(StringBuilder sb, String toFind, Object toSet)
	{
		int index = 0;
		if ((index = sb.indexOf(toFind)) != -1)
			sb.replace(index, index + toFind.length(), toSet.toString());
	}
	
	protected void replaceOnce(StringBuilder sb, int index, String toFind, Object toSet)
	{
		if ((index = sb.indexOf(toFind, index)) != -1)
			sb.replace(index, index + toFind.length(), toSet.toString());
	}

	public static class StartConsoleLoggerAction extends StartAction
	{
		public StartConsoleLoggerAction(Page page, ObjectName objectName)
		{
			super(page, "activate-console-message-log", objectName);
		}
	}
	
	public static class StopConsoleLoggerAction extends StopAction
	{
		public StopConsoleLoggerAction(Page page, ObjectName objectName)
		{
			super(page, "deactivate-console-message-log", objectName);
		}
	}
	
	public static class ClearConsoleLoggerAction extends Action
	{
		public ClearConsoleLoggerAction(Page page)
		{
			super(page, "clear-logs");
		}

		@Override
		protected void doProcess(HttpServletRequest request) throws Exception
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public static class MessageInMemoryAction extends Action
	{
		private ObjectName _objectName;
		
		public MessageInMemoryAction(Page page, ObjectName objectName)
		{
			super(page, "msg-in-memory");
			_objectName = objectName;
		}

		@Override
		protected void doProcess(HttpServletRequest request) throws Exception
		{
			String maxMsg = request.getParameter(Parameters.MAX_SAVED_MESSAGES);
			if (maxMsg != null)
			{
				getConnection().setAttribute(_objectName, new Attribute("maxMessages", Integer.parseInt(maxMsg)));
			}
		}
		
		public void print(Writer out, int maxSavedMsg) throws IOException
		{
			out.write("<form method=\"get\" action=\"" + getPage().getName() + "\">" + "Server keeps in memory the "
					+ "<input type=\"text\" name=\"" + Parameters.MAX_SAVED_MESSAGES 
					+ "\" value=\"" + maxSavedMsg + "\" size=\"1\" maxlength=\"5\"/>");
			out.write("<input type=\"hidden\" name=\"" + Parameters.ACTION+ "\" value=\"" + getParameter() + "\"/>");
			out.write(" last messages. <input type=\"submit\" value=\"Apply\"/></FORM>");
			out.write("</div>\n");
		}
	}
}
