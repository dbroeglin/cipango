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

import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.ObjectNameFactory;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ObjectListPrinters implements HtmlPrinter
{
	private static Logger __logger = Log.getLogger("console");
	private MBeanServerConnection _connection;
	private String _prefix;

	public ObjectListPrinters(MBeanServerConnection connection, String prefix,
			boolean readOnly)
	{
		_connection = connection;
		_prefix = prefix;
	}

	public void print(Writer out) throws Exception
	{
		out.write("<h2>" + (PrinterUtil.getTitle(_prefix)) + "</h2>\n");

		out.write(PrinterUtil.TABLE_HEADER);
		HashMap<String, ObjectName> objectNames = getObjectNames();
		Iterator<String> it = objectNames.keySet().iterator();
		int i = 0;
		while (it.hasNext())
		{
			String key = it.next();
			print(out, objectNames.get(key), PrinterUtil.getValueSplit(key), i);
		}
		out.write("</table></div>\n");

	}

	private void print(Writer out, ObjectName objectName, String[] params, int i)
			throws Exception
	{
		if (!_connection.isRegistered(objectName))
		{
			out.write("Could not get values for parameters: ");
			for (int j = 0; j < params.length; j++)
			{
				out.write(params[j]);
				if (j + 1 < params.length)
				{
					out.write(", ");
				}
			}
			out.write(" as there are not registered in JMX\n");
			return;
		}

		MBeanInfo info = _connection.getMBeanInfo(objectName);

		MBeanAttributeInfo[] attrInfo = info.getAttributes();
		for (int j = 0; j < params.length; j++)
		{
			int k;
			for (k = 0; k < attrInfo.length; k++)
			{
				if (attrInfo[k].getName().equals(params[j]))
				{
					break;
				}
			}
			if (k >= attrInfo.length)
			{
				__logger.warn("Could not found attribute: {} in {}", params[j], objectName);
			}
			else
			{
				Property property = new Property(attrInfo[k], _connection.getAttribute(objectName, params[j]),
						i, PrinterUtil.getNote(_prefix, params[j]));
				property.print(out);
			}
		}
	}

	public HashMap<String, ObjectName> getObjectNames()
	{
		HashMap<String, ObjectName> objectNames = new HashMap<String, ObjectName>();
		Enumeration<String> enumeration = PrinterUtil.PARAMETERS.getKeys();
		while (enumeration.hasMoreElements())
		{
			String key = (String) enumeration.nextElement();
			if (key.startsWith(_prefix)
					&& key.endsWith(PrinterUtil.PARAMS_POSTFIX))
			{
				String className = key.substring(_prefix.length() + 1);
				className = className.substring(0, className.lastIndexOf('.')).toLowerCase();
				Hashtable<String, String> table = new Hashtable<String, String>();
				table.put("type", className.substring(className.lastIndexOf('.') + 1));
				table.put("id", "0");
				ObjectName objectName = ObjectNameFactory.create(
						className.substring(0, className.lastIndexOf('.')), table);
				objectNames.put(key, objectName);
			}
		}
		return objectNames;
	}

}
