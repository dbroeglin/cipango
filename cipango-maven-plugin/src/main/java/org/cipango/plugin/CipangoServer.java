//========================================================================
//$Id: Jetty6PluginServer.java 2094 2007-09-10 06:11:26Z janb $
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.cipango.plugin;

import org.cipango.server.AbstractSipConnector;
import org.cipango.server.SipConnector;
import org.cipango.server.bio.TcpConnector;
import org.cipango.server.bio.UdpConnector;
import org.cipango.server.handler.SipContextHandlerCollection;
import org.cipango.server.log.AccessLog;
import org.cipango.server.log.FileMessageLog;
import org.mortbay.jetty.plugin.PluginLog;



public class CipangoServer extends org.cipango.plugin.JettyServer
{

	public static int DEFAULT_SIP_PORT = 5060;
	
	public SipConnector[] createDefaultSipConnectors(String host, String portnum) throws Exception
	{
		AbstractSipConnector[] sipConnectors = new AbstractSipConnector[2];
		int port = ((portnum==null||portnum.equals(""))?DEFAULT_SIP_PORT:Integer.parseInt(portnum.trim()));
		sipConnectors[0] = new UdpConnector();
		sipConnectors[1] = new TcpConnector();
		if (host != null && !host.trim().equals(""))
		{
			sipConnectors[0].setHost(host);
			sipConnectors[1].setHost(host);
		}
		sipConnectors[0].setPort(port);
		sipConnectors[1].setPort(port);

		return sipConnectors;
	}

	public SipConnector[] getSipConnectors()
	{
		return getConnectorManager().getConnectors();
	}

	public void setSipConnectors(SipConnector[] connectors) throws Exception
	{
		if (connectors==null || connectors.length==0)
            return;
        
        for (int i=0; i<connectors.length;i++)
        {
            PluginLog.getLog().debug("Setting SIP Connector: " + connectors[i].getClass().getName()
            		+ " on port "+connectors[i].getPort());
            getConnectorManager().addConnector(connectors[i]);
        }
	} 
	
	public void configureHandlers () throws Exception 
	{
		setHandler(new SipContextHandlerCollection());
		super.configureHandlers();
	}

	public void setMessageLogger(AccessLog messageLog, String buildDirectory)
	{
		if (messageLog == null)
		{
            FileMessageLog log = new FileMessageLog();
            log.setFilename(buildDirectory + "/logs/yyyy_mm_dd.message.log");
            messageLog = log;
		}
        if (getConnectorManager().getAccessLog() == null)
        	getConnectorManager().setAccessLog(messageLog);      
	}
	
}
