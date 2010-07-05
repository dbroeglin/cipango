// ========================================================================
// $Id: AbstractConfiguration.java 1594 2007-02-14 02:45:12Z janb $
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.cipango.plus.sipapp;


import java.util.EventListener;
import java.util.Iterator;

import javax.servlet.UnavailableException;

import org.cipango.plus.servlet.SipServletHandler;
import org.cipango.sipapp.SipAppContext;
import org.cipango.sipapp.SipXmlConfiguration;
import org.eclipse.jetty.plus.annotation.Injection;
import org.eclipse.jetty.plus.annotation.InjectionCollection;
import org.eclipse.jetty.plus.annotation.LifeCycleCallback; 
import org.eclipse.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.eclipse.jetty.plus.annotation.PostConstructCallback;
import org.eclipse.jetty.plus.annotation.PreDestroyCallback;
import org.eclipse.jetty.plus.annotation.RunAsCollection;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.xml.XmlParser;




/**
 * Configuration
 *
 *
 */
public abstract class AbstractConfiguration extends SipXmlConfiguration
{
    
    protected LifeCycleCallbackCollection _callbacks = new LifeCycleCallbackCollection();
    protected InjectionCollection _injections = new InjectionCollection();
    protected RunAsCollection _runAsCollection = new RunAsCollection();
        
    public abstract void bindEnvEntry (String name, Object value) throws Exception;
    
    public abstract void bindResourceRef (String name, Class type) throws Exception;
    
    public abstract void bindResourceEnvRef (String name, Class type) throws Exception;
    
    public abstract void bindUserTransaction () throws Exception;
    
    public abstract void bindMessageDestinationRef (String name, Class type)  throws Exception;
    
    public abstract void bindSipResources() throws Exception;
    
    @Override
    public void setWebAppContext (WebAppContext context)
    {
        super.setWebAppContext(context);
        
        //set up our special ServletHandler to remember injections and lifecycle callbacks
        SipServletHandler servletHandler = new SipServletHandler();
        SecurityHandler securityHandler = getWebAppContext().getSecurityHandler();        
        org.cipango.servlet.SipServletHandler existingHandler = 
        	(org.cipango.servlet.SipServletHandler) getWebAppContext().getServletHandler();   
        
        servletHandler.setFilterMappings(existingHandler.getFilterMappings());
        servletHandler.setFilters(existingHandler.getFilters());
        servletHandler.setServlets(existingHandler.getServlets());
        servletHandler.setServletMappings(existingHandler.getServletMappings());
        
        servletHandler.setSipServlets(existingHandler.getSipServlets());
        servletHandler.setSipServletMappings(existingHandler.getSipServletMappings());
        if (existingHandler.getMainServlet() != null)
        	servletHandler.setMainServletName(existingHandler.getMainServlet().getName());
        
        getWebAppContext().setServletHandler(servletHandler);
        securityHandler.setHandler(servletHandler);       
    }
    
    @Override
    public void configureDefaults ()
    throws Exception
    {
        super.configureDefaults();
    }
   
    @Override
    public void configureWebApp ()
    throws Exception
    {
        super.configureWebApp();
        /*if (findSipXml() == null)
        {
        	Log.info("No sip.xml found, assume SIP Servlet 1.1 application and parse annotations");
        	_version = SipApp.VERSION_11;
        }*/

        if (_version != SipAppContext.VERSION_10)
        {
	        if (Log.isDebugEnabled()) 
	        	Log.debug("Processing annotations");
	        parseAnnotations();
	        bindSipResources();
        }
        //do any injects on the listeners that were created and then
        //also callback any postConstruct lifecycle methods
        injectAndCallPostConstructCallbacks();
        bindUserTransaction();
        
    }
    
    @Override
    public void deconfigureWebApp()
    throws Exception
    {
        //call any preDestroy methods on the listeners
        callPreDestroyCallbacks();
        
        super.deconfigureWebApp();
    }
     
    
    @Override
    protected void initialize(XmlParser.Node config) 
    throws ClassNotFoundException,UnavailableException
    {
        super.initialize(config);
        
        //configure injections and callbacks to be called by the FilterHolder and ServletHolder
        //when they lazily instantiate the Filter/Servlet.
        ((SipServletHandler)getWebAppContext().getServletHandler()).setInjections(_injections);
        ((SipServletHandler)getWebAppContext().getServletHandler()).setCallbacks(_callbacks);
        
    }
    
