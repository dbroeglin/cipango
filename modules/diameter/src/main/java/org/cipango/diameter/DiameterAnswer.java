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

import java.io.IOException;

import org.cipango.diameter.base.Base;

public class DiameterAnswer extends DiameterMessage
{
	private DiameterRequest _request;
	private int _resultCode = -1;
	private int _vendorId;
	
	public DiameterAnswer()
	{	
	}
	
	public DiameterAnswer(DiameterRequest request, int vendorId, int resultCode)
	{
		super(request);
		_request = request;
		_vendorId = vendorId;
		_resultCode = resultCode;
		
		if (_vendorId == Base.IETF_VENDOR_ID)
		{
			_avps.addInt(Base.RESULT_CODE, resultCode);
		}
		else
		{
			AVP expRc = AVP.ofAVPs(Base.EXPERIMENTAL_RESULT,
					AVP.ofInt(Base.VENDOR_ID, _vendorId),
					AVP.ofInt(Base.EXPERIMENTAL_RESULT_CODE, _resultCode));
			_avps.add(expRc);
		}
	}
	
	public void setRequest(DiameterRequest request)
	{
		_request = request;
	}
	
	public DiameterRequest getRequest()
	{
		return _request;
	}
	
	public boolean isRequest()
	{
		return false;
	}
	
	public int getResultCode()
	{
		if (_resultCode == -1)
		{
			AVP avp = _avps.getAVP(Base.RESULT_CODE);
			if (avp != null)
			{
				_resultCode = avp.getInt();
			}
			else 
			{
				avp = _avps.getAVP(Base.EXPERIMENTAL_RESULT);
				_resultCode = avp.getGrouped().getInt(Base.EXPERIMENTAL_RESULT_CODE);
			}
		}
		return _resultCode;
	}
	
	public void setResultCode(int resultCode)
	{
		_resultCode = resultCode;
	}
	
	public int getVendorId()
	{
		return _vendorId;
	}
	
	public void send() throws IOException
	{
		_request.getConnection().write(this);
	}
}