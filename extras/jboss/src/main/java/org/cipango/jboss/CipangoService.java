//========================================================================
//$Id:  $
//JBoss Jetty Integration
//------------------------------------------------------------------------
//Licensed under LGPL.
//See license terms at http://www.gnu.org/licenses/lgpl.html
//========================================================================
package org.cipango.jboss;


import java.io.CharArrayWriter;
import java.lang.reflect.Method;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cipango.SipServer;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.deployment.SubDeployerExt;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.web.AbstractWebContainer;
import org.jboss.web.AbstractWebDeployer;
import org.mortbay.log.Log;
import org.mortbay.xml.XmlConfiguration;
import org.w3c.dom.Element;

//------------------------------------------------------------------------------
/**
 * CipangoService
 * A service to launch cipangpo as the sipserver for JBoss.
 *
 *
 * @jmx:mbean name="jboss.cipango:service=cipango"
 *            extends="org.jboss.web.AbstractWebContainerMBean"
 *
 * @todo convert to use JMXDoclet...
 * 
 */

public class CipangoService extends AbstractWebContainer implements CipangoServiceMBean, MBeanRegistration
{
    public static final String NAME = "Cipango";
    public static final String MBEAN_DOMAIN = "org.cipango";
    
    private MBeanServer _server = null;
    private SipServer _cipango = null;
    private boolean _supportJSR77;
    private String _webDefaultResource;
    private SubDeployerExt _subDeployerProxy = null;
    private Element _cipangoConfig = null;
 
	private SsarDeployer _ssarDeployer;

    
    /**
     * ConfigurationData
     *
     * Holds info that the jboss API sets on the
     * AbstractWebContainer but is needed by the
     * AbstractWebDeployer.
     */
    public static class ConfigurationData
    {
        private boolean _loaderCompliance;
        private boolean _unpackWars;
        private boolean _lenientEjbLink;
        private String _subjectAttributeName;
        private String _defaultSecurityDomain;
        private boolean _acceptNonWarDirs;
        private String _webDefaultResource;
        private boolean _supportJSR77;
        private String _mbeanDomain;
        
        /**
         * @return the _webDefaultResource
         */
        public String getWebDefaultResource()
        {
            return _webDefaultResource;
        }

        /**
         * @param defaultResource the _webDefaultResource to set
         */
        public void setWebDefaultResource(String defaultResource)
        {
            _webDefaultResource = defaultResource;
        }

        public void setJava2ClassLoadingCompliance(boolean loaderCompliance)
        {
           _loaderCompliance=loaderCompliance;
        }

        public boolean getJava2ClassLoadingCompliance()
        {
            return _loaderCompliance;
        }
       
        public boolean getUnpackWars()
        {
            return _unpackWars;
        }

        public void setUnpackWars(boolean unpackWars)
        {
            _unpackWars=unpackWars;
        }
        
        public void setLenientEjbLink (boolean lenientEjbLink)
        {
            _lenientEjbLink=lenientEjbLink;
        }
        
        public boolean getLenientEjbLink()
        {
            return _lenientEjbLink;
        }

        public String getSubjectAttributeName()
        {
            return _subjectAttributeName;
        }

        /**
         * @jmx:managed-attribute
         */
        public void setSubjectAttributeName(String subjectAttributeName)
        {
            _subjectAttributeName=subjectAttributeName;
        }

        /**
         * @return the _defaultSecurityDomain
         */
        public String getDefaultSecurityDomain()
        {
            return _defaultSecurityDomain;
        }

        /**
         * @param securityDomain the _defaultSecurityDomain to set
         */
        public void setDefaultSecurityDomain(String securityDomain)
        {
            _defaultSecurityDomain = securityDomain;
        }

        /**
         * @return the _acceptNonWarDirs
         */
        public boolean getAcceptNonWarDirs()
        {
            return _acceptNonWarDirs;
        }

        /**
         * @param nonWarDirs the _acceptNonWarDirs to set
         */
        public void setAcceptNonWarDirs(boolean nonWarDirs)
        {
            _acceptNonWarDirs = nonWarDirs;
        }

        /**
         * @return the _supportJSR77
         */
        public boolean getSupportJSR77()
        {
            return _supportJSR77;
        }

