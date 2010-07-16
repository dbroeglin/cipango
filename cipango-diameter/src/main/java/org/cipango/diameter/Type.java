package org.cipango.diameter;

import org.cipango.diameter.base.Common;

/**
 * AVP type. It contains the AVP name, vendorId, code and AVP data format.
 *
 * @param <T> AVP data format 
 */
public class Type<T> 
{
	private int _vendorId;
	private int _code;
	private String _name;
	private DataFormat<T> _format;
	
	public Type(int vendorId, int code, String name, DataFormat<T> format)
	{
		_vendorId = vendorId;
		_code = code;
		_name = name;
		_format = format;
	}
	
	public int getVendorId()
	{
		return _vendorId;
	}
	
	public boolean isVendorSpecific()
	{
		return _vendorId != Common.IETF_VENDOR_ID;
	}
	
	public int getCode()
	{
		return _code;
	}
	
	public DataFormat<T> getDataFormat()
	{
		return _format;
	}
	
	@Override
	public int hashCode()
	{
		return _vendorId ^_code;
	}
	
	public String toString()
	{
		return _name + " (" + _vendorId + "/" + _code + ")";
	}
}
