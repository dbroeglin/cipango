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

package org.cipango.io;

import junit.framework.TestCase;

public class SipBufferTest extends TestCase
{
	public void testBuffer() throws Exception
	{
		String s = "ŽˆÛ£";
		
		SipBuffer sh = new SipBuffer(s);
		
		byte[] b1 = sh.asArray();
		byte[] b2 = s.getBytes("UTF-8");
		
		assertEquals(b1.length, b2.length);
		for (int i = 0; i < b1.length; i++)
		{
			assertEquals(b1[i], b2[i]);
		}
		
		SipBuffer sh2 = new SipBuffer(b1);
		
		assertEquals(sh.toString(), sh2.toString());
	}
}
