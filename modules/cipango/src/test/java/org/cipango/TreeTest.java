// ========================================================================
// Copyright 2007-2008 NEXCOM Systems
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

package org.cipango;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.mortbay.util.LazyList;

public class TreeTest 
{
	static class Branch
	{
		private String _s;
		
		public Branch(String s)
		{
			_s = s;
		}
		
		public void add(Branch branch)
		{
			_branches = LazyList.add(_branches, branch);
		}
		private Object _branches;
	}
	
	static class BranchIterator implements Iterator<Branch>
	{
		private Iterator<Branch> _it;
		
		private Object _branches;
		private Branch _next;
		private int _index;
		
		public BranchIterator(Object branches)
		{
			_branches = branches;
			_index = 0;
		}

		public boolean hasNext()
		{
			if (_next == null)
			{
				if (_it != null && _it.hasNext())
				{
					_next = _it.next();
				}
				else
				{
					if (_index < LazyList.size(_branches))
					{
						Branch branch = (Branch) LazyList.get(_branches, _index++);
						_next = branch;
						if (LazyList.size(branch._branches) > 0)
						{
							_it = new BranchIterator(branch._branches);
						}
					}
				}
			}
			return _next != null;
		}

		public Branch next() 
		{
			if (hasNext())
			{
				Branch next = _next;
				_next = null;
				return next;
			}
			throw new NoSuchElementException();
		}

		public void remove() 
		{	
		}
	}
	
	public static void main(String[] args)
	{
		Branch a = new Branch("a");
		Branch b = new Branch("b");
		Branch c = new Branch("c");
		Branch d = new Branch("d");
		Branch e = new Branch("e");
		Branch f = new Branch("f");
		Branch g = new Branch("g");
		
		a.add(b);
		a.add(c);
		a.add(e);
		b.add(d);
		b.add(f);
		c.add(g);
		
		
		List<Branch> list = new ArrayList<Branch>();
		list.add(a);
		
		Iterator<Branch> it = new BranchIterator(list);
		while (it.hasNext())
		{
			System.out.println(it.next()._s);
		}
	}
}
