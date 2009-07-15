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

public abstract class AbstractResource implements Resource
{
	private String _uri;
	
	private Map<String, Subscription> _subscriptions = new HashMap<String, Subscription>();
	
	public AbstractResource(String uri)
	{
		_uri = uri;
	}
	
	public String getUri()
	{
		return _uri;
	}
}
