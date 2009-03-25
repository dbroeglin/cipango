package org.cipango.ims;

public class IntegerFactory<E extends Factory.Int> implements Factory<Factory.Int>
{
	
	
	public Factory.Int build() 
	{
		return new Int();
	}	
}
