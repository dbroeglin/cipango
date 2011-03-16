// ========================================================================
// Copyright 2011 NEXCOM Systems
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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import javax.management.MBeanServerConnection;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Property;
import org.cipango.console.PropertyList;
import org.cipango.console.Row;
import org.cipango.console.Row.Header;
import org.cipango.console.Row.Value;
import org.cipango.console.Table;
import org.cipango.console.printer.generic.HtmlPrinter;
import org.cipango.console.printer.generic.MultiplePrinter;
import org.cipango.console.printer.generic.PropertiesPrinter;
import org.cipango.console.printer.generic.SetPrinter;
import org.cipango.console.printer.statistics.DiameterStatisticsPrinter;
import org.cipango.console.printer.statistics.HttpStatisticsPrinter;
import org.cipango.console.printer.statistics.SipStatisticPrinter;


@SuppressWarnings("rawtypes")
public class DumpPrinter extends MultiplePrinter
{
	private static final int TITLE_SIZE = 100;
	
	public DumpPrinter(final MBeanServerConnection connection, ConsoleFilter consoleFilter) throws IOException
	{
		try
		{
			// About
			add(new PropertiesTextPrinter(consoleFilter.getVersion()));
			add(new PropertiesTextPrinter(consoleFilter.getEnvironment()));
			// Stats
			addPrinter(new SipStatisticPrinter(connection, null), "SIP statistics");
			
			if (MenuPrinter.STATISTICS_DIAMETER.isEnabled(connection))
				addPrinter(new DiameterStatisticsPrinter(connection), "Diameter statistics");
			
			addPrinter(new HttpStatisticsPrinter(connection), "HTTP statistics");
			
			add(new HtmlPrinter()
			{
				public void print(Writer out) throws Exception
				{
					appendTitle(out, "Server");
					out.append((String) connection.invoke(ConsoleFilter.SERVER, "dump", null, new String[0]));
				}
			});
			
			add(new HtmlPrinter()
			{
				@SuppressWarnings("unchecked")
				public void print(Writer out) throws Exception
				{
					appendTitle(out, "System properties");
					Properties systemProperties = System.getProperties();
					Iterator it = new TreeSet(systemProperties.keySet()).iterator();
					while (it.hasNext())
					{
						Object key = it.next();
						out.append(key.toString()).append(": ").append(String.valueOf(systemProperties.get(key))).append("\n");
					}
				}
			});
		}
		catch (Exception e) 
		{
			add(new ErrorTextPrinter(e));
		}
	}
	
	private void addPrinter(MultiplePrinter multiplePrinter, String titlePrefix) throws Exception
	{
		for (HtmlPrinter printer : multiplePrinter)
		{
			if (printer instanceof PropertiesPrinter)
			{
				PropertyList propertyList = ((PropertiesPrinter) printer).getProperties();
				if (titlePrefix != null)
					propertyList.setTitle(titlePrefix + ": " + propertyList.getTitle());
				add(new PropertiesTextPrinter(propertyList));
			}
			if (printer instanceof SetPrinter)
			{
				Table table = ((SetPrinter) printer).getTable();
				if (titlePrefix != null)
					table.setTitle(titlePrefix + ": " + table.getTitle());
				add(new SetTextPrinter(table));
			}
		}
	}
	
	@Override
	public void print(Writer out) throws IOException
	{
		try 
		{		
			super.print(out);
		}
		catch (Throwable e) 
		{
			new ErrorTextPrinter(e).print(out);
		}
	}
	
	public static void appendTitle(Writer out, String title) throws IOException
	{
		out.append("\n\n");
		for (int i = 0; i < TITLE_SIZE; i++)
			out.append('=');
		out.append("\n|");
		int i = 1;
		for (; i < ((TITLE_SIZE - title.length()) /2); i++)
			out.append(' ');
		out.append(title);
		i +=title.length();
		for (; i < TITLE_SIZE - 1; i++)
			out.append(' ');
		out.append("|\n");
		for (int j = 0; j < TITLE_SIZE; j++)
			out.append('=');
		out.append("\n");
	}
	
	
	static class PropertiesTextPrinter implements HtmlPrinter
	{
		private PropertyList _properties;
		
		public PropertiesTextPrinter(PropertyList properties)
		{
			_properties = properties;
		}
		
		public void print(Writer out) throws Exception
		{
			appendTitle(out, _properties.getTitle());

			Iterator<Property> it = _properties.iterator();
			while (it.hasNext())
			{
				Property property = (Property) it.next();
				out.write(property.getName().trim() + ": " + property.getValue() + "\n");
			}
		}
	}
	
	class SetTextPrinter implements HtmlPrinter
	{
		private Table _table;
		private static final int CELL_SIZE = 25;
		
		public SetTextPrinter(Table table)
		{
			_table = table;
		}
		
		public void print(Writer out) throws Exception
		{
			appendTitle(out, _table.getTitle());
			for (Header header : _table.getHeaders())
			{
				
				append(out, header.getName());
			}
			out.write("|\n");

			for (Row row : _table)
			{
				for (Value value : row.getValues())
				{
					append(out, value.getValue() == null ? "" : value.getValue().toString());
				}
				out.write("|\n");
			}
		}
		
		private void append(Writer out, String cellContent) throws IOException
		{
			out.append('|');
			if (cellContent.length() >= CELL_SIZE)
				out.append(cellContent.substring(0, CELL_SIZE - 1));
			else
			{
				int i = 1;
				for (; i < ((CELL_SIZE - cellContent.length()) /2); i++)
					out.append(' ');
				out.append(cellContent);
				i +=cellContent.length();
				for (; i < CELL_SIZE; i++)
					out.append(' ');
			}
			
		}
	}
	
	static class ErrorTextPrinter implements HtmlPrinter
	{
		private Throwable _t;

		public ErrorTextPrinter(Throwable t)
		{

			_t = t;
		}

		public void print(Writer out) throws IOException
		{
			appendTitle(out, _t.getClass().getSimpleName());
			_t.printStackTrace(new PrintWriter(out));

		}
	}
}
