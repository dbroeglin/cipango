//========================================================================
//$Id: Configuration.java 3347 2008-07-22 09:14:01Z janb $
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.cipango.annotations;

import java.util.EventListener;

import javax.servlet.sip.annotation.SipApplication;

import org.cipango.plus.servlet.SipServletHandler;
import org.cipango.servlet.SipServletHolder;
import org.mortbay.jetty.annotations.AnnotationFinder;
import org.mortbay.jetty.annotations.ClassNameResolver;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

/**
 * Configuration
 *
 *
 */
public class Configuration extends org.cipango.plus.sipapp.Configuration
{
    public static final String __web_inf_pattern = "org.mortbay.jetty.webapp.WebInfIncludeAnnotationJarPattern";
    public static final String __container_pattern = "org.mortbay.jetty.webapp.ContainerIncludeAnnotationJarPattern";
                                                      
    
    
    public Configuration ()
    {
        super();
    }
    
    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#parseAnnotations()
     */
    public void parseAnnotations() throws Exception
    {
        /*
         * TODO Need to also take account of hidden classes on system classpath that should never
         * contribute annotations to a webapp (system and server classes):
         * 
         * --- when scanning system classpath:
         *   + system classes : should always be scanned (subject to pattern)
         *   + server classes : always ignored
         *   
         * --- when scanning webapp classpath:
         *   + system classes : always ignored
         *   + server classes : always scanned
         * 
         * 
         * If same class is found in both container and in context then need to use
         * webappcontext parentloaderpriority to work out which one contributes the
         * annotation.
         */
       
       
        AnnotationFinder finder = new AnnotationFinder();

        //if no pattern for the container path is defined, then by default scan NOTHING
        Log.debug("Scanning system jars");
        finder.find(getWebAppContext().getClassLoader().getParent(), true, getWebAppContext().getInitParameter(__container_pattern), false, 
                new ClassNameResolver ()
                {
                    public boolean isExcluded (String name)
                    {
                        if (getSipAppContext().isSystemClass(name)) return false;
                        if (getSipAppContext().isServerClass(name)) return true;
                        return false;
                    }

                    public boolean shouldOverride (String name)
                    { 
                        //looking at system classpath
                        if (getWebAppContext().isParentLoaderPriority())
                            return true;
                        return false;
                    }
                });

        Log.debug("Scanning WEB-INF/lib jars");
        //if no pattern for web-inf/lib is defined, then by default scan everything in it
        finder.find (getWebAppContext().getClassLoader(), false, getWebAppContext().getInitParameter(__web_inf_pattern), true,
                new ClassNameResolver()
                {
                    public boolean isExcluded (String name)
                    {    
                        if (getSipAppContext().isSystemClass(name)) return true;
                        if (getSipAppContext().isServerClass(name)) return false;
                        return false;
                    }

                    public boolean shouldOverride (String name)
                    {
                        //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
                        if (getWebAppContext().isParentLoaderPriority())
                            return false;
                        return true;
                    }
                });       
        
        Log.debug("Scanning classes in WEB-INF/classes");
        finder.find(getWebAppContext().getWebInf().addPath("classes/"), 
                new ClassNameResolver()
                {
                    public boolean isExcluded (String name)
                    {
                        if (getSipAppContext().isSystemClass(name)) return true;
                        if (getSipAppContext().isServerClass(name)) return false;
                        return false;
                    }

                    public boolean shouldOverride (String name)
                    {
                        //looking at webapp classpath, found already-parsed class of same name - did it come from system or duplicate in webapp?
                        if (getWebAppContext().isParentLoaderPriority())
                            return false;
                        return true;
                    }
                });
        
        AnnotationProcessor processor = new AnnotationProcessor(getSipAppContext(), _appName,
        		finder, _runAsCollection, _injections, _callbacks, 
                LazyList.getList(_servlets), _listenerClasses);
        processor.process();
        _servlets = processor.getServlets();
        _listenerClasses = processor.getListenerClasses();
        initListeners();
        SipServletHandler servletHandler = (SipServletHandler) getWebAppContext().getServletHandler();
        servletHandler.setSipServlets((SipServletHolder[])LazyList.toArray(_servlets,SipServletHolder.class));
        SipApplication sipApp = processor.getSipApplication();
        if (sipApp != null)
        {
        	getSipAppContext().setDistributable(sipApp.distributable());
        	getSipAppContext().setProxyTimeout(sipApp.proxyTimeout());
        	getSipAppContext().setSessionTimeout(sipApp.sessionTimeout());
        	getSipAppContext().setDisplayName(sipApp.displayName());
        	servletHandler.setMainServletName(sipApp.mainServlet());
        	//TODO description, icons
        }
        getSipAppContext().setSipApplicationKeyMethod(processor.getSipApplicationKeyMethod());
        Log.debug("processor.getSipApplicationKeyMethod()={}", processor.getSipApplicationKeyMethod());
        getSipAppContext().setName(processor.getAppName());
        getWebAppContext().setEventListeners((EventListener[])LazyList.toArray(_listeners,EventListener.class));
    }
}
