package org.cipango.ims;

import org.cipango.diameter.Node;

public class RfServer 
{
	public static void main(String[] args) throws Exception
	{
		Node node = new Node(3868);
		node.setIdentity("charging.cipango.org");
		node.setRealm("cipango.org");
		
		node.start();
	}
}
