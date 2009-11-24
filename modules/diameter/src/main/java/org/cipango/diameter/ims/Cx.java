package org.cipango.diameter.ims;

import org.cipango.diameter.AVPList;
import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.Type;
import org.cipango.diameter.base.Base;

public class Cx 
{
	public static final int CX_APPLICATION_ID = 16777216;

	public static final int 
		SAR_ORDINAL = 301,
		SAA_ORDINAL = 301;
	
	/**
	 * The Server-Assignment-Request (SAR) command, indicated by the Command-Code field set to 301 
	 * and the ÔRÕ bit set in the Command Flags field, is sent by a Diameter Multimedia client to 
	 * a Diameter Multimedia server in order to request it to store the name of the server that is 
	 * currently serving the user.
	 * <pre> {@code
	 * <Server-Assignment-Request> ::= < Diameter Header: 301, REQ, PXY, 16777216 > 
	 * 		< Session-Id >
	 * 		{ Vendor-Specific-Application-Id } 
	 * 		{ Auth-Session-State } 
	 * 		{ Origin-Host } 
	 * 		{ Origin-Realm }
	 * 		[ Destination-Host ] 
	 * 		{ Destination-Realm } 
	 * 		[ User-Name ] 
	 * 		*[ Public-Identity ] 
	 * 		{ Server-Name } 
	 * 		{ Server-Assignment-Type } 
	 * 		{ User-Data-Already-Available } 
	 * 		*[ AVP ] 
	 * 		*[ Proxy-Info ] 
	 * 		*[ Route-Record ]
	 */
	public static final DiameterCommand SAR = IMS.newRequest(SAR_ORDINAL, "Server-Assignment-Request");
	
	public static final int 
		PUBLIC_IDENTITY_ORDINAL = 601,
		SERVER_NAME_ORDINAL = 602,
		SUPPORTED_FEATURES_ORDINAL = 628;
	
	/**
	 * The Supported-Features AVP is of type Grouped. If this AVP is present it may inform the destination host about 
	 * the features that the origin host supports. 
	 * The Feature-List AVP contains a list of supported features of the origin host. 
	 * The Vendor-Id AVP and the Feature-List AVP shall together identify which feature list is 
	 * carried in the Supported-Features AVP.
	 * <p>
	 * Where a Supported-Features AVP is used to identify features that have been defined by 3GPP, the Vendor-Id AVP 
	 * shall contain the vendor ID of 3GPP. Vendors may define proprietary features, but it is strongly recommended 
	 * that the possibility is used only as the last resort. Where the Supported-Features AVP is used to identify 
	 * features that have been defined by a vendor other than 3GPP, it shall contain the vendor ID of the specific 
	 * vendor in question.
	 * <p>
	 * If there are multiple feature lists defined by the same vendor, the Feature-List-ID AVP shall differentiate 
	 * those lists from one another. The destination host shall use the value of the Feature-List-ID AVP to identify 
	 * the feature list.
	 * 
	 * <pre> {code
	 * Supported-Features ::= < AVP header: 628 10415 > 
	 * 		{ Vendor-Id } 
	 * 		{ Feature-List-ID } 
	 * 		{ Feature-List }
	 * 		*[AVP]
	 * } </pre>
	 */
	public static final Type<AVPList> SUPPORTED_FEATURES = IMS.newIMSType("Supported-Features", 
			SUPPORTED_FEATURES_ORDINAL, Base.__grouped);
		
	/**
	 * The Public-Identity AVP is of type UTF8String. This AVP contains the public identity of a user in the IMS. 
	 * The syntax of this AVP corresponds either to a SIP URL (with the format defined in IETF RFC 3261 [3] and 
	 * IETF RFC 2396 [4]) or a TEL URL (with the format defined in IETF RFC 3966 [8])
	 */
	public static final Type<String> PUBLIC_IDENTITY = IMS.newIMSType("Public-Identity", 
			PUBLIC_IDENTITY_ORDINAL, Base.__utf8String);
	
	/**
	 * The Server-Name AVP is of type UTF8String. This AVP contains a SIP-URL (as defined in IETF 
	 * RFC 3261 [3] and IETF RFC 2396 [4]), used to identify a SIP server (e.g. S-CSCF name).
	 */
	public static final Type<String> SERVER_NAME = IMS.newIMSType("Server-Name", 
			SERVER_NAME_ORDINAL, Base.__utf8String);
	
}
