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

import org.cipango.kaleo.util.ID;

/**
 * State information for a resource.
 * 
 * @see <a href="http://www.faqs.org/rfcs/rfc3903.html">RFC 3903</a>
 */
public class State
{
	private String _etag;
	private String _contentType;
	private Object _content;
	
	public State(String contentType, Object content)
	{
		_contentType = contentType;
		_content = content;
		_etag = ID.newETag();
	}
	
	public State resetETag()
	{
		_etag = ID.newETag();
		return this;
	}
	
	public String getETag()
	{
		return _etag;
	}
	
	public Object getContent()
	{
		return _content;
	}
	
	public String getContentType()
	{
		return _contentType;
	}
}
