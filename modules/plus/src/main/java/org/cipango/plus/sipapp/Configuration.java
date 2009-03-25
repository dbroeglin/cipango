// ========================================================================
// $Id: Configuration.java 1598 2007-02-15 05:29:05Z janb $
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;

import org.mortbay.jetty.plus.naming.EnvEntry;
import org.mortbay.jetty.plus.naming.Link;
import org.mortbay.jetty.plus.naming.NamingEntry;
import org.mortbay.jetty.plus.naming.NamingEntryUtil;
import org.mortbay.jetty.plus.naming.Transaction;
import org.mortbay.log.Log;
import org.mortbay.naming.NamingUtil;


/**
 * Configuration
 *
 *
 */
public class Configuration extends AbstractConfiguration
{

    public static final String JNDI_SIP_PREFIX = "sip/";
    public static final String JNDI_SIP_FACTORY_POSTFIX = "/SipFactory";
    public static final String JNDI_TIMER_SERVICE_POSTFIX = "/TimerService";
    public static final String JNDI_SIP_SESSIONS_UTIL_POSTFIX = "/SipSessionsUtil";
    
    public Configuration ()
    {

    }
    
    
    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindEnvEntry(java.lang.String, java.lang.String)
     * @param name
     * @param value
     * @throws Exception
     */
    public void bindEnvEntry(String name, Object value) throws Exception
    {    
        InitialContext ic = null;
        boolean bound = false;
        //check to see if we bound a value and an EnvEntry with this name already
        //when we processed the server and the webapp's naming environment
        //@see EnvConfiguration.bindEnvEntries()
        ic = new InitialContext();
        try
        {
            NamingEntry ne = (NamingEntry)ic.lookup("java:comp/env/"+NamingEntryUtil.makeNamingEntryName(ic.getNameParser(""), name));
            if (ne!=null && ne instanceof EnvEntry)
            {
                EnvEntry ee = (EnvEntry)ne;
                bound = ee.isOverrideWebXml();
            }
        }
        catch (NameNotFoundException e)
        {
            bound = false;
        }

        if (!bound)
        {
            //either nothing was bound or the value from web.xml should override
            Context envCtx = (Context)ic.lookup("java:comp/env");
            NamingUtil.bind(envCtx, name, value);
        }
    }

    /** 
     * Bind a resource reference.
     * 
     * If a resource reference with the same name is in a jetty-env.xml
     * file, it will already have been bound.
     * 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindResourceRef(java.lang.String)
     * @param name
     * @throws Exception
     */
    public void bindResourceRef(String name, Class typeClass)
    throws Exception
    {
        bindEntry(name, typeClass);
    }

    /** 
     * @see org.mortbay.jetty.plus.webapp.AbstractConfiguration#bindResourceEnvRef(java.lang.String)
     * @param name
     * @throws Exception
     */
    public void bindResourceEnvRef(String name, Class typeClass)
    throws Exception
    {
        bindEntry(name, typeClass);
    }
    
    
    public void bindMessageDestinationRef(String name, Class typeClass)
    throws Exception
    {
        bindEntry(name, typeClass);
    }
    
    public void bindUserTransaction ()
    throws Exception
    {
        try
        {
           Transaction.bindToENC();
        }
        catch (NameNotFoundException e)
        {
            Log.info("No Transaction manager found - if your webapp requires one, please configure one.");
        }
        catch (InvalidNameException e) 
        {
			Log.info("Could not bind to Transaction manager");
			Log.debug("Could not bind to Transaction manager", e);
		}
    }
    
    public void configureClassLoader ()
    throws Exception
    {      
        super.configureClassLoader();
    }

    
    public void configureDefaults ()
    throws Exception
    {
        super.configureDefaults();
    }


    /** 
     * @see org.cipango.plus.sipapp.AbstractConfiguration#parseAnnotations()
     */
    protected void parseAnnotations() throws Exception
    {
        Log.info(getClass().getName()+" does not support annotations on source. Use org.cipango.annotations.Configuration instead");
    }

	@Override
	public void bindSipResources() throws Exception
	{
		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getWebAppContext().getClassLoader());
        Context context = new InitialContext();
        Context compCtx = (Context) context.lookup("java:comp/env");
        compCtx.createSubcontext("sip").createSubcontext(getSipAppContext().getName());
        compCtx.bind(JNDI_SIP_PREFIX + getSipAppContext().getName() + JNDI_SIP_FACTORY_POSTFIX, getSipAppContext().getSipFactory());
        compCtx.bind(JNDI_SIP_PREFIX + getSipAppContext().getName() + JNDI_TIMER_SERVICE_POSTFIX, getSipAppContext().getTimerService());
        compCtx.bind(JNDI_SIP_PREFIX + getSipAppContext().getName() + JNDI_SIP_SESSIONS_UTIL_POSTFIX, getSipAppContext().getSipSessionsUtil());
        Log.debug("Bind SIP Resources on app " + getSipAppContext().getName());
        Thread.currentThread().setContextClassLoader(oldLoader);
	}
	
    /**
     * Bind a resource with the given name from web.xml of the given type
     * with a jndi resource from either the server or the webapp's naming 
     * environment.
     * 
     * As the servlet spec does not cover the mapping of names in web.xml with
     * names from the execution environment, jetty uses the concept of a Link, which is
     * a subclass of the NamingEntry class. A Link defines a mapping of a name
     * from web.xml with a name from the execution environment (ie either the server or the
     * webapp's naming environment).
     * 
     * @param name name of the resource from web.xml
     * @param typeClass 
     * @throws Exception
     */
    private void bindEntry (String name, Class typeClass)
    throws Exception
    {
        String nameInEnvironment = name;
        boolean bound = false;
        
        //check if the name in web.xml has been mapped to something else
        //check a context-specific naming environment first
        Object scope = getWebAppContext();
        NamingEntry ne = NamingEntryUtil.lookupNamingEntry(scope, name);
    
        if (ne!=null && (ne instanceof Link))
        {
            //if we found a mapping, get out name it is mapped to in the environment
            nameInEnvironment = (String)((Link)ne).getObjectToBind();
            Link l = (Link)ne;
        }

        //try finding that mapped name in the webapp's environment first
        scope = getWebAppContext();
        bound = NamingEntryUtil.bindToENC(scope, name, nameInEnvironment);
        
        if (bound)
            return;

        //try the server's environment
        scope = getWebAppContext().getServer();
        bound = NamingEntryUtil.bindToENC(scope, name, nameInEnvironment);
        if (bound)
            return;

        //try the jvm environment
        bound = NamingEntryUtil.bindToENC(null, name, nameInEnvironment);
        if (bound)
            return;


        //There is no matching resource so try a default name.
        //The default name syntax is: the [res-type]/default
        //eg       javax.sql.DataSource/default
        nameInEnvironment = typeClass.getName()+"/default";
        //First try the server scope
        NamingEntry defaultNE = NamingEntryUtil.lookupNamingEntry(getWebAppContext().getServer(), nameInEnvironment);
        if (defaultNE==null)
            defaultNE = NamingEntryUtil.lookupNamingEntry(null, nameInEnvironment);
        
        if (defaultNE!=null)
            defaultNE.bindToENC(name);
        else
            throw new IllegalStateException("Nothing to bind for name "+nameInEnvironment);
    }
    
}
