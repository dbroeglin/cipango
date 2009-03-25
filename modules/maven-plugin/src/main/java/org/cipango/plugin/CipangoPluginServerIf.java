package org.cipango.plugin;

import org.cipango.log.AccessLog;
import org.cipango.sip.SipConnector;
import org.mortbay.jetty.plugin.util.JettyPluginServer;

public interface CipangoPluginServerIf extends JettyPluginServer
{

	public void setSipConnectors(SipConnector[] connectors) throws Exception;
	
    public SipConnector[] getSipConnectors();
    
	public SipConnector[] createDefaultSipConnectors(String host, String port) throws Exception;
	
	public void setMessageLogger(AccessLog messageLog, String buildDirectory);
}
