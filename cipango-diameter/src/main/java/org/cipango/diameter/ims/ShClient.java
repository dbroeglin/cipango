package org.cipango.diameter.ims;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;

import javax.net.SocketFactory;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.ApplicationId;
import org.cipango.diameter.DiameterHandler;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.Node;
import org.cipango.diameter.Peer;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.base.Common.AuthSessionState;
import org.cipango.diameter.bio.DiameterSocketConnector;
import org.cipango.diameter.log.BasicMessageLog;
import org.cipango.diameter.sh.data.ShDataDocument;
import org.cipango.diameter.sh.data.TShData;
import org.eclipse.jetty.util.log.Log;

public class ShClient implements DiameterHandler
{
	public static void main(String[] args) throws Exception
	{
		
		System.out.println(SocketFactory.getDefault());
		
		Log.getLog().setDebugEnabled(true);
		
		ApplicationId sh = Sh.SH_APPLICATION_ID;
		
		Node node = new Node();
		node.setIdentity("avalon.nexcom.bzh");
		node.setHandler(new ShClient());
		
		DiameterSocketConnector connector = new DiameterSocketConnector();
		connector.setHost(InetAddress.getLocalHost().getHostAddress());
		connector.setPort(3868);
		connector.setMessageListener(new BasicMessageLog());
		
		node.addConnector(connector);
		
		Peer peer = new Peer("192.168.2.10");
		peer.setAddress(InetAddress.getByName("192.168.2.10"));
		peer.setPort(3869);
		
		node.addPeer(peer);
		
		node.start();
		
		String destinationRealm = "open-ims.net";
		String destinationHost = "192.168.2.10";
		
		while (!peer.isOpen())
			Thread.sleep(100);
		
		DiameterRequest request = new DiameterRequest(node, Sh.UDR, sh.getId(), "123456789");
		
		request.getAVPs().add(Common.DESTINATION_REALM, destinationRealm);
		if (destinationHost != null)
			request.getAVPs().add(Common.DESTINATION_HOST, destinationHost);
		
		request.getAVPs().add(sh.getAVP());
		request.getAVPs().add(Common.AUTH_SESSION_STATE, AuthSessionState.NO_STATE_MAINTAINED);
		
		AVP<AVPList> userIdentity = new AVP<AVPList>(Sh.USER_IDENTITY, new AVPList());
		userIdentity.getValue().add(Cx.PUBLIC_IDENTITY, "sip:thomas@cipango.org");
		request.getAVPs().add(userIdentity);
		
		request.getAVPs().add(Sh.DATA_REFERENCE, Sh.DataReference.IMSUserState);
		
		request.send();
	}

	public void handle(DiameterMessage message) throws IOException 
	{
		try
		{
			TShData data = TShData.Factory.parse(new ByteArrayInputStream(message.get(Sh.USER_DATA)));
			System.out.println(data.getShIMSData());
			ShDataDocument doc =  ShDataDocument.Factory.parse(new ByteArrayInputStream(message.get(Sh.USER_DATA)));
			TShData data2 = doc.getShData();
			
			System.out.println(data2.getShIMSData());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println("Sh: " + new String(message.get(Sh.USER_DATA)));
	}
}
