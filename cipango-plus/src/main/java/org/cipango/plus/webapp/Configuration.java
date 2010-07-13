// ========================================================================
// Copyright (c) 2006-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================

package org.cipango.plus.webapp;

import org.cipango.plus.servlet.SipServletHandler;
import org.eclipse.jetty.plus.annotation.InjectionCollection;
import org.eclipse.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.eclipse.jetty.plus.annotation.RunAsCollection;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Configuration
 *
 *
 */
public class Configuration extends org.eclipse.jetty.plus.webapp.Configuration
{

	@Override
	/**
     *  Same as super.preConfigure() but using a SipServletHandler 
     */
    public void preConfigure (WebAppContext context)
    throws Exception
    {
    	//set up our special ServletHandler to remember injections and lifecycle callbacks
        SipServletHandler servletHandler = new SipServletHandler();
        SecurityHandler securityHandler = context.getSecurityHandler();
        org.eclipse.jetty.servlet.ServletHandler existingHandler = context.getServletHandler(); 
        servletHandler.setFilters(existingHandler.getFilters());
        servletHandler.setFilterMappings(existingHandler.getFilterMappings());    
        servletHandler.setServlets(existingHandler.getServlets());
        servletHandler.setServletMappings(existingHandler.getServletMappings());
        context.setServletHandler(servletHandler);
        if (securityHandler != null)
        	securityHandler.setHandler(servletHandler);  
        
        LifeCycleCallbackCollection callbacks = new LifeCycleCallbackCollection();
        context.setAttribute(LifeCycleCallbackCollection.LIFECYCLE_CALLBACK_COLLECTION, callbacks);
        InjectionCollection injections = new InjectionCollection();
        context.setAttribute(InjectionCollection.INJECTION_COLLECTION, injections);
        RunAsCollection runAsCollection = new RunAsCollection();
        context.setAttribute(RunAsCollection.RUNAS_COLLECTION, runAsCollection);  
    }

}
