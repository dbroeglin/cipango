// ========================================================================
// $Id: AnnotationProcessor.java 3353 2008-07-22 10:39:41Z janb $
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
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

package org.cipango.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.annotation.security.RunAs;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.annotation.SipApplication;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;

import org.cipango.plus.sipapp.Configuration;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.mortbay.jetty.annotations.AnnotationFinder;
import org.mortbay.jetty.plus.annotation.Injection;
import org.mortbay.jetty.plus.annotation.InjectionCollection;
import org.mortbay.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.mortbay.jetty.plus.annotation.PostConstructCallback;
import org.mortbay.jetty.plus.annotation.PreDestroyCallback;
import org.mortbay.jetty.plus.annotation.RunAsCollection;
import org.mortbay.log.Log;
import org.mortbay.util.IntrospectionUtil;
import org.mortbay.util.LazyList;



/**
 * AnnotationProcessor
 *
 * Act on the annotations discovered in the webapp.
 */
@SuppressWarnings("unchecked")
public class AnnotationProcessor
{
    private AnnotationFinder _finder;
    //private ClassLoader _loader;
    private RunAsCollection _runAs;
    private InjectionCollection _injections;
    private LifeCycleCallbackCollection _callbacks;
    private List<SipServletHolder> _servlets;
    private Object _listenerClasses;
    private SipApplication _sipApplication;
    private String _appName;
    private Method _sipApplicationKeyMethod;
    private SipAppContext _sipApp;
    
    private static Class[] __envEntryTypes = 
        new Class[] {String.class, Character.class, Integer.class, Boolean.class, Double.class, Byte.class, Short.class, Long.class, Float.class};
   
    public AnnotationProcessor(SipAppContext sipAppContext, String appName, AnnotationFinder finder, RunAsCollection runAs, InjectionCollection injections, LifeCycleCallbackCollection callbacks,
            List<SipServletHolder> servlets, Object listenerClasses)
    {
    	_sipApp = sipAppContext;
    	_appName = appName;
        _finder=finder;
        _runAs=runAs;
        _injections=injections;
        _callbacks=callbacks;
        _servlets=servlets;
        _listenerClasses = listenerClasses;
    }
    
    
    public void process ()
    throws Exception
    { 
    	processSipApplication();
        processSipServlets();
        processListeners();
        processRunAsAnnotations();
        processLifeCycleCallbackAnnotations();
        processResourcesAnnotations();
        processResourceAnnotations();
        processSipApplicationKeyAnnotations();
    }
    
    
    public void processSipApplication ()
    throws Exception
    {
        //@SipApplication(name=String, description=String, displayName=String, sessionTimeout=int,
        //				mainServlet=String, proxyTimeout=int, distributable=boolean,
        //				largeIcon=String, smallIcon=String)
    	List<Class<?>> classes = _finder.getClassesForAnnotation(SipApplication.class);
    	if (classes.size() == 1)
    	{
    		_sipApplication = (SipApplication) classes.get(0).getAnnotation(SipApplication.class);
    		if (_appName != null && !_appName.equals(_sipApplication.name()))
    			throw new IllegalStateException("App-name in sip.xml: " + _appName 
    					+ " does not match with SipApplication annotation: " + _sipApplication.name());
    		else if (_appName == null)
    			_appName = _sipApplication.name();
    	}
    	else if (classes.size() > 1)
    		throw new IllegalStateException("More than one javax.servlet.sip.annotation.SipApplication annotation");
    	else if (_appName == null)
			throw new IllegalStateException("Missing SipApplication annotation or app-name in sip.xml");
    }
    
