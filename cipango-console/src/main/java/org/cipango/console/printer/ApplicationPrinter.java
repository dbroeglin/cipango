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
package org.cipango.console.printer;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cipango.console.Action;
import org.cipango.console.Attributes;
import org.cipango.console.Parameters;
import org.cipango.console.Row;
import org.cipango.console.Row.Header;
import org.cipango.console.Row.Value;
import org.cipango.console.Table;
import org.cipango.console.printer.generic.HtmlPrinter;
import org.cipango.console.printer.generic.MultiplePrinter;
import org.cipango.console.printer.generic.PrinterUtil;
import org.cipango.console.printer.generic.SetPrinter;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ApplicationPrinter extends MultiplePrinter
{
	private static Logger __logger = Log.getLogger("console");
	
	public static final Action START_APP = Action.add(new Action(MenuPrinter.MAPPINGS, "start")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			String name = request.getParameter(Parameters.OBJECT_NAME);
			ObjectName objectName = new ObjectName(name);
			if (isContext(objectName))
			{
				getConnection().invoke(objectName, "start", null, null);
				String path = (String) getConnection().getAttribute(objectName, "contextPath");
				request.getSession().setAttribute(Attributes.INFO, "Application with context path " + path
						+ " sucessfully started");
			}
			else
				request.getSession().setAttribute(Attributes.WARN, "Could not found application");
		}	
		
		private boolean isContext(ObjectName objectName) throws Exception
		{
			ObjectName[] contexts = PrinterUtil.getContexts(getConnection());
			for (ObjectName name : contexts)
				if (name.equals(objectName))
					return true;
			return false;
		}
	});
	
	public static final Action STOP_APP = Action.add(new Action(MenuPrinter.MAPPINGS, "stop")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			String name = request.getParameter(Parameters.OBJECT_NAME);
			ObjectName objectName = new ObjectName(name);
			if (isContext(objectName))
			{
				getConnection().invoke(objectName, "stop", null, null);
				String path = (String) getConnection().getAttribute(objectName, "contextPath");
				request.getSession().setAttribute(Attributes.INFO, "Application with context path " + path
						+ " sucessfully stopped");
			}
			else
				request.getSession().setAttribute(Attributes.WARN, "Could not found application");
		}	
		
		private boolean isContext(ObjectName objectName) throws Exception
		{
			ObjectName[] contexts = PrinterUtil.getContexts(getConnection());
			for (ObjectName name : contexts)
				if (name.equals(objectName))
					return true;
			return false;
		}
	});
	
	public static final Action DEPLOY_APP = Action.add(new Action(MenuPrinter.MAPPINGS, "deploy")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			if (ServletFileUpload.isMultipartContent(request))
			{
				FileItem item = null;
				try
				{
					ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
					@SuppressWarnings("unchecked")
					List<FileItem>  items = upload.parseRequest(request);
		
					Iterator<FileItem> it = items.iterator();
					while (it.hasNext())
					{
						item = it.next();
						if (!item.isFormField())
						{
							getConsoleFilter().getDeployer().deploy(item.getName(), item.get());
							request.getSession().setAttribute(Attributes.INFO,
									"Successful request to deploy " + item.getName());
							Log.info("User " + request.getUserPrincipal() 
									+ " requested to deploy application: " + item.getName());
						}
					}
				}
				catch (Throwable e)
				{
					__logger.warn("Unable to deploy " + item.getName(), e);
					request.getSession().setAttribute(Attributes.WARN, "Unable to deploy "
							+ item.getName() + ": " + e.getMessage());
				}
			}
		}	
		
		@Override
		public void print(Writer out) throws Exception
		{
			out.write("<h2>Upload a WAR file to install</h2>\n");
			out.write("<form action=\"" + getPage().getName() + "?" + Parameters.ACTION + "=" + getParameter() +"\" method=\"post\" enctype=\"multipart/form-data\" "
							+ " name=\"upload\" onsubmit=\"return check();\">\n");
			out.write("Select WAR file to upload:&nbsp;");
			out.write("<input name=\"installSar\" size=\"40\" type=\"file\">");
			out.write("<br/><input type=\"submit\" name=\"" + Parameters.ACTION
					+ "\" value=\"" + getDescription() + "\"/>");
			out.write("</form>\n");
		}
		
	});
	
	public static final Action UNDEPLOY_APP = Action.add(new Action(MenuPrinter.MAPPINGS, "undeploy")
	{
		@Override
		public void doProcess(HttpServletRequest request) throws Exception
		{
			String name = request.getParameter(Parameters.OBJECT_NAME);
			ObjectName objectName = new ObjectName(name);
			if (isContext(objectName))
			{
				getConsoleFilter().getDeployer().undeploy(objectName);	
				String path = (String) getConnection().getAttribute(objectName, "contextPath");
				request.getSession().setAttribute(Attributes.INFO, "Successfull request to undeploy application " + path);
				Log.info("User " + request.getUserPrincipal() 
						+ " requested to undeploy application " + path);
			}
			else
				request.getSession().setAttribute(Attributes.WARN, "Could not found application");
		}	
		
		private boolean isContext(ObjectName objectName) throws Exception
		{
			ObjectName[] contexts = PrinterUtil.getContexts(getConnection());
			for (ObjectName name : contexts)
				if (name.equals(objectName))
					return true;
			return false;
		}
	});
	
	
	public ApplicationPrinter(MBeanServerConnection connection) throws Exception
	{
		ObjectName[] sipContexts = PrinterUtil.getSipAppContexts(connection);
		
		Table contextsTable = new Table(connection, sipContexts, "appContexts");

		add(new SetPrinter(contextsTable));
		
		ObjectName[] otherContexts = PrinterUtil.getNonSipAppContexts(connection);
		if (otherContexts != null && otherContexts.length > 0)
		{
			Table otherContextsTable = new Table(connection, otherContexts, "otherContexts");
			for (Row row : otherContextsTable)
			{
				int index = 0;
				for (Header header : contextsTable.getHeaders())
				{
					Value value = row.get(header);
					if (value == null)
						row.getValues().add(index, new Value("N/A", header));
					index++;
				}
				contextsTable.add(row);
			}
		}
		
		for (Row row : contextsTable)
		{
			boolean running = (Boolean) connection.getAttribute(row.getObjectName(), "running");
			if (running)
				row.addOperation(new AppActionPrinter(STOP_APP, row.getObjectName()));
			else
				row.addOperation(new AppActionPrinter(START_APP, row.getObjectName()));
			
			row.addOperation(new AppActionPrinter(UNDEPLOY_APP, row.getObjectName()));
		}
				
		add(new ServletMappingPrinter(sipContexts, connection));
		add(DEPLOY_APP);
	}
	
	public static class AppActionPrinter implements HtmlPrinter
	{
		private Action _action;
		private ObjectName _objectName;
		
		public AppActionPrinter(Action action, ObjectName objectName)
		{
			_action = action;
			_objectName = objectName;
		}
		
		public void print(Writer out) throws Exception
		{
			String appName = (String) _action.getConnection().getAttribute(_objectName, "contextPath");

			out.append("<INPUT TYPE=\"button\" value=\"" + _action.getDescription() + "\" ");
			out.append("onClick=\"confirmAction('").append(appName).append("','").append(_action.getPage().getName());
			out.append("?" + Parameters.ACTION + "=").append(_action.getParameter());
			out.append("&" + Parameters.OBJECT_NAME + "=" + _objectName);
			out.append("','").append(_action.getDescription());
			out.append("')\">");
		}
		
	}
}
