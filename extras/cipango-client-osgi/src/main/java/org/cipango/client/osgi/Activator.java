// ========================================================================
// Copyright 2011 NEXCOM Systems
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
package org.cipango.client.osgi;

import java.net.InetAddress;

import org.cipango.client.SipClient;
import org.cipango.client.osgi.impl.SipServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{

	private SipClient _sipClient;
	
	public void start(BundleContext context) throws Exception
	{
		_sipClient = new SipClient(5061);
		_sipClient.start();
		SipServiceImpl serviceImpl = new SipServiceImpl();
		serviceImpl.setUserAgent(_sipClient.createUserAgent("alice", InetAddress.getLocalHost().getHostName()));
		context.registerService(SipService.class.getName(), serviceImpl, null);
	}

	public void stop(BundleContext context) throws Exception
	{
		if (_sipClient != null)
		{
			_sipClient.stop();
			_sipClient = null;
		}
	}

}
