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

import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.cipango.console.printer.generic.HtmlPrinter;


public class SystemPropertiesPrinter implements HtmlPrinter
{

	
	public void print(Writer out) throws Exception
	{
		Properties properties = System.getProperties();
		
		Iterator<Object> it = new TreeSet<Object>(properties.keySet()).iterator();
		out.append("<pre><table>\n");
		while (it.hasNext())
		{
			Object key = it.next();
			out.append("<tr><td><b>").append(key.toString()).append("</b></td>");
			out.append("<td>").append(String.valueOf(properties.get(key))).append("</td></tr>\n");
		}
		out.append("</table></pre>\n");
	}

}
