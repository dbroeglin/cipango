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
import java.util.Iterator;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Property;
import org.cipango.console.PropertyList;
import org.cipango.console.printer.MenuPrinter;
import org.cipango.console.printer.generic.HtmlPrinter;
import org.cipango.console.printer.generic.MultiplePrinter;
import org.cipango.console.printer.generic.PrinterUtil;
import org.cipango.console.printer.generic.PropertiesPrinter;

public class HttpStatisticsPrinter extends MultiplePrinter
{
	private MBeanServerConnection _mbsc;
	
	public HttpStatisticsPrinter(MBeanServerConnection mbsc) throws Exception
	{
		_mbsc = mbsc;
		ObjectName[] connectors = (ObjectName[]) _mbsc.getAttribute(ConsoleFilter.SERVER, "connectors");
	
		for (int i = 0; i < connectors.length; i++)
		{
			final ObjectName objectName = connectors[i];
			
			PropertyList propertyList = new PropertyList(_mbsc, objectName, "http.statistics");
			propertyList.setTitle("Connector: " + (String) _mbsc.getAttribute(objectName, "name"));
			Iterator<Property> it = propertyList.iterator();
			while (it.hasNext())
			{
				Property property = (Property) it.next();
				String name = property.getName();
				int index = name.indexOf("since statsReset()");
				if (index != -1)
					property.setName(name.substring(0, index));
			}
			add(new PropertiesPrinter(propertyList));
			
			add(new HtmlPrinter()
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
