package org.cipango.kaleo.util;

import java.util.Random;

public class ID 
{
	private static Random __random = new Random();
	
	public static synchronized String newETag()
	{
		return Integer.toString(__random.nextInt(), Character.MAX_RADIX);
	}
}
