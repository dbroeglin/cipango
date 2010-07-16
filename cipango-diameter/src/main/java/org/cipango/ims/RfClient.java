package org.cipango.ims;

import java.net.InetAddress;

import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.Node;
import org.cipango.diameter.Peer;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.ims.Cx;

public class RfClient 
{
	public static void main(String[] args) throws Exception
	{
		Node node = new Node(3869);
		node.setIdentity("client.rf.cipango.org");
		node.setRealm("cipango.org");
		
		node.addSupportedApplication(Cx.CX_APPLICATION_ID);
		
		Peer peer = new Peer("hss.cipango.org");
		peer.setAddress(InetAddress.getByName("192.168.2.208"));
		peer.setPort(3868);
		
		node.addPeer(peer);
		node.start();
		
		while (!peer.isOpen())
			Thread.sleep(100);
		
		/*
		DiameterRequest request = new DiameterRequest(node, Accounting.ACR, Accounting.ACCOUNTING_ORDINAL, "12345");
		request.add(Common.DESTINATION_REALM, "cipango.org");
		request.add(Common.DESTINATION_HOST, "server.rf.cipango.org");
		
		request.add(Accounting.ACCOUNTING_RECORD_TYPE, Accounting.AccountingRecordType.EVENT_RECORD);
		request.add(Accounting.ACCOUNTING_RECORD_NUMBER, 1);

		request.send(); */
	}
}
