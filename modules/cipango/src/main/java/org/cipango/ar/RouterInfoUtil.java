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

package org.cipango.ar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;

import org.mortbay.util.TypeUtil;

public class RouterInfoUtil 
{
	public static final String ROUTER_INFO = "X-Cipango-Router-Info";
	private static final String NEXT_APPLICATION_NAME = "appName";
	private static final String STATE_INFO = "stateInfo";
	
	public static void encode(SipURI uri, SipApplicationRouterInfo routerInfo) throws IOException
	{
		uri.setUser(ROUTER_INFO);
		
		uri.setParameter(NEXT_APPLICATION_NAME, routerInfo.getNextApplicationName());
		
		Serializable s = routerInfo.getStateInfo();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(s);
		
		String stateInfo = TypeUtil.toHexString(bout.toByteArray());
		uri.setParameter(STATE_INFO, stateInfo);		
	}
	
	public static SipApplicationRouterInfo decode(SipURI uri) throws Exception
	{
		String appName = uri.getParameter(NEXT_APPLICATION_NAME);
		
		String s = uri.getParameter(STATE_INFO);
		byte[] b = TypeUtil.fromHexString(s);
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
		Serializable stateInfo = (Serializable) in.readObject();
		
		return new SipApplicationRouterInfo(appName, null, null, null, null, stateInfo);
	}
}
