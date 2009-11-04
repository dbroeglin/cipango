package org.cipango.diameter;

import org.cipango.diameter.base.Base;

public class Factory 
{
	public static <T> Type<T> newType(String name, int vendorId, int code, DataFormat<T> dataFormat)
	{
		return new Type<T>(vendorId, code, name, dataFormat);
	}
	
	public static <T> Type<T> newType(String name, int code, DataFormat<T> dataFormat)
	{
		return newType(name, Base.IETF_VENDOR_ID, code, dataFormat);
	}
	
	public static DiameterCommand newCommand(boolean request, int code, String name, boolean proxiable)
	{
		return new DiameterCommand(request, code, name, proxiable);
	}
	
	public static DiameterCommand newRequest(int code, String name)
	{
		return new DiameterCommand(true, code, name);
	}
	
	public static DiameterCommand newAnswer(int code, String name)
	{
		return new DiameterCommand(false, code, name);
	}
	
	public static ResultCode newResultCode(int code, String name)
	{
		return new ResultCode(Base.IETF_VENDOR_ID, code, name);
	}
	
	public static ResultCode newResultCode(int vendorId, int code, String name)
	{
		return new ResultCode(vendorId, code, name);
	}
}
