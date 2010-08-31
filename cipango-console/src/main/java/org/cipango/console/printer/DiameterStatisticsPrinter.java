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

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.cipango.console.ConsoleFilter;

public class DiameterStatisticsPrinter extends MultiplePrinter
{

	private MBeanServerConnection _connection;
	
	public DiameterStatisticsPrinter(MBeanServerConnection connection) throws IOException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException
	{
		_connection = connection;
		ObjectName objectName = (ObjectName) _connection.getAttribute(ConsoleFilter.DIAMETER_NODE, "sessionManager");
		
		addLast(new ObjectPrinter(objectName, "diameter.sessions",  _connection));
	}
	
	public void print(Writer out) throws Exception
	{
		super.print(out);
		printActions(out);
	}
	
	private void printActions(Writer out) throws Exception
	{
		ObjectName objectName = (ObjectName) _connection.getAttribute(ConsoleFilter.DIAMETER_NODE, "sessionManager");
		Boolean on = (Boolean) _connection.getAttribute(objectName, "statsOn");
		if (on.booleanValue())
		{
			out.write("Statistics are started since ");
			out.write(new Date((Long)  _connection.getAttribute(objectName, "statsStartedAt")).toString() + ".<br/>");
			out.write(PrinterUtil.getSetterLink("statsOn", "false", objectName,
					MenuPrinter.STATISTICS_DIAMETER, "Disable statistics"));
			out.write("&nbsp;&nbsp;&nbsp;");
			out.write(PrinterUtil.getActionLink("statsReset", objectName,
					MenuPrinter.STATISTICS_DIAMETER, "Reset statistics"));
		}
		else
		{
			out.write(PrinterUtil.getSetterLink("statsOn", "true", objectName,
					MenuPrinter.STATISTICS_DIAMETER, "Enable statistics"));
		}

	}
}
