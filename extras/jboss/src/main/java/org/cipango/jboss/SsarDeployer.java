package org.cipango.jboss;

import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.ObjectName;

import org.cipango.SipServer;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerSupport;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.log.Log;



public class SsarDeployer extends SubDeployerSupport implements SsarDeployerMBean {
	
	private SipServer _sipServer;
	private CipangoServiceMBean _service;
	private ContextHandlerCollection _contexts;
	
	Hashtable _deployed = new Hashtable();
	
	/**
	 * A constructor for this class.
	 * 
	 * 
	 */
	public SsarDeployer(SipServer server, CipangoServiceMBean service) {
		super();
		_sipServer = server;
		_service = service;
	    _contexts = (ContextHandlerCollection)_sipServer.getChildHandlerByClass(ContextHandlerCollection.class);


	}

	/*
	 * @see org.jboss.deployment.SubDeployerMBean#accepts(org.jboss.deployment.DeploymentInfo)
	 */
	public boolean accepts(DeploymentInfo di) {	
		if (!di.url.getProtocol().equals("file")) {
			return false;
		}
		
		if (!di.url.getFile().endsWith(".ssar")
				&& !di.url.getFile().endsWith(".ssar/")) {
			return false;
		}
		
		if (di.isScript || di.isXML) {
			return false;
		}
		
		// TODO check for presence WEB-INF/sip.xml
		// with some .ssar created with Solaris, this resource is not found

		if (Log.isDebugEnabled()) {
			Log.debug(di.url + " is deployable");
		}
		return true;
	}



	
	/*
	 * @see org.jboss.deployment.SubDeployerMBean#start(org.jboss.deployment.DeploymentInfo)
	 */
	public void start(DeploymentInfo di) throws DeploymentException {
		try {		
			String warUrl = di.url.toString();
			Log.debug("deploying webapp at " + warUrl);        
	        try
	        {
	            //String contextPath = webApp.getMetaData().getContextRoot();

	            if (_deployed.get(warUrl) != null)
	                throw new DeploymentException(warUrl+" is already deployed");

	            //make a context for the webapp and configure it from the jetty jboss-service.xml defaults
	            //and the jboss-web.xml descriptor
	            JBossSipAppContext app = new JBossSipAppContext(di, warUrl);
	            
	            // In case of directory, there is a '/' after .ssar. 
	            int index1 = warUrl.lastIndexOf('/', warUrl.length() - 4);
	            int index2= warUrl.lastIndexOf(".ssar");
	            String contextPath = warUrl.substring(index1, index2);
	            	
	            app.setContextPath(contextPath);
	            
	            URL webXml = di.localCl.findResource("WEB-INF/web.xml");
	    		if (webXml != null)
	    		{
		            app.setConfigurationClasses (new String[]{ 
		            		"org.mortbay.jetty.webapp.WebInfConfiguration",
		            		"org.cipango.jboss.JBossWebXmlConfiguration", 
		            		"org.mortbay.jetty.webapp.JettyWebXmlConfiguration",  
		            		"org.mortbay.jetty.webapp.TagLibConfiguration",
		            		"org.cipango.jboss.JBossSipXmlConfiguration"});
	    		}
	    		else
	    		{
		            app.setConfigurationClasses (new String[]{ 
		            		"org.mortbay.jetty.webapp.WebInfConfiguration",
		            		"org.cipango.jboss.JBossSipXmlConfiguration"});
	    		}
	            app.setExtractWAR(_service.getUnpackWars());
	            app.setParentLoaderPriority(_service.getJava2ClassLoadingCompliance());
	            
	            //permit urls without a trailing '/' even though it is not a valid url
	            //as the jboss webservice client tests seem to use these invalid urls
	            if (Log.isDebugEnabled())
	            	Log.debug("Allowing non-trailing '/' on context path");
	            app.setAllowNullPathInfo(true);
	                 
	            // if a different webdefault.xml file has been provided, use it
	            if (_service.getWebDefaultResource() != null)
	            {
	                try
	                {
	                    URL url = getClass().getClassLoader().getResource(_service.getWebDefaultResource());
	                    String fixedUrl = (fixURL(url.toString()));
	                    app.setDefaultsDescriptor(fixedUrl);
	                    if (Log.isDebugEnabled())
	                    	Log.debug("webdefault specification is: " + _service.getWebDefaultResource());
	                }
	                catch (Exception e)
	                {
	                	Log.warn("Could not find resource: " + _service.getWebDefaultResource()+" using default", e);
	                }
	            }

	            // Add the webapp to jetty 
	            _contexts.addHandler(app);
	            

	            //if jetty has been started, then start the
	            //handler just added
	            if (_contexts.isStarted())
	                app.start();
	            
	            // keep track of deployed contexts for undeployment
	            _deployed.put(warUrl, app);
	    

	            //tell jboss about the jsr77 mbeans we've created               
	            //first check that there is an mbean for the webapp itself
	            ObjectName webAppMBean = new ObjectName(CipangoService.MBEAN_DOMAIN + ":J2EEServer=none,J2EEApplication=none,J2EEWebModule="+app.getUniqueName());
	            if (server.isRegistered(webAppMBean)) {
	                di.deployedObject = webAppMBean;
	            } else {
	             	 throw new IllegalStateException("No mbean registered for webapp at "+app.getUniqueName());
	            }
	            
	            //now get all the mbeans that represent servlets and set them on the 
	            //deployment info so they will be found by the jsr77 management system
	            ObjectName servletQuery = new ObjectName
	            (CipangoService.MBEAN_DOMAIN + ":J2EEServer=none,J2EEApplication=none,J2EEWebModule="+app.getUniqueName()+ ",j2eeType=Servlet,*");
	            Iterator iterator = server.queryNames(servletQuery, null).iterator();
	            while (iterator.hasNext())
	            {
	                di.mbeans.add(iterator.next());
	            }
	        }
	        catch (Exception e)
	        {
	        	Log.warn("Undeploying on start due to error", e);
	            throw new DeploymentException(e);
	        }
			
			
			
			
		} catch (Exception ex) {
			throw new DeploymentException(ex);
		}

		super.start(di);
	}
	
