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
import java.util.Date;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.cipango.console.Action;
import org.cipango.console.ConsoleFilter;
import org.cipango.console.printer.MenuPrinter;
import org.cipango.console.printer.generic.MultiplePrinter;
import org.cipango.console.printer.generic.PrinterUtil;
import org.cipango.console.printer.generic.PropertiesPrinter;
import org.cipango.console.printer.generic.SetPrinter;

public class DiameterStatisticsPrinter extends MultiplePrinter
{
	public static final Action ENABLE_STATS = Action.add(new Action(MenuPrinter.STATISTICS_DIAMETER, "enable-statistics")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			getConnection().setAttribute(ConsoleFilter.DIAMETER_NODE, new Attribute("statsOn", Boolean.TRUE));
		}	
	});
	
	public static final Action DISABLE_STATS = Action.add(new Action(MenuPrinter.STATISTICS_DIAMETER, "disable-statistics")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			getConnection().setAttribute(ConsoleFilter.DIAMETER_NODE, new Attribute("statsOn", Boolean.FALSE));
		}	
	});
	
	public static final Action RESET_STATS = Action.add(new Action(MenuPrinter.STATISTICS_DIAMETER, "reset-statistics")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			getConnection().invoke(ConsoleFilter.DIAMETER_NODE, "statsReset", null, null);
		}	
	});
	
	private MBeanServerConnection _connection;
	
	public DiameterStatisticsPrinter(MBeanServerConnection connection) throws Exception
	{
		_connection = connection;
		ObjectName objectName = (ObjectName) _connection.getAttribute(ConsoleFilter.DIAMETER_NODE, "sessionManager");
		
		add(new PropertiesPrinter(objectName, "diameter.stats.sessions",  _connection));
		
		ObjectName[] transports = (ObjectName[]) _connection.getAttribute(
				ConsoleFilter.DIAMETER_NODE, "connectors");
		for (int i = 0; i < transports.length; i++)
			add(new PropertiesPrinter(transports[i], "diameter.stats.msg",  _connection));
		
		@SuppressWarnings("unchecked")
		Set<ObjectName> peers = _connection.queryNames(ConsoleFilter.DIAMETER_PEERS, null);
		add(new SetPrinter(peers, "diameter.stats.pending", _connection));
	}
	
	public void print(Writer out) throws Exception
	{
		super.print(out);
		printActions(out);
	}
	
	private void printActions(Writer out) throws Exception
	{
		out.write("<br/>\n");
		Boolean on = (Boolean) _connection.getAttribute(ConsoleFilter.DIAMETER_NODE, "statsOn");
		if (on.booleanValue())
		{
			out.write("Statistics are started since ");
			Long start = (Long)  _connection.getAttribute(ConsoleFilter.DIAMETER_NODE, "statsStartedAt");
			out.write(PrinterUtil.getDuration(System.currentTimeMillis() - start) + ".<br/>");
			DISABLE_STATS.print(out);
			out.write("&nbsp;&nbsp;&nbsp;");
			RESET_STATS.print(out);
		}
		else
		{
			ENABLE_STATS.print(out);
		}

	}
}
