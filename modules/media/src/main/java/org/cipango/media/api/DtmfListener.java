// ========================================================================
// Copyright 2008-2010 NEXCOM Systems
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

package org.cipango.media.api;

/**
 * A DtmfListener is an object that needs to receive DTMF events.
 * 
 * DtmfListener implementations will be invoked when {@link DtmfFilter}
 * detect that a combination of DTMF RTP packets corresponds to one key
 * pressed event. To be invoked, a DtmfListener must be added to a DtmfFilter
 * using its {@link DtmfFilter#addDtmfListener(DtmfListener)} method.
 * 
 * @author yohann
 */
public interface DtmfListener
{

	/**
	 * Notifies this DtmfListener that a new DTMF has been detected.
	 * 
	 * @param c the character corresponding to the DTMF event.
	 */
	public void dtmfReceived(char c);

}
