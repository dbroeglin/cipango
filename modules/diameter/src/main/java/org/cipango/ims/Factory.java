package org.cipango.ims;

public interface Factory<T>
{
	public class Int
	{
		
	}

	public class Enu extends Int
	{
		
	}
	
	T build();
}
