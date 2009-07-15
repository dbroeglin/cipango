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

package org.cipango.kaleo.event;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEventPackage<T extends Resource> implements EventPackage<T> 
{
	public int _minExpires = 60;
	public int _maxExpires = 3600;
	public int _defaultExpires = 3600;
	
	private Map<String, T> _resources = new HashMap<String, T>();
	
	public int getMinExpires()
	{
		return _minExpires; 
	}

	public int getMaxExpires()
	{
		return _maxExpires;
	}

	public int getDefaultExpires()
	{
		return _defaultExpires;
	}
	
	public void setMinExpires(int minExpires)
	{
		_minExpires = minExpires;
	}
	
	public void setMaxExpires(int maxExpires)
	{
		_maxExpires = maxExpires;
	}
	
	public void setDefaultExpires(int defaultExpires)
	{
		_defaultExpires = defaultExpires;
	}
	
	protected abstract T newResource(String uri);
	
	public T getResource(String uri) 
	{
		synchronized (_resources)
		{
			T resource = _resources.get(uri);
			if (resource == null)
			{ 
				resource = newResource(uri);
				//presentity.addListener(_presenceListener);
				_resources.put(uri, resource);
			}
			return resource;
		}
	}
	
	
	
}
