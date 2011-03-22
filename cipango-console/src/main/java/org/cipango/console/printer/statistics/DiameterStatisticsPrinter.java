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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.printer.MenuPrinter;
import org.cipango.console.printer.generic.MultiplePrinter;
import org.cipango.console.printer.generic.PrinterUtil;
import org.cipango.console.printer.generic.PropertiesPrinter;
import org.cipango.console.printer.generic.SetPrinter;

public class DiameterStatisticsPrinter extends MultiplePrinter
{

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
			out.write(new Date((Long)  _connection.getAttribute(ConsoleFilter.DIAMETER_NODE, "statsStartedAt")).toString() + ".<br/>");
			out.write(PrinterUtil.getSetterLink("statsOn", "false", ConsoleFilter.DIAMETER_NODE,
					MenuPrinter.STATISTICS_DIAMETER, "Disable statistics"));
			out.write("&nbsp;&nbsp;&nbsp;");
			out.write(PrinterUtil.getActionLink("statsReset", ConsoleFilter.DIAMETER_NODE,
					MenuPrinter.STATISTICS_DIAMETER, "Reset statistics"));
		}
		else
		{
			out.write(PrinterUtil.getSetterLink("statsOn", "true", ConsoleFilter.DIAMETER_NODE,
					MenuPrinter.STATISTICS_DIAMETER, "Enable statistics"));
		}

	}
}
