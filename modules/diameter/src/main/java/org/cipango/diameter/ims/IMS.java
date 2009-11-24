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

import org.cipango.diameter.DataFormat;
import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.Factory;
import org.cipango.diameter.Type;
import org.cipango.diameter.base.Base;

public abstract class IMS extends Factory
{
	public static final int IMS_VENDOR_ID = 10415;

	protected static <T> Type<T> newIMSType(String name, int code, DataFormat<T> format) 
	{
		return newType(name, IMS_VENDOR_ID, code, format);
	}
	
	public static final int 
		UAR = 300, 
		UAA = 300,
		LIR = 302,
		LIA = 302,
		MAR = 303, 
		MAA = 303,
		RTR = 304,
		RTA = 304,
		PPR = 305,
		PPA = 305,
		BIR = 310,
		BIA = 310;
	
	
	
	
	
	
	
	public static final int 
		GBA_USER_SEC_SETTINGS = 400,
		TRANSACTION_IDENTIFIER = 401,
		NAF_ID = 402,
		KEY_EXPIRY_TIME = 404,
		ME_KEY_MATERIAL = 405,
		GUSS_TIMESTAMP = 409,
		
		PUBLIC_IDENTITY_ORDINAL = 601,
		SERVER_NAME_ORDINAL = 602,
		USER_DATA = 606,
		SIP_NUMBER_AUTH_ITEMS = 607,
		VISITED_NETWORK_IDENTIFIER_ORDINAL = 600,
		
		SIP_AUTHENTICATION_SCHEME = 608,
		SIP_AUTHENTICATE = 609,
		SIP_AUTHORIZATION = 610,
		SIP_AUTH_DATA_ITEM = 612,
		SERVER_ASSIGNMENT_TYPE_ORDINAL = 614,
		DERISTRATION_REASON = 615,
		REASON_CODE = 616,
		REASON_INFO = 617,
		USER_AUTHORIZATION_TYPE = 623,
		USER_DATA_ALREADY_AVAILABLE_ORDINAL = 624,
		CONFIDENTIALITY_KEY = 625,
		INTEGRITY_KEY = 626,
		ASSOCIATED_IDENTITIES = 632,
		ORIGININATING_REQUEST = 633,
		WILCARDED_PSI = 634,
		SIP_DIGEST_AUTHENTICATE = 635,
		WILCARDED_IMPU = 636,
		UAR_FLAGS = 637;
	
	/**
	 * The Visited-Network-Identifier AVP is of type OctetString. This AVP contains an identifier that helps the home network to identify the visited network (e.g. the visited network domain name)
	 */
	public static final Type<byte[]> VISITED_NETWORK_IDENTIFIER = newIMSType("Visited-Network-Identifier", 
			VISITED_NETWORK_IDENTIFIER_ORDINAL, Base.__octetString);
	
	/**
	 * The Server-Name AVP is of type UTF8String. This AVP contains a SIP-URL (as defined in IETF RFC 3261 [3] and IETF RFC 2396 [4]), used to identify a SIP server (e.g. S-CSCF name).
	 */
	public static final Type<String> SERVER_NAME = newIMSType("Server-Name", SERVER_NAME_ORDINAL, Base.__utf8String);

	public static enum ServerAssignmentType
	{
		NO_ASSIGNMENT, REGISTRATION, RE_REGISTRATION, UNREGISTERED_USER, TIMEOUT_DEREGISTRATION, USER_DEREGISTRATION,
		TIMEOUT_DEREGISTRATION_STORE_SERVER_NAME, USER_DEREGISTRATION_STORE_SERVER_NAME, ADMINISTRATIVE_DEREGISTRATION,
		AUTHENTICATION_FAILURE, AUTHENTICATION_TIMEOUT, DEREGISTRATION_TOO_MUCH_DATA
	}
	
	/**
	 * The Server-Assignment-Type AVP is of type Enumerated, and indicates the type of server update being performed 
	 * in a Server-Assignment-Request operation. The following values are defined:
	 * NO_ASSIGNMENT (0) This value is used to request from HSS the user profile assigned to one or more public 
	 * identities, without affecting the registration state of those identities.
	 * REGISTRATION (1) The request is generated as a consequence of a first registration of an identity.
	 * RE_REGISTRATION (2) The request corresponds to the re-registration of an identity.
	 * UNREGISTERED_USER (3) The request is generated because the S-CSCF received an INVITE for a public identity that is not registered.
	 * TIMEOUT_DEREGISTRATION (4) The SIP registration timer of an identity has expired.
	 * USER_DEREGISTRATION (5) The S-CSCF has received a user initiated de-registration request.
	 * TIMEOUT_DEREGISTRATION_STORE_SERVER_NAME (6) The SIP registration timer of an identity has expired. The S-CSCF keeps the user data stored in the S-CSCF and requests HSS to store the S-CSCF name.
	 * USER_DEREGISTRATION_STORE_SERVER_NAME (7) The S-CSCF has received a user initiated de-registration request. The S-CSCF keeps the user data stored in the S-CSCF and requests HSS to store the S-CSCF name.
	 * ADMINISTRATIVE_DEREGISTRATION (8) The S-CSCF, due to administrative reasons, has performed the de-registration of an identity.
	 * AUTHENTICATION_FAILURE (9) The authentication of a user has failed.
	 * AUTHENTICATION_TIMEOUT (10) The authentication timeout has expired.
	 * DEREGISTRATION_TOO_MUCH_DATA (11) The S-CSCF has requested user profile information from the HSS and has received a volume of data higher than it can accept.
	 */
	public static final Type<ServerAssignmentType> SERVER_ASSIGNMENT_TYPE = Base.newEnumType(
			"Server-Assignment-Type", IMS_VENDOR_ID, SERVER_ASSIGNMENT_TYPE_ORDINAL, ServerAssignmentType.class);
	
	public static enum UserDataAlreadyAvailable
	{
		/** (0) The S-CSCF does not have the data that it needs to serve the user */
		USER_DATA_NOT_AVAILABLE,
		
		/** (1) The S-CSCF already has the data that it needs to serve the user */
		USER_DATA_ALREADY_AVAILABLE
	}
	
	/**
	 * The User-Data-Already-Available AVP is of type Enumerated, and indicates to the HSS whether or not the S-CSCF already has the part of the user profile that it needs to serve the user. The following values are defined:
	 * @see UserDataAlreadyAvailable
	 */
	public static final Type<UserDataAlreadyAvailable> USER_DATA_ALREADY_AVAILABLE = Base.newEnumType(
			"User-Data-Already-Available", IMS_VENDOR_ID, USER_DATA_ALREADY_AVAILABLE_ORDINAL, UserDataAlreadyAvailable.class);
	
	/**
	 * The Public-Identity AVP is of type UTF8String. This AVP contains the public identity of a user in the IMS. The syntax of this AVP corresponds either to a SIP URL (with the format defined in IETF RFC 3261 [3] and IETF RFC 2396 [4]) or a TEL URL (with the format defined in IETF RFC 2806 [8]).
	 */
	public static final Type<String> PUBLIC_IDENTITY = newIMSType(
			"Public-Identity", PUBLIC_IDENTITY_ORDINAL, Base.__utf8String);
	
	
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
