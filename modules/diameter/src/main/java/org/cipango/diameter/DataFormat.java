package org.cipango.diameter;

import org.cipango.diameter.io.AbstractCodec;

/**
 * Base class for AVP data format.
 * 
 * @param <T>
 */
public abstract class DataFormat<T> extends AbstractCodec<T>
{
	private String _name;
	
	public DataFormat(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String toString()
	{
		return _name;
	}
}
