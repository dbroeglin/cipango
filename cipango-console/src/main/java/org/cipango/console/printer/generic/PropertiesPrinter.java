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
package org.cipango.console.printer.generic;

import java.io.Writer;
import java.util.Iterator;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.Property;
import org.cipango.console.PropertyList;

public class PropertiesPrinter implements HtmlPrinter
{
	private PropertyList _properties;
	
	public PropertiesPrinter(PropertyList properties)
	{
		_properties = properties;
	}
	
	public PropertiesPrinter(ObjectName objectName, String propertyName,
			MBeanServerConnection connection) throws Exception
	{
		this(new PropertyList(connection, objectName, propertyName));
	}

	public void print(Writer out) throws Exception
	{
		out.write("<h2>" + _properties.getTitle() + "</h2>\n");

		boolean hasNotes = _properties.hasNotes();
		out.write(hasNotes ? PrinterUtil.TABLE_HEADER : PrinterUtil.TABLE_HEADER_NO_NOTES);
		Iterator<Property> it = _properties.iterator();
		boolean odd = true;
		while (it.hasNext())
		{
			Property property = (Property) it.next();

			out.write("<tr class=\"" + (odd ? "odd" : "even") + "\">");
			out.write("<td>" + property.getName() + "</td><td>");
			out.write((property.getValue() == null ? "" : property.getValue()) + "</td>");
			if (hasNotes)
				out.write("<td>" + (property.getNote() == null ? "&nbsp;" : property.getNote()) + "</td></tr>\n");
			odd = !odd;
		}
		out.write("</table></div>\n");
	}
	
	
}
