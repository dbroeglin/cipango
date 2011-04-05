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

import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.http.HttpServletRequest;

import org.cipango.console.printer.generic.HtmlPrinter;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;


public abstract class Action implements HtmlPrinter
{
	private static Logger __logger = Log.getLogger("console");
	
	public static final List<Action> ACTIONS = new ArrayList<Action>();
		
	public static Action add(Action action)
	{
		if (!ACTIONS.contains(action))
		{
			ACTIONS.add(action);
			//__logger.debug("Added action: " + action);
		}
		return action;
	}
			
	private ConsoleFilter _consoleFilter;
	private String _parameter;
	private Page _page;
		
	public static void load(Class<?> clazz) 
	{
		Field [] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) 
		{
			Field field = fields[i];
			if (((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) 
			{
				try 
				{
					if (Action.class.isAssignableFrom(field.getType())) 
					{
			
						Action action= (Action) field.get(null);
						//__logger.debug("Loaded action: " + action);
						add(action);
					}
				} 
				catch (Exception e) 
				{
					Log.warn(e);
				}
			}
		}
	}
		
	public Action(Page page, String parameter)
	{
		_parameter = parameter;
		_page = page;
	}
	
	public final void process(HttpServletRequest request)
	{
		try
		{
			doProcess(request);
			if (request.getSession().getAttribute(Attributes.INFO) == null
					&& request.getSession().getAttribute(Attributes.WARN) == null)
				request.getSession().setAttribute(Attributes.INFO, "Action " + getDescription() + " successful");
		}
		catch (ReflectionException e)
		{
			Throwable cause = e.getCause();
			request.getSession().setAttribute(Attributes.WARN, "Unable to "
					+ getDescription() + ": " + cause);
		}
		catch (Throwable e)
		{
			__logger.warn(e.getMessage(), e);
			request.getSession().setAttribute(Attributes.WARN, "Unable to "
					+ getDescription() + ": " + e);
		}
	}
	
	protected abstract void doProcess(HttpServletRequest request) throws Exception;

	public MBeanServerConnection getConnection()
	{
		return _consoleFilter.getMbsc();
	}
	
	public ConsoleFilter getConsoleFilter()
	{
		return _consoleFilter;
	}
	
	public StatisticGraph getStatisticGraph()
	{
		return _consoleFilter.getStatisticGraph();
	}

	public void setConsoleFilter(ConsoleFilter consoleFilter)
	{
		_consoleFilter = consoleFilter;
	}
		
	public Page getPage()
	{
		return _page;
	}

	public String getParameter()
	{
		return _parameter;
	}
	
	public void print(Writer out, Map<String, Object> attributes) throws Exception
	{
		print(out);
	}
	
	public void print(Writer out) throws Exception
	{
		out.write("<a href=\"" + getPage().getName());
		out.write("?" + Parameters.ACTION + "=" + getParameter() + "\">");
		out.write(getDescription());
		out.write("</a>");
	}
	
	public String getDescription()
	{
		String description = _parameter.replace('-', ' ');
		return description.substring(0,1).toUpperCase() + description.substring(1);
	}
	
	@Override
	public String toString()
	{
		return "Action: " + getParameter();
	}
	
	public static class StartAction extends Action
	{
		private ObjectName _objectName;
		
		public StartAction(Page page, String parameter, ObjectName objectName)
		{
			super(page, parameter);
			_objectName = objectName;
		}

		@Override
		protected void doProcess(HttpServletRequest request) throws Exception
		{
			getConnection().invoke(_objectName, "start", null, null);
		}	
	}
	
	public static class StopAction extends Action
	{
		private ObjectName _objectName;
		
		public StopAction(Page page, String parameter, ObjectName objectName)
		{
			super(page, parameter);
			_objectName = objectName;
		}

		@Override
		protected void doProcess(HttpServletRequest request) throws Exception
		{
			getConnection().invoke(_objectName, "stop", null, null);
		}	
	}
	
}


