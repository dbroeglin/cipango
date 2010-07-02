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

import org.mortbay.io.BufferCache;
import org.mortbay.io.BufferCache.CachedBuffer;

public class SipMethods
{
	public static final String 
		ACK = "ACK",
		BYE = "BYE",
	 	CANCEL = "CANCEL",
	 	INFO = "INFO",
	 	INVITE = "INVITE",
	 	MESSAGE = "MESSAGE",
	 	NOTIFY = "NOTIFY",
	 	OPTIONS = "OPTIONS",
	 	PRACK = "PRACK",
	 	PUBLISH = "PUBLISH",
	 	REFER = "REFER",
	 	REGISTER = "REGISTER",
	 	SUBSCRIBE = "SUBSCRIBE",
	 	UPDATE = "UPDATE";
	
	public static final int 
		ACK_ORDINAL = 1,
		BYE_ORDINAL = 2,
		CANCEL_ORDINAL = 3,
		INFO_ORDINAL = 4,
		INVITE_ORDINAL = 5,
		MESSAGE_ORDINAL = 6,
		NOTIFY_ORDINAL = 7,
		OPTIONS_ORDINAL = 8,
		PRACK_ORDINAL = 9,
		PUBLISH_ORDINAL = 10,
		REFER_ORDINAL = 11,
		REGISTER_ORDINAL = 12,
		SUBSCRIBE_ORDINAL = 13,
		UPDATE_ORDINAL = 14;
	
	public static final BufferCache CACHE = new BufferCache();
	
	public static final CachedBuffer
		ACK_BUFFER = CACHE.add(ACK, ACK_ORDINAL),
		BYE_BUFFER = CACHE.add(BYE, BYE_ORDINAL),
		CANCEL_BUFFER = CACHE.add(CANCEL, CANCEL_ORDINAL),
		INFO_BUFFER = CACHE.add(INFO, INFO_ORDINAL),
		INVITE_BUFFER = CACHE.add(INVITE, INVITE_ORDINAL),
		MESSAGE_BUFFER = CACHE.add(MESSAGE, MESSAGE_ORDINAL),
		NOTIFY_BUFFER = CACHE.add(NOTIFY, NOTIFY_ORDINAL),
		OPTIONS_BUFFER = CACHE.add(OPTIONS, OPTIONS_ORDINAL),
		PRACK_BUFFER = CACHE.add(PRACK, PRACK_ORDINAL),
		PUBLISH_BUFFER = CACHE.add(PUBLISH, PUBLISH_ORDINAL),
		REFER_BUFFER = CACHE.add(REFER, REFER_ORDINAL),
		REGISTER_BUFFER = CACHE.add(REGISTER, REGISTER_ORDINAL),
		SUBSCRIBE_BUFFER = CACHE.add(SUBSCRIBE, SUBSCRIBE_ORDINAL),
		UPDATE_BUFFER = CACHE.add(UPDATE, UPDATE_ORDINAL);
}
	
