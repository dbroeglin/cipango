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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationService 
{
	private Map<String, List<Binding>> _bindings = new HashMap<String, List<Binding>>();
	
	public List<Binding> getBindings(String uri)
	{
		return _bindings.get(uri);
	}
	
	public void removeBinding(Binding binding)
	{
		synchronized (_bindings)
		{
			
		}
	}
	
	public void addBinding(Binding binding)
	{
		synchronized(_bindings)
		{
			List<Binding> bindings = _bindings.get(binding.getAOR());
			if (bindings == null)
			{
				bindings = new ArrayList<Binding>(1);
				_bindings.put(binding.getAOR(), bindings);
			}
			bindings.add(binding);
		}
	}
}
