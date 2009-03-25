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

import junit.framework.TestCase;

public class IDTest extends TestCase
{

	public void testGetCallId()
	{
		testGetCallId(ID.newCallID());
		testGetCallId("71fc337dfa784a764dfee73bb3387a14%%192.168.1.205@nexcom-nicolas");
		testGetCallId("71fc337dfa784a764dfee73bb3387a14%192.168.1.205@nexcom-nicolas");
	}
	
	public void testGetCallId(String initial)
	{
		String[] callIDs = new String[10];
		callIDs[0] = initial;
		for (int i = 1; i < 5; i++)
		{
			callIDs[i] = ID.newCallId(callIDs[i - 1]);
			assertEquals("Failed to compare call-IDs at i = " + i, callIDs[0], ID.getCallId(callIDs[i]));
			for (int j = 0; j < i; j++)
				assertNotSame(callIDs[i], callIDs[j]);
		}
	}
}
