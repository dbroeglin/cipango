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

import org.cipango.console.ConsoleFilter;
import org.cipango.console.StatisticGraph;


public class SipStatisticPrinter extends ObjectListPrinters implements HtmlPrinter
{
	private static final int[] STATS_TIME_VALUE =
		{ 800, 3600, 14400, 86400, 604800, 1209600};
	private static final String[] STATS_TIME_TITLE = 
		{"last 15 minutes", "last hour", "last 4 hours", "last 24 hours", "last 7 days", "last 2 weeks"};
	
	private MBeanServerConnection _connection;
	private StatisticGraph _statisticGraph;
	private int _time;

	public SipStatisticPrinter(MBeanServerConnection connection, StatisticGraph statisticGraph)
	{
		super(connection, "sip.messages", true);
		_connection = connection;
		_statisticGraph = statisticGraph;
	}

	public void print(Writer out) throws Exception
	{
		super.print(out);
		ObjectName sessionManager = (ObjectName) _connection.getAttribute(ConsoleFilter.SERVER, "sessionManager");
		new ObjectPrinter(sessionManager, "sip.callSessions", _connection).print(out);
		new SetPrinter(PrinterUtil.getContexts(_connection), "sessions", _connection).print(out);
		printActions(out);
		printStatisticGraphs(out);
	}

	private void printActions(Writer out) throws Exception
	{
		Boolean on = (Boolean) _connection.getAttribute(ConsoleFilter.SERVER,
				"allStatsOn");
		if (on.booleanValue())
		{
			out.write(PrinterUtil.getSetterLink("allStatsOn", "false", ConsoleFilter.SERVER,
					MenuPrinter.STATISTICS_SIP, "Disable statistics"));
			out.write("&nbsp;&nbsp;&nbsp;");
			out.write(PrinterUtil.getActionLink("allStatsReset", ConsoleFilter.SERVER,
					MenuPrinter.STATISTICS_SIP, "Reset statistics"));
		}
		else
		{
			out.write(PrinterUtil.getSetterLink("allStatsOn", "true", ConsoleFilter.SERVER,
					MenuPrinter.STATISTICS_SIP, "Enable statistics"));
		}

	}

	private void printStatisticGraphs(Writer out) throws Exception
	{
		if (_statisticGraph != null)
		{

			out.write("<h2>Statistic Graph</h2>\n");
			/*Boolean started = (Boolean) _connection.getAttribute(
					ConsoleFilter.STATISTIC_GRAPH, "running");
			if (started.booleanValue())
			{*/
							
				out.write("<form method=\"get\" action=\"" + MenuPrinter.STATISTICS_SIP.getName() + "\">"
						+ "Statistic graph should show to the " 
						+ "<SELECT name=\"time\">");
				for (int i = 0; i < STATS_TIME_VALUE.length; i++)
				{
					out.write("<OPTION VALUE=\"" + STATS_TIME_VALUE[i] + "\"");
					if (_time == STATS_TIME_VALUE[i])
						out.write(" selected");
					out.write(">" + STATS_TIME_TITLE[i] + "</OPTION>");
				}
 
				out.write("</SELECT>"
						+ "<input type=\"submit\" name=\"submit\" value=\"change\"/></form>");

				printGraph(out, "Calls", "calls");
				printGraph(out, "JVM Memory", "memory");
				printGraph(out, "SIP messages", "messages");
				out.write("<br/>");
			/*	out.write(PrinterUtil.getActionLink("stop", ConsoleFilter.STATISTIC_GRAPH,
						_connection, "statistics", null)
						+ " statistic graph.");
			}
			else
			{
				out.write("Statistic graph are disabled.");
				out.write("<br/>");
				out.write(PrinterUtil.getActionLink("start", ConsoleFilter.STATISTIC_GRAPH,
						_connection, "statistics", null)
						+ " statistics graphics.");
			}*/
		}
	}

	private void printGraph(Writer out, String title, String type) throws Exception
	{
		out.write("<h3>" + title + "</h3>" + "<img src=\"statisticGraph.png?time=" + _time 
				+ "&type=" + type + "\"/><br/>\n");
	}

	public void setTime(String time)
	{
		try 
		{
			_time = Integer.parseInt(time);
		} catch (Exception e) {
		}
		if (_time <= 0)
			_time = STATS_TIME_VALUE[1];
	}

}
