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

package org.cipango.diameter.log;

import org.cipango.diameter.node.DiameterConnection;
import org.cipango.diameter.node.DiameterMessage;
import org.eclipse.jetty.util.log.Log;

public class BasicMessageLog implements DiameterMessageListener
{
	enum Direction { IN, OUT }
	
	protected void doLog(Direction direction, DiameterMessage message, DiameterConnection connection)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(connection.getLocalAddr());
		sb.append(':');
		sb.append(connection.getLocalPort());
		sb.append((direction == Direction.IN ? " < " : " > "));
		sb.append(connection.getRemoteAddr());
		sb.append(':');
		sb.append(connection.getRemotePort());
		sb.append(' ');
		sb.append(message);
		
		Log.info(sb.toString());
	}
	
	public void messageReceived(DiameterMessage message, DiameterConnection connection) 
	{
		doLog(Direction.IN, message, connection);
	}

	public void messageSent(DiameterMessage message, DiameterConnection connection) 
	{
		doLog(Direction.OUT, message, connection);
	}
}
