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

import java.io.IOException;
import java.io.Writer;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.cipango.console.Parameters;

public class Property implements HtmlPrinter
{

	private String _name;
	private Object _value;
	private String _note;
	private boolean _odd;
	private boolean _hasNotes = true;
	private boolean _readOnly = true;
	private ObjectName _objectName;
	
	public Property()
	{
	}
	
	public Property(String name, Object value)
	{
		_name = name;
		_value = value;
	}
	
	public Property(MBeanAttributeInfo info, Object value, int i, String note)
	{
		setName(info.getDescription());
		setValue(value);
		setOdd((i % 2 ) == 0);
		setNote(note);
	}
	
	public String getName()
	{
		return _name;
	}
	public void setName(String name)
	{
		_name = name;
	}
	public Object getValue()
	{
		return _value;
	}
	public void setValue(Object value)
	{
		_value = value;
	}
	public String getNote()
	{
		return _note;
	}
	public void setNote(String note)
	{
		_note = note;
	}
	public boolean isOdd()
	{
		return _odd;
	}
	public void setOdd(boolean odd)
	{
		_odd = odd;
	}
	
	public void print(Writer out) throws IOException
	{
		if (_readOnly)
		{
			out.write("<tr class=\"" + (_odd ? "even" : "odd") + "\">");
			out.write("<td>" + _name + "</td><td>" + (_value == null ? "" : _value) + "</td>");
			if (_hasNotes)
				out.write("<td>" + (_note == null ? "&nbsp;" : _note) + "</td></tr>\n");
		}
		else
		{
			out.write("<tr class=\"" + (_odd ? "even" : "odd") + "\">");
			out.append("<td>").append(_name).append("</td>");
			out.write("<td><input name=\"" + _name + Parameters.DOT_VALUE + "\" value=\""
					+ (_value == null ? "" : _value));
			out.write("\" type=\"text\"></td>");
			out.write("<td>" + (_note == null ? "&nbsp;" : _note) + "</td></tr>\n");
			out.write("<input type=\"hidden\" name=\"" + _name
					+ Parameters.DOT_OBJECT_NAME + "\" value=\"" + _objectName + "\"/>");
		}
	}

	public boolean isHasNotes()
	{
		return _hasNotes;
	}

	public void setHasNotes(boolean hasNotes)
	{
		_hasNotes = hasNotes;
	}

}
