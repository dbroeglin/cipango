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

package org.cipango.kaleo.presence;

import java.util.Random;
import org.cipango.kaleo.event.State;

public class SoftState extends State
{
	private static Random __random = new Random();
	
	public static synchronized String newETag()
	{
		return Integer.toString(Math.abs(__random.nextInt()), Character.MAX_RADIX);
	}
	
	private String _etag;
	
	public SoftState(String contentType, Object content)
	{
		super(contentType, content);
	}
	
	public void setContent(String contentType, Object content)
	{
		super.setContent(contentType, content);
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
	
	public boolean equals(Object o)
	{
		if (!(o instanceof SoftState)) return false;
		
		return ((SoftState) o).getETag().equals(_etag);
	}
	
	public String toString()
	{
		return  _etag + "= " + getContent();
	}
}
