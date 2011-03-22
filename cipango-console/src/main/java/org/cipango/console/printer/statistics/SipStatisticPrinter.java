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
package org.cipango.console.printer.statistics;

import java.io.Writer;
import java.text.DecimalFormat;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Parameters;
import org.cipango.console.PropertyList;
import org.cipango.console.Row;
import org.cipango.console.Row.Header;
import org.cipango.console.Row.Value;
import org.cipango.console.StatisticGraph;
import org.cipango.console.Table;
import org.cipango.console.printer.MenuPrinter;
import org.cipango.console.printer.generic.HtmlPrinter;
import org.cipango.console.printer.generic.MultiplePrinter;
import org.cipango.console.printer.generic.PrinterUtil;
import org.cipango.console.printer.generic.PropertiesPrinter;
import org.cipango.console.printer.generic.SetPrinter;


public class SipStatisticPrinter extends MultiplePrinter implements HtmlPrinter
{
	private static final int[] STATS_TIME_VALUE =
		{ 800, 3600, 14400, 86400, 604800, 1209600};
	private static final String[] STATS_TIME_TITLE = 
		{"last 15 minutes", "last hour", "last 4 hours", "last 24 hours", "last 7 days", "last 2 weeks"};
	
	public static final String START_GRAPH = "startStatisticsGraph";
	public static final String STOP_GRAPH = "stopStatisticsGraph";
	
	private MBeanServerConnection _connection;
	private StatisticGraph _statisticGraph;
	private int _time;

	public SipStatisticPrinter(MBeanServerConnection connection, StatisticGraph statisticGraph) throws Exception
	{
		_connection = connection;
		_statisticGraph = statisticGraph;
		add(new PropertiesPrinter(new PropertyList(connection, "sip.messages")));
		
		ObjectName sessionManager = (ObjectName) _connection.getAttribute(ConsoleFilter.SERVER, "sessionManager");
		add(new PropertiesPrinter(sessionManager, "sip.callSessions", _connection));
		ObjectName[] contexts = PrinterUtil.getSipAppContexts(_connection);
		
		Table table = new Table(_connection, contexts, "sip.applicationSessions");
		for (Header header : table.getHeaders())
		{
			int index = header.getName().indexOf("Sip application sessions");
			if (index != -1)
				header.setName(header.getName().substring(0, index));
		}
		add(new SetPrinter(table));
		
		table = new Table(_connection, contexts, "sip.applicationSessions.time");
		for (Header header : table.getHeaders())
		{
			int index = header.getName().indexOf("amount of time in seconds a Sip application session remained valid");
			if (index != -1)
				header.setName(header.getName().substring(0, index));
		}
		for (Row row : table)
		{
			for (Value value : row.getValues())
			{
				if (value.getValue() instanceof Double)
				{
					DecimalFormat format = new DecimalFormat();
					format.setMaximumFractionDigits(2);
					value.setValue(format.format(value.getValue()));
				}	
			}
		}
		add(new SetPrinter(table));
	}

	public void print(Writer out) throws Exception
	{
		super.print(out);
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

			if (_statisticGraph.isStarted())
			{
							
				out.write("<form method=\"get\" action=\"" + MenuPrinter.STATISTICS_SIP.getName() + "\">\n"
						+ "Statistic graph should show to the " 
						+ "<SELECT name=\"time\">");
				for (int i = 0; i < STATS_TIME_VALUE.length; i++)
				{
					out.write("<OPTION VALUE=\"" + STATS_TIME_VALUE[i] + "\"");
					if (_time == STATS_TIME_VALUE[i])
						out.write(" selected");
					out.write(">" + STATS_TIME_TITLE[i] + "</OPTION>\n");
				}
 
				out.write("</SELECT>"
						+ "<input type=\"submit\" name=\"submit\" value=\"change\"/></form>\n");

				printGraph(out, "Calls", "calls");
				printGraph(out, "JVM Memory", "memory");
				printGraph(out, "SIP messages", "messages");
				out.write("<br/>\n");
				
				out.write("<a href=\"" + MenuPrinter.STATISTICS_SIP.getName());
				out.write("?" + Parameters.ACTION + "=" + STOP_GRAPH + "\">");
				out.write("Stop statistics graph</a>");			
			}
			else
			{
				out.write("<a href=\"" + MenuPrinter.STATISTICS_SIP.getName());
				out.write("?" + Parameters.ACTION + "=" + START_GRAPH + "\">");
				out.write("Start statistics graph</a>");
			}
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
