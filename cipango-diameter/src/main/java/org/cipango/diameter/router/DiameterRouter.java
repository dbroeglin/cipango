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
package org.cipango.diameter.router;

import org.cipango.diameter.node.DiameterRequest;
import org.cipango.diameter.node.Peer;


public interface DiameterRouter
{

	/**
	 * Returns the first open peer which can send the request.
	 * 
	 * @param request the request for which a peer should be find.
	 * @return the first open peer which can send the request.
	 */
	public Peer getRoute(DiameterRequest request);
	
	public void peerAdded(Peer peer);
	
	public void peerRemoved(Peer peer);
	
}
