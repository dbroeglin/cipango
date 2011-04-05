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

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.cipango.console.Action;
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
	public static final Action ENABLE_STATS = Action.add(new Action(MenuPrinter.STATISTICS_HTTP, "enable-statistics")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			ObjectName[] connectors = (ObjectName[]) getConnection().getAttribute(ConsoleFilter.SERVER, "connectors");
			for (ObjectName objectName : connectors)
				getConnection().setAttribute(objectName, new Attribute("statsOn", Boolean.TRUE));
		}	
	});
	
	public static final Action DISABLE_STATS = Action.add(new Action(MenuPrinter.STATISTICS_HTTP, "disable-statistics")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			ObjectName[] connectors = (ObjectName[]) getConnection().getAttribute(ConsoleFilter.SERVER, "connectors");
			for (ObjectName objectName : connectors)
				getConnection().setAttribute(objectName, new Attribute("statsOn", Boolean.FALSE));
		}	
	});
	
	public static final Action RESET_STATS = Action.add(new Action(MenuPrinter.STATISTICS_HTTP, "reset-statistics")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			ObjectName[] connectors = (ObjectName[]) getConnection().getAttribute(ConsoleFilter.SERVER, "connectors");
			for (ObjectName objectName : connectors)
				getConnection().invoke(objectName, "statsReset", null, null);
		}	
	});
	
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
						DISABLE_STATS.print(out);
						out.write("&nbsp;&nbsp;&nbsp;");
						RESET_STATS.print(out);
					}
					else
					{
						ENABLE_STATS.print(out);
					}
				}
			});
		}
	}

}