        /**
         * @param _supportjsr77 the _supportJSR77 to set
         */
        public void setSupportJSR77(boolean _supportjsr77)
        {
            _supportJSR77 = _supportjsr77;
        }

        /**
         * @return the _mbeanDomain
         */
        public String getMBeanDomain()
        {
            return _mbeanDomain;
        }

        /**
         * @param domain the _mbeanDomain to set
         */
        public void setMBeanDomain(String domain)
        {
            _mbeanDomain = domain;
        }
    }

    
    
    /** 
     * Constructor
     */
    public CipangoService()
    {
        super();
        _cipango = new SipServer();
		System.setProperty("cipango.home", 
				System.getProperty("jboss.server.home.dir"));
		Log.setLog(Log.getLogger("cipango"));

    }



    /** 
     * Listen for our registration as an mbean and remember our name.
     * @see org.jboss.system.ServiceMBeanSupport#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception
    {
        super.preRegister(server, name);
        name = getObjectName(server, name);
        _server = server;
        return name;
    }

    
    /** 
     * Listen for post-mbean registration and set up the jetty
     * mbean infrastructure so it can generate mbeans according
     * to the elements contained in the <configuration> element
     * of the jboss-service.xml file.
     * @see org.jboss.system.ServiceMBeanSupport#postRegister(java.lang.Boolean)
     */
    public void postRegister(Boolean done)
    {
        super.postRegister(done);
        try
        {
            log.debug("Setting up mbeanlistener on Cipango");
            _cipango.getContainer().addEventListener(new JBossMBeanContainer(_server, MBEAN_DOMAIN));
        }
        catch (Throwable e)
        {
            log.error("could not create MBean peers", e);
        }
    }


    /** 
     * @see org.jboss.system.ServiceMBeanSupport#getName()
     */
    public String getName()
    {
        return NAME;
    }


    /** 
     * @see org.jboss.deployment.SubDeployerSupport#createService()
     */
    public void createService() throws Exception {
        super.createService();
		if (_cipangoConfig != null)
			configure();
        
    }

    /** 
     * Start up the jetty service. Also, as we need to be able
     * to have interceptors injected into us to support jboss.ws:service=WebService,
     * we need to create a proxy to ourselves and register that proxy with the
     * mainDeployer.
     * See <a href="http://wiki.jboss.org/wiki/Wiki.jsp?page=SubDeployerInterceptorSupport">SubDeployerInterceptorSupport</a>
     * @see org.jboss.web.AbstractWebContainer#startService()
     */
    public void startService() throws Exception
    {
        //do what AbstractWebContainer.startService() would have done
        serviceController = (ServiceControllerMBean)
        MBeanProxyExt.create(ServiceControllerMBean.class,
                             ServiceControllerMBean.OBJECT_NAME,
                             server);

        //instead of calling mainDeployer.addDeployer(this) as SubDeployerSupport super class does,
        //we register instead a proxy to oursevles so we can support dynamic addition of interceptors
        _subDeployerProxy = (SubDeployerExt)MBeanProxyExt.create(SubDeployerExt.class, super.getServiceName(), super.getServer());
        mainDeployer.addDeployer(_subDeployerProxy);
        
        _cipango.start();
        
        registerDeployers();
    }

    public void stopService() throws Exception
    {
        mainDeployer.removeDeployer(_subDeployerProxy);
        unregisterDeployers();
        _cipango.stop();
    }

    public void destroyService() throws Exception
    {
        super.destroyService();
        _cipango.stop();
        _cipango = null;
    }

    /**
     * @jmx:managed-attribute
     */
    public boolean getSupportJSR77()
    {
        return _supportJSR77;
    }

    /**
     * @jmx:managed-attribute
     */
    public void setSupportJSR77(boolean supportJSR77)
    {
        if (log.isDebugEnabled())
            log.debug("set SupportJSR77 to " + supportJSR77);

        _supportJSR77=supportJSR77;
    }

    /**
     * Get the custom webdefault.xml file.
     * @jmx:managed-attribute
     */
    public String getWebDefaultResource()
    {
        return _webDefaultResource;
    }

