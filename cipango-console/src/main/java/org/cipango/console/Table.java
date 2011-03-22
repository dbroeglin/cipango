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
package org.cipango.console;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.Row.Header;
import org.cipango.console.Row.Value;
import org.cipango.console.printer.generic.PrinterUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class Table extends AbstractList<Row>
{
	private static Logger __logger = Log.getLogger("console");
	private List<Row> _rows = new ArrayList<Row>();
	private List<Header> _headers;
	private String _title;
	
	public Table()
	{
	}
	
	public Table(MBeanServerConnection connection, Set<ObjectName> objectNameSet, String propertyName) throws Exception
	{
		List<Header> headers = getHeaders(connection, objectNameSet, propertyName);
		setTitle(PrinterUtil.getTitle(propertyName));
		setHeaders(headers);
		
		if (objectNameSet != null)
		{
			for (ObjectName objectName : objectNameSet)
			{
				Row row = new Row();
				List<Value> values = row.getValues();
				for (Header header : headers)
					values.add(new Value(connection.getAttribute(objectName, header.getSimpleName()), header));
				row.setObjectName(objectName);
				add(row);
			}
		}
	}
	
	public Table(MBeanServerConnection connection, ObjectName[] objectNames, String propertyName) throws Exception
	{
		this(connection, new HashSet<ObjectName>(Arrays.asList(objectNames)), propertyName);
	}
	
	private List<Header> getHeaders(MBeanServerConnection connection, Set<ObjectName> objectNameSet, String propertyName) throws Exception
	{
		String[] params = PrinterUtil.getParams(propertyName);
		List<Header> headers = new ArrayList<Row.Header>();
		if (objectNameSet == null || objectNameSet.isEmpty())
		{
			for (String param : params)
				headers.add(new Header(param, param));
		}
		else
		{
			Iterator<ObjectName> it = objectNameSet.iterator();
			MBeanInfo info = connection.getMBeanInfo(it.next());
			MBeanAttributeInfo[] attrInfos = info.getAttributes();
			for (String param : params)
			{
				boolean found = false;
				for (MBeanAttributeInfo attrInfo : attrInfos)
				{
					if (attrInfo.getName().equals(param))
					{
						headers.add(new Header(param, attrInfo.getDescription(), PrinterUtil.getNote(propertyName, param)));
						found = true;
						break;
					}
				}
				if (!found)
					__logger.info("Could not display param {} as it is not exposed by JMX", param, null);				
			}
		}
		return headers;
	}
	
	public String getTitle()
	{
		return _title;
	}

	public void setTitle(String title)
	{
		_title = title;
	}
	
	public boolean hasOperations()
	{
		if (_rows.isEmpty())
			return false;
		return _rows.get(0).getOperations() != null;
	}

	public Row get(int index)
	{
		return _rows.get(index);
	}

	@Override
	public int size()
	{
		return _rows.size();
	}
	
	@Override
	public void add(int index, Row row)
	{
		_rows.add(index, row);
	}

	public List<Header> getHeaders()
	{
		return _headers;
	}

	public void setHeaders(List<Header> headers)
	{
		_headers = headers;
	}
	
}
