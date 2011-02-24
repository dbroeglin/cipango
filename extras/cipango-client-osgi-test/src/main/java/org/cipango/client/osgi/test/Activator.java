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
package org.cipango.client.osgi.test;

import java.io.IOException;

import org.cipango.client.osgi.SipService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator, ServiceListener
{
	private BundleContext _context;
	private ServiceReference _ref;
	private SipService _sipService;

	public void start(BundleContext context) throws Exception
	{
		_context = context;

		_context.addServiceListener(this, "(objectClass=" + SipService.class.getName() + ")");

		querySipService();
	}

	public void stop(BundleContext context)
	{
		if (_sipService != null)
			try
			{
				_sipService.register(0);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		_ref = null;
		_sipService = null;
		_context = null;
	}

	public synchronized void serviceChanged(ServiceEvent event)
	{
		if (event.getType() == ServiceEvent.REGISTERED)
		{
			if (_ref == null)
			{
				_ref = event.getServiceReference();
				_sipService = (SipService) _context.getService(_ref);
				sipServiceAvailable();
			}
		}
		else if (event.getType() == ServiceEvent.UNREGISTERING)
		{
			if (event.getServiceReference() == _ref)
			{
				_context.ungetService(_ref);
				_ref = null;
				_sipService = null;
				querySipService();
			}
		}
	}

	protected void querySipService()
	{
		_ref = _context.getServiceReference(SipService.class.getName());
		if (_ref != null)
		{
			_sipService = (SipService) _context.getService(_ref);
			sipServiceAvailable();
		}
	}

	protected void sipServiceAvailable()
	{
		try
		{
			_sipService.register(3600);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
