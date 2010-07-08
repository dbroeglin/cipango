// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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

package org.cipango.util;

import org.eclipse.jetty.util.LazyList;

/**
 * Extension to {@see LazyList} to offer a map-like interface
 */
public class LazyMap 
{
	public static Object add(Object list, String name, Object value)
	{
		return LazyList.add(list, new Entry(name, value));
	}
	
	public static Object get(Object list, String name)
	{
		for (int i = LazyList.size(list); i-->0;)
		{
			Entry entry = (Entry) LazyList.get(list, i);
			if (entry._name.equals(name))
				return entry._value;
		}
		return null; 
	}
	
	static class Entry
	{
		String _name;
    	Object _value;
		
    	Entry(String name, Object value) { _name = name; _value = value; }
	}    	
}
