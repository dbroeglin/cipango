package org.cipango.kaleo.util;


public class Timer 
{
	private static java.util.Timer ___Timer = new java.util.Timer();

	public static synchronized java.util.Timer getTimer()
	{
		return ___Timer;
	}
}
