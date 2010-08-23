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
package org.cipango.util;

import org.cipango.util.HexString;

import junit.framework.TestCase;

public class HexStringTest extends TestCase 
{
	public void testHex()
	{
		String s = new String(HexString.fromHexString(HexString.toHexString("hello world")));
		assertEquals("hello world", s);
		
		s = "f";
		byte[] b = HexString.fromHexString(s);
		assertEquals(15, b[0]);
	}
}
