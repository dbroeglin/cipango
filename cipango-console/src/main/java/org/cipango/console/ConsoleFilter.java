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
package org.cipango.console;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.Principal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cipango.console.Row.Header;
import org.cipango.console.Row.Value;
import org.cipango.console.printer.DarPrinter;
import org.cipango.console.printer.DumpPrinter;
import org.cipango.console.printer.MenuPrinter;
import org.cipango.console.printer.OamPrinter;
import org.cipango.console.printer.ServletMappingPrinter;
import org.cipango.console.printer.SystemPropertiesPrinter;
import org.cipango.console.printer.UploadWarPrinter;
import org.cipango.console.printer.generic.ErrorPrinter;
import org.cipango.console.printer.generic.HtmlPrinter;
import org.cipango.console.printer.generic.MultiplePrinter;
import org.cipango.console.printer.generic.PrinterUtil;
import org.cipango.console.printer.generic.PropertiesPrinter;
import org.cipango.console.printer.generic.SetPrinter;
import org.cipango.console.printer.logs.AbstractLogPrinter.Output;
import org.cipango.console.printer.logs.CallsPrinter;
import org.cipango.console.printer.logs.DiameterLogPrinter;
import org.cipango.console.printer.logs.FileLogPrinter;
import org.cipango.console.printer.logs.SipLogPrinter;
import org.cipango.console.printer.statistics.DiameterStatisticsPrinter;
import org.cipango.console.printer.statistics.HttpStatisticsPrinter;
import org.cipango.console.printer.statistics.SipStatisticPrinter;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.w3c.dom.Node;


public class ConsoleFilter implements Filter
{
	
	public static final ObjectName 
		APPLICATION_PAGES = ObjectNameFactory.create("org.cipango.console:page=*"),
		CONNECTOR_MANAGER = ObjectNameFactory.create("org.cipango.server:type=connectormanager,id=0"),
		CONTEXT_DEPLOYER = ObjectNameFactory.create("org.cipango.deployer:type=contextdeployer,id=0"),
		DAR = ObjectNameFactory.create("org.cipango.dar:type=defaultapplicationrouter,id=0"),
		DIAMETER_NODE = ObjectNameFactory.create("org.cipango.diameter.node:type=node,id=0"),
		DIAMETER_PEERS = ObjectNameFactory.create("org.cipango.diameter.node:type=peer,*"),
		DIAMETER_FILE_LOG = ObjectNameFactory.create("org.cipango.diameter.log:type=filemessagelogger,id=0"),
		DIAMETER_CONSOLE_LOG = ObjectNameFactory.create("org.cipango.callflow.diameter:type=jmxmessagelogger,id=0"),
		HTTP_LOG = ObjectNameFactory.create("org.eclipse.jetty:type=ncsarequestlog,id=0"),
		SIP_APP_DEPLOYER = ObjectNameFactory.create("org.cipango.deployer:type=sipappdeployer,id=0"),
		SIP_CONSOLE_MSG_LOG = ObjectNameFactory.create("org.cipango.callflow:type=jmxmessagelog,id=0"),
		SERVER = ObjectNameFactory.create("org.cipango.server:type=server,id=0"), 
		SNMP_AGENT = ObjectNameFactory.create("org.cipango.snmp:type=snmpagent,id=0"), 
		SIP_MESSAGE_LOG = ObjectNameFactory.create("org.cipango.server.log:type=filemessagelog,id=0"),
		TRANSACTION_MANAGER = ObjectNameFactory.create("org.cipango.server.transaction:type=transactionmanager,id=0");
	
	private static final String[] RESOURCES_EXT = { ".css", ".js", ".jpg", ".png", ".gif", ".xsl"};
	
	private static final Long ONE_HOUR = new Long(3600);

	private Logger _logger = Log.getLogger("console");
	private MBeanServerConnection _mbsc;
	private StatisticGraph _statisticGraph;
	private Deployer _deployer;
	private ServletContext _servletContext;
	
	public void init(FilterConfig config) throws ServletException
	{
		initConnection();
		_servletContext = config.getServletContext();
		if (isJmxEnabled())
		{
			try
			{
				_statisticGraph = new StatisticGraph(_mbsc);
				_statisticGraph.start();
			}
			catch (Exception e)
			{
				_logger.warn("Failed to start statistic graph", e);
			}
			_deployer = new Deployer(_mbsc);
			if (_servletContext.getAttribute(MenuFactory.class.getName()) == null)
				_servletContext.setAttribute(MenuFactory.class.getName(), new MenuFactoryImpl(_mbsc));
		}
	}
	
	
	public void destroy()
	{	
		if (_statisticGraph != null)
			_statisticGraph.stop();
	}
	
	public MenuFactory getMenuFactory()
	{
		return (MenuFactory) _servletContext.getAttribute(MenuFactory.class.getName());
	}
	
	private void initConnection() throws ServletException
	{
		try
		{
			@SuppressWarnings("unchecked")
			List<MBeanServer> l = MBeanServerFactory.findMBeanServer(null);
			Iterator<MBeanServer> it = l.iterator();
			while (it.hasNext())
			{
				MBeanServer server = it.next();
				for (int j = 0; j < server.getDomains().length; j++)
				{
					if (server.isRegistered(SERVER))
					{
						_mbsc = server;
						break;
					}
				}
			}
			_logger.debug("Use MBeanServerConnection {}", _mbsc, null);
		}
		catch (Throwable t)
		{
			_logger.warn("Unable to get MBeanServer", t);
			throw new IllegalStateException("Unable to get MBeanServer", t);
		}

	}
	
	public boolean isJmxEnabled()
	{
		return _mbsc != null;
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		boolean forward = true;
		boolean handled = true;
				
		String command = request.getRequestURI().substring(request.getContextPath().length() + 1);
		if (command.indexOf(';') != -1)
			command = command.substring(0, command.indexOf(';') - 1);
				
		if (!isJmxEnabled())
		{
			response.sendError(503 ,"JMX is not enabled, unable to use cipango console. Please start Cipango with:\n" +
			"\tjava -jar start.jar --ini=start-cipango.ini --pre=etc/cipango-jmx.xml");
			return;
		}
		
		Principal principal = request.getUserPrincipal();
		if (principal != null && !principal.equals(request.getSession().getAttribute(Principal.class.getName())))
		{		
			_logger.info("User " + principal.getName() + " has logged in console");
			request.getSession().setAttribute(Principal.class.getName(), principal);
		}
		
		Menu menuPrinter = getMenuFactory().getMenu(command, request.getContextPath());
		try
		{

			if (command == null || command.equals(""))
			{
				forward = false;
				response.sendRedirect(MenuPrinter.ABOUT.getName());
				return;
			}
			Page currentPage = menuPrinter.getCurrentPage();
			
			if (currentPage != null && !currentPage.isEnabled(_mbsc))
			{
				forward = false;
				request.getSession().setAttribute(Attributes.WARN, "The page " + command + " is not available");
				response.sendRedirect(MenuPrinter.ABOUT.getName());
				return;
			}
				
			if (currentPage != null && !currentPage.isDynamic()) // Apply generic action only on page managed by the console
			{
				try
				{
					boolean hasActions = processGenericAction(request);
					if(hasActions)
					{
						forward = false;
						response.sendRedirect(command);
						return;
					}
				}
				catch (ReflectionException e)
				{
					Throwable cause = e.getCause();
					request.getSession().setAttribute(Attributes.WARN, "Unable to process action: "
							+ cause);
				}
				catch (Throwable e)
				{
					_logger.warn(e.getMessage(), e);
					request.getSession().setAttribute(Attributes.WARN, "Unable to process action: " + e
							+ ":" + e.getMessage());
				}
				
			}

			if (command.equals(MenuPrinter.CONFIG_SIP.getName()))
				doSipConfig(request);
			else if (command.equals(MenuPrinter.MAPPINGS.getName()))
				doApplications(request);
			else if (command.equals(MenuPrinter.STATISTICS_SIP.getName()))
				doSipStatistics(request);
			else if (command.equals(MenuPrinter.ABOUT.getName()))
				doAbout(request);
			else if (command.equals(MenuPrinter.SYSTEM_PROPERTIES.getName()))
				request.setAttribute(Attributes.CONTENT, new SystemPropertiesPrinter());
			else if (command.equals(MenuPrinter.CONFIG_DIAMETER.getName()))
				doDiameterConfig(request);
			else if (command.equals(MenuPrinter.STATISTICS_DIAMETER.getName()))
				request.setAttribute(Attributes.CONTENT, new DiameterStatisticsPrinter(_mbsc));
			else if (command.equals(MenuPrinter.CONFIG_HTTP.getName()))
				doHttpConfig(request);
			else if (command.equals(MenuPrinter.STATISTICS_HTTP.getName()))
				request.setAttribute(Attributes.CONTENT, new HttpStatisticsPrinter(_mbsc));
			else if (command.equals(MenuPrinter.HTTP_LOGS.getName()))
				doHttpLog(request);
			else if (command.equals(MenuPrinter.DAR.getName()))
				request.setAttribute(Attributes.CONTENT, new DarPrinter(_mbsc));
			else if (command.equals("statisticGraph.png"))
			{
				forward = false;
				doGraph(request, response);
			}
			else if (command.equals(MenuPrinter.SIP_LOGS.getName()))
				request.setAttribute(Attributes.CONTENT, new SipLogPrinter(_mbsc, request, Output.HTML));
			else if (command.equals(MenuPrinter.DIAMETER_LOGS.getName()))
				request.setAttribute(Attributes.CONTENT, new DiameterLogPrinter(_mbsc, request, Output.HTML));
			else if (command.equals(MenuPrinter.CONFIG_SNMP.getName()))
			{
				MultiplePrinter printer = new MultiplePrinter();
				ObjectName[] connectors = (ObjectName[]) _mbsc.getAttribute(SNMP_AGENT, "connectors");
				printer.add(new SetPrinter(connectors, "snmp.connectors", _mbsc));
				
				ObjectName[] traps = (ObjectName[]) _mbsc.getAttribute(SNMP_AGENT, "trapReceivers");
				printer.add(new SetPrinter(traps, "snmp.trap", _mbsc));
				request.setAttribute(Attributes.CONTENT, printer);
			}
			else if (command.equals(MenuPrinter.CALLS.getName()))
				request.setAttribute(Attributes.CONTENT, new CallsPrinter(_mbsc, request.getParameter("callID")));
			else if (command.equals("message.log"))
			{
				forward = false;
				response.setContentType("text/plain");
				PrintWriter out = response.getWriter();
				new SipLogPrinter(_mbsc, request, Output.TEXT).print(out);
			}
			else if (command.equals("diameter.log"))
			{
				forward = false;
				response.setContentType("text/plain");
				PrintWriter out = response.getWriter();
				new DiameterLogPrinter(_mbsc, request, Output.TEXT).print(out);
			}
			else if (command.equals("message.svg"))
			{
				forward = false;
				doMessageSvg(request, response);
			}
			else if (command.equals("dump.txt"))
			{
				forward = false;
				response.setContentType("text/plain");
				response.setBufferSize(65536);
				new DumpPrinter(getMbsc(), this).print(response.getWriter());
			}
			else if (doResource(command, response))
			{
				forward = false;
			}
			else if (currentPage != null && currentPage.isDynamic())
			{
				request.setAttribute(Attributes.CONTENT, new OamPrinter(_mbsc, request, currentPage.getObjectName()));
			}
			else
			{
				ObjectName objectName = new ObjectName("org.cipango.console", "page", command);
				if (_mbsc.isRegistered(objectName))
					request.setAttribute(Attributes.CONTENT, new OamPrinter(_mbsc, request, objectName));
				else
				{
					handled = false;
					forward = false;
				}
			}
			
		}
		catch (Throwable e)
		{
			_logger.warn("Unable to process request: {}", request.getRequestURL().toString(), e);
			_logger.debug("Unable to process request: " +  request.getRequestURL().toString(), e);
			HtmlPrinter printer = new ErrorPrinter(e.toString());
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			request.setAttribute(Attributes.CONTENT, printer);
		}
		finally
		{
			if (!handled)
				filterChain.doFilter(request, response);
			else if (forward)
			{
				request.setAttribute(Attributes.MENU, menuPrinter);
				response.setContentType("text/html");
				response.setHeader("Cache-Control", "no-cache");
				response.setHeader("Pragma", "no-cache");
				response.setDateHeader("Expires", 0);
				request.getRequestDispatcher("/WEB-INF/template.jsp").forward(request, response);
			}
		}
	}
	
	private void doAbout(HttpServletRequest request) throws Exception
	{
		MultiplePrinter printer = new MultiplePrinter();
		printer.add(new PropertiesPrinter(getVersion()));
		printer.add(new PropertiesPrinter(getEnvironment()));
		request.setAttribute(Attributes.CONTENT, printer);
	}
	
	public PropertyList getVersion() throws Exception
	{
		PropertyList properties = new PropertyList(_mbsc, ConsoleFilter.SERVER, "version");
		Long startupTime = (Long) _mbsc.getAttribute(ConsoleFilter.SERVER, "startupTime");
		properties.add(new Property("Startup Time", new Date(startupTime)));
		properties.add(new Property("Server Uptime", PrinterUtil.getDuration(System.currentTimeMillis() - startupTime)));
		return properties;
	}
	
	public PropertyList getEnvironment() throws Exception
	{
		PropertyList env = new PropertyList();
		env.setTitle("Environment");
		env.add(new Property("OS / Hardware", System.getProperty("os.name") + " " + System.getProperty("os.version")
				+ " - " + System.getProperty("os.arch")));
		env.add(new Property("Jetty Home", System.getProperty("jetty.home")));
		env.add(new Property("Java Runtime", System.getProperty("java.runtime.name") + " " + System.getProperty("java.runtime.version")));
		
		Runtime r = Runtime.getRuntime();
		long usedMemory = r.totalMemory() - r.freeMemory();
		NumberFormat f = DecimalFormat.getPercentInstance();
		f.setMinimumFractionDigits(1);
		String percentage = f.format(((float) usedMemory) / r.maxMemory());
		env.add(new Property("Memory", (usedMemory >> 20) + " Mb of " + (r.maxMemory() >> 20) + " Mb (" +
				percentage + ")"));
		return env;
	}

	private boolean processGenericAction(HttpServletRequest request)
			throws Exception
	{
		String action = request.getParameter(Parameters.ACTION);
		String actions = request.getParameter(Parameters.ACTIONS);
		boolean hasAction = false;
		
		String sObjectName = request.getParameter(Parameters.OBJECT_NAME);
		
		if (action != null && sObjectName != null)
		{
			ObjectName objectName = ObjectNameFactory.create(sObjectName);
			if (action.equals("undeploy"))
			{
				_deployer.undeploy(objectName);	
				String name = objectName.getKeyProperty("name");
				request.getSession().setAttribute(Attributes.INFO, "Successfull request to undeploy application " + name);
				Log.info("User " + request.getUserPrincipal() 
						+ " requested to undeploy application " + name);
			}
			else
			{
				_mbsc.invoke(objectName, action, null, null);
				request.getSession().setAttribute(Attributes.INFO, "Action " + action + " successfull");
			}
			hasAction = true;
		}
				
		if (actions == null)
			return hasAction;
		
		@SuppressWarnings("rawtypes")
		Enumeration enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements())
		{
			String element = (String) enumeration.nextElement();
			if (element.endsWith(Parameters.DOT_VALUE))
			{
				String name = element.substring(0, element.lastIndexOf('.'));
				sObjectName = request.getParameter(name + Parameters.DOT_OBJECT_NAME);
				ObjectName objectName = ObjectNameFactory.create(sObjectName);
				MBeanInfo mBeanInfo = _mbsc.getMBeanInfo(objectName);
				MBeanAttributeInfo attributesInfos[] = mBeanInfo.getAttributes();

				for (int i = 0; i < attributesInfos.length; i++)
				{
					if (name.equals(attributesInfos[i].getName()))
					{
						if (attributesInfos[i].isWritable())
						{
							String sValue = request.getParameter(name + Parameters.DOT_VALUE);
							Object value;
							String type = attributesInfos[i].getType();
							if (type.equals(String.class.getName()))
							{
								value = sValue;
							}
							else if (type.equals(Integer.class.getName()) || type.equals("int"))
							{
								value = new Integer(sValue);
							}
							else if (type.equals(Boolean.class.getName())
									|| type.equals("boolean"))
							{
								value = new Boolean(sValue);
							}
							else
							{
								_logger.warn("Unable to set {} = {}: Unknown type:" + type, name, sValue);
								break;
							}
							Attribute attribute = new Attribute(name, value);
							_mbsc.setAttribute(objectName, attribute);
						}
						break;
					}
				}
			}
		}			
		return hasAction || actions != null;
	}

	@SuppressWarnings("unchecked")
	private void doApplications(HttpServletRequest request)
			throws Exception
	{
		if (ServletFileUpload.isMultipartContent(request))
		{
			FileItem item = null;
			try
			{
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				List<FileItem>  items = upload.parseRequest(request);
	
				Iterator<FileItem> it = items.iterator();
				while (it.hasNext())
				{
					item = it.next();
					if (!item.isFormField())
					{
						_deployer.deploy(item.getName(), item.get());
						request.getSession().setAttribute(Attributes.INFO,
								"Successful request to deploy " + item.getName());
						Log.info("User " + request.getUserPrincipal() 
								+ " requested to deploy application: " + item.getName());
					}
				}
			}
			catch (Throwable e)
			{
				_logger.warn("Unable to deploy " + item.getName(), e);
				request.getSession().setAttribute(Attributes.WARN, "Unable to deploy "
						+ item.getName() + ": " + e.getMessage());
			}
		}

		MultiplePrinter printer = new MultiplePrinter();

		ObjectName[] sipContexts = PrinterUtil.getSipAppContexts(_mbsc);
	
		Table contextsTable = new Table(_mbsc, sipContexts, "appContexts");

		printer.add(new SetPrinter(contextsTable));
		
		ObjectName[] otherContexts = PrinterUtil.getNonSipAppContexts(getMbsc());
		if (otherContexts != null && otherContexts.length > 0)
		{
			Table otherContextsTable = new Table(_mbsc, otherContexts, "otherContexts");
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
			List<String> operationLinks = new ArrayList<String>();
			boolean running = (Boolean) _mbsc.getAttribute(row.getObjectName(), "running");
			if (running)
				operationLinks.add(PrinterUtil.getActionLinkWithConfirm("stop", row.getObjectName(), _mbsc, MenuPrinter.MAPPINGS, null));
			else
				operationLinks.add(PrinterUtil.getActionLinkWithConfirm("start", row.getObjectName(), _mbsc, MenuPrinter.MAPPINGS, null));
			operationLinks.add(PrinterUtil.getActionLinkWithConfirm("undeploy", row.getObjectName(), _mbsc, MenuPrinter.MAPPINGS, null));
			row.setOperations(operationLinks);
		}
				
		printer.add(new ServletMappingPrinter(sipContexts, _mbsc));
		printer.add(new UploadWarPrinter());
		request.setAttribute(Attributes.JAVASCRIPT_SRC, "javascript/upload.js");

		request.setAttribute(Attributes.CONTENT, printer);
	}
		
//  ---------------------------  SIP -------------------------------------

	private void doSipConfig(HttpServletRequest request) throws Exception
	{
		MultiplePrinter printer = new MultiplePrinter();
		ObjectName[] connectors = (ObjectName[]) _mbsc.getAttribute(ConsoleFilter.CONNECTOR_MANAGER, "connectors");

		printer.add(new SetPrinter(connectors, "sip.connectors", _mbsc));
		ObjectName threadPool = (ObjectName) _mbsc.getAttribute(
				ConsoleFilter.SERVER, "sipThreadPool");
		
		PropertyList properties = new PropertyList(_mbsc, threadPool, "sip.threadPool");
		for (Property property : properties)
		{
			String name = property.getName();
			int index = Math.max(name.indexOf("in pool"), name.indexOf("in the pool"));
			if (index != -1)
				property.setName(name.substring(0, index));
		}
		printer.add(new PropertiesPrinter(properties));
		
		printer.add(new PropertiesPrinter(ConsoleFilter.TRANSACTION_MANAGER, "sip.timers", _mbsc)
		{
			@Override
			protected void printHeaders(Writer out, boolean hasNotes) throws Exception
			{
				out.write("<div class=\"data\">\n<table>\n"
				+ "<tr><th>Name</th><th>Value</th><th>Default Value</th></tr>\n");
			}
		});
		request.setAttribute(Attributes.CONTENT, printer);
	}
	
	private void doSipStatistics(HttpServletRequest request)
			throws Exception
	{
		String action = request.getParameter(Parameters.ACTION);
		if (action != null && SipStatisticPrinter.START_GRAPH.equals(action))
			_statisticGraph.start();
		else if (action != null && SipStatisticPrinter.STOP_GRAPH.equals(action))
			_statisticGraph.stop();
		
		SipStatisticPrinter printer = new SipStatisticPrinter(_mbsc, _statisticGraph);
		String time = request.getParameter(Parameters.TIME);
		if (time == null)
			time = (String) request.getSession().getAttribute(Parameters.TIME);
		else
			request.getSession().setAttribute(Parameters.TIME, time);
		printer.setTime(time);

		request.setAttribute(Attributes.CONTENT, printer);
	}
	
//  ---------------------------  Diameter -------------------------------------
	
	private void doDiameterConfig(HttpServletRequest request)
	throws Exception
	{		
		MultiplePrinter printer = new MultiplePrinter();
		printer.add(new PropertiesPrinter(DIAMETER_NODE, "diameter.node",  _mbsc));
		
		ObjectName[] transports = (ObjectName[]) _mbsc.getAttribute(
				ConsoleFilter.DIAMETER_NODE, "connectors");
		printer.add(new SetPrinter(transports, "diameter.transport", _mbsc));
		
		printer.add(new PropertiesPrinter(DIAMETER_NODE, "diameter.timers",  _mbsc));

		@SuppressWarnings("unchecked")
		Set<ObjectName> peers = _mbsc.queryNames(ConsoleFilter.DIAMETER_PEERS, null);
		printer.add(new SetPrinter(peers, "diameter.peers", _mbsc));	
					
		request.setAttribute(Attributes.CONTENT, printer);
	}
	
	//  ---------------------------  HTTP -------------------------------------
	
	private void doHttpConfig(HttpServletRequest request)
	throws Exception
	{		
		MultiplePrinter printer = new MultiplePrinter();
		ObjectName[] connectors = (ObjectName[]) _mbsc.getAttribute(SERVER, "connectors");
	
		printer.add(new SetPrinter(connectors, "http.connectors", _mbsc));
		
		ObjectName threadPool = (ObjectName) _mbsc.getAttribute(
				ConsoleFilter.SERVER, "threadPool");
		PropertyList properties = new PropertyList(_mbsc, threadPool, "http.threadPool");
		for (Property property : properties)
		{
			String name = property.getName();
			int index = Math.max(name.indexOf("in pool"), name.indexOf("in the pool"));
			if (index != -1)
				property.setName(name.substring(0, index));
		}
		printer.add(new PropertiesPrinter(properties));
							
		request.setAttribute(Attributes.CONTENT, printer);
	}
	
	private void doHttpLog(HttpServletRequest request)
	throws Exception
	{		
		HtmlPrinter printer = new FileLogPrinter(_mbsc, MenuPrinter.HTTP_LOGS, HTTP_LOG, false);
							
		request.setAttribute(Attributes.CONTENT, printer);
	}
	
	private void doGraph(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		try
		{
			response.setContentType("image/png");
			String sTime = request.getParameter("time");
			String type = request.getParameter("type");
			Long time;
			if (sTime == null)
				time = ONE_HOUR;
			else
			{
				try
				{
					time = Long.valueOf(sTime);
				}
				catch (NumberFormatException e)
				{
					time = ONE_HOUR;
				}
			}
			byte[] image = _statisticGraph.createGraphAsPng(time, type);

			response.getOutputStream().write(image);
		}
		catch (Exception e)
		{
			_logger.warn("Unable to create graph", e);
		}
	}

	private void doMessageSvg(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		response.setContentType("image/svg+xml");
		int maxMessages = 
			PrinterUtil.getInt(request.getParameter(Parameters.MAX_MESSAGES), SipLogPrinter.DEFAULT_MAX_MESSAGES);
		String msgFilter = request.getParameter(Parameters.MESSAGE_FILTER);
		if (_mbsc.isRegistered(SIP_CONSOLE_MSG_LOG))
		{
			String userAgent = request.getHeader("User-Agent");
			// Firefox does not support animation and IE does not support foreignObject
			boolean supportAnimation = (userAgent.indexOf("Firefox") == -1 
				&& userAgent.indexOf("MSIE") == -1 )
				|| "Chrome".equalsIgnoreCase(request.getParameter("ua"));
			
			Object[] params = {new Integer(maxMessages), msgFilter, "dataToSvg.xsl", supportAnimation};
			byte[] image = (byte[]) _mbsc.invoke(
					SIP_CONSOLE_MSG_LOG, 
					"generateGraph", 
					params,
					new String[] {Integer.class.getName(), String.class.getName(), String.class.getName(), Boolean.class.getName()});
			
			// Only Internet explorer does NOT applies XSL on a XML document.
			if (userAgent == null || userAgent.indexOf("MSIE") != -1)
				image = doXsl(image);
			response.getOutputStream().write(image);
		}
	}
	
	private byte[] doXsl(byte[] source)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			TransformerFactory factory = TransformerFactory.newInstance();
			DocumentBuilderFactory documentBuilderFactory = 
				DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			//documentBuilder.setEntityResolver(new EasipEntityResolver());
			
			Node doc = documentBuilder.parse(new ByteArrayInputStream(source));
			
			Transformer transformer = factory.newTransformer(
					new StreamSource(_servletContext.getResourceAsStream("/dataToSvg.xsl")));
			transformer.transform(new DOMSource(doc), result);
			return os.toByteArray();
		}
		catch (Throwable e)
		{
			_logger.warn("Unable to do XSL transformation", e);
			return source;
		}
	}

	private boolean doResource(String command, HttpServletResponse response) throws IOException
	{
		for (int i = 0; i < RESOURCES_EXT.length; i++)
		{
			if (command.endsWith(RESOURCES_EXT[i]))
			{
				InputStream is = getClass().getResourceAsStream(command);
				if (is != null)
				{
					int read;
					byte[] b = new byte[1024];
					while ((read = is.read(b)) != -1)
						response.getOutputStream().write(b, 0, read);
					return true;
				}
			}
		}
		return false;
	}


	public MBeanServerConnection getMbsc()
	{
		return _mbsc;
	}

}
