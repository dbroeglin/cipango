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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Parameters;
import org.cipango.console.printer.MenuPrinter.Page;

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
		_msgFilter = request.getParameter(Parameters.MESSAGE_FILTER);
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

			out.write(PrinterUtil.getActionLink("clear",
					ConsoleFilter.SIP_CONSOLE_MSG_LOG, MenuPrinter.SIP_LOGS, "Clear logs"));
			out.write("&nbsp;|&nbsp;");
			out.write(PrinterUtil.getActionLink("stop",
					getObjectName(), getPage(), "Deactivate console message log"));
			out.write("&nbsp;|&nbsp;<A href=\"" + getLogFile() + "?"
					+ Parameters.MAX_MESSAGES + "=" + _maxMessages);
			if (_msgFilter != null && !_msgFilter.trim().equals(""))
				out.write("&" + Parameters.MESSAGE_FILTER + "=" + _msgFilter);

			out.write("\">Display as log file</A>\n");
			
			out.write("<form method=\"get\" action=\"" + getPage().getName() + "\">" + "Server keeps in memory the "
					+ "<input type=\"text\" name=\"maxMessages" + Parameters.DOT_VALUE 
					+ "\" value=\"" + maxSavedMsg + "\" size=\"1\" maxlength=\"5\"/>");
			out.write("<input type=\"hidden\" name=\"maxMessages" + Parameters.DOT_OBJECT_NAME 
					+ "\" value=\"" + getObjectName() + "\"/>");
			out.write(" last messages. <input type=\"submit\" name=\"" + Parameters.ACTIONS + "\" value=\"Apply\"/></FORM>");
			out.write("</div>\n");
		}
		else
		{
			out.write(PrinterUtil.getActionLink("start",
					getObjectName(), getPage(), "Activate console message log"));
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

}
