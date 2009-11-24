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
import org.cipango.diameter.Factory;
import org.cipango.diameter.ResultCode;
import org.cipango.diameter.Type;

public abstract class IMS extends Factory
{
	public static final int IMS_VENDOR_ID = 10415;

	protected static <T> Type<T> newIMSType(String name, int code, DataFormat<T> format) 
	{
		return newType(name, IMS_VENDOR_ID, code, format);
	}
	
	protected static ResultCode newImsResultCode(int code, String name)
	{
		return new ResultCode(IMS_VENDOR_ID, code, name);
	}
	
	public static final int 
		BIR = 310,
		BIA = 310;
	
		
	public static final int 
		GBA_USER_SEC_SETTINGS = 400,
		TRANSACTION_IDENTIFIER = 401,
		NAF_ID = 402,
		KEY_EXPIRY_TIME = 404,
		ME_KEY_MATERIAL = 405,
		GUSS_TIMESTAMP = 409;
	
}
