// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.cipango;

import org.cipango.deployer.SipAppDeployer;
import org.cipango.handler.SipContextHandlerCollection;
import org.cipango.sip.SipConnector;
import org.cipango.sip.TcpConnector;
import org.cipango.sip.UdpConnector;
import org.cipango.sipapp.SipAppContext;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.util.log.Log;

public class Main 
{
	public static void main(String[] args) throws Exception
	{
		if (args.length != 3)
        {
            System.err.println("Usage - java org.cipango.Main [<addr>:]<port> -sipapp myapp.war");
            System.err.println("Usage - java org.cipango.Main [<addr>:]<port> -sipapps webapps");
            System.exit(1);
        }
		
		try
		{
			Server server = new Server();
			
			
			/*
			MBeanServer mbean = MBeanServerFactory.createMBeanServer();
	        MBeanContainer mBeanContainer = new MBeanContainer(mbean);
	        mBeanContainer.setManagementPort(8082);
	        mBeanContainer.start();
	        server.getContainer().addEventListener(mBeanContainer);
	        */
			
			SipContextHandlerCollection contexts = new SipContextHandlerCollection();
			server.setHandler(contexts);
			
			String address = args[0];
	        String host = null;
	        int port = -1;
	        
	        int colon = address.indexOf(':');
	        if (colon < 0) 
	        {
	        	port = Integer.parseInt(address);
	        } 
	        else 
	        {
	        	host = address.substring(0, colon);
	        	port = Integer.parseInt(address.substring(colon + 1));
	        }
	        
	        UdpConnector udp = new UdpConnector();
	        TcpConnector tcp = new TcpConnector();
	        
	        udp.setHost(host); tcp.setHost(host);
	        udp.setPort(port); tcp.setPort(port);
	        
	        server.getConnectorManager().setConnectors(new SipConnector[] {udp, tcp});
			
	        Connector connector=new SocketConnector();
	        connector.setPort(8080);
	        server.setConnectors(new Connector[]{connector});
	        
	        if ("-sipapp".equals(args[1])) 
	        {
	        	SipAppContext sipapp = new SipAppContext(args[2], "/");
	        	contexts.addHandler(sipapp);
	        } 
	        else if ("-sipapps".equals(args[1])) 
	        {
	        	SipAppDeployer deployer = new SipAppDeployer();
	        	deployer.setWebAppDir(args[2]);
	        	deployer.setContexts(contexts);
	        	server.addLifeCycle(deployer);
	        }
	        
	        server.start();
		}
		catch (Exception e)
		{
			Log.warn(Log.EXCEPTION, e);
		}
	}
}
