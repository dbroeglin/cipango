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
package org.cipango.client;

import java.util.EventListener;

import org.cipango.client.labs.SipRequest;
import org.cipango.client.labs.SipResponse;

public interface CallListener extends EventListener
{
	
	void newIncomingCall(SipCall call, SipRequest request);

	void ringing(SipCall call, SipResponse response);
	
	void answered(SipCall call, SipResponse response);
	
	void rejected(SipCall call, SipResponse response);
	
	void redirected(SipCall call, SipResponse response);
	
	void reInvited(SipCall call, SipRequest request);
	
	void canceled(SipCall call, SipRequest request);
	
	void terminated(SipCall call, SipRequest request);
}
