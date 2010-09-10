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
package org.cipango.server;

import javax.servlet.sip.Address;

import org.cipango.server.B2bHelper;
import org.cipango.sip.NameAddr;

import junit.framework.TestCase;

public class B2bHelperTest extends TestCase
{
	public void testMergeContact() throws Exception
	{
		Address destination = new NameAddr("<sip:127.0.0.1:5060>");
		// FIMXE new B2bHelper(null).mergeContact("Bob <sip:bob@127.0.0.22:5070;transport=UDP;ttl=1>;p=2", destination);
		// assertEquals("Bob <sip:bob@127.0.0.1:5060;transport=UDP>;p=2", destination.toString());
	}
}
