package org.cipango.ims;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Say {

	public static void main(String[] args) throws Exception {
		Hello hello = new Hello();
		Field[] fields= Hello.class.getDeclaredFields();
		Method method;

		fields[0].setAccessible(true);
		System.out.println(fields[0].get(hello));
	}
}
