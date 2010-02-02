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

package org.cipango.media;

/**
 * This interface defines objects that can interpret DTMF reception.
 * 
 * @author yohann
 */
public interface DtmfListener {

    /**
     * This method is invoked on DtmfListener when a new telephone-event
     * occurs. This DtmfListener is notified of the character typed by
     * user, i.e. '0' to '9', '*', '#' or 'A' to 'B'.
     * 
     * @param event character decoded from incoming RTP packet.
     */
    public void telephoneEvent(char event);

}
