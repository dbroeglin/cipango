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

import org.cipango.console.Parameters;
import org.cipango.console.printer.generic.HtmlPrinter;

public class UploadSarPrinter implements HtmlPrinter
{

	public void print(Writer out) throws Exception
	{
		out.write("<h2>Upload a SAR file to install</h2>");
		// Set connection in URI, to be able to get connection normally
		// else, multipart impact request.getParameter("connection"); to return
		// null
		out.write("<form action=\"applications\" method=\"post\" enctype=\"multipart/form-data\" "
						+ " name=\"upload\" onsubmit=\"return check();\">");
		out.write("Select SAR file to upload:&nbsp;");
		out.write("<input name=\"installSar\" size=\"40\" type=\"file\">");
		out.write("<br/><input type=\"submit\" name=\"" + Parameters.ACTION
				+ "\" value=\"" + Parameters.ACTION_INSTALL + "\"/>");
		out.write("</form>");
	}

}
