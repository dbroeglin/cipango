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

package org.cipango;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletMessage;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;

/**
 * Base interface for all Sip handlers. 
 * @see Handler
 */
public interface SipHandler
{
	void handle(SipServletMessage message) throws IOException, ServletException;
	
    void setServer(Server server);
    Server getServer();
}
