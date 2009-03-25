/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2007 Bull S.A.S.
 * Contact: jonas-team@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * --------------------------------------------------------------------------
 * $Id: Jetty6Service.java 13172 2008-03-14 09:16:08Z alitokmen $
 * --------------------------------------------------------------------------
 */

package org.cipango.jonas;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.sip.ar.spi.SipApplicationRouterProvider;

import org.cipango.Server;
import org.cipango.management.MBeanContainer;
import org.cipango.sipapp.SipAppContext;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.xml.XmlConfiguration;
import org.objectweb.util.monolog.api.BasicLevel;
import org.ow2.jonas.lib.execution.ExecutionResult;
import org.ow2.jonas.lib.execution.IExecution;
import org.ow2.jonas.lib.execution.RunnableHelper;
import org.ow2.jonas.service.ServiceException;
import org.ow2.jonas.web.JWebContainerService;
import org.ow2.jonas.web.JWebContainerServiceException;
import org.ow2.jonas.web.base.BaseWebContainerService;

/**
 * This class provides an implementation of the Jetty service (as web container
 * service).
 * @author Florent Benoit
 */
public class CipangoService extends BaseWebContainerService implements JWebContainerService {

	   /**
     * Name of the configuration file.
     */
    private static final String CIPANGO_CONFIGURATION_FILE = BaseWebContainerService.JONAS_BASE + File.separator
            + "conf" + File.separator + "cipango.xml";

    /**
     * Name of the default web.xml file.
     */
    private static final String JETTY_DEFAULT_WEB_XML_FILE = BaseWebContainerService.JONAS_BASE + File.separator
            + "conf" + File.separator + "jetty6-web.xml";
    
    /**
     * Name of the default web.xml file.
     */
    private static final String DEFAULT_SIP_XML_FILE = BaseWebContainerService.JONAS_BASE + File.separator
            + "conf" + File.separator + "cipango-sip.xml";

    public static final String MANAGEMENT_ENABLED = "org.cipango.jonas.management.enabled";
    
    /**
     * As no dependency to dar is required, just copy the property name
     */
    public static final String __J_S_DAR_CONFIGURATION = "javax.servlet.sip.ar.dar.configuration";
    
    /**
     * 
     * Configuration used to configure Cipango.
     */
    private static String config = CIPANGO_CONFIGURATION_FILE;

    /**
     * Our own instance of Jetty server.
     */
    private Server _server = null;

    /**
     * List of contexts (web-apps) deployed on the Cipango server.
     */
    private ContextHandlerCollection webAppContexts = null;

    /**
     * Default constructor.
     */
    public CipangoService() {
        super();
    }


    /**
     * Start the Jetty service in a new thread.
     * @throws ServiceException if the startup failed.
     */
    @Override
    public void doStart() throws ServiceException {
        getLogger().log(BasicLevel.LEVEL_DEBUG, "Using configuration file " + config);

        System.setProperty("cipango.home", BaseWebContainerService.JONAS_BASE);
        System.setProperty("jetty.home", BaseWebContainerService.JONAS_BASE);
        if (System.getProperty(__J_S_DAR_CONFIGURATION) == null)
        	System.setProperty(__J_S_DAR_CONFIGURATION, 
        			BaseWebContainerService.JONAS_BASE + "/conf/cipango-dar.properties");

        if (System.getProperty(SipApplicationRouterProvider.class.getName()) == null)
        	System.setProperty(SipApplicationRouterProvider.class.getName(), 
        			"org.cipango.dar.DefaultApplicationRouterProvider");

        this._server = new Server();

        List mbeanServers = MBeanServerFactory.findMBeanServer(null);
        if (mbeanServers.size() > 0 && Boolean.getBoolean(MANAGEMENT_ENABLED)) 
            _server.getContainer().addEventListener(new MBeanContainer( (MBeanServer) mbeanServers.get(0)));

        IExecution<Server> serverConfiguration = new IExecution<Server>() {
            public Server execute() throws Exception {
                XmlConfiguration configuration = new XmlConfiguration(new File(config).toURL());
                configuration.configure(_server);
                _server.start();
                return _server;
            }
        };

       // Execute
        ExecutionResult<Server> result = RunnableHelper.execute(getClass().getClassLoader(),
                                                                    serverConfiguration);
        // Throw an ServiceException if needed
        if (result.hasException()) {
            getLogger().log(BasicLevel.LEVEL_ERROR,
                           "Error has occured while starting Cipango server using configuration file "
                           + config, result.getException());
        }

        webAppContexts = (ContextHandlerCollection) _server.getHandler();
        // ... and deploy wars of the jonas.properties
        super.doStart();
        
        getLogger().log(BasicLevel.INFO, "Cipango Service started");
    }

    /**
     * Stop the Cipango service.
     * @throws ServiceException if the stop failed.
     */
    @Override
    protected void doStop() throws ServiceException {
        // Undeploy the wars ...
        super.doStop();

        // ... and shut down embedded jetty
        if (getLogger().isLoggable(BasicLevel.DEBUG)) {
            getLogger().log(BasicLevel.DEBUG, "");
        }
        if (isStarted()) {
            if (_server != null) {
                try {
                    _server.stop();
                    _server.destroy();
                    _server = null;
                    getLogger().log(BasicLevel.INFO, "Cipango Service stopped");
                } catch (Exception eExc) {
                    getLogger().log(BasicLevel.LEVEL_ERROR,
                            "error has occured while stopping Cipango server using configuration file " + config, eExc);
                 }
            }
        }
    }

    /**
     * Create the environment and delegate the operation to the implementation
     * of the web container.
     * @param ctx the context which contains the configuration in order to
     *        deploy a WAR.
     * @throws JWebContainerServiceException if the registration of the WAR
     *         failed.
     */
    @Override
    protected void doRegisterWar(final Context ctx) throws JWebContainerServiceException {
        // Get the 5 parameters :
        // - warURL is the URL of the war to register (required param).
        // - contextRoot is the context root to which this application
        // should be installed (must be unique) (required param).
        // - hostName is the name of the host on which deploy the war
        // (optional param taken into account only if no <context> element
        // was declared in server.xml for this web application) .
        // - java2DelegationModel the compliance to java2 delegation model
        // - parentCL the war classloader of this war.
        // URL warURL = null;
        URL unpackedWarURL = null;
        String contextRoot = null;
        boolean java2DelegationModel = true;
        try {
            unpackedWarURL = (URL) ctx.lookup("unpackedWarURL");
            contextRoot = (String) ctx.lookup("contextRoot");
            Boolean bool = (Boolean) ctx.lookup("java2DelegationModel");
            java2DelegationModel = bool.booleanValue();
        } catch (NamingException e) {
            String err = "Error while getting parameter from context param ";
            getLogger().log(BasicLevel.ERROR, err + e.getMessage());
            throw new JWebContainerServiceException(err, e);
        }

        ClassLoader webClassLoader = null;
        try {
            webClassLoader = (ClassLoader) ctx.lookup("parentCL");
        } catch (NamingException e) {
            String err = "error while getting parameter from context param ";
            getLogger().log(BasicLevel.ERROR, err + e.getMessage());
            throw new JWebContainerServiceException(err, e);
        }

        String hostName = null;
        try {
            hostName = (String) ctx.lookup("hostName");
        } catch (NamingException e) {
            hostName = "";
        }

        String earAppName = null;
        try {
            earAppName = (String) ctx.lookup("earAppName");
        } catch (NamingException e) {
            // no ear case, so no ear application name
            earAppName = null;
        }

        // Install a new web application, whose web application archive is
        // at the specified URL, into this container with the specified
        // context root.
        // A context root of "" (the empty string) should be used for the root
        // application for this container. Otherwise, the context root must
        // start with a slash.

        if (contextRoot.equals("/")) {
            contextRoot = "";
        } else if (contextRoot.equalsIgnoreCase("ROOT")) {
            // Jetty uses ROOT.war and ROOT directory to as root context
            contextRoot = "";
        }

        // install the war.
        File fWar = new File(unpackedWarURL.getFile());
        String fileName = fWar.getAbsolutePath();

        if (_server != null) {
            try {
                SipAppContext sipAppContext = new SipAppContext();

                // Set the name of the context
                sipAppContext.setContextPath("/" + contextRoot);

                // Set path to the war file
                sipAppContext.setWar(unpackedWarURL.toString());

                if ((hostName != null) && (hostName.length() > 0)) {
                    // Host was specified
                    sipAppContext.setConnectorNames(new String[] {hostName});
                }

                sipAppContext.setAttribute("J2EEDomainName", getDomainName());
                sipAppContext.setAttribute("J2EEServerName", getJonasServerName());
                sipAppContext.setAttribute("J2EEApplicationName", earAppName);

                // Add default xml descriptor
                File webDefaults = new File(JETTY_DEFAULT_WEB_XML_FILE);
                if (webDefaults.exists()) {
                    sipAppContext.setDefaultsDescriptor(webDefaults.toURL().toExternalForm());
                } else {
                    getLogger().log(BasicLevel.WARN, "The file '" + JETTY_DEFAULT_WEB_XML_FILE
                                    + "' is not present. Check that your JONAS_BASE is up-to-date.");
                }
                
                File sipDefaults = new File(DEFAULT_SIP_XML_FILE);
                if (sipDefaults.exists()) {
                    sipAppContext.setDefaultsSipDescriptor(sipDefaults.toURL().toExternalForm());
                } else {
                    getLogger().log(BasicLevel.WARN, "The file '" + DEFAULT_SIP_XML_FILE
                                    + "' is not present. Check that your JONAS_BASE is up-to-date.");
                }

                // Specifying the jsp class path used by jasper
                sipAppContext.setAttribute("org.apache.catalina.jsp_classpath", getJOnASClassPath(webClassLoader));

                // Set this classloader to the Java2 compliant mode ?
                sipAppContext.setParentLoaderPriority(java2DelegationModel);

                if (getLogger().isLoggable(BasicLevel.DEBUG)) {
                    getLogger().log(BasicLevel.DEBUG,
                            "Webapp class loader java 2 delegation model set to " + java2DelegationModel);

                    getLogger().log(BasicLevel.DEBUG, "Cipango server starting web app " + fileName);
                }
                
                sipAppContext.setConfigurationClasses (new String[]{ 
                		"org.mortbay.jetty.webapp.WebInfConfiguration",
                		"org.cipango.jonas.EnvConfiguration", 
                		"org.mortbay.jetty.webapp.WebXmlConfiguration",
                		"org.cipango.annotations.Configuration",  
                		"org.mortbay.jetty.webapp.JettyWebXmlConfiguration",
                		"org.mortbay.jetty.webapp.TagLibConfiguration"});

                // Add handler
                webAppContexts.addHandler(sipAppContext);

                // start context with the parent classloader as parent classloader
                ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(webClassLoader);
                try {
                    sipAppContext.start();
                } finally {
                    //reset classloader
                    Thread.currentThread().setContextClassLoader(oldCL);
                }

                if (getLogger().isLoggable(BasicLevel.DEBUG)) {
                    getLogger().log(BasicLevel.DEBUG, "Cipango server is running web app " + fileName);
                }

            } catch (IOException ioeExc) {
                String err = "Cannot install this web application " + ioeExc;
                getLogger().log(BasicLevel.ERROR, err);
                throw new JWebContainerServiceException(err, ioeExc);
            } catch (Exception eExc) {
                String err = "Cannot start this web application " + eExc;
                getLogger().log(BasicLevel.ERROR, err);
                throw new JWebContainerServiceException(err, eExc);
            }
        } else {
            if (getLogger().isLoggable(BasicLevel.DEBUG)) {
                getLogger().log(BasicLevel.DEBUG, "No Cipango server to install web app " + fileName);
            }
        }

        // TODO We need to have the J2EE WebModule MBean here, should add it to
        // the Context
        // Store WebModule ObjectName in Context
        try {
            ctx.rebind("WebModule", getDummyJSR77ObjectName(hostName, contextRoot, earAppName));
        } catch (Exception e) {
            // NamingException or Mbean related Exception
            // TODO i18n
            String err = "Cannot rebind WebModule ObjectName in Context";
            getLogger().log(BasicLevel.ERROR, err, e);
            throw new JWebContainerServiceException(err, e);
        }

    }

    /**
     * Create a fake, JSR77 MBean ObjectName.
     * @param hostName host name
     * @param contextRoot context root name
     * @param earAppName application name
     * @return a fake JSR77 WebModule ObjectName
     * @throws MalformedObjectNameException if ObjectName incorrect
     */
    private ObjectName getDummyJSR77ObjectName(final String hostName, final String contextRoot, final String earAppName)
            throws MalformedObjectNameException {
        // jonas:j2eeType=WebModule,name=//localhost/,J2EEApplication=none,J2EEServer=jonas
        return ObjectName.getInstance(getDomainName() + ":j2eeType=WebModule,name=" + "/" + contextRoot
                + ",J2EEApplication=" + earAppName + ",J2EEServer=" + getJonasServerName());
    }

    /**
     * Return the classpath which can be used for jsp compiling by Jasper. This
     * classpath is extracted from the web classloader.
     * @param webClassLoader the ClassLoader used for extract URLs.
     * @return the jonas classpath which is useful for JSP compiling.
     */
    public String getJOnASClassPath(final ClassLoader webClassLoader) {

        StringBuffer classpath = new StringBuffer();
        int n = 0;

        ClassLoader tmpLoader = webClassLoader;
        while (tmpLoader != null) {
            if (!(tmpLoader instanceof URLClassLoader)) {
                break;
            }
            URL[] repositories = ((URLClassLoader) tmpLoader).getURLs();
            for (int i = 0; i < repositories.length; i++) {
                String repository = repositories[i].toString();
                if (repository.startsWith("file://")) {
                    repository = repository.substring("file://".length());
                } else if (repository.startsWith("file:")) {
                    repository = repository.substring("file:".length());
                } else {
                    continue;
                }
                if (repository == null) {
                    continue;
                }
                if (n > 0) {
                    classpath.append(File.pathSeparator);
                }
                classpath.append(repository);
                n++;
            }
            tmpLoader = tmpLoader.getParent();
        }

        return classpath.toString();
    }

    /**
     * Delegate the unregistration to the implementation of the web container.
     * @param ctx the context which contains the configuration in order to
     *        undeploy a WAR.
     * @throws JWebContainerServiceException if the unregistration failed.
     */
    @Override
    protected void doUnRegisterWar(final Context ctx) throws JWebContainerServiceException {
        // Get the 2 parameters :
        // - contextRoot is the context root to be removed (required param).
        // - hostName is the name of the host to remove the war (optional).
        String contextRoot = null;
        try {
            contextRoot = (String) ctx.lookup("contextRoot");
        } catch (NamingException e) {
            String err = "Error while getting parameter from context param ";
            getLogger().log(BasicLevel.ERROR, err + e.getMessage());
            throw new JWebContainerServiceException(err, e);
        }

        // A context root of "" (the empty string) should be used for the root
        // application for this container. Otherwise, the context root must
        // start with a slash.

        if (contextRoot.equals("/")) {
            contextRoot = "";
        } else if (contextRoot.equalsIgnoreCase("ROOT")) {
            // Jetty uses ROOT.war and ROOT directory to as root context
            contextRoot = "";
        }

        if (_server != null) {
        	SipAppContext sipAppContext = null;

            // find the deployed context
            Handler[] handlers = webAppContexts.getHandlers();

            for (Handler handler : handlers) {
                if (handler instanceof SipAppContext) {
                	SipAppContext context = (SipAppContext) handler;
                    if (contextRoot.equals(context.getContextPath().substring(1))) {
                        sipAppContext = context;
                        break;
                    }
                }
            }

            if (sipAppContext != null) {
                // Stop it gracefully
                try {
                    sipAppContext.stop();
                } catch (Exception e) {
                    getLogger().log(BasicLevel.LEVEL_WARN,
                            "Cipango server encoutered exception while stopping web application ", e);
                }
                if (getLogger().isLoggable(BasicLevel.DEBUG)) {
                    getLogger().log(BasicLevel.DEBUG,
                            "Cipango server stopped and is removing web app at context " + contextRoot);
                }

                webAppContexts.removeHandler(sipAppContext);
                if (getLogger().isLoggable(BasicLevel.DEBUG)) {
                    getLogger().log(BasicLevel.DEBUG,
                            "Cipango server removed and is destroying web app at context " + contextRoot);
                }
                sipAppContext.destroy();

                if (getLogger().isLoggable(BasicLevel.DEBUG)) {
                    getLogger().log(BasicLevel.DEBUG, "Cipango server unloaded web app at context " + contextRoot);
                }
            } else {
                getLogger().log(BasicLevel.WARN, "Cipango server didn't find web app at context " + contextRoot);
            }

        } else {
            getLogger().log(BasicLevel.WARN, "No Cipango server to install web app at context " + contextRoot);
        }
    }

    /**
     * Update info of the serverName and serverVersion.
     */
    @Override
    protected void updateServerInfos() {
        setServerName("Cipango");
        setServerVersion(Server.getVersion());
    }

    /**
     * Return the Default host name of the web container.
     * @return the Default host name of the web container.
     * @throws JWebContainerServiceException when default host cannot be
     *         resolved (multiple services).
     */
    @Override
    public String getDefaultHost() throws JWebContainerServiceException {
        Connector[] connectors = _server.getConnectors();
        // If we have more than 1 host, we cannot determine default host!
        if (connectors.length == 0) {
            String err = "Cannot determine default host : Cipango server has no host!";
            throw new JWebContainerServiceException(err);
        }

        return connectors[0].getHost();
    }

    /**
     * Return the Default HTTP port number of the web container (can be null if
     * multiple HTTP connector has been set).
     * @return the Default HTTP port number of the web container.
     * @throws JWebContainerServiceException when default HTTP port cannot be
     *         resolved (multiple occurences).
     */
    @Override
    public String getDefaultHttpPort() throws JWebContainerServiceException {
        return String.valueOf(getFirstListenerFromScheme("http").getPort());
    }

    /**
     * Return the Default HTTPS port number of the web container (can be null if
     * multiple HTTPS connector has been set).
     * @return the Default HTTPS port number of the web container.
     * @throws JWebContainerServiceException when default HTTPS port cannot be
     *         resolved (multiple occurences).
     */
    @Override
    public String getDefaultHttpsPort() throws JWebContainerServiceException {
        return String.valueOf(getFirstListenerFromScheme("https").getPort());
    }

    /**
     * @param myScheme matching URL scheme (http, https, ...)
     * @return Returns the first HttpListener found
     */
    private Connector getFirstListenerFromScheme(final String myScheme) {

        Connector[] connectors = _server.getConnectors();
        List<Connector> matchingConnectors = new ArrayList<Connector>();
        for (int i = 0; i < connectors.length; i++) {
            Connector connector = connectors[i];
            String scheme = connector.getIntegralScheme();
            if (scheme.equalsIgnoreCase(myScheme)) {
                matchingConnectors.add(connector);
            }
        }
        if (matchingConnectors.isEmpty()) {
            String err = "Cannot determine default '" + myScheme + "' port :" + " Cipango server has 0 '" + myScheme
                    + "' Listener";
            throw new JWebContainerServiceException(err);
        }

        Connector firstConnector = matchingConnectors.get(0);
        // Check if there are more than one HTTP connectors specified, if so,
        // warn the administrator.
        if (matchingConnectors.size() > 1) {
            if (getLogger().isLoggable(BasicLevel.WARN)) {
                getLogger().log(
                        BasicLevel.WARN,
                        "Found multiple Listener for scheme '" + myScheme + "'" + ", using first by default! (port:"
                                + firstConnector.getPort() + ")");
            }
        }

        return firstConnector;
    }

}
