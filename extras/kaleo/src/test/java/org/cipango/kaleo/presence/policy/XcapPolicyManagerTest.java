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
package org.cipango.kaleo.presence.policy;

import java.io.File;

import junit.framework.TestCase;

import org.cipango.kaleo.presence.Presentity;
import org.cipango.kaleo.presence.policy.PolicyManager.SubHandling;
import org.cipango.kaleo.xcap.XcapService;
import org.cipango.kaleo.xcap.XcapServiceTest;
import org.cipango.kaleo.xcap.dao.FileXcapDao;

public class XcapPolicyManagerTest extends TestCase
{

	private XcapService _xcapService;
	private XcapPolicyManager _policyManager;
	private File _xcapRoot;
	
	public void setUp() throws Exception
	{
		_xcapService = new XcapService();
		FileXcapDao dao = new FileXcapDao();
		_xcapRoot = new File("target/test-data");
		_xcapRoot.mkdirs();
		dao.setBaseDir(_xcapRoot);
		_xcapService.setDao(dao);
		
		_xcapService.start();
		_xcapService.setRootName("/");
		_policyManager = new XcapPolicyManager(_xcapService);
	}
	
	public void testGetPolicyNicolas() throws Exception
	{
		setContent("/org.openmobilealliance.pres-rules/users/sip:nicolas@cipango.org/pres-rules");
		Presentity presentity = new Presentity("sip:nicolas@cipango.org");

		assertEquals(SubHandling.ALLOW, 
				_policyManager.getPolicy("sip:user@example.com", presentity));
		
		assertEquals(SubHandling.BLOCK, 
				_policyManager.getPolicy("sip:user@example.com", new Presentity("sip:unknown@cipango.org")));
		
		assertEquals(SubHandling.BLOCK,
				_policyManager.getPolicy("sip:unknown@example.com", presentity));
		
	}
	
	public void testGetPolicyIetf() throws Exception
	{
		setContent("/org.openmobilealliance.pres-rules/users/sip:ietf@cipango.org/pres-rules");
		Presentity presentity = new Presentity("sip:ietf@cipango.org");
		
		// Match rule a
		assertEquals(SubHandling.POLITE_BLOCK,
				_policyManager.getPolicy("sip:polite-block@example.com", presentity));
		// Match rule b
		assertEquals(SubHandling.ALLOW,
				_policyManager.getPolicy("sip:alice@example.com", presentity));
		// Match no rule (as use except)
		assertEquals(SubHandling.BLOCK,
				_policyManager.getPolicy("sip:except@cipango.org", presentity));
		// Match rule a and rule b on domain cipango.org but rule b is more permissive
		assertEquals(SubHandling.ALLOW,
				_policyManager.getPolicy("sip:allow@cipango.org", presentity));
		// Match no rule
		assertEquals(SubHandling.BLOCK,
				_policyManager.getPolicy("sip:otherDomain@example.com", presentity));

	}
	
	public void testGetPolicyOma() throws Exception
	{
		setContent("/org.openmobilealliance.pres-rules/users/sip:oma@cipango.org/pres-rules");
		setContent("/resource-lists/users/sip:oma@cipango.org/index");
		Presentity presentity = new Presentity("sip:oma@cipango.org");
		

		// Granted by resource list
		assertEquals(SubHandling.ALLOW,
				_policyManager.getPolicy("sip:carol@cipango.org", presentity));
		// Own URI
		assertEquals(SubHandling.ALLOW,
				_policyManager.getPolicy("sip:oma@cipango.org", presentity));
		// Other identity
		assertEquals(SubHandling.CONFIRM,
				_policyManager.getPolicy("sip:other-identity@example.com", presentity));
		// Blocked by resource list
		assertEquals(SubHandling.BLOCK,
				_policyManager.getPolicy("sip:edwige@cipango.org", presentity));
		
		
	}
	
	protected void setContent(String xcapUri) throws Exception
	{
		XcapServiceTest.setContent(_xcapService, _xcapRoot, xcapUri);
	}
	
}