    /**
     * Set a custom webdefault.xml file.
     * @jmx:managed-attribute
     */
    public void setWebDefaultResource(String webDefaultResource)
    {
        if (log.isDebugEnabled())
            log.debug("set WebDefaultResource to " + webDefaultResource);

        _webDefaultResource=webDefaultResource;
    }

    
    /** 
     * @see org.jboss.web.AbstractWebContainer#getDeployer(org.jboss.deployment.DeploymentInfo)
     */
    public AbstractWebDeployer getDeployer(DeploymentInfo di) throws Exception
    {
        CipangoDeployer deployer = new CipangoDeployer(_cipango, di);
        ConfigurationData configData = new ConfigurationData();
        configData.setMBeanDomain(MBEAN_DOMAIN);
        configData.setAcceptNonWarDirs(getAcceptNonWarDirs());
        configData.setJava2ClassLoadingCompliance(getJava2ClassLoadingCompliance());
        configData.setLenientEjbLink(getLenientEjbLink());
        configData.setSubjectAttributeName(getSubjectAttributeName());
        configData.setSupportJSR77(getSupportJSR77());
        configData.setUnpackWars(getUnpackWars());
        configData.setWebDefaultResource(getWebDefaultResource());
        //defaultSecurityDomain was added at a certain point, so do it
        //this way so we have backwards compatibility
        try
        {
            Method method = AbstractWebContainer.class.getDeclaredMethod("getDefaultSecurityDomain", new Class[0]);
            String defaultSecurityDomain = (String) method.invoke(CipangoService.this, new Object[0]);
            configData.setDefaultSecurityDomain(defaultSecurityDomain);
        }
        catch (Exception e)
        {
            // ignore - it means the currently executing version of jboss
            // does not support this method
            log.info("Getter/setter for DefaultSecurityDomain not available in this version of JBoss");
        }
        deployer.setServer(_server);
        deployer.init(configData);
        return deployer;
    }
    
    

    
	private void registerDeployers() {
		try {
			_ssarDeployer = new SsarDeployer(_cipango, this);

			server.registerMBean(_ssarDeployer, SsarDeployerMBean.OBJECT_NAME);

			server.invoke(MainDeployerMBean.OBJECT_NAME, "addDeployer",
					new Object[] { _ssarDeployer },
					new String[] { "org.jboss.deployment.SubDeployer" });
		} catch (Exception e) {
			Log.warn("Could not deploy SsarDeployer - ssar deployment feature will not work.\n"
							+ e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	private void unregisterDeployers() {
		try {
			
			server.invoke(MainDeployerMBean.OBJECT_NAME, "removeDeployer",
					new Object[] { _ssarDeployer },
					new String[] { "org.jboss.deployment.SubDeployer" });

			server.unregisterMBean(SsarDeployerMBean.OBJECT_NAME);

		} catch (Exception e) {
			Log.warn("Could not undeploy SsarDeployer - ssar deployment will continue (badly).\n"
							+ e.getClass().getName() + ": " + e.getMessage());
		}
	}


    public Element getConfigurationElement()
    {
        return _cipangoConfig;
    }

	public void setConfigurationElement(Element configElement)
	{
		_cipangoConfig = configElement;
	}

	
	   /**
     * @param configElement XML fragment from jboss-service.xml
     */
    private void configure()
    {

        // convert to an xml string to pass into Jetty's normal
        // configuration mechanism


        try
        {
            DOMSource source = new DOMSource(_cipangoConfig);

            CharArrayWriter writer = new CharArrayWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            String _xmlConfigString = writer.toString();

            // get rid of the first line, as this will be prepended by
            // the XmlConfiguration
            int index = _xmlConfigString.indexOf("?>");
            if (index >= 0)
            {
                index += 2;

                while ((_xmlConfigString.charAt(index) == '\n')
                        || (_xmlConfigString.charAt(index) == '\r'))
                    index++;
            }

            _xmlConfigString = _xmlConfigString.substring(index);

            if (Log.isDebugEnabled())
                    Log.debug("Passing xml config to Cipango:\n" + _xmlConfigString);

            XmlConfiguration xmlConfigurator = new XmlConfiguration(_xmlConfigString);
            xmlConfigurator.configure(_cipango);

        }
        catch (TransformerConfigurationException tce)
        {
            Log.warn("Can't transform config Element -> xml:", tce);
        }
        catch (TransformerException te)
        {
        	Log.warn("Can't transform config Element -> xml:", te);
        }
        catch (Exception e)
        {
        	Log.warn("Unexpected exception converting configuration Element -> xml", e);
        }
    }


}
