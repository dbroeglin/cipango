package org.cipango.ims;

import java.net.InetAddress;

import org.cipango.diameter.AVP;
import org.cipango.diameter.ApplicationId;
import org.cipango.diameter.api.DiameterFactory;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.node.DiameterRequest;
import org.cipango.diameter.node.Node;
import org.cipango.diameter.node.Peer;
import org.cipango.diameter.node.SessionManager;

public class SCSCF 
{
	/*
	public static void main(String[] args) throws Exception
	{
		Node node = new Node(3869);
		node.setIdentity("thomas.nexcom.voip");
		node.setRealm("cipango.org");
	
		
		Peer peer = new Peer("hss.open-ims.test");
		peer.setAddress(InetAddress.getByName("192.168.1.205"));
		peer.setNode(node);	
		
		Peer peer = new Peer("cipango");
		peer.setAddress(InetAddress.getLocalHost());
		peer.setNode(node);	
		
		node.addPeer(peer);
		
		node.start();
	
		
		Thread.sleep(3000);
		
		ApplicationId appId = new ApplicationId(ApplicationId.Type.Auth, IMS.CX_APPLICATION_ID, IMS.IMS_VENDOR_ID);
		
		DiameterFactory factory = new DiameterFactory();
		factory.setNode(node);
		
		//DiameterRequest request = factory.createRequest(appId, IMS.SAR, "open-ims.test", "hss.open-ims.test");
		DiameterRequest request = factory.createRequest(appId, IMS.SAR, "open-ims.test", "cipango");
		
		
		request.add(AVP.ofString(Base.USER_NAME, "alice@open-ims.test"));
		request.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY, "sip:alice@open-ims.test"));
		request.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, "sip:thomas.nexcom.voip"));
		request.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.SERVER_ASSIGNMENT_TYPE, 3));
		request.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.USER_DATA_ALREADY_AVAILABLE, 0));
		
		request.send();
	
		DiameterRequest mar = factory.createRequest(appId, IMS.MAR, "open-ims.test", "cipango");
		
		
		mar.add(AVP.ofString(Base.USER_NAME, "alice@open-ims.test"));
		mar.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY, "sip:alice@open-ims.test"));
		mar.add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.SIP_AUTH_DATA_ITEM, 
				AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SIP_AUTHENTICATION_SCHEME, "SIP Digest")));
		mar.add(AVP.ofInt(IMS.IMS_VENDOR_ID,IMS.SIP_NUMBER_AUTH_ITEMS, 1));
		mar.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, "sip:thomas.nexcom.voip"));
		
		mar.send();
	
		/*
		DiameterRequest sar = new DiameterRequest(node, IMS.SAR, IMS.CX_APPLICATION_ID, "thomas.nexcom.voip;123456789");
		sar.add(AVP.ofAVPs(Base.VENDOR_SPECIFIC_APPLICATION_ID, 
				AVP.ofInt(Base.VENDOR_ID, IMS.IMS_VENDOR_ID),
				AVP.ofInt(Base.AUTH_APPLICATION_ID, IMS.CX_APPLICATION_ID)));
		sar.add(AVP.ofInt(Base.AUTH_SESSION_STATE, 1));
		sar.add(AVP.ofString(Base.DESTINATION_REALM, "open-ims.test"));
		//sar.add(AVP.ofString(Base.DESTINATION_HOST, "hss.open-ims.test"));
		sar.add(AVP.ofString(Base.USER_NAME, "alice@open-ims.test"));
		sar.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY, "sip:alice@open-ims.test"));
		sar.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, "sip:thomas.nexcom.voip"));
		sar.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.SERVER_ASSIGNMENT_TYPE, 3));
		sar.add(AVP.ofInt(IMS.IMS_VENDOR_ID, IMS.USER_DATA_ALREADY_AVAILABLE, 0));
		
		
		
		DiameterRequest mar = new DiameterRequest(node, IMS.MAR, IMS.CX_APPLICATION_ID, "thomas.nexcom.voip;123456789");
		mar.add(AVP.ofAVPs(Base.VENDOR_SPECIFIC_APPLICATION_ID, 
				AVP.ofInt(Base.VENDOR_ID, IMS.IMS_VENDOR_ID),
				AVP.ofInt(Base.AUTH_APPLICATION_ID, IMS.CX_APPLICATION_ID)));
		mar.add(AVP.ofInt(Base.AUTH_SESSION_STATE, 1));
		mar.add(AVP.ofString(Base.DESTINATION_REALM, "open-ims.test"));
		mar.add(AVP.ofString(Base.DESTINATION_HOST, "hss.open-ims.test"));
		
		mar.add(AVP.ofString(Base.USER_NAME, "alice@open-ims.test"));
		mar.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY, "sip:alice@open-ims.test"));
		mar.add(AVP.ofAVPs(IMS.IMS_VENDOR_ID, IMS.SIP_AUTH_DATA_ITEM, 
				AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SIP_AUTHENTICATION_SCHEME, "SIP Digest")));
		mar.add(AVP.ofInt(IMS.IMS_VENDOR_ID,IMS.SIP_NUMBER_AUTH_ITEMS, 1));
		mar.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.SERVER_NAME, "sip:thomas.nexcom.voip"));
		
		mar.send();
		
		
		
	}*/
}