	public void processSipServlets()
    throws Exception
    {
        //@SipServlet (applicationName=String, name=String, description=String, loadOnStartup=int)
        for (Class clazz:_finder.getClassesForAnnotation(SipServlet.class))
        {
            SipServlet annotation = (SipServlet) clazz.getAnnotation(SipServlet.class);
            String name = (annotation.name().equals("")?clazz.getSimpleName():annotation.name());                
            SipServletHolder holder = getSipServlet(name);
            if (holder == null)
            	holder = new SipServletHolder();
            else
            {
            	if (holder.getHeldClass() != null && !holder.getHeldClass().equals(clazz))
            		throw new IllegalStateException("Two servlets are defined with name " + name 
            				+ " and different classes");

            	Log.debug("Servlet " + name + " already defined, should have been defined in sip.xml");
            }
            holder.setHeldClass(clazz);
            holder.setName(name);
            Log.debug("Create SIP Servlet holder with name " + holder.getName() + " from annotation");
            holder.setInitOrder(annotation.loadOnStartup());
            // TODO annotation.description();
            checkAppName(annotation.applicationName(), clazz);
            LazyList.add(_servlets, holder);
   
        } 
    }
    
    private SipServletHolder getSipServlet(String name)
    {
    	for (SipServletHolder servlet: _servlets)
    	{
    		if (servlet.getName() != null && servlet.getName().equals(name))
    			return servlet;
    	}
    	return null;
    }
    
    public void processListeners () throws Exception
    {
        //@SipListener(applicationName=String, description=String)
        for (Class clazz:_finder.getClassesForAnnotation(SipListener.class))
        { 
        	SipListener annotation = (SipListener) clazz.getAnnotation(SipListener.class);
        	checkAppName(annotation.applicationName(), clazz);
        	_listenerClasses = LazyList.add(_listenerClasses, clazz.getName());
        }
    }
    
    public SipApplication getSipApplication()
	{
		return _sipApplication;
	}


	public String getAppName()
	{
		return _appName;
	}


	protected Object newListenerInstance(Class clazz) 
	throws InstantiationException, IllegalAccessException 
	{	
		return clazz.newInstance();
	}

	private void checkAppName(String appName, Class clazz)
    {
    	if (appName != null && !appName.trim().equals("")) 
        {
        	if (!_appName.equals(appName))
        		throw new IllegalStateException("Only one application allowed: got SIP application name: " 
        				+ _appName + " and servlet SIP application name: " + appName);
        } 
        else
        {
        	SipApplication sipApplication = (SipApplication) clazz.getPackage().getAnnotation(SipApplication.class);
        	if (sipApplication == null)
        		throw new IllegalStateException("SipApplication name not defined in the servlet: " + clazz.getName()
        				+ ", nor in package");
        	if (!_appName.equals(sipApplication.name()))
        		throw new IllegalStateException("Only one application allowed: got SIP application name: " 
            			+ _appName + " and servlet SIP application name: " + sipApplication.name());	
        }
    }
    
    public List<SipServletHolder> getServlets()
    {
        return _servlets;
    }
    
    public Object getListenerClasses()
    {
        return _listenerClasses;
    }
   
    public Method getSipApplicationKeyMethod()
    {
    	return _sipApplicationKeyMethod;
    }

	public void processRunAsAnnotations ()
    throws Exception
    {
        for (Class clazz:_finder.getClassesForAnnotation(RunAs.class))
        {
        	if (!javax.servlet.Servlet.class.isAssignableFrom(clazz))
            {
                Log.debug("Ignoring runAs notation on on-servlet class "+clazz.getName());
                continue;
            }
            RunAs runAs = (RunAs)clazz.getAnnotation(RunAs.class);
            if (runAs != null)
            {
                String role = runAs.value();
                if (role != null)
                {
                    org.mortbay.jetty.plus.annotation.RunAs ra = new org.mortbay.jetty.plus.annotation.RunAs();
                    ra.setTargetClass(clazz);
                    ra.setRoleName(role);
                    _runAs.add(ra);
                }
            }
        } 
    }
    
    
    public void processLifeCycleCallbackAnnotations()
    throws Exception
    {
        processPostConstructAnnotations();
        processPreDestroyAnnotations();
    }

