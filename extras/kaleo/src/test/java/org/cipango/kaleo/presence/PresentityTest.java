// ========================================================================
// Copyright 2009 NEXCOM Systems
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

import junit.framework.TestCase;

import org.cipango.kaleo.presence.pidf.Presence;
import org.cipango.kaleo.presence.pidf.PresenceDocument;


public class PresentityTest extends TestCase
{

	public void testGetState() throws Exception
	{		
		Presentity presentity = new Presentity("sip:alice@cipango.org");
		assertEquals(presentity.getNeutralState().getContent().toString(), 
				presentity.getState().getContent().toString());
		PresenceDocument doc1 = PresenceDocument.Factory.parse(getClass().getResourceAsStream("/org/cipango/kaleo/sipunit/publish1.xml"));
		presentity.addState(PresenceEventPackage.PIDF, doc1, 60);
		PresenceDocument doc2 = PresenceDocument.Factory.parse(getClass().getResourceAsStream("/pidf1.xml"));
		presentity.addState(PresenceEventPackage.PIDF, doc2, 60);
		Presence presence =  ((PresenceDocument) presentity.getState().getContent()).getPresence();
		assertEquals(2, presence.getTupleArray().length);
		assertEquals(doc1.getPresence().getTupleArray(0).getId(), presence.getTupleArray(0).getId());
		assertEquals(doc2.getPresence().getTupleArray(0).getId(), presence.getTupleArray(1).getId());
		
		PresenceDocument doc3 = PresenceDocument.Factory.parse(getClass().getResourceAsStream("/org/cipango/kaleo/sipunit/publish2.xml"));
		presentity.addState(PresenceEventPackage.PIDF, doc3, 60);
		presence =  ((PresenceDocument) presentity.getState().getContent()).getPresence();
		assertEquals(3, presence.getTupleArray().length);
		
		//System.out.println(presence);
	}
	
	public void testInsertSameId() throws Exception
	{		
		Presentity presentity = new Presentity("sip:alice@cipango.org");
		PresenceDocument doc1 = PresenceDocument.Factory.parse(getClass().getResourceAsStream("/org/cipango/kaleo/sipunit/publish1.xml"));
		presentity.addState(PresenceEventPackage.PIDF, doc1, 60);
		presentity.addState(PresenceEventPackage.PIDF, doc1, 60);
		Presence presence =  ((PresenceDocument) presentity.getState().getContent()).getPresence();
		assertEquals(1, presence.getTupleArray().length);
	}
}
