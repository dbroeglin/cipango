// ========================================================================
// Copyright (c) 2009 Mort Bay Consulting Pty. Ltd.
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

package org.cipango.sipapp;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.cipango.servlet.SipServletHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.DescriptorProcessor;
import org.eclipse.jetty.webapp.DiscoveredAnnotation;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * MetaData
 *
 * All data associated with the configuration and deployment of a SIP application.
 */
public class SipMetaData
{        
    protected SipDescriptor _sipDefaultsRoot;
    protected SipDescriptor _sipXmlRoot;
    protected final List<SipDescriptor> _sipOverrideRoots=new ArrayList<SipDescriptor>();

    protected final List<DiscoveredAnnotation> _annotations = new ArrayList<DiscoveredAnnotation>();
    protected final List<DescriptorProcessor> _descriptorProcessors = new ArrayList<DescriptorProcessor>();
 
    private String _mainServletName;
    
    private final List<String> _listeners = new ArrayList<String>();
   
    public SipMetaData ()
    {
    }
    
    /**
     * Empty ready for reuse
     */
    public void clear ()
    {
        _sipDefaultsRoot = null;
        _sipXmlRoot = null;
        _sipOverrideRoots.clear();
        _annotations.clear();
        _descriptorProcessors.clear();
    }
    
    public void setDefaults (Resource sipDefaults)
    throws Exception
    {
        _sipDefaultsRoot =  new SipDescriptor(sipDefaults); 
        _sipDefaultsRoot.parse();   
    }
    
    public void setSipXml (Resource sipXml)
    throws Exception
    {
        _sipXmlRoot = new SipDescriptor(sipXml);
        _sipXmlRoot.parse();  
    }
    
    public void addOverride (Resource override)
    throws Exception
    {
    	SipDescriptor sipOverrideRoot = new SipDescriptor(override);
        sipOverrideRoot.setValidating(false);
        sipOverrideRoot.parse();

        _sipOverrideRoots.add(sipOverrideRoot);
    }
    
    /**
     * Annotations not associated with a WEB-INF/lib fragment jar.
     * These are from WEB-INF/classes or the ??container path??
     * @param annotations
     */
    public void addDiscoveredAnnotations(List<DiscoveredAnnotation> annotations)
    {
        _annotations.addAll(annotations);
    }
    
    public void addDescriptorProcessor(DescriptorProcessor p)
    {
        _descriptorProcessors.add(p);
    }
    
    /**
     * Resolve all servlet/filter/listener metadata from all sources: descriptors and annotations.
     * 
     */
    public void resolve (WebAppContext context)
    throws Exception
    {
        //Ensure origins is fresh
        
        for (DescriptorProcessor p:_descriptorProcessors)
        {
            p.process(context,getSipDefault());
            p.process(context,getSipXml());
            for (SipDescriptor wd : getOverrideSips())   
                p.process(context,wd);
        }
        
        for (DiscoveredAnnotation a:_annotations)
            a.apply();      
        
        if (_mainServletName != null)
        	((SipAppContext) context).getSipServletHandler().setMainServletName(_mainServletName);
        
        initListeners((SipAppContext) context);
    }
    
    protected SipServletHolder getServlet(SipAppContext context, String className)
	{
		for (SipServletHolder holder : context.getSipServletHandler().getSipServlets())
		{
			if (className.equals(holder.getClassName()))
				return holder;
		}
		return null;
	}

    
    private void initListeners(SipAppContext context)
    {
    	for (String className : _listeners)
		{
			SipServletHolder holder = getServlet(context, className);
				
			// Check listener has not been already added.
			boolean found = false;
			if (context.getEventListeners() != null)
			{
				for (EventListener listener : context.getEventListeners())
				{
					if (listener.getClass().getName().equals(className))
					{
						Log.debug("Found multiple listener declaration " +  className);
						if (holder != null)
							holder.setServlet((Servlet) listener);
						found = true;
						break;
					}
				}
			}
			
			if (found)
				continue;
			
			try
			{
				@SuppressWarnings("rawtypes")
				Class listenerClass = context.loadClass(className);
				@SuppressWarnings("unchecked")
				EventListener listener = newListenerInstance(context, listenerClass);
				
				if (holder != null)
					holder.setServlet((Servlet) listener);
					
				context.addEventListener(listener);
			}
			catch (Exception e) 
			{
				Log.warn("Could not instantiate listener: " + className, e);
			}
		}

    }
    
	public EventListener newListenerInstance(WebAppContext context,Class<? extends EventListener> clazz) throws ServletException, InstantiationException, IllegalAccessException
    {
        try
        {
            return ((ServletContextHandler.Context)context.getServletContext()).createListener(clazz);
        }
        catch (ServletException se)
        {
            Throwable cause = se.getRootCause();
            if (cause instanceof InstantiationException)
                throw (InstantiationException)cause;
            if (cause instanceof IllegalAccessException)
                throw (IllegalAccessException)cause;
            throw se;
        }
    }
    
    public boolean isDistributable ()
    {
        boolean distributable = (
                (_sipDefaultsRoot != null && _sipDefaultsRoot.isDistributable()) 
                || (_sipXmlRoot != null && _sipXmlRoot.isDistributable()));
        
        for (SipDescriptor d : _sipOverrideRoots)
            distributable&=d.isDistributable();
        
        return distributable;
    }
   
    
    public SipDescriptor getSipXml ()
    {
        return _sipXmlRoot;
    }
    
    public List<SipDescriptor> getOverrideSips ()
    {
        return _sipOverrideRoots;
    }
    
    public SipDescriptor getSipDefault ()
    {
        return _sipDefaultsRoot;
    }

	public String getMainServletName()
	{
		return _mainServletName;
	}

	public void setMainServletName(String mainServletName)
	{
		_mainServletName = mainServletName;
	}
	
	public void addListener(String classname)
	{
		_listeners.add(classname);
	}
}
