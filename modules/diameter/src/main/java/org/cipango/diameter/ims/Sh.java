package org.cipango.diameter.ims;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.cipango.diameter.AVPList;
import org.cipango.diameter.ApplicationId;
import org.cipango.diameter.DataFormat;
import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.Type;
import org.cipango.diameter.base.Base;
import org.cipango.diameter.io.AbstractCodec;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import static org.cipango.diameter.Factory.*;

public class Sh 
{
	public static final int SH_APPLICATION = 16777217;
	
	public static final ApplicationId SH_APPLICATION_ID = new ApplicationId(
			org.cipango.diameter.ApplicationId.Type.Auth, 
			SH_APPLICATION, 
			IMS.IMS_VENDOR_ID);
	
	public static final int 
		UDR_ORDINAL = 306, 
		UDA_ORDINAL = 306;
	
	/**
	 * The User-Data-Request (UDR) command, indicated by the Command-Code field set to 306 and the ÔRÕ bit 
	 * set in the Command Flags field, is sent by a Diameter client to a Diameter server in order to request user data.
	 * <p>
	 * <pre> {@code
	 * < User-Data -Request> ::= < Diameter Header: 306, REQ, PXY, 16777217 > 
	 * 		< Session-Id >
	 * 		{ Vendor-Specific-Application-Id } 
	 * 		{ Auth-Session-State } 
	 * 		{ Origin-Host } 
	 * 		{ Origin-Realm }
	 * 		[ Destination-Host ] 
	 * 		{ Destination-Realm } 
	 * 		*[ Supported-Features ] 
	 * 		{ User-Identity } 
	 * 		[ Wildcarded-PSI ] 
	 * 		[ Server-Name ] 
	 * 		*[ Service-Indication ] 
	 * 		*{ Data-Reference } 
	 * 		*[ Identity-Set ] 
	 * 		[ Requested-Domain ] 
	 * 		[ Current-Location ] 
	 * 		*[ AVP ] 
	 * 		*[ Proxy-Info ] 
	 * 		*[ Route-Record ]
	 * } </pre>
	 */
	public static final DiameterCommand UDR = newRequest(UDR_ORDINAL, "User-Data-Request");
	
	/**
	 * The User-Data-Answer (UDA) command, indicated by the Command-Code field set to 306 and 
	 * the ÔRÕ bit cleared in the Command Flags field, is sent by a server in response to the 
	 * User-Data-Request command. The Experimental-Result AVP may contain one of the values 
	 * defined in section 6.2 or in 3GPP TS 29.229 [6].
	 * 
	 * <pre> {@code
	 * < User-Data-Answer > ::= < Diameter Header: 306, PXY, 16777217 > 
	 * 		< Session-Id > 
	 * 		{ Vendor-Specific-Application-Id } 
	 * 		[ Result-Code ]
	 * 		[ Experimental-Result ] 
	 * 		{ Auth-Session-State } 
	 * 		{ Origin-Host } 
	 * 		{ Origin-Realm }
	 * 		*[ Supported-Features ] 
	 * 		[ Wildcarded-PSI ]
	 * 		[ User-Data ]
	 * 		*[ AVP ] 
	 * 		*[ Failed-AVP ] 
	 * 		*[ Proxy-Info ] 
	 * 		*[ Route-Record ]
	 * } </pre>
	 */
	public static final DiameterCommand UDA = newAnswer(UDA_ORDINAL, "User-Data-Answer");
	
	public static final int 
		USER_IDENTITY_ORDINAL = 700,
		MSISDN_ORDINAL = 701, 
		USER_DATA_ORDINAL = 702,
		DATA_REFERENCE_ORDINAL = 703;
	
	/**
	 * The User-Identity AVP is of type Grouped. This AVP contains either a Public- Identity AVP or 
	 * an MSISDN AVP. 
	 * 
	 * <pre> {@code
	 * User-Identity ::= <AVP header: 700 10415> 
	 * 		[Public-Identity]
     * 		[MSISDN] 
     * 		*[AVP]
     * } </pre>
     * 
     * @see Cx#PUBLIC_IDENTITY
     * @see Sh#MSISDN
	 */
	public static final Type<AVPList> USER_IDENTITY = IMS.newIMSType("User-Identity", 
			USER_IDENTITY_ORDINAL, Base.__grouped);
	
	/**
	 * The MSISDN AVP is of type OctetString. This AVP contains an MSISDN, in international number 
	 * format as described in ITU-T Rec E.164 [8], encoded as a TBCD-string, i.e. digits from 0 through 9 
	 * are encoded 0000 to 1001; 1111 is used as a filler when there is an odd number of digits; bits 8 to 5 
	 * of octet n encode digit 2n; bits 4 to 1 of octet n encode digit 2(n-1)+1.
	 */
	public static final Type<byte[]> MSISDN = IMS.newIMSType("MSISDN", 
			MSISDN_ORDINAL, Base.__octetString);
	
	/**
	 * The User-Data AVP is of type OctetString. This AVP contains the user data requested 
	 * in the UDR/UDA, SNR/SNA and PNR/PNA operations and the data to be modified in the 
	 * PUR/PUA operation. The exact content and format of this AVP is described in 3GPP 
	 * TS 29.328 [1] Annex C as Sh-Data.
	 */
	public static final Type<byte[]> USER_DATA = IMS.newIMSType("User-Data",
			USER_DATA_ORDINAL, Base.__octetString);
	
	public static enum DataReference implements Base.CustomEnumValues
	{
		RepositoryData(0), 
		IMSPublicIdentity(10),
		IMSUserState(11),
		SCSCFName(12),
		InitialFilterCriteria(13),
		LocationInformation(14),
		UserState(15),
		ChargingInformation(16),
		MSISDN(17),
		PSIActivation(18),
		DSAI(19),
		AliasesRepositoryData(20);
		
		private int _value;
		DataReference(int value) { _value = value; }
		public int getValue() { return _value; }
	}
	
	/**
	 * The Data-Reference AVP is of type Enumerated, and indicates the type of the requested user 
	 * data in the operation UDR and SNR.
	 */
	public static Type<DataReference> DATA_REFERENCE = IMS.newIMSType("Data-Reference", 
			DATA_REFERENCE_ORDINAL, new Base.CustomEnumDataFormat<DataReference>(DataReference.class));
}
