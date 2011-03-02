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
package org.cipango.console.printer;

import java.io.Writer;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Page;
import org.cipango.console.Parameters;

public class DiameterLogPrinter extends AbstractLogPrinter
{
	private static final String[] GET_MSG_SIGNATURE =
	{ Integer.class.getName(), String.class.getName() };

	private static final ResourceBundle FILTERS = ResourceBundle
			.getBundle("org.cipango.console.diameter-filters");

	private Object[][] _messagesLogs;
	private Output _output;

	public DiameterLogPrinter(MBeanServerConnection connection,
			HttpServletRequest request, Output output) throws Exception
	{
		super(connection, request);
		_output = output;

		if (isLoggerRunning())
		{
			Object[] params =
			{ new Integer(_maxMessages), _msgFilter };
			_messagesLogs = (Object[][]) connection.invoke(
					ConsoleFilter.DIAMETER_CONSOLE_LOG, "getMessages", params,
					GET_MSG_SIGNATURE);
		}
	}

	public void print(Writer out) throws Exception
	{
		if (_output == Output.TEXT)
		{
			if (_messagesLogs == null)
				out.write("JMX message logger is not configured");
			else
			{
				for (int i = 0; i < _messagesLogs.length; i++)
				{
					out.write(_messagesLogs[i][0].toString());
					out.write(_messagesLogs[i][1].toString());
					out.write('\n');
				}
			}
			return;
		}

		if (_connection.isRegistered(ConsoleFilter.DIAMETER_FILE_LOG))
		{
			out.write("<h2>File Log</h2>");
			new FileLogPrinter(_connection, getPage(), ConsoleFilter.DIAMETER_FILE_LOG, true).print(out);
		}
		if (_connection.isRegistered(getObjectName()))
		{
			out.write("<h2>Console Log</h2>");
			printHeaders(out);
			if (isLoggerRunning())			
				printConsoleLog(out);
		}
	}

	protected void printCommonFilters(Writer out) throws Exception
	{
		out.write("Filter:&nbsp;" + "<SELECT name=\""
				+ Parameters.MESSAGE_FILTER + "\">");
		Enumeration<String> keys = FILTERS.getKeys();
		boolean selected = false;
		while (keys.hasMoreElements())
		{
			String key = keys.nextElement();
			if (key.endsWith(".title"))
			{
				String title = FILTERS.getString(key);
				String prefix = key.substring(0, key.length()
						- ".title".length());
				String value = FILTERS.getString(prefix + ".filter").trim();
				out.write("<OPTION value=\"" + value + "\" ");
				if (value.equals(_msgFilter)
						|| (value.equals("") && _msgFilter == null))
				{
					out.write(" selected");
					selected = true;
				}
				out.write(">" + title + "</OPTION>");
			}
		}
		if (!selected)
			out.write("<OPTION selected>" + _msgFilter + "</OPTION>");

		out.write("</SELECT>");
	}

	private void printConsoleLog(Writer out) throws Exception
	{
		out.write("<div id=\"messageLog\">");
		for (int i = 0; i < _messagesLogs.length; i++)
		{
			out.write("<div id=\"log_" + i + "\">");
			MessageLog log = new MessageLog(_messagesLogs[i]);
			String info = log.getInfoLine().replaceFirst(log.getRemote(),
					getFilterLink("remote", log.getRemote()));
			out.write("<div class=\"info\">" + info + "</div>");
			out.write("<pre class=\"message\">");
			out.write(diameterToHtml(log));
			out.write("</pre>");
			out.write("</div>");
		}
		out.write("</div>");
	}


	private String diameterToHtml(MessageLog log)
	{
		StringBuilder sb = new StringBuilder(log.getMessage());

		replaceAll(sb, "<", "&lt;");
		replaceAll(sb, ">", "&gt;");
		replaceAll(sb, "\"", "&quot;");

		return sb.toString();
	}

	private String getFilterLink(String name, String value)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<A href=\"" + MenuPrinter.DIAMETER_LOGS.getName() + "?").append(Parameters.MESSAGE_FILTER).append(
				"=");
		sb.append(name).append(".equals(%27").append(value).append("%27)");
		sb.append("&").append(Parameters.MAX_MESSAGES).append("=").append(
				_maxMessages);
		sb.append("\">").append(value).append("</A>");
		return sb.toString();
	}
	
	@Override
	public Page getPage()
	{
		return MenuPrinter.DIAMETER_LOGS;
	}
	

	@Override
	public ObjectName getObjectName()
	{
		return ConsoleFilter.DIAMETER_CONSOLE_LOG;
	}
	@Override
	public String getLogFile()
	{
		return "diameter.log";
	}


	class MessageLog
	{
		private Object[] _array;

		public MessageLog(Object[] array)
		{
			_array = array;
		}

		public String getInfoLine()
		{
			return _array[0].toString();
		}

		public String getMessage()
		{
			return _array[1].toString();
		}

		public String getRemote()
		{
			return _array[2].toString();
		}
	}
}
