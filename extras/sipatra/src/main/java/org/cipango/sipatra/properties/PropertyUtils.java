// ========================================================================
// Copyright 2003-2011 the original author or authors.
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
package org.cipango.sipatra.properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;

public class PropertyUtils
{
	private static final Logger _log = org.slf4j.LoggerFactory.getLogger(PropertyUtils.class);
			
	public static int getIntegerProperty(String name, int defaultValue, ServletContext context)
	{
		String property = System.getProperty(name);
		if(property == null || "".equals(property))
			property = context.getInitParameter(name);
		if(property == null || "".equals(property))
			return defaultValue;
		else
		{
			try
			{
				return Integer.valueOf(property);
			}
			catch (Exception e) 
			{
				_log.warn("Property: "+name+" is not an int. Default value is used: "+defaultValue);
				return defaultValue;
			}
		}
	}

	public static long getLongProperty(String name, long defaultValue, ServletContext context)
	{
		String property = System.getProperty(name);
		if(property == null || "".equals(property))
			property = context.getInitParameter(name);
		if(property == null || "".equals(property))
			return defaultValue;
		else
		{
			try
			{
				return Long.valueOf(property);
			}
			catch (Exception e) 
			{
				_log.warn("Property: "+name+" is not a long. Default value is used: "+defaultValue);
				return defaultValue;
			}
		}
	}

	public static String getStringProperty(String name, String defaultValue, ServletContext context)
	{
		String property = System.getProperty(name);
		if(property == null || "".equals(property))
			property = context.getInitParameter(name);
		if(property == null || "".equals(property))
			return defaultValue;
		else
			return property;
	}

	public static boolean getBooleanProperty(String name, boolean defaultValue, ServletContext context)
	{
		String property = System.getProperty(name);
		if(property == null || "".equals(property))
			property = context.getInitParameter(name);
		if(property == null || "".equals(property))
			return defaultValue;
		else
		{
			try
			{
				return Boolean.valueOf(property);
			}
			catch (Exception e) 
			{
				_log.warn("Property: "+name+" is not a boolean. Default value is used: "+defaultValue);
				return defaultValue;
			}
		}
	}
}
