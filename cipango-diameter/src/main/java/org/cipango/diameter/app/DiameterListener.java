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

package org.cipango.diameter.app;

import java.io.IOException;
import java.util.EventListener;

import org.cipango.diameter.DiameterHandler;
import org.cipango.diameter.DiameterMessage;

/**
 * Callback for diameter messages. 
 */
public interface DiameterListener extends EventListener, DiameterHandler
{
	/**
	 * The <code>handle</code> method is invoked by the container when a Diameter message should be
	 * passed to the application.
	 */
	void handle(DiameterMessage message) throws IOException;
}
