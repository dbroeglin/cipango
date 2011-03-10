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

import java.io.IOException;
import java.io.Writer;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.printer.MenuPrinter;
import org.cipango.console.printer.generic.HtmlPrinter;
import org.cipango.console.printer.generic.MultiplePrinter;
import org.cipango.console.printer.generic.ObjectPrinter;
import org.cipango.console.printer.generic.PrinterUtil;
import org.cipango.console.printer.generic.Property;

public class HttpStatisticsPrinter extends MultiplePrinter
{
	private MBeanServerConnection _mbsc;
	
	public HttpStatisticsPrinter(MBeanServerConnection mbsc) throws JMException, IOException
	{
		_mbsc = mbsc;
		ObjectName[] connectors = (ObjectName[]) _mbsc.getAttribute(ConsoleFilter.SERVER, "connectors");
	
		for (int i = 0; i < connectors.length; i++)
		{
			final ObjectName objectName = connectors[i];
			addLast(new ObjectPrinter(objectName, "http.statistics", _mbsc)
			{
				@Override
				public String getTitle()
				{
					try
					{
						return "Connector: " + (String) _mbsc.getAttribute(getObjectName(), "name");
					}
					catch (Exception e)
					{
						return super.getTitle();
					}
				}
				
				@Override
				protected void customizeProperty(Property property)
				{
					String name = property.getName();
					int index = name.indexOf("since statsReset()");
					if (index != -1)
						property.setName(name.substring(0, index));
				}
			});
			
			addLast(new HtmlPrinter()
			{
				
				public void print(Writer out) throws Exception
				{
					Boolean on = (Boolean) _mbsc.getAttribute(objectName, "statsOn");
					if (on.booleanValue())
					{
						Long duration = (Long) _mbsc.getAttribute(objectName, "statsOnMs");
						out.write("Statisitics are enabled since " + PrinterUtil.getDuration(duration)
								+ ".<br/>");
						out.write(PrinterUtil.getSetterLink("statsOn", "false", objectName,
								MenuPrinter.STATISTICS_HTTP, "Disable statistics"));
						out.write("&nbsp;&nbsp;&nbsp;");
						out.write(PrinterUtil.getActionLink("statsReset", objectName,
								MenuPrinter.STATISTICS_HTTP, "Reset statistics"));
					}
					else
					{
						out.write(PrinterUtil.getSetterLink("statsOn", "true", objectName,
								MenuPrinter.STATISTICS_HTTP, "Enable statistics"));
					}
				}
			});
		}
	}

}
