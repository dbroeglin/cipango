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
import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ObjectPrinter extends AbstractJmxPrinter implements HtmlPrinter
{
	private static Logger __logger = Log.getLogger("console");
	private List<Property> _properties;
	private ObjectName _objectName;

	public ObjectPrinter(ObjectName objectName, String propertyName,
			MBeanServerConnection connection)
	{
		this(objectName, propertyName, connection, true);
	}

	public ObjectPrinter(ObjectName objectName, String propertyName,
			MBeanServerConnection connection, boolean readOnly)
	{
		super(propertyName, connection);
		_objectName = objectName;
	}

	public void setProperties(List<Property> properties)
	{
		_properties = properties;
	}

	public void print(Writer out) throws Exception
	{
		if (!getMbsc().isRegistered(_objectName))
		{
			out.write("Could not get values for parameters: ");
			String[] params = getParams();
			for (int i = 0; i < params.length; i++)
			{
				out.write(params[i]);
				if (i + 1 < params.length)
				{
					out.write(", ");
				}
			}
			out.write(" as there are not registered in JMX");
			return;
		}
		MBeanInfo info = getMbsc().getMBeanInfo(_objectName);
		out.write("<h2>"
				+ (getTitle() != null ? getTitle() : info.getDescription())
				+ "</h2>");

		boolean hasNotes = hasNotes();
		
		out.write(hasNotes ? PrinterUtil.TABLE_HEADER : PrinterUtil.TABLE_HEADER_NO_NOTES);
		int i = 0;
		MBeanAttributeInfo[] attrInfo = info.getAttributes();
		for (int j = 0; j < getParams().length; j++)
		{
			if (getParams()[j] == null ||  "".equals(getParams()[j].trim()))
				continue;
			
			int k;
			for (k = 0; k < attrInfo.length; k++)
			{
				if (attrInfo[k].getName().equals(getParams()[j]))
				{
					break;
				}
			}
			if (k >= attrInfo.length)
			{
				__logger.warn("Could not found attribute: {} in {}", getParams()[j], _objectName);
			}
			else
			{
				Property property = new Property(attrInfo[k], getMbsc().getAttribute(_objectName, getParams()[j]), i++, getParamFullDescription(getParams()[j]));
				property.setHasNotes(hasNotes);
				customizeProperty(property);
				property.print(out);
			}
		}
		if (_properties != null)
		{
			Iterator<Property> it = _properties.iterator();
			while (it.hasNext())
			{
				Property property = it.next(); 
				property.setHasNotes(hasNotes);
				property.setOdd((i++ % 2) == 0);
				property.print(out);
			}
		}
		out.write("</table></div>");
	}
	
	protected void customizeProperty(Property property)
	{
		
	}
		
	protected ObjectName getObjectName()
	{
		return _objectName;
	}

	protected String getActionLink(String action, String displayText)
	{
		return getActionLink(action, _objectName, displayText);
	}

	protected String getSetterLink(String name, Object value, String displayText)
	{
		return getSetterLink(name, value, _objectName, displayText);
	}

}
