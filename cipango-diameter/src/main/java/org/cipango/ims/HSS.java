package org.cipango.ims;

import org.cipango.diameter.Node;

public class HSS 
{
	public static void main(String[] args) throws Exception
	{
		Node node = new Node(3868);
		node.setIdentity("hss.cipango.org");
		node.setRealm("cipango.org");
		
		node.start();
	}
}
