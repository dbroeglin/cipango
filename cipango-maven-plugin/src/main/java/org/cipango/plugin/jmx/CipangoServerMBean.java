// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.plugin.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.jetty.server.jmx.ServerMBean;

public class CipangoServerMBean extends ServerMBean
{

	public CipangoServerMBean(Object managedObject)
	{
		super(managedObject);
	}

	/*
	 * Ensure Object name is the same as normal.
	 */
	@Override
	public ObjectName getObjectName()
	{
		try
		{
			return new ObjectName("org.cipango.server:type=server,id=0");
		}
		catch (MalformedObjectNameException e)
		{
			throw new RuntimeException("Invalid name");
		}
	}

}
