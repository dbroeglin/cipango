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

package org.cipango.sip;
import java.util.Comparator;
import java.util.TreeMap;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferCache.CachedBuffer;
import org.junit.Test;

public class TestSipFields
{
	@Test
	public void testOrder() 
	{
		Comparator<Buffer> comparator = new Comparator<Buffer>()
		{
			public int compare(Buffer o1, Buffer o2)
			{
				if (o1 instanceof CachedBuffer && o2 instanceof CachedBuffer)
				{
					return ((CachedBuffer) o1).getOrdinal() - ((CachedBuffer) o2).getOrdinal();
				}
				return 0;
			}
		};
		TreeMap<Buffer, String> map = new TreeMap<Buffer, String>(comparator);
		
		map.put(SipHeaders.VIA_BUFFER, "via");
		map.put(SipHeaders.FROM_BUFFER, "from");
		map.put(SipHeaders.TO_BUFFER, "to");
		
		System.out.println(map);
	}
}
