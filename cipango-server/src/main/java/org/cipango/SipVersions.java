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

import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.io.BufferCache.CachedBuffer;

public class SipVersions 
{
	public static final String 
		SIP_2_0 = "SIP/2.0";
	
	public static final BufferCache CACHE = new BufferCache();
	
	public static final int 
		SIP_2_0_ORDINAL = 2;
	
	public static final CachedBuffer 
		SIP_2_0_BUFFER = CACHE.add(SIP_2_0, SIP_2_0_ORDINAL);
}
