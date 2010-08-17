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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.management.MBeanServerConnection;

import org.cipango.console.ConsoleFilter;

public class DarPrinter implements HtmlPrinter
{
	private MBeanServerConnection _connection;
	
	
	public DarPrinter(MBeanServerConnection connection)
	{
		_connection = connection;
	}
	
	public void print(Writer out) throws Exception
	{
		if (!_connection.isRegistered(ConsoleFilter.DAR))
		{
			out.write("Default application router is not running.");
			return;
		}
		out.write("DAR configuration URI: <pre>" +  _connection.getAttribute(ConsoleFilter.DAR, "configuration")
				+ "</pre><br/>\n");
		
		String defaultApplication = (String) _connection.getAttribute(ConsoleFilter.DAR, "defaultApplication");
		if (defaultApplication != null)
		{
			out.write("Default application router is routing all requests to the application: <pre>");
			out.write(defaultApplication);
			out.write("</pre>\n");
		}
		else
		{
			printDarConfig(out);
		}
	}

	
	private void printDarConfig(Writer out) throws ParseException, Exception
	{
		out.write("<div class=\"data\">\n<table class=\"main\">\n"
				+ "<tr><th>Method</th><th>Application name</th><th>Identity</th><th>Routing region</th>" +
						"<th>URI</th><th>Route modifier</th><th>State info</th></tr>\n");
				
		String config = (String) _connection.getAttribute(ConsoleFilter.DAR, "config");
		InputStream is = new ByteArrayInputStream(config.getBytes());
		Properties properties = new Properties();
		properties.load(is);
		Enumeration<Object> e = properties.keys();
		int index = 0;
		while (e.hasMoreElements())
		{
			String method = e.nextElement().toString();
			String infos = properties.get(method).toString().trim();
			int li = infos.indexOf('(');
			boolean first = true;
			int count = 0;
			StringBuilder sb = new StringBuilder();
			while (li >= 0)
			{
				count++;
				int ri = infos.indexOf(')', li);
				if (ri < 0)
					throw new ParseException(infos, li);

				sb.append("<tr class=\"" + (index++ % 2 == 0 ? "even" : "odd") + "\">\n");
				if (first)
				{
					sb.append("<td rowspan=\"${rowSpan}\">" + method + "</td>");
					first = false;
				}
				
				String info = infos.substring(li + 1, ri);
	
				li = infos.indexOf('(', ri);
				InfoIterator it = new InfoIterator(info);
				while (it.hasNext())
					sb.append("<td>" + it.next() + "</td>");
				sb.append("</tr>\n");
			}
			out.write(sb.toString().replace("${rowSpan}", String.valueOf(count)));
		}
		out.write("</table>\n</div>\n");
	}


	
	
	class InfoIterator implements Iterator<String>
	{
		private String _info;
		private int i;
		private String token;

		public InfoIterator(String info)
		{
			_info = info;
		}

		public boolean hasNext()
		{
			if (token == null)
			{
				int li = _info.indexOf('"', i);
				if (li != -1)
				{
					int ri = _info.indexOf('"', li + 1);
					if (ri != -1)
					{
						token = _info.substring(li + 1, ri);
						i = ri + 1;
					}
				}
			}
			return token != null;
		}

		public String next()
		{
			if (hasNext())
			{
				String s = token;
				token = null;
				return s;
			} else
			{
				return null;
			}
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
