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
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Page;
import org.cipango.console.Parameters;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;


public class PrinterUtil
{
	private static Logger __logger = Log.getLogger("console");
	public static final String PARAMS_POSTFIX = ".params";
	public static final ResourceBundle PARAMETERS = ResourceBundle
			.getBundle("org.cipango.console.methods");
	public static final ResourceBundle DESCRIPTION = ResourceBundle
			.getBundle("org.cipango.console.description");
	
	public static final int getInt(String sValue, int defaultValue)
	{
		if (sValue == null)
			return defaultValue;
		try 
		{
			return Integer.parseInt(sValue);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}
	
	public static String[] getParams(String name)
	{
		return getValueSplit(name + PARAMS_POSTFIX);
	}

	public static String[] getValueSplit(String name)
	{
		try
		{
			return PARAMETERS.getString(name).split("\\p{Space}*,\\p{Space}*");
		}
		catch (MissingResourceException e)
		{
			return null;
		}
	}

	public static String getTitle(String name)
	{
		try
		{
			return PARAMETERS.getString(name + ".title").trim();
		}
		catch (MissingResourceException e)
		{
			return null;
		}
	}

	public static String getNote(String propertyName, String param)
	{
		try
		{
			return DESCRIPTION.getString(propertyName + ".params." + param).trim();
		}
		catch (MissingResourceException e)
		{
			return null;
		}
	}
		
	public static String getActionLink(String action, ObjectName objectName, Page page, String displayText)
	{
		StringBuffer sb = new StringBuffer(128);
		sb.append("<a href=\"").append(page.getName());
		sb.append("?" + Parameters.ACTION + "=").append(action);
		sb.append("&" + Parameters.OBJECT_NAME + "=").append(objectName);

		sb.append("\">");
		sb.append(displayText == null ? action : displayText);
		sb.append("</a>");
		return sb.toString();
	}
	

	public static String getActionLinkWithConfirm(String action, ObjectName objectName,
			MBeanServerConnection connection, Page page, String displayText)
	{
		String appName = null;
		try
		{
			appName = (String) connection.getAttribute(objectName, "contextPath");
		}
		catch (Exception e)
		{
			__logger.warn("Unable to get application name", e);
		}
		if (displayText == null)
			displayText = action;
		StringBuffer sb = new StringBuffer(128);
		sb.append("<INPUT TYPE=\"button\" value=\"").append(action).append("\" ");
		sb.append("onClick=\"confirmAction('").append(appName).append("','").append(page.getName());
		sb.append("?" + Parameters.ACTION + "=").append(action);
		sb.append("&" + Parameters.OBJECT_NAME + "=").append(objectName);
		sb.append("','").append(displayText);
		sb.append("')\">");
		return sb.toString();	
	}

	public static String getSetterLink(String name, Object value, ObjectName objectName,
			Page page, String displayText)
	{
		StringBuffer sb = new StringBuffer(128);
		sb.append("<a href=\"").append(page.getName());
		sb.append("?").append(name).append(Parameters.DOT_VALUE).append("=").append(value);
		sb.append("&").append(name).append(Parameters.DOT_OBJECT_NAME + "=").append(objectName);
		sb.append("&").append(Parameters.ACTIONS).append("=set");
		sb.append("\">");
		sb.append(displayText == null ? name : displayText);
		sb.append("</a>");
		return sb.toString();
	}

	public static ObjectName[] getContexts(MBeanServerConnection connection) throws Exception
	{
		return (ObjectName[]) connection.getAttribute(ConsoleFilter.SERVER,
				"contexts");
	}
	
	/**
	 * Returns an array with all contexts that extends SipAppContext.
	 */
	public static ObjectName[] getSipAppContexts(MBeanServerConnection connection) throws Exception
	{
		ObjectName[] objectNames = getContexts(connection);
		if (objectNames == null)
			return null;
		List<ObjectName> l = new ArrayList<ObjectName>();
		for (ObjectName objectName : objectNames)
		{
			MBeanInfo mBeanInfo = connection.getMBeanInfo(objectName);
			if (!"org.eclipse.jetty.webapp.WebAppContext".equals(mBeanInfo.getClassName())
					&& !"org.eclipse.jetty.server.handler.ContextHandler".equals(mBeanInfo.getClassName()))
				l.add(objectName);	
		}
		return l.toArray(new ObjectName[0]);
	}
	
	public static String getDuration(long millis)
	{
		long seconds = millis/1000;
		long minutes = seconds /60;
		long hours = minutes /60;
		long days = hours/24;
		StringBuilder sb = new StringBuilder();
		if (days >= 1)
		{
			sb.append(days).append(" day");
			if (days >= 1)
				sb.append('s');
			sb.append(", ");
		}
		if (hours  >= 1)
		{
			sb.append(hours % 24).append(" hour");
			if ((hours % 24) > 1)
				sb.append('s');
			sb.append(", ");
		}
		if (minutes >= 1)
		{
			sb.append(minutes % 60).append(" minute");
			if ((minutes % 60) > 1)
				sb.append('s');
			sb.append(", ");
		}
		sb.append(seconds % 60).append(" second");
		if ((seconds % 60) > 1)
			sb.append('s');
		return sb.toString();
	}
}