    /**
     * Work around broken JarURLConnection caching...
     * @param url
     * @return
     */
    private static String fixURL(String url)
    {
        String fixedUrl = url;
        
        // Get the separator of the JAR URL and the file reference
        int index = url.indexOf('!');
        if (index >= 0)
            index = url.lastIndexOf('/', index);
        else
            index = url.lastIndexOf('/');
       
        // If there is at least one forward slash, add a "/." before the JAR file 
        // change the path just slightly. Otherwise, the url is malformed, but
        // we will ignore that.
        if (index >= 0)
            fixedUrl = url.substring(0, index) + "/." + url.substring(index);

        return fixedUrl;
    } 

	/*
	 * @see org.jboss.system.ServiceMBeanSupport#stop()
	 */
	public void stop(DeploymentInfo di) throws DeploymentException {
		try {
			String warUrl = di.url.toString();
	        JBossSipAppContext app = (JBossSipAppContext) _deployed.get(warUrl);

	        if (app == null)
	        	Log.warn("app (" + warUrl + ") not currently deployed");
	        else
	        {
	            try
	            {
	                app.stop();
	                _contexts.removeHandler(app);
	                // The stop can take sometimes.
	                if (app.isStopped())
	                	app.destroy();
	                app = null;
	                Log.info("Successfully undeployed " + warUrl);
	            }
	            catch (Exception e)
	            {
	                throw new DeploymentException(e);
	            }
	            finally
	            {
	                _deployed.remove(warUrl);
	            }
	        }
		} catch (Exception ex) {
			throw new DeploymentException(ex);
		}
		super.stop(di);
	}

	/*
	 * @see org.jboss.deployment.SubDeployerMBean#create(org.jboss.deployment.DeploymentInfo)
	 */
	public void create(DeploymentInfo di) throws DeploymentException {
		Log.info("Received a request to create deployment  " + di.url);
		super.create(di);
	}

	/*
	 * @see org.jboss.deployment.SubDeployerMBean#destroy(org.jboss.deployment.DeploymentInfo)
	 */
	public void destroy(DeploymentInfo di) throws DeploymentException {
		Log.info("Received a request to delete deployment  " + di.url);
		super.destroy(di);
	}

	/*
	 * @see org.jboss.deployment.SubDeployerMBean#init(org.jboss.deployment.DeploymentInfo)
	 */
	public void init(DeploymentInfo di) throws DeploymentException {
		super.init(di);
	}

	/*
	 * @see org.jboss.deployment.SubDeployerSupport#isDeployable(java.lang.String,
	 *      java.net.URL)
	 */
	protected boolean isDeployable(String name, URL url) {
		return name.endsWith(".ssar");
	}

}