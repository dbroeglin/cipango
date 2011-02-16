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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.cipango.console.ConsoleFilter;
import org.cipango.console.Menu;
import org.cipango.console.ObjectNameFactory;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class MenuPrinter implements HtmlPrinter, Menu
{

	private static final Page PAGES = new Page("");

	public static final Page 
		SERVER = PAGES.add(new Page("Server")),
		ABOUT = SERVER.add(new Page("about", "About")),
		SYSTEM_PROPERTIES = SERVER.add(new Page("system-properties", "System Properties")),
		
		CONFIG = PAGES.add(new Page("Configuration")),
		CONFIG_SIP = CONFIG.add(new Page("configuration-sip", "SIP Configuration", "SIP")
		{
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return c.isRegistered(ConsoleFilter.CONNECTOR_MANAGER);
			}
		}),
		CONFIG_HTTP = CONFIG.add(new Page("configuration-http", "HTTP Configuration", "HTTP")),
		CONFIG_DIAMETER = CONFIG.add(new Page("configuration-diameter", "Diameter Configuration", "Diameter")
		{
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return c.isRegistered(ConsoleFilter.DIAMETER_NODE);
			}
		}),
		CONFIG_SNMP = CONFIG.add(new Page("configuration-snmp", "SNMP Configuration", "SNMP")
		{
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return c.isRegistered(ConsoleFilter.SNMP_AGENT);
			}
		}),
		
		STATISTICS = PAGES.add(new Page("Statistics")),
		STATISTICS_SIP = STATISTICS.add(new Page("statistics-sip", "SIP Statistics", "SIP")),
		STATISTICS_HTTP = STATISTICS.add(new Page("statistics-http", "HTTP Statistics", "HTTP")),
		STATISTICS_DIAMETER = STATISTICS.add(new Page("statistics-diameter", "Diameter Statistics", "Diameter"){
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return c.isRegistered(ConsoleFilter.DIAMETER_NODE);
			}
		}),
		
		LOGS = PAGES.add(new Page("Logs")),
		SIP_LOGS = LOGS.add(new Page("logs-sip", "SIP Logs", "SIP"){
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return c.isRegistered(ConsoleFilter.SIP_CONSOLE_MSG_LOG)
							|| c.isRegistered(ConsoleFilter.SIP_MESSAGE_LOG);
			}
		}),
		HTTP_LOGS = LOGS.add(new Page("logs-http", "HTTP Logs", "HTTP")),
		DIAMETER_LOGS = LOGS.add(new Page("logs-diameter", "Diameter Logs", "Diameter")
		{
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return c.isRegistered(ConsoleFilter.DIAMETER_NODE);
			}
		}),
		CALLS = LOGS.add(new Page("logs-calls", "Calls")),

		APPLICATIONS = PAGES.add(new Page("Applications")
		{
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return !c.isRegistered(ObjectNameFactory.create("org.cipango.console:page-disabled=application"));
			}
		}),
		MAPPINGS = APPLICATIONS.add(new Page("applications", "Applications Mapping")
		{
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return getFather().isEnabled(c) && c.isRegistered(ConsoleFilter.DAR);
			}
		}),
		DAR = APPLICATIONS.add(new Page("dar", "Default Application Router", "DAR")
		{
			@Override
			public boolean isEnabled(MBeanServerConnection c) throws IOException
			{
				return getFather().isEnabled(c) && c.isRegistered(ConsoleFilter.DAR);
			}
		});
	
	
	private MBeanServerConnection _connection;
	private Page _currentPage;
	private List<Page> _pages;
	private static Logger _logger = Log.getLogger("console");
	private String _contextPath;

	public MenuPrinter(MBeanServerConnection c, String command, String contextPath)
	{
		_connection = c;
		_pages = getPages();
		_contextPath = contextPath;
		Iterator<Page> it = _pages.iterator();
		while (it.hasNext())
		{
			Page subPage = getPage(command, it.next());
			
			if (subPage != null)
			{
				_currentPage = subPage;
				break;
			}
		}
	}
	
	private Page getPage(String command, Page page)
	{
		Iterator<Page> it = page.getPages().iterator();
		while (it.hasNext())
		{
			Page subPage = getPage(command, it.next());
			if (subPage != null)
				return subPage;
		}

		if (command != null && command.equals(page.getName()))
			return page;
		
		return null;
	}
	
	
	public Page getCurrentPage()
	{
		return _currentPage;
	}
	
	public String getTitle()
	{
		return _currentPage.getTitle();
	}
	
		
	public boolean isKnownPage()
	{
		return _currentPage != null && !_currentPage.isDynamic();
	}
	
	public String getHtmlTitle()
	{
		if (_currentPage.getFather() == null)
			return "<h1>" + _currentPage.getTitle() + "</h1>";
		else
			return "<h1>" + _currentPage.getFather().getTitle() + 
					"<span> > " + _currentPage.getMenuTitle() + "</span></h1>";
	}

	public void print(Writer out) throws Exception
	{
		out.write("<div id=\"menu\">\n");
		out.write("<ul>\n");
		Iterator<Page> it = _pages.iterator();
		while (it.hasNext())
			print(out, it.next());
		out.write("</u1>\n");
		out.write("</div>\n");
	}
	
	public void print(Writer out, Page page) throws Exception
	{
		if (page.isEnabled(_connection))
		{
			out.write("<li>");
			out.write("<a href=\"" + _contextPath + "/" + page.getLink(_connection) + "\"");
			if (page == _currentPage || page == _currentPage.getFather())
				out.write("class=\"selected\"");
			out.write("><span>" + page.getMenuTitle() + "</span></a>\n");
			out.write("</li>\n");
		}
	}
	
	public HtmlPrinter getSubMenu()
	{
		return new HtmlPrinter()
		{
			
			public void print(Writer out) throws Exception
			{
				out.write("<div id=\"submenu\">\n<ul>");
				if (_currentPage.getFather() != null)
				{	
					Iterator<Page> it = _currentPage.getFather().getPages().iterator();
					while (it.hasNext())
						MenuPrinter.this.print(out, it.next());
				}
				out.write("</ul>\n</div>\n");
			}
		};
	}

	private List<Page> getPages()
	{
		List<Page> l = new ArrayList<Page>(PAGES.getPages());
				
		try
		{
			@SuppressWarnings("unchecked")
			Set<ObjectName> set = _connection.queryNames(ConsoleFilter.APPLICATION_PAGES, null);
			if (set != null && !set.isEmpty())
			{
				Iterator<ObjectName> it = set.iterator();
				while (it.hasNext())
				{
					ObjectName objectName = it.next();
					Page page = getDynamicPage(objectName);
					addDynamicSubPages(objectName, page);
					l.add(page);
				}
			}
		}
		catch (Exception e) 
		{
			_logger.warn("Unable to get applications pages", e);
		}
		
		return l;
	}
	
	private Page getDynamicPage(ObjectName objectName) throws Exception
	{
		String name = objectName.getKeyProperty("page");
		MBeanInfo info = _connection.getMBeanInfo(objectName);
		String title = null;
		String menuTitle = null;
		for (int i = 0; i < info.getAttributes().length; i++)
		{
			MBeanAttributeInfo attr = info.getAttributes()[i];
			if ("Title".equals(attr.getName()) && attr.isReadable())
				title = (String) _connection.getAttribute(objectName, attr.getName());
			if ("MenuTitle".equals(attr.getName()) && attr.isReadable())
				menuTitle = (String) _connection.getAttribute(objectName, attr.getName());

		}
		Page page = new Page(name, title, menuTitle);
		page.setObjectName(objectName);
		return page;
	}
	
	private void addDynamicSubPages(ObjectName objectName, Page page) throws Exception
	{
		MBeanInfo info = _connection.getMBeanInfo(objectName);

		for (int i = 0; i < info.getAttributes().length; i++)
		{
			MBeanAttributeInfo attr = info.getAttributes()[i];
			if ("SubPages".equals(attr.getName()) && attr.isReadable())
			{
				ObjectName[] subPages = (ObjectName[]) _connection.getAttribute(objectName, attr.getName());
				if (subPages != null)
				{
					for (ObjectName name : subPages)
						page.add(getDynamicPage(name));
				}
				
				break;
			}
		}
	}
	
	
	public static class Page
	{
		private List<Page> _pages = new ArrayList<Page>();
		private Page _father;
		private String _name;
		private String _title;
		private String _menuTitle;
		private ObjectName _objectName;

		Page(String title)
		{
			_title = title;
		}
		
		Page(String name, String title)
		{
			_name = name;
			_title = title;
		}
		
		Page(String name, String title, String menuTitle)
		{
			_name = name;
			_title = title;
			_menuTitle = menuTitle;
		}
		
		public List<Page> getPages()
		{
			return _pages;
		}

		public String getName()
		{
			return _name;
		}
		
		protected String getLink(MBeanServerConnection c) throws IOException
		{
			if (_name == null && !_pages.isEmpty())
			{
				Iterator<Page> it = _pages.iterator();
				while (it.hasNext())
				{
					Page page = it.next();
					if (page.isEnabled(c))
						return page.getName();
				}
				return _pages.get(0).getName();
			}
			return _name;
		}

		public String getTitle()
		{
			if (_title != null)
				return _title;
			return _name.substring(0, 1).toUpperCase() + _name.substring(1);
		}
		
		public String getMenuTitle()
		{
			if (_menuTitle != null)
				return _menuTitle;
			return getTitle();
		}
		
		public Page add(Page page)
		{
			_pages.add(page);
			page.setFather(this);
			return page;
		}

		public Page getFather()
		{
			return _father;
		}

		private void setFather(Page father)
		{
			_father = father;
		}

		public boolean isDynamic()
		{
			return _objectName != null;
		}

		public void setObjectName(ObjectName objectName)
		{
			_objectName = objectName;
		}
		
		public ObjectName getObjectName()
		{
			return _objectName;
		}
		
		public boolean isEnabled(MBeanServerConnection c) throws IOException
		{
			return true;
		}
	}

}
