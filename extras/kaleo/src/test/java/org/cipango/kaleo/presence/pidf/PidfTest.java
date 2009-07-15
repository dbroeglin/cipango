package org.cipango.kaleo.presence.pidf;

import org.apache.xmlbeans.XmlObject;

import junit.framework.TestCase;

public class PidfTest extends TestCase
{
	public void testParsePidf() throws Exception
	{
		PresenceDocument doc = PresenceDocument.Factory.parse(getClass().getResourceAsStream("/pidf1.xml"));
		System.out.println(doc.getPresence().getTupleArray(0).getStatus().selectChildren("urn:ietf:params:xml:ns:pidf:geopriv10", "geopriv").length);		

		XmlObject o;
		
	}
}
