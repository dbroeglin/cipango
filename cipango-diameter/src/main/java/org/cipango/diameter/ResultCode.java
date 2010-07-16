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

import org.cipango.diameter.base.Common;

public class ResultCode 
{
	private int _vendorId;
	private int _code;
	private String _name;
	
	public ResultCode(int vendorId, int code, String name)
	{
		_code = code;
		_name = name;
		_vendorId = vendorId;
	}
	
	public int getCode()
	{
		return _code;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean isInformational()
	{
		return (_code / 1000) == 1;
	}
	
	public boolean isSuccess()
	{
		return (_code / 1000) == 2;
	}
	
	public boolean isProtocolError()
	{
		return (_code / 1000) == 3;
	}
	
	public boolean isTransientFailure()
	{
		return (_code / 1000) == 4;
	}
	
	public boolean isPermanentFailure()
	{
		return (_code / 1000) == 5;
	}
	
	public int getVendorId()
	{
		return _vendorId;
	}
	
	/**
	 * Returns <code>true</code> if the AVP is an {@link Common#EXPERIMENTAL_RESULT_CODE}.
	 */
	public boolean isExperimentalResultCode()
	{
		return _vendorId != Common.IETF_VENDOR_ID;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		if (_name != null)
			sb.append(_name).append(' ');
		sb.append("(").append(_vendorId).append('/').append(_code).append(")");
		return sb.toString();
	}
	
	public AVP<?> getAVP()
	{
		if (_vendorId == Common.IETF_VENDOR_ID)
			return new AVP<Integer>(Common.RESULT_CODE, _code);
		else
		{
			AVPList expRc = new AVPList();
			expRc.add(Common.EXPERIMENTAL_RESULT_CODE, _code);
			expRc.add(Common.VENDOR_ID, _vendorId);
			return new AVP<AVPList>(Common.EXPERIMENTAL_RESULT, expRc);
		}
	}
}