    @Override
    protected void initSipXmlElement(XmlParser.Node node) throws Exception
    {
    	String element = node.getTag();
        if ("env-entry".equals(element))
        {
            initEnvEntry (node);
        }
        else if ("resource-ref".equals(element))
        {
            //resource-ref entries are ONLY for connection factories
            //the resource-ref says how the app will reference the jndi lookup relative
            //to java:comp/env, but it is up to the deployer to map this reference to
            //a real resource in the environment. At the moment, we insist that the
            //jetty.xml file name of the resource has to be exactly the same as the
            //name in web.xml deployment descriptor, but it shouldn't have to be
            initResourceRef(node);
        }
        else if ("resource-env-ref".equals(element))
        {
            //resource-env-ref elements are a non-connection factory type of resource
            //the app looks them up relative to java:comp/env
            //again, need a way for deployer to link up app naming to real naming.
            //Again, we insist now that the name of the resource in jetty.xml is
            //the same as web.xml
            initResourceEnvRef(node);      
        }
        else if ("message-destination-ref".equals(element))
        {
            initMessageDestinationRef(node);
        }
        else if ("post-construct".equals(element))
        {
            //post-construct is the name of a class and method to call after all
            //resources have been setup but before the class is put into use
            initPostConstruct(node);
        }
        else if ("pre-destroy".equals(element))
        {
            //pre-destroy is the name of a class and method to call just as
            //the instance is being destroyed
            initPreDestroy(node);
        }
        else
        {
            super.initSipXmlElement(node);
        }
 
    }
    
    /**
     * JavaEE 5.4.1.3 
     * 
     * 
     * @param node
     * @throws Exception
     */
    protected void initEnvEntry (XmlParser.Node node)
    throws Exception
    {
        String name=node.getString("env-entry-name",false,true);
        String type = node.getString("env-entry-type",false,true);
        String valueStr = node.getString("env-entry-value",false,true);
        
        //if there's no value there's no point in making a jndi entry
        //nor processing injection entries
        if (valueStr==null || valueStr.equals(""))
        {
            Log.warn("No value for env-entry-name "+name);
            return;
        }
      
        //the javaee_5.xsd says that the env-entry-type is optional
        //if there is an <injection> element, because you can get
        //type from the element, but what to do if there is more
        //than one <injection> element, do you just pick the type
        //of the first one?
        
        //check for <injection> elements
        initInjection (node, name, TypeUtil.fromName(type));
       
        //bind the entry into jndi
        Object value = TypeUtil.valueOf(type,valueStr);
        bindEnvEntry(name, value);
        
    }
    
    
    /**
     * Common Annotations Spec section 2.3:
     *  resource-ref is for:
     *    - javax.sql.DataSource
     *    - javax.jms.ConnectionFactory
     *    - javax.jms.QueueConnectionFactory
     *    - javax.jms.TopicConnectionFactory
     *    - javax.mail.Session
     *    - java.net.URL
     *    - javax.resource.cci.ConnectionFactory
     *    - org.omg.CORBA_2_3.ORB
     *    - any other connection factory defined by a resource adapter
     * @param node
     * @throws Exception
     */
    protected void initResourceRef (XmlParser.Node node)
    throws Exception
    {
        String jndiName = node.getString("res-ref-name",false,true);
        String type = node.getString("res-type", false, true);
        String auth = node.getString("res-auth", false, true);
        String shared = node.getString("res-sharing-scope", false, true);

        //check for <injection> elements
        Class typeClass = TypeUtil.fromName(type);
        if (typeClass==null)
            typeClass = getWebAppContext().loadClass(type);
        initInjection (node, jndiName, typeClass);
        
        bindResourceRef(jndiName, typeClass);
    }
    
    
    /**
     * Common Annotations Spec section 2.3:
     *   resource-env-ref is for:
     *     - javax.transaction.UserTransaction
     *     - javax.resource.cci.InteractionSpec
     *     - anything else that is not a connection factory
     * @param node
     * @throws Exception
     */
    protected void initResourceEnvRef (XmlParser.Node node)
    throws Exception
    {
        String jndiName = node.getString("resource-env-ref-name",false,true);
        String type = node.getString("resource-env-ref-type", false, true);

        //check for <injection> elements
        
        //JavaEE Spec sec 5.7.1.3 says the resource-env-ref-type
        //is mandatory, but the schema says it is optional!
        Class typeClass = TypeUtil.fromName(type);
        if (typeClass==null)
            typeClass = getWebAppContext().loadClass(type);
        initInjection (node, jndiName, typeClass);
        
        bindResourceEnvRef(jndiName, typeClass);
    }
    
    
    /**
     * Common Annotations Spec section 2.3:
     *   message-destination-ref is for:
     *     - javax.jms.Queue
     *     - javax.jms.Topic
     * @param node
     * @throws Exception
     */
    protected void initMessageDestinationRef (XmlParser.Node node)
    throws Exception
    {
        String jndiName = node.getString("message-destination-ref-name",false,true);
        String type = node.getString("message-destination-type",false,true);
        String usage = node.getString("message-destination-usage",false,true);
        
        Class typeClass = TypeUtil.fromName(type);
        if (typeClass==null)
            typeClass = getWebAppContext().loadClass(type);
        initInjection(node, jndiName, typeClass);
        
        bindMessageDestinationRef(jndiName, typeClass);
    }
    
    
    
