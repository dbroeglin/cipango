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

import java.util.Random;

/**
 * State information for a resource.
 * 
 * @see <a href="http://www.faqs.org/rfcs/rfc3903.html">RFC 3903</a>
 */
public class State
{
	private static Random __random = new Random();
	
	public static synchronized String newETag()
	{
		return Integer.toString(__random.nextInt(), Character.MAX_RADIX);
	}
	
	private String _etag;
	private String _contentType;
	private Object _content;
	private Resource _resource;
	
	public State(Resource resource, String contentType, Object content)
	{
		_resource = resource;
		setContent(contentType, content);
	}
	
	public void setContent(String contentType, Object content)
	{
		_contentType = contentType;
		_content = content;
		updateETag();
	}
	
	public void updateETag()
	{
		_etag = newETag();
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
	
	public boolean equals(Object o)
	{
		if (!(o instanceof State)) return false;
		
		return ((State) o).getETag().equals(_etag);
	}
	
	public String toString()
	{
		return _resource + "/" + _etag + "= " + _content;
	}
}
