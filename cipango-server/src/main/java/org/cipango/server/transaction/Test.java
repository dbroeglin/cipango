package org.cipango.server.transaction;

public class Test {

	private String[] s = new String[] { null, null, null };
	private String[] s2 = new String[3];
	
	public static void main(String[] args) {
		Test test = new Test();
		for (int i = 0; i < 3; i++)
		{
			System.out.println(test.s[i] + "/" + test.s2[i]);
		}
	}
	
}