    /**
     * Process &lt;post-construct&gt;
     * @param node
     */
    protected void initPostConstruct(XmlParser.Node node)
    {
        String className = node.getString("lifecycle-callback-class", false, true);
        String methodName = node.getString("lifecycle-callback-method", false, true);
        
        if (className==null || className.equals(""))
        {
            Log.warn("No lifecycle-callback-class specified");
            return;
        }
        if (methodName==null || methodName.equals(""))
        {
            Log.warn("No lifecycle-callback-method specified for class "+className);
            return;
        }
        
        try
        {
            Class clazz = getWebAppContext().loadClass(className);
            LifeCycleCallback callback = new PostConstructCallback();
            callback.setTarget(clazz, methodName);
            _callbacks.add(callback);
        }
        catch (ClassNotFoundException e)
        {
            Log.warn("Couldn't load post-construct target class "+className);
        }
    }
    
    
    /**
     * Process &lt;pre-destroy&gt;
     * @param node
     */
    protected void initPreDestroy(XmlParser.Node node)
    {
        String className = node.getString("lifecycle-callback-class", false, true);
        String methodName = node.getString("lifecycle-callback-method", false, true);
        if (className==null || className.equals(""))
        {
            Log.warn("No lifecycle-callback-class specified for pre-destroy");
            return;
        }
        if (methodName==null || methodName.equals(""))
        {
            Log.warn("No lifecycle-callback-method specified for pre-destroy class "+className);
            return;
        } 
        
        try
        {
            Class clazz = getWebAppContext().loadClass(className);
            LifeCycleCallback callback = new PreDestroyCallback();
            callback.setTarget(clazz, methodName);
            _callbacks.add(callback);
        }
        catch (ClassNotFoundException e)
        {
            Log.warn("Couldn't load pre-destory target class "+className);
        }
    }
    
    
    /**
     * Iterate over the &lt;injection-target&gt; entries for a node
     * 
     * @param node
     * @param jndiName
     * @param valueClass
     * @return the type of the injectable
     */
    protected void initInjection (XmlParser.Node node, String jndiName, Class valueClass)
    {
        Iterator  itor = node.iterator("injection-target");
        
        while(itor.hasNext())
        {
            XmlParser.Node injectionNode = (XmlParser.Node)itor.next(); 
            String targetClassName = injectionNode.getString("injection-target-class", false, true);
            String targetName = injectionNode.getString("injection-target-name", false, true);
            if ((targetClassName==null) || targetClassName.equals(""))
            {
                Log.warn("No classname found in injection-target");
                continue;
            }
            if ((targetName==null) || targetName.equals(""))
            {
                Log.warn("No field or method name in injection-target");
                continue;
            }

            // comments in the javaee_5.xsd file specify that the targetName is looked
            // for first as a java bean property, then if that fails, as a field
            try
            {
                Class clazz = getWebAppContext().loadClass(targetClassName);
                Injection injection = new Injection();
                injection.setTargetClass(clazz);
                injection.setJndiName(jndiName);
                injection.setTarget(clazz, targetName, valueClass);
                 _injections.add(injection);
            }
            catch (ClassNotFoundException e)
            {
                Log.warn("Couldn't load injection target class "+targetClassName);
            }
        }
    }
    
    
    /**
     * Parse all classes that are mentioned in web.xml (servlets, filters, listeners)
     * for annotations.
     * 
     * 
     * 
     * @throws Exception
     */
    protected abstract void parseAnnotations () throws Exception;
   
    
    
    protected void injectAndCallPostConstructCallbacks()
    throws Exception
    {
        //look thru the servlets to apply any runAs annotations
        //NOTE: that any run-as in web.xml will already have been applied
        ServletHolder[] holders = getWebAppContext().getServletHandler().getServlets();
        for (int i=0;holders!=null && i<holders.length;i++)
        {
            _runAsCollection.setRunAs(holders[i]);
        }
        
        
        EventListener[] listeners = getWebAppContext().getEventListeners();
        for (int i=0;i<listeners.length;i++)
        {
            _injections.inject(listeners[i]);
            _callbacks.callPostConstructCallback(listeners[i]);
        }
    }
    
    
    protected void callPreDestroyCallbacks ()
    throws Exception
    {
        EventListener[] listeners = getWebAppContext().getEventListeners();
        for (int i=0;i<listeners.length;i++)
        {
            _callbacks.callPreDestroyCallback(listeners[i]);
        }
    }
   
}