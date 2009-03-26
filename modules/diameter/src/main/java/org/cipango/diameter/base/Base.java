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

package org.cipango.diameter.base;

/**
 * Defines all data formats, commands and AVPs specified in Diameter Base (RFC3588).
 */
public abstract class Base 
{	
	public static final int IETF_VENDOR_ID = 0;
	
	// ======================== AVP Types ========================
	
	public static final int
		USER_NAME = 1,
		AUTH_APPLICATION_ID = 258,
		AUTH_SESSION_STATE = 277,
		ACCT_APPLICATION_ID = 259,
		SESSION_ID = 263,
		DESTINATION_HOST = 293,
		DESTINATION_REALM = 283,
		DISCONNECT_CAUSE = 273,
		ORIGIN_HOST = 264,
		ORIGIN_REALM = 296,
		ORIGIN_STATE_ID = 278,
		FIRMWARE_REVISION = 267,
		HOST_IP_ADDRESS = 257,
		RESULT_CODE = 268,
		SUPPORTED_VENDOR_ID = 265,
		VENDOR_ID = 266,
		VENDOR_SPECIFIC_APPLICATION_ID = 260,
		PRODUCT_NAME = 269,
		REDIRECT_HOST = 292,
		EXPERIMENTAL_RESULT = 297,
		EXPERIMENTAL_RESULT_CODE = 298;
	
	// Radius for digest authentication
	public static final int
		DIGEST_REALM = 104,
		DIGEST_QOP = 110,
		DIGEST_ALGORITHM = 111,
		DIGEST_HA1 = 121;
	
	// ======================== Commands ========================
	
	public static final int 
		CER = 257,
		CEA = 257, 
		DWR = 280,
		DWA = 280,
		DPR = 282,
		DPA = 282;
	
	// ======================== Result codes ========================
	
	public static final int
		DIAMETER_SUCCESS = 2001,
		DIAMETER_COMMAND_UNSUPPORTED = 3001,
		DIAMETER_MISSING_AVP = 5005,
		DIAMETER_UNABLE_TO_COMPLY = 5012;
}
