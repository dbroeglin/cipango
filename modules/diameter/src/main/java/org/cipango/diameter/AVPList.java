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

import java.util.AbstractList;
import java.util.ArrayList;

import org.cipango.diameter.base.Base;

public class AVPList extends AbstractList<AVP>
{
	private ArrayList<AVP> _avps = new ArrayList<AVP>();

	@Override
	public void add(int index, AVP avp)
	{
		_avps.add(index, avp);
	}
	
	@Override
	public AVP get(int index)
	{
		return _avps.get(index);
	}

	@Override
	public int size()
	{
		return _avps.size();
	}	
	
	public AVP getAVP(int code)
	{
		return getAVP(Base.IETF_VENDOR_ID, code);
	}
	
	public AVP getAVP(int vendorId, int code)
	{
		for (int i = 0; i < _avps.size(); i++)
		{
			AVP avp = _avps.get(i);
			if (avp.getVendorId() == vendorId && avp.getCode() == code)
				return avp;
		}
		return null;
	}
	
	public void addString(int code, String value)
	{
		addString(Base.IETF_VENDOR_ID, code, value);
	}
	
	public void addInt(int code, int value)
	{
		addInt(Base.IETF_VENDOR_ID, code, value);
	}
	
	public void addInt(int vendorId, int code, int value)
	{
		add(AVP.ofInt(vendorId, code, value));
	}
	
	public void addString(int vendorId, int code, String value)
	{
		add(AVP.ofString(vendorId, code, value));
	}
		
	public int getInt(int code)
	{
		return getInt(Base.IETF_VENDOR_ID, code);
	}
	
	public int getInt(int vendorId, int code)
	{
		AVP avp = getAVP(vendorId, code);
		if (avp != null)
			return avp.getInt();
		return -1;
	}
	
	public String getString(int code)
	{
		return getString(Base.IETF_VENDOR_ID, code);
	}
	
	public String getString(int vendorId, int code)
	{
		AVP avp = getAVP(vendorId, code);
		if (avp != null)
			return avp.getString();
		return null;
	}
}
