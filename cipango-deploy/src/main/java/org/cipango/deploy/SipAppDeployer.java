//========================================================================
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

package org.cipango.deploy;

import java.util.ArrayList;

import org.cipango.sipapp.SipAppContext;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.deploy.WebAppDeployer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.URIUtil;

public class SipAppDeployer extends WebAppDeployer 
{
    private ArrayList<SipAppContext> _deployed;
    private String _defaultsSipDescriptor;
    
    public void doStart() throws Exception 
    {
		_deployed = new ArrayList<SipAppContext>();
        scan();
    }
    
    public String getDefaultsSipDescriptor()
    {
        return _defaultsSipDescriptor;
    }

    public void setDefaultsSipDescriptor(String defaultsDescriptor)
    {
        _defaultsSipDescriptor = defaultsDescriptor;
    }
    
    @Override
    public void scan() throws Exception
    {
    	MultiException mex = new MultiException();
        if (getContexts() == null) 
            throw new IllegalArgumentException("No HandlerContainer");
        
        Resource r = Resource.newResource(getWebAppDir());
        if (!r.exists())
            throw new IllegalArgumentException("No such sipapps resource "+r);

        if (!r.isDirectory())
            throw new IllegalArgumentException("Not directory sipapps resource "+r);

        String[] files = r.list();

        files: for (int f = 0; files != null && f < files.length; f++)
        {
        	try 
        	{
	            String context = files[f];
	
	            if (context.equalsIgnoreCase("CVS/") || context.equalsIgnoreCase("CVS")||context.startsWith("."))
	                continue;
	
	            Resource app = r.addPath(r.encode(context));
	
	            if (context.toLowerCase().endsWith(".war")
	            		|| context.toLowerCase().endsWith(".jar")
	            		|| context.toLowerCase().endsWith(".sar")) 
	            {
	            	context = context.substring(0, context.length() - 4);
	                Resource unpacked = r.addPath(context);
	                if (unpacked != null && unpacked.exists() && unpacked.isDirectory())
	                    continue;
	            }
	            else if (!app.isDirectory()) 
	            {
	                continue;
	            }
	
	            if (context.equalsIgnoreCase("root") || context.equalsIgnoreCase("root/")) 
	                context = URIUtil.SLASH;
	            else 
	                context = "/" + context;
	            
	            if (context.endsWith("/") && context.length() > 0) 
	                context = context.substring(0, context.length() - 1);
	
	            // Check the context path has not already been added or the webapp itself is not already deployed
	            if (!getAllowDuplicates()) 
	            {            	
	                Handler[] installed = getContexts().getChildHandlersByClass(ContextHandler.class);
	                for (int i=0; i<installed.length; i++) 
	                {
	                    ContextHandler c=(ContextHandler)installed[i];
	                    
	                    if (c instanceof SipAppContext)
	                    {
	                    	SipAppContext sipAppContext = (SipAppContext) c;
	                    	if (context.equals(sipAppContext.getContextPath()))
	                    		 continue files;
	                    } 
	                    
	                    if (context.equals(c.getContextPath()))
	                        continue files;
	                    
	                    if (c.getBaseResource()!=null && c.getBaseResource().getFile().getAbsolutePath().equals(app.getFile().getAbsolutePath()))
	                        continue files;          
	                }
	            }
	
	            // create a sipapp
	            SipAppContext sac = null;
	            if (getContexts() instanceof ContextHandlerCollection && 
	                SipAppContext.class.isAssignableFrom(((ContextHandlerCollection) getContexts()).getContextClass())) 
	            {
	                try 
	                {
	                    sac =(SipAppContext)((ContextHandlerCollection) getContexts()).getContextClass().newInstance();
	                } 
	                catch (Exception e) 
	                {
	                    throw new Error(e);
	                }
	            } else 
	            {
	                sac = new SipAppContext();
	            }
	            
	            // configure it
	            sac.setContextPath(context);
	            if (getConfigurationClasses() != null) 
	                sac.setConfigurationClasses(getConfigurationClasses());
	            
	            if (getDefaultsDescriptor() != null) 
	                sac.setDefaultsDescriptor(getDefaultsDescriptor());
	            
	            if (getDefaultsSipDescriptor() != null)
	            	sac.setDefaultsSipDescriptor(getDefaultsSipDescriptor());
	            
	            sac.setExtractWAR(isExtract());
	            sac.setWar(app.toString());
	            sac.setParentLoaderPriority(isParentLoaderPriority());
	            
	            // add it
	            getContexts().addHandler(sac);
	            _deployed.add(sac);
	            
	            if (getContexts().isStarted())
	                getContexts().start();
        	} 
        	catch (Throwable e) 
        	{
        		mex.add(e);
			}
        }
        mex.ifExceptionThrow();
    }
    
    @Override
    public void doStop() throws Exception 
    {
    	MultiException mex = new MultiException();
        for (int i = _deployed.size(); i-- > 0;) 
        {
            ContextHandler wac = (ContextHandler)_deployed.get(i);
            try { wac.stop(); } catch (Throwable e) { mex.add(e);}
        }
        mex.ifExceptionThrow();
    }
}