    private void processPostConstructAnnotations ()
    throws Exception
    {
    	try 
    	{
	        //      TODO: check that the same class does not have more than one
	        for (Method m:_finder.getMethodsForAnnotation(PostConstruct.class))
	        {
	        	if (!isServletType(m.getDeclaringClass()))
	            {
	                Log.debug("Ignoring "+m.getName()+" as non-servlet type");
	                continue;
	            }
	            if (m.getParameterTypes().length != 0)
	                throw new IllegalStateException(m+" has parameters");
	            if (m.getReturnType() != Void.TYPE)
	                throw new IllegalStateException(m+" is not void");
	            if (m.getExceptionTypes().length != 0)
	                throw new IllegalStateException(m+" throws checked exceptions");
	            if (Modifier.isStatic(m.getModifiers()))
	                throw new IllegalStateException(m+" is static");
	
	            PostConstructCallback callback = new PostConstructCallback();
	            callback.setTargetClass(m.getDeclaringClass());
	            callback.setTarget(m);
	            _callbacks.add(callback);
	        }
    	} catch (ClassNotFoundException e) 
    	{
    		// Ignore this exception as we're too eager to look at classes that might not be loaded by the application.
    	}
    }

    public void processPreDestroyAnnotations ()
    throws Exception
    {
    	try
    	{
	        //TODO: check that the same class does not have more than one
	
	        for (Method m: _finder.getMethodsForAnnotation(PreDestroy.class))
	        {
	        	if (!isServletType(m.getDeclaringClass()))
	            {
	                Log.debug("Ignoring "+m.getName()+" as non-servlet type");
	                continue;
	            }
	            if (m.getParameterTypes().length != 0)
	                throw new IllegalStateException(m+" has parameters");
	            if (m.getReturnType() != Void.TYPE)
	                throw new IllegalStateException(m+" is not void");
	            if (m.getExceptionTypes().length != 0)
	                throw new IllegalStateException(m+" throws checked exceptions");
	            if (Modifier.isStatic(m.getModifiers()))
	                throw new IllegalStateException(m+" is static");
	           
	            PreDestroyCallback callback = new PreDestroyCallback(); 
	            callback.setTargetClass(m.getDeclaringClass());
	            callback.setTarget(m);
	            _callbacks.add(callback);
	        }
    	} catch (ClassNotFoundException e)
    	{
    		// Ignore this exception as we're too eager to look at classes that might not be loaded by the application.
    	}
    }
    
    
    /**
     * Process @Resources annotation on classes
     */
    public void processResourcesAnnotations ()
    throws Exception
    {
        List<Class<?>> classes = _finder.getClassesForAnnotation(Resources.class);
        for (Class<?> clazz:classes)
        {
        	if (!isServletType(clazz))
            {
                Log.debug("Ignoring @Resources annotation on on-servlet type class "+clazz.getName());
                continue;
            }
            //Handle Resources annotation - add namespace entries
            Resources resources = (Resources) clazz.getAnnotation(Resources.class);
            if (resources == null)
                continue;

            Resource[] resArray = resources.value();
            if (resArray==null||resArray.length==0)
                continue;

            for (int j=0;j<resArray.length;j++)
            {
                String name = resArray[j].name();
                String mappedName = resArray[j].mappedName();
                //Resource.AuthenticationType auth = resArray[j].authenticationType();
                //Class type = resArray[j].type();
                //boolean shareable = resArray[j].shareable();

                if (name==null || name.trim().equals(""))
                    throw new IllegalStateException ("Class level Resource annotations must contain a name (Common Annotations Spec Section 2.3)");
                try
                {
                    //TODO don't ignore the shareable, auth etc etc

                       if (!org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(_sipApp, name, mappedName))
                           if (!org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(_sipApp.getServer(), name, mappedName))
                               throw new IllegalStateException("No resource bound at "+(mappedName==null?name:mappedName));
                }
                catch (NamingException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
    
    
    public void processResourceAnnotations ()
    throws Exception
    {
        processClassResourceAnnotations();
        processMethodResourceAnnotations();
        processFieldResourceAnnotations();
    }
    
    /**
     *  Class level Resource annotations declare a name in the
     *  environment that will be looked up at runtime. They do
     *  not specify an injection.
     */
	public void processClassResourceAnnotations ()
    throws Exception
    {
        List<Class<?>> classes = _finder.getClassesForAnnotation(Resource.class);
        for (Class<?> clazz:classes)
        {
        	if (!isServletType(clazz))
            {
                Log.debug("Ignoring @Resource annotation on on-servlet type class "+clazz.getName());
                continue;
            }
            //Handle Resource annotation - add namespace entries
            Resource resource = (Resource) clazz.getAnnotation(Resource.class);
            if (resource != null)
            {
               String name = resource.name();
               String mappedName = resource.mappedName();
               //Resource.AuthenticationType auth = resource.authenticationType();
               //Class type = resource.type();
               // boolean shareable = resource.shareable();
               
               if (name==null || name.trim().equals(""))
                   throw new IllegalStateException ("Class level Resource annotations must contain a name (Common Annotations Spec Section 2.3)");
               
               try
               {
                   //TODO don't ignore the shareable, auth etc etc
                   if (!org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(_sipApp, name,mappedName))
                       if (!org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(_sipApp.getServer(), name,mappedName))
                           throw new IllegalStateException("No resource at "+(mappedName==null?name:mappedName));
               }
               catch (NamingException e)
               {
                   throw new IllegalStateException(e);
               }
            }
       }
    }
    

    /**
     * Process a Resource annotation on the Methods.
     * 
     * This will generate a JNDI entry, and an Injection to be
     * processed when an instance of the class is created.
     * @param injections
     */
    public void processMethodResourceAnnotations ()
    throws Exception
    {
        //Get all methods that have a Resource annotation
        List<Method> methods = _finder.getMethodsForAnnotation(javax.annotation.Resource.class);

        for (Method m: methods)
        {
        	if (!isServletType(m.getDeclaringClass()))
            {
                Log.debug("Ignoring @Resource annotation on on-servlet type method "+m.getName());
                continue;
            }
            /*
             * Commons Annotations Spec 2.3
             * " The Resource annotation is used to declare a reference to a resource.
             *   It can be specified on a class, methods or on fields. When the 
             *   annotation is applied on a field or method, the container will 
             *   inject an instance of the requested resource into the application 
             *   when the application is initialized... Even though this annotation 
             *   is not marked Inherited, if used all superclasses MUST be examined 
             *   to discover all uses of this annotation. All such annotation instances 
             *   specify resources that are needed by the application. Note that this 
             *   annotation may appear on private fields and methods of the superclasses. 
             *   Injection of the declared resources needs to happen in these cases as 
             *   well, even if a method with such an annotation is overridden by a subclass."
             *  
             *  Which IMHO, put more succinctly means "If you find a @Resource on any method
             *  or field, inject it!".
             */
            Resource resource = (Resource)m.getAnnotation(Resource.class);
            if (resource == null)
                continue;

            //JavaEE Spec 5.2.3: Method cannot be static
            if (Modifier.isStatic(m.getModifiers()))
                throw new IllegalStateException(m+" cannot be static");


            // Check it is a valid javabean 
            if (!IntrospectionUtil.isJavaBeanCompliantSetter(m))
                throw new IllegalStateException(m+" is not a java bean compliant setter method");

            Class type = m.getParameterTypes()[0];
            
            boolean checkJndiEntry = false; // SipFactory, SipSessionsUtil, TimerService are not registered yet.
            String name = getSipResourceJndiName(type);
            if (name == null)
            {          
            	checkJndiEntry = true;
	            //default name is the javabean property name
	            name = m.getName().substring(3);
	            name = name.substring(0,1).toLowerCase()+name.substring(1);
	            name = m.getDeclaringClass().getCanonicalName()+"/"+name;
	            //allow default name to be overridden
	            name = (resource.name()!=null && !resource.name().trim().equals("")? resource.name(): name);
            }
            
            //get the mappedName if there is one
            String mappedName = (resource.mappedName()!=null && !resource.mappedName().trim().equals("")?resource.mappedName():null);
         
            //get other parts that can be specified in @Resource
            //Resource.AuthenticationType auth = resource.authenticationType();
            //boolean shareable = resource.shareable();

            //if @Resource specifies a type, check it is compatible with setter param
            if ((resource.type() != null) 
                    && 
                    !resource.type().equals(Object.class)
                    &&
                    (!IntrospectionUtil.isTypeCompatible(type, resource.type(), false)))
                throw new IllegalStateException("@Resource incompatible type="+resource.type()+ " with method param="+type+ " for "+m);

            //check if an injection has already been setup for this target by web.xml
            Injection webXmlInjection = _injections.getInjection(m.getDeclaringClass(), m);
            if (webXmlInjection == null)
            {
                try
                {
                    //try binding name to environment
                    //try the webapp's environment first
                    boolean bound = org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(_sipApp, name, mappedName);
                    
                    //try the server's environment
                    if (!bound)
                        bound = org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(_sipApp.getServer(), name, mappedName);
                    
                    //try the jvm's environment
                    if (!bound)
                        bound = org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(null, name, mappedName);
                    
                    //TODO if it is an env-entry from web.xml it can be injected, in which case there will be no
                    //NamingEntry, just a value bound in java:comp/env
                    if (!bound)
                    {
                        try
                        {
                            InitialContext ic = new InitialContext();
                            String nameInEnvironment = (mappedName!=null?mappedName:name);
                            ic.lookup("java:comp/env/"+nameInEnvironment);                               
                            bound = true;
                        }
                        catch (NameNotFoundException e)
                        {
                            bound = false;
                        }
                    }
                    
                    if (bound)
                    {
                        Log.debug("Bound "+(mappedName==null?name:mappedName) + " as "+ name);
                        //   Make the Injection for it
                        Injection injection = new Injection();
                        injection.setTargetClass(m.getDeclaringClass());
                        injection.setJndiName(name);
                        injection.setMappingName(mappedName);
                        injection.setTarget(m);
                        _injections.add(injection);
                    } 
                    else if (!isEnvEntryType(type))
                    {

                        //if this is an env-entry type resource and there is no value bound for it, it isn't
                        //an error, it just means that perhaps the code will use a default value instead
                        // JavaEE Spec. sec 5.4.1.3   
                        throw new IllegalStateException("No resource at "+(mappedName==null?name:mappedName));
                    }
                }
                catch (NamingException e)
                {  
                    //if this is an env-entry type resource and there is no value bound for it, it isn't
                    //an error, it just means that perhaps the code will use a default value instead
                    // JavaEE Spec. sec 5.4.1.3
                    if (!isEnvEntryType(type))
                        throw new IllegalStateException(e);
                }
            }
            else
            {
                //if an injection is already set up for this name, then the types must be compatible
                //JavaEE spec sec 5.2.4

                Object value = webXmlInjection.lookupInjectedValue();
                if (!IntrospectionUtil.isTypeCompatible(type, value.getClass(), false))
                    throw new IllegalStateException("Type of field="+type+" is not compatible with Resource type="+value.getClass());
            }
        }
    }
    
    public void processSipApplicationKeyAnnotations()
    throws Exception
    {
        List<Method> methods = _finder.getMethodsForAnnotation(SipApplicationKey.class);

        if (methods.size() > 1)
        	throw new IllegalStateException("Found multiple SipApplicationKey annotations");
        if (methods.size() == 0)
        	return;
        
        Method  m = methods.get(0);

        if (!Modifier.isStatic(m.getModifiers()))
            throw new IllegalStateException(m+" must be static");
        
        if (!Modifier.isPublic(m.getModifiers()))
            throw new IllegalStateException(m+" must be public");
        
        if (m.getParameterTypes().length != 1)
        	throw new IllegalStateException(m+" argument must have a single argument");
                
        if (!SipServletRequest.class.equals(m.getParameterTypes()[0]))
        	throw new IllegalStateException(m+" argument must be of type SipServletRequest");
            
        if (!String.class.equals(m.getReturnType()))
        	throw new IllegalStateException(m+" must return a String");
        
        _sipApplicationKeyMethod = m;
    }

	private String getSipResourceJndiName(Class clazz)
    {
    	if (clazz.isAssignableFrom(SipFactory.class))
        {
        	Log.info("Detect SipFactory Resource from annotation");
        	return Configuration.JNDI_SIP_PREFIX + _appName + Configuration.JNDI_SIP_FACTORY_POSTFIX;
        } 
        else if (clazz.isAssignableFrom(SipSessionsUtil.class))
        {
        	Log.info("Detect SipSessionsUtil Resource from annotation");
        	return Configuration.JNDI_SIP_PREFIX + _appName + Configuration.JNDI_SIP_SESSIONS_UTIL_POSTFIX;
        } 
        else if (clazz.isAssignableFrom(TimerService.class))
        {
        	Log.info("Detect TimerService Resource from annotation");
        	return Configuration.JNDI_SIP_PREFIX + _appName + Configuration.JNDI_TIMER_SERVICE_POSTFIX;
        } else
        {
        	return null;
        }
    }

    /**
     * Process @Resource annotation for a Field. These will both set up a
     * JNDI entry and generate an Injection. Or they can be the equivalent
     * of env-entries with default values
     * 
     * @param injections
     */
    public void processFieldResourceAnnotations ()
    throws Exception
    {
        //Get all fields that have a Resource annotation
        List<Field> fields = _finder.getFieldsForAnnotation(Resource.class);
        for (Field f: fields)
        {
        	if (!isServletType(f.getDeclaringClass()))
            {
                Log.debug("Ignoring @Resource annotation on on-servlet type field "+f.getName());
                continue;
            }
        	
            Resource resource = (Resource)f.getAnnotation(Resource.class);
            if (resource == null)
                continue;
           	
            
            //JavaEE Spec 5.2.3: Field cannot be static
            if (Modifier.isStatic(f.getModifiers()))
                throw new IllegalStateException(f+" cannot be static");

            //JavaEE Spec 5.2.3: Field cannot be final
            if (Modifier.isFinal(f.getModifiers()))
                throw new IllegalStateException(f+" cannot be final");

            //work out default name
            String name = f.getDeclaringClass().getCanonicalName()+"/"+f.getName();
            //allow @Resource name= to override the field name
            
            //get the type of the Field
            Class type = f.getType();
            
            boolean checkJndiEntry = false; // SipFactory, SipSessionsUtil, TimerService are not registered yet.
            name = getSipResourceJndiName(type);
            if (name == null)
            {
            	name = (resource.name()!=null && !resource.name().trim().equals("")? resource.name(): name);
            	checkJndiEntry = true;
            }

            //if @Resource specifies a type, check it is compatible with field type
            if ((resource.type() != null)
                    && 
                    !resource.type().equals(Object.class)
                    &&
                    (!IntrospectionUtil.isTypeCompatible(type, resource.type(), false)))
                throw new IllegalStateException("@Resource incompatible type="+resource.type()+ " with field type ="+f.getType());

            //get the mappedName if there is one
            String mappedName = null;
            if (mappedName == null && resource.mappedName()!=null && !resource.mappedName().trim().equals(""))
            	mappedName = resource.mappedName();
            
            //get other parts that can be specified in @Resource
            // Resource.AuthenticationType auth = resource.authenticationType();
            // boolean shareable = resource.shareable();
            //check if an injection has already been setup for this target by web.xml
            Injection webXmlInjection = _injections.getInjection(f.getDeclaringClass(), f);
            if (webXmlInjection == null)
            {
                try
                {               	
                	boolean bound = false;
                	
                	if (checkJndiEntry)
                	{
	                    bound = org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(_sipApp, name, mappedName);
	                    if (!bound)
	                        bound = org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(_sipApp.getServer(), name, mappedName);
	                    if (!bound)
	                        bound =  org.mortbay.jetty.plus.naming.NamingEntryUtil.bindToENC(null, name, mappedName); 
	                    if (!bound)
	                    {
	                        //see if there is an env-entry value been bound from web.xml
	                        try
	                        {
	                            InitialContext ic = new InitialContext();
	                            String nameInEnvironment = (mappedName!=null?mappedName:name);
	                            ic.lookup("java:comp/env/"+nameInEnvironment);                               
	                            bound = true;
	                        }
	                        catch (NameNotFoundException e)
	                        {
	                            bound = false;
	                        }
	                    }
                	}
                	
                    //Check there is a JNDI entry for this annotation 
                    if (bound || !checkJndiEntry)
                    { 
                        Log.debug("Bound "+(mappedName==null?name:mappedName) + " as "+ name);
                        //   Make the Injection for it if the binding succeeded
                        Injection injection = new Injection();
                        injection.setTargetClass(f.getDeclaringClass());
                        injection.setJndiName(name);
                        injection.setMappingName(mappedName);
                        injection.setTarget(f);
                        _injections.add(injection); 
                    }  
                    else if (!isEnvEntryType(type))
                    {
                        //if this is an env-entry type resource and there is no value bound for it, it isn't
                        //an error, it just means that perhaps the code will use a default value instead
                        // JavaEE Spec. sec 5.4.1.3

                        throw new IllegalStateException("No resource at "+(mappedName==null?name:mappedName));
                    }
                	
                	
                }
                catch (NamingException e)
                {
                    //if this is an env-entry type resource and there is no value bound for it, it isn't
                    //an error, it just means that perhaps the code will use a default value instead
                    // JavaEE Spec. sec 5.4.1.3
                    if (!isEnvEntryType(type))
                        throw new IllegalStateException(e);
                }
            }
            else
            {
                //if an injection is already set up for this name, then the types must be compatible
                //JavaEE spec sec 5.2.4
                Object value = webXmlInjection.lookupInjectedValue();
                if (!IntrospectionUtil.isTypeCompatible(type, value.getClass(), false))
                    throw new IllegalStateException("Type of field="+type+" is not compatible with Resource type="+value.getClass());
            }
        }
    }

    private static boolean isEnvEntryType (Class type)
    {
        boolean result = false;
        for (int i=0;i<__envEntryTypes.length && !result;i++)
        {
            result = (type.equals(__envEntryTypes[i]));
        }
        return result;
    }
    
    protected static String normalizePattern(String p)
    {
        if (p!=null && p.length()>0 && !p.startsWith("/") && !p.startsWith("*"))
            return "/"+p;
        return p;
    }


	public RunAsCollection getRunAs()
	{
		return _runAs;
	}


	public InjectionCollection getInjections()
	{
		return _injections;
	}
	
    /**
     * Check if the presented method belongs to a class that is one
     * of the classes with which a servlet container should be concerned.
     * @param m
     * @return
     */
    private boolean isServletType (Class c)
    {    
        boolean isServlet = false;
        if (javax.servlet.Servlet.class.isAssignableFrom(c) ||
                javax.servlet.Filter.class.isAssignableFrom(c) || 
                javax.servlet.ServletContextListener.class.isAssignableFrom(c) ||
                javax.servlet.ServletContextAttributeListener.class.isAssignableFrom(c) ||
                javax.servlet.ServletRequestListener.class.isAssignableFrom(c) ||
                javax.servlet.ServletRequestAttributeListener.class.isAssignableFrom(c) ||
                javax.servlet.http.HttpSessionListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.SipApplicationSessionAttributeListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.SipApplicationSessionActivationListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.SipApplicationSessionListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.SipSessionAttributeListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.SipServletListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.SipSessionActivationListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.SipSessionListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.SipErrorListener.class.isAssignableFrom(c) ||
                javax.servlet.sip.TimerListener.class.isAssignableFrom(c))

                isServlet=true;
        
        return isServlet;  
    }
}
