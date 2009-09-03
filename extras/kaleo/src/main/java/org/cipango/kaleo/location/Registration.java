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

package org.cipango.kaleo.location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cipango.kaleo.Resource;

public class Registration implements Resource
{
	private String _aor;
	private List<Binding> _bindings = new ArrayList<Binding>();
	
	public Registration(String aor)
	{
		_aor = aor;
	}
	
	public String getUri()
	{
		return _aor;
	}
	
	public void addBinding(Binding binding)
	{
		synchronized (_bindings)
		{
			_bindings.add(binding);
		}
	}
	
	public List<Binding> getBindings()
	{
		return _bindings;
	}
	
	public void removeBinding(Binding binding)
	{
		synchronized (_bindings)
		{
			_bindings.remove(binding);
		}
	}
	
	public void removeAllBindings()
	{
		synchronized (_bindings)
		{
			_bindings.clear();
		}
	}
	
	public boolean isDone()
	{
		return _bindings.isEmpty();
	}
	
	public long nextTimeout()
	{
		if (_bindings.size() == 0)
			return -1;
		long time = Long.MAX_VALUE;
		
		for (Binding binding : _bindings)
		{
			if (binding.getExpirationTime() < time)
				time = binding.getExpirationTime();
		}
		return time;
	}
	
	public void doTimeout(long time)
	{
		List<Binding> expired = new ArrayList<Binding>();
		
		synchronized (_bindings)
		{
			Iterator<Binding> it = _bindings.iterator();
			while (it.hasNext())
			{
				Binding binding = it.next();
				if (binding.getExpirationTime() <= time)
				{
					it.remove();
					expired.add(binding);
				}
			}
		}
	}
}
