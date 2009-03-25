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

package org.cipango.log;

public class EventLog
{
	private static EventLogger __log;
	
	public static final int START 					= 0;
	public static final int STOP 					= 1;
	public static final int DEPLOY_FAIL 			= 2;

	public static EventLogger getLog()
	{
		return __log;
	}

	public static void setLog(EventLogger log)
	{
		__log = log;
	}

	public static void log(int eventType, String message)
	{
		if (__log == null)
			return;
		__log.log(eventType, message);
	}
}
