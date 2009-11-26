//========================================================================
//$Id: Jetty6RunMojo.java 2299 2008-01-03 23:40:50Z janb $
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

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.cipango.Server;
import org.cipango.diameter.AbstractDiameterConnector;
import org.cipango.diameter.DiameterConnector;
import org.cipango.diameter.Node;
import org.cipango.diameter.Peer;
import org.cipango.diameter.app.DiameterConfiguration;
import org.cipango.diameter.log.FileMessageLog;
import org.cipango.log.AccessLog;
import org.cipango.sip.SipConnector;
import org.mortbay.jetty.plugin.Jetty6RunMojo;
import org.mortbay.jetty.plugin.util.JettyPluginServer;
import org.mortbay.jetty.plugin.util.PluginLog;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.util.LazyList;

/**
 *  <p>
 *  This goal is used in-situ on a Maven project without first requiring that the project 
 *  is assembled into a war, saving time during the development cycle.
 *  The plugin forks a parallel lifecycle to ensure that the "compile" phase has been completed before invoking Jetty. This means
 *  that you do not need to explicity execute a "mvn compile" first. It also means that a "mvn clean jetty:run" will ensure that
 *  a full fresh compile is done before invoking Jetty.
 *  </p>
 *  <p>
 *  Once invoked, the plugin can be configured to run continuously, scanning for changes in the project and automatically performing a 
 *  hot redeploy when necessary. This allows the developer to concentrate on coding changes to the project using their IDE of choice and have those changes
 *  immediately and transparently reflected in the running web container, eliminating development time that is wasted on rebuilding, reassembling and redeploying.
 *  </p>
 *  <p>
 *  You may also specify the location of a jetty.xml file whose contents will be applied before any plugin configuration.
 *  This can be used, for example, to deploy a static webapp that is not part of your maven build. 
 *  </p>
 *  <p>
 *  There is a <a href="run-mojo.html">reference guide</a> to the configuration parameters for this plugin, and more detailed information
 *  with examples in the <a href="http://docs.codehaus.org/display/JETTY/Maven+Jetty+Plugin">Configuration Guide</a>.
 *  </p>
 * @author janb
 * 
 * @goal run
 * @requiresDependencyResolution runtime
 * @execute phase="test-compile"
 * @description Runs Cipango directly from a maven project
 */
public class CipangoRunMojo extends Jetty6RunMojo
{
	
	public static final String SIP_PORT_PROPERTY = "sip.port";
	public static final String SIP_HOST_PROPERTY = "sip.host";
	

	protected CipangoPluginServerIf plugin;
	
    /**
     * List of sip connectors to use. If none are configured
     * then UDP and TCP connectors at port 5060 and on first public address. 
     * 
     * You can override this default port number  and host by using the system properties
     *  {{sip.port}} and {{sip.host}} on the command line, eg:  
     *  {{mvn -Dsip.port=9999 -Dsip.host=localhost cipango:run}}.
     * 
     * @parameter 
     */
    private SipConnector[] sipConnectors;
    
    
    /**
     * The sip messages logger to use.
     * If none are configured, then a file message logger is created in the directory
     * <code>target/logs</code>. 
     * @parameter 
     */
    private AccessLog messageLog;
    
    /**
     * A sipdefault.xml file to use instead
     * of the default for the sipapp. Optional.
     *
     * @parameter
     */
    protected File sipDefaultXml;
    
    /**
     * A sip.xml file to be applied AFTER the webapp's sip.xml file. Useful for
     * applying different build profiles, eg test, production etc. Optional.
     * @parameter
     */
    protected File overrideSipXml;
    
    /**
     * Allow to disable annotations parsing.
     * @parameter default-value="true"
     */
    protected boolean annotationsEnabled;
    
    /**
     * Diameter node
     * @parameter
     */
    protected Node diameterNode;
    
    /**
     * A properties file containing system properties to set.
     * Note that these properties will NOT override System properties 
     * that have been set on the command line or by the JVM. Optional. 
     * @parameter
     */
    protected File systemPropertiesFile;
    
    /**
     * @see org.cipango.plugin.AbstractJettyRunMojo#createServer()
     */
    public JettyPluginServer createServer()
    {
    	plugin = new CipangoPluginServer();
        return plugin;
    }
        
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        super.execute();
    }


	@Override
	public void finishConfigurationBeforeStart() throws Exception
	{
		if (systemPropertiesFile != null)
		{
			Properties properties = new Properties();
			properties.load(new FileInputStream(systemPropertiesFile));
			properties.putAll(System.getProperties());
			System.setProperties(properties);
		}
		
        plugin.setSipConnectors(sipConnectors);
        SipConnector[] connectors = plugin.getSipConnectors();

        if (connectors == null|| connectors.length == 0)
        {
            //if a SystemProperty -Dsip.port=<portnum> has been supplied, use that as the default port
        	sipConnectors = plugin.createDefaultSipConnectors(
            		System.getProperty(SIP_HOST_PROPERTY, null),
            		System.getProperty(SIP_PORT_PROPERTY, null));
            plugin.setSipConnectors(sipConnectors);
        }
        
        plugin.setMessageLogger(messageLog, project.getBuild().getDirectory());
        
        if (diameterNode != null)
        {
        	Server sipServer = plugin.getServer();
        	diameterNode.setServer(sipServer);
        	sipServer.setAttribute(Node.class.getName(), diameterNode);
           	webAppConfig.addConfiguration(new DiameterConfiguration());
           	DiameterConnector connector = diameterNode.getConnectors()[0];
        	PluginLog.getLog().info("Diameter port = " + connector.getPort());
        	if (connector instanceof AbstractDiameterConnector)
        	{
        		AbstractDiameterConnector c = (AbstractDiameterConnector) connector;
        		if (c.getMessageListener() == null)
        		{
        			FileMessageLog log = new FileMessageLog();
        			log.setFilename(project.getBuild().getDirectory() + "/logs/yyyy_mm_dd.diameter.log");
        			c.setMessageListener(log);
        		}
        	}
        	sipServer.addLifeCycle(diameterNode);
        }

		super.finishConfigurationBeforeStart();
	}

	@Override
	public void configureWebApplication () throws Exception
	{
		super.configureWebApplication();
		
		if (sipDefaultXml != null)
            webAppConfig.setDefaultsSipDescriptor(sipDefaultXml.getCanonicalPath());
        if (overrideSipXml != null)
            webAppConfig.setOverrideSipDescriptor(overrideSipXml.getCanonicalPath());
        webAppConfig.setAnnotationsEnabled(annotationsEnabled);
        
        getLog().info("Sip defaults = "+(webAppConfig.getDefaultsSipDescriptor()==null?" cipango default":webAppConfig.getDefaultsSipDescriptor()));
        getLog().info("Sip overrides = "+(webAppConfig.getOverrideSipDescriptor()==null?" none":webAppConfig.getOverrideSipDescriptor()));

	}
    
    
}
