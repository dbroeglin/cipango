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

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.Page;
import org.cipango.console.PageImpl;


public abstract class AbstractJmxPrinter implements HtmlPrinter
{

	private String[] _params;
	private String[] _operations;
	private String _page;
	private MBeanServerConnection _connection;
	private String _title;
	private String _propertyName;
	
	public AbstractJmxPrinter(String propertyName, MBeanServerConnection connection)
	{
		_params = PrinterUtil.getParams(propertyName);
		if (_params == null)
			throw new IllegalArgumentException("Could not found parameters for property " + propertyName);
		_operations = PrinterUtil.getOperations(propertyName);
		_page = PrinterUtil.getPage(propertyName);
		_connection = connection;
		_title = PrinterUtil.getTitle(propertyName);
		_propertyName = propertyName;
	}

	protected MBeanServerConnection getMbsc()
	{
		return _connection;
	}

	protected String[] getOperations()
	{
		return _operations;
	}

	protected String[] getParams()
	{
		return _params;
	}
	
	protected void removeParam(String name)
	{
		List<String> newParams = new ArrayList<String>();
		for (int i = 0; i < _params.length; i++)
		{
			if (!_params[i].equals(name))
				newParams.add(_params[i]);
		}
		_params = newParams.toArray(new String[0]);
	}

	protected String getTitle()
	{
		return _title;
	}

	public void setTitle(String title)
	{
		this._title = title;
	}

	protected Page getPage()
	{
		return new PageImpl(_page, "");
	}
	
	public String getParamFullDescription(String param)
	{
		return PrinterUtil.getNote(_propertyName, param);
	}
	
	public boolean hasNotes()
	{
		return PrinterUtil.hasNotes(_propertyName);
	}

	protected String getActionLink(String action, ObjectName objectName, String displayText)
	{
		return PrinterUtil.getActionLink(action, objectName, getPage(), displayText);
	}

	protected String getActionLinkWithConfirm(String action, ObjectName objectName,
			String displayText)
	{
		return PrinterUtil.getActionLinkWithConfirm(action, objectName, _connection, getPage(),
				displayText);
	}

	protected String getSetterLink(String name, Object value, ObjectName objectName,
			String displayText)
	{
		return PrinterUtil.getSetterLink(name, value, objectName, getPage(),
				displayText);
	}

}
