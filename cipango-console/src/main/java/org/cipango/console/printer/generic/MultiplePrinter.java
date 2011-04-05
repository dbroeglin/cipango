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

import java.io.Writer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiplePrinter extends AbstractList<HtmlPrinter> implements HtmlPrinter
{

	private List<HtmlPrinter> _list;

	public MultiplePrinter()
	{
		_list = new ArrayList<HtmlPrinter>();
	}

	@Deprecated 
	public void addLast(HtmlPrinter printer)
	{
		_list.add(printer);
	}

	public void print(Writer out) throws Exception
	{
		Iterator<HtmlPrinter> it = _list.iterator();
		while (it.hasNext())
		{
			it.next().print(out);
			out.write('\n');
		}
	}

	@Override
	public HtmlPrinter get(int index)
	{
		return _list.get(index);
	}

	@Override
	public int size()
	{
		return _list.size();
	}
	
	@Override
	public void add(int index, HtmlPrinter printer)
	{
		_list.add(index, printer);
	}
}
