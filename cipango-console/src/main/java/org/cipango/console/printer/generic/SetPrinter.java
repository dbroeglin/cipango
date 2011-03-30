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
package org.cipango.console.printer.generic;

import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.Row;
import org.cipango.console.Row.Header;
import org.cipango.console.Row.Value;
import org.cipango.console.Table;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;


public class SetPrinter implements HtmlPrinter
{
	protected static Logger __logger = Log.getLogger("console");
	private Table _table;
	
	public SetPrinter(Table table)
	{
		_table = table;
	}
	
	public SetPrinter(Set<ObjectName> objectNameSet, String propertyName, MBeanServerConnection connection) throws Exception
	{
		_table = new Table(connection, objectNameSet, propertyName);
	}

	public SetPrinter(ObjectName[] objectNames, String propertyName, MBeanServerConnection connection) throws Exception
	{
		_table = new Table(connection, objectNames, propertyName);
	}

	public void print(Writer out) throws Exception
	{
		out.write("<h2>" + _table.getTitle() + "</h2>\n");
		out.write("<div class=\"data\">\n<table");
		if (_table.hasOperations())
			out.write(" class=\"table_hover\"");
		out.write(">\n");
		out.write("\t<tr>");
		for (Header header : _table.getHeaders())
		{
			out.write("<th>" + header.getName());
			if (header.getNote() != null)
			{
				out.write("&nbsp;<img src=\"images/question.gif\" title=\"" + header.getNote()
						+ "\"/>");
			}
			out.write("</th>");
		}
		if (_table.hasOperations())
			out.write("<th>Operations</th>");
		out.write("\t</tr>\n");

		boolean odd = true;
		for (Row row : _table)
		{
			out.write("\t<tr class=\"" + (odd ? "odd" : "even") + "\">");
			odd = !odd;
			for (Value value : row.getValues())
			{
				out.write("<td>" + (value.getValue() == null ? "" : value.getValue()) + "</td>");
			}
			if (row.getOperations() != null)
			{
				out.write("<td>");
				Iterator<?> it = row.getOperations().iterator();
				while (it.hasNext())
				{
					Object o = it.next();
					if (o instanceof HtmlPrinter)
						((HtmlPrinter) o).print(out);
					else
						out.write(String.valueOf(o));
					if (it.hasNext())
						out.write("&nbsp;&nbsp;&nbsp;");
				}
				out.write("</td>");
			}
			out.write("\t</tr>\n");
		}
		out.write("</table>\n</div>\n");
	}

	public Table getTable()
	{
		return _table;
	}
}
