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
package org.cipango.jmx.log.event;

import java.lang.management.ManagementFactory;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.cipango.jmx.log.event.event.EventDispatcher;

public class JmxEventDispatcher extends NotificationBroadcasterSupport implements EventDispatcher, JmxEventDispatcherMBean
{

	private long _sequenceNumber = 0;
	
	public JmxEventDispatcher()
	{
		try
		{
			ManagementFactory.getPlatformMBeanServer().registerMBean(this,
					new ObjectName("org.cipango.log:type=jmxeventlogger,id=0"));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	
	public void dispatch(int eventType, String message)
	{
		Notification notification = new Notification(String.valueOf(eventType), this, _sequenceNumber++, message);
		sendNotification(notification);
	}


	public long getSequenceNumber()
	{
		return _sequenceNumber;
	}

}
