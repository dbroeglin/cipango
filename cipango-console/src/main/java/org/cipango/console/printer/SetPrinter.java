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
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;


public class SetPrinter extends AbstractJmxPrinter implements HtmlPrinter
{
	private static Logger __logger = Log.getLogger("console");
	private ObjectName[] _objectNames;
	
	public SetPrinter(Set<ObjectName> objectNameSet, String propertyName, MBeanServerConnection connection)
	{
		super(propertyName, connection);

		_objectNames = new ObjectName[objectNameSet.size()];
		Iterator<ObjectName> it = objectNameSet.iterator();
		for (int i = 0; i < _objectNames.length; i++)
			_objectNames[i] = it.next();
	}

	public SetPrinter(ObjectName[] objectNames, String propertyName, MBeanServerConnection connection)
	{
		super(propertyName, connection);
		if (objectNames == null)
			_objectNames = new ObjectName[0];
		else
			_objectNames = objectNames;
	}

	public void print(Writer out) throws Exception
	{

		boolean first = true;

		// Case no MBean registered
		if (_objectNames == null || _objectNames.length == 0)
			printSimpleHeaders(out);

		for (int i = 0; i < _objectNames.length; i++)
		{
			if (first)
			{
				MBeanInfo info = getMbsc().getMBeanInfo(_objectNames[i]);
				printHeaders(info, out);
				first = false;
			}
			out.write("<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\">");
			for (int j = 0; j < getParams().length; j++)
			{
				if (getParams()[j] != null && !"".equals(getParams()[j].trim()))
				{
					try
					{
						Object value = getMbsc().getAttribute(_objectNames[i], getParams()[j]);
						printValue(value, getParams()[j], i, out);
					}
					catch (AttributeNotFoundException e)
					{
						__logger.warn("Could not found attribute {} on object name {}", getParams()[j], _objectNames[i]);
					}
				}
			}
			if (getOperations() != null)
			{
				out.write("<td>");
				for (int j = 0; j < getOperations().length; j++)
				{
					out.write(getActionLinkWithConfirm(getOperations()[j], _objectNames[i], null));
					if (j < getOperations().length)
					{
						out.write("&nbsp;&nbsp;&nbsp;");
					}
				}
				out.write("</td>");
			}
			printRowPostfix(out, i);
			out.write("</tr>");
		}
		printLastRow(out);
		out.write("</table></div>");
	}

	protected void printValue(Object value, String name, int i, Writer out) throws IOException
	{
		out.write("<td>" + (value == null ? "" : value) + "</td>");
	}

	private void printHeaders(MBeanInfo info, Writer out) throws IOException
	{
		out.write("<h2>" + (getTitle() != null ? getTitle() : info.getDescription()) + "</h2>");
		out.write("<div class=\"data\"><table class=\"table_hover\">");
		out.write("<tr>");
		MBeanAttributeInfo[] attrInfo = info.getAttributes();
		String[] params = getParams();
		for (int j = 0; j < params.length; j++)
		{
			boolean found = false;
			for (int k = 0; k < attrInfo.length; k++)
			{
				if (attrInfo[k].getName().equals(params[j]))
				{
					out.write("<th>" + attrInfo[k].getDescription());
					String description = getParamFullDescription(params[j]);
					if (description != null && !description.trim().equals(""))
					{
						out.write("&nbsp;<img src=\"images/question.gif\" title=\"" + description
								+ "\"/>");
					}
					out.write("</th>");
					found = true;
					break;
				}
			}
			if (!found)
			{
				__logger.info("Could not display param {} as it is not exposed by JMX", params[j], null);
				removeParam(params[j]);
			}
				
		}
		if (getOperations() != null)
		{
			out.write("<th>Operations</th>");
		}
		printHeaderPostfix(out);
		out.write("</tr>");
	}

	private void printSimpleHeaders(Writer out) throws IOException
	{
		out.write("<h2>" + getTitle() + "</h2>");
		out.write("<div class=\"data\"><table class=\"main\">");
		out.write("<tr>");
		for (int j = 0; j < getParams().length; j++)
		{
			out.write("<th>" + getParams()[j]);
			String description = getParamFullDescription(getParams()[j]);
			if (description != null && !description.trim().equals(""))
			{
				out.write("&nbsp;<img src=\"images/question.gif\" title=\"" + description + "\"/>");
			}
			out.write("</th>");
		}
		if (getOperations() != null)
		{
			out.write("<th>Operations</th>");
		}
		printHeaderPostfix(out);
		out.write("</tr>");
	}

	protected void printLastRow(Writer out) throws IOException
	{

	}

	protected void printRowPostfix(Writer out, int i) throws IOException
	{

	}

	protected void printHeaderPostfix(Writer out) throws IOException
	{

	}

	protected ObjectName[] getObjectNames()
	{
		return _objectNames;
	}

}
