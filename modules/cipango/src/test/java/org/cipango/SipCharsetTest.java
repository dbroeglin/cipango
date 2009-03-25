/// ========================================================================
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
package org.cipango;

import junit.framework.TestCase;

public class SipCharsetTest extends TestCase
{
	public void testCharset()
	{
		SipCharset charset = new SipCharset("cipango");
		assertTrue(charset.contains('c'));
		assertTrue(charset.contains('i'));
		assertTrue(charset.contains('p'));
		assertTrue(charset.contains('a'));
		assertTrue(charset.contains('n'));
		assertTrue(charset.contains('g'));
		assertTrue(charset.contains('o'));
		assertFalse(charset.contains(' '));
		assertFalse(charset.contains('\r'));
		assertFalse(charset.contains('C'));
	}
}
