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


/**
 * A resource. Subscribers may subscribe to a resource state and 
 * publication agent may publish event state for a resource.
 * 
 */
public interface Resource 
{
	void addState(State state, int expires);

	String getUri();

	State getState(String etag);
	
	void removeState(String etag);
	
	State modifyState(State state, int expires, String contentType, Object content);
	
	State refreshState(State state, int expires);

	class Content 
	{
		private Object _value;
		
		private String _type;

		public Content(Object value, String type) 
		{
			_value = value;
			_type = type;
		}

		public Object getValue() 
		{
			return _value;
		}

		public String getType() 
		{
			return _type;
		}
	}
}
