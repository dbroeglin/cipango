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

package org.cipango.diameter;

import java.util.Collections;
import java.util.List;

import org.cipango.diameter.base.Common;

/**
 * the Application Identifier is used to identify a specific Diameter
 * Application. There are standards-track application ids and vendor specific
 * application ids.
 * 
 * IANA [IANA] has assigned the range 0x00000001 to 0x00ffffff for
 * standards-track applications; and 0x01000000 - 0xfffffffe for vendor specific
 * applications, on a first-come, first-served basis. The following values are
 * allocated.
 * <ul>
 * 	<li>Diameter Common Messages 0 
 *  <li>NASREQ 1
 *  <li>Mobile-IP 2 
 *  <li>Diameter Base Accounting 3 
 *  <li>Relay 0xffffffff
 *  </ul>
 * 
 * Assignment of standards-track application IDs are by Designated Expert with
 * Specification Required [IANA].
 * 
 * Both Application-Id and Acct-Application-Id AVPs use the same Application
 * Identifier space.
 * 
 * Vendor-Specific Application Identifiers, are for Private Use. Vendor-Specific
 * Application Identifiers are assigned on a First Come, First Served basis by
 * IANA.
 */
public class ApplicationId
{
	public static enum Type { Acct, Auth }
	
	private int _id;
	private Type _type;
	private List<Integer> _vendors;
	
	public ApplicationId(Type type, int id, List<Integer> vendors)
	{
		_id = id;
		_type = type;
		_vendors = vendors;
	}
	
	public ApplicationId(Type type, int id, int vendor)
	{
		this(type, id, Collections.singletonList(vendor));
	}
	
	public ApplicationId(Type type, int id)
	{
		this(type, id, null);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public boolean isAuth()
	{
		return (_type == Type.Auth);
	}
	
	public boolean isAcct()
	{
		return (_type == Type.Acct);
	}
	
	public boolean isVendorSpecific()
	{
		return _vendors != null && _vendors.size() != 0;
	}
	
	public List<Integer> getVendors()
	{
		return _vendors;
	}
	
	public AVP<?> getAVP()
	{
		AVP<Integer> appId;
		if (_type == Type.Auth)
			appId = new AVP<Integer>(Common.AUTH_APPLICATION_ID, _id);
		else 
			appId = new AVP<Integer>(Common.ACCT_APPLICATION_ID, _id);
		
		if (_vendors != null && _vendors.size() > 0)
		{
			AVP<AVPList> vsai = new AVP<AVPList>(Common.VENDOR_SPECIFIC_APPLICATION_ID, new AVPList());
			for (Integer vendorId : _vendors)
			{
				vsai.getValue().add(Common.VENDOR_ID, vendorId);
			}
			vsai.getValue().add(appId);
			return vsai;
		}
		else
			return appId;
	}
	
	public String toString()
	{
		return _id + _vendors.toString();
	}
	
	/*
	public static ApplicationId ofAVP(DiameterMessage message)
	{
		List<AVP> list = message.getAVPs();
		for (int i = 0; i < list.size(); i++)
		{
			AVP avp = list.get(i);
			if (avp.getVendorId() == 0)
			{
				switch(avp.getCode())
				{
				case Base.ACCT_APPLICATION_ID:
					return new ApplicationId(Type.Acct, avp.getInt());
				case Base.AUTH_APPLICATION_ID:
					return new ApplicationId(Type.Auth, avp.getInt());
				case Base.VENDOR_SPECIFIC_APPLICATION_ID:
					List<AVP> vsai = avp.getGrouped();
					
					int id = -1;
					Type type = null;
					List<Integer> vendors = new ArrayList<Integer>();
					
					for (int j = 0; j < vsai.size(); j++)
					{
						AVP avp2 = vsai.get(j);
						switch (avp2.getCode())
						{
						case Base.ACCT_APPLICATION_ID:
							id = avp2.getInt();
							type = Type.Acct;
							break;
						case Base.AUTH_APPLICATION_ID:
							id = avp2.getInt();
							type = Type.Auth;
							break;
						case Base.VENDOR_ID:
							vendors.add(avp2.getInt());
							break;
						}
					}
					return new ApplicationId(type, id, vendors);
				}
			}
		}
		return null;
	}
	*/
}
