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

package org.cipango.diameter.ims;

public abstract class IMS 
{
	public static final int IMS_VENDOR_ID = 10415;
	public static final int CX_APPLICATION_ID = 16777216;

	public static final int 
		UAR = 300, 
		UAA = 300,
		SAR = 301,
		SAA = 301, 
		LIR = 302,
		LIA = 302,
		MAR = 303, 
		MAA = 303,
		BIR = 310,
		BIA = 310;
	
	public static final int 
		TRANSACTION_IDENTIFIER = 401,
		NAF_ID = 402,
		KEY_EXPIRY_TIME = 404,
		ME_KEY_MATERIAL = 405,
		
		PUBLIC_IDENTITY = 601,
		SERVER_NAME = 602,
		USER_DATA = 606,
		SIP_NUMBER_AUTH_ITEMS = 607,
		VISITED_NETWORK_IDENTIFIER = 600,
		
		SIP_AUTHENTICATION_SCHEME = 608,
		SIP_AUTHENTICATE = 609,
		SIP_AUTHORIZATION = 610,
		SIP_AUTH_DATA_ITEM = 612,
		SERVER_ASSIGNMENT_TYPE = 614,
		USER_AUTHORIZATION_TYPE = 623,
		USER_DATA_ALREADY_AVAILABLE = 624,
		CONFIDENTIALITY_KEY = 625,
		INTEGRITY_KEY = 626,
		ASSOCIATED_IDENTITIES = 632,
		ORIGININATING_REQUEST = 633,
		WILCARDED_PSI = 634,
		SIP_DIGEST_AUTHENTICATE = 635,
		WILCARDED_IMPU = 636,
		UAR_FLAGS = 637;
	
	public static final int 
		// Success
		DIAMETER_FIRST_REGISTRATION = 2001,
		DIAMETER_SUBSEQUENT_REGISTRATION = 2002,
		
		// Permanent Failures
		DIAMETER_ERROR_USER_UNKNOWN = 5001,
		DIAMETER_ERROR_IDENTITIES_DONT_MATCH = 5002,
		DIAMETER_ERROR_IDENTITY_NOT_REGISTERED = 5003,
		DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED = 5005,
		DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED = 5006;
}
