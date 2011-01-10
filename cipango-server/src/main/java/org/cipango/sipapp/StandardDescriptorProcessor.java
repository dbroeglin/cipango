// ========================================================================
// Copyright (c) 2006-2010 Mort Bay Consulting Pty. Ltd.
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.EventListener;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.rules.AndRule;
import org.cipango.sipapp.rules.ContainsRule;
import org.cipango.sipapp.rules.EqualsRule;
import org.cipango.sipapp.rules.ExistsRule;
import org.cipango.sipapp.rules.MatchingRule;
import org.cipango.sipapp.rules.NotRule;
import org.cipango.sipapp.rules.OrRule;
import org.cipango.sipapp.rules.SubdomainRule;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.IterativeDescriptorProcessor;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlParser;

/**
 * StandardDescriptorProcessor
 *
 * Process a sip.xml, sip-defaults.xml, sip-overrides.xml.
 */
public class StandardDescriptorProcessor extends IterativeDescriptorProcessor
{
    public static final String STANDARD_PROCESSOR = "org.eclipse.jetty.standardDescriptorProcessor";
        
    public StandardDescriptorProcessor ()
    {
 
        try
        {
        	registerVisitor("app-name", this.getClass().getDeclaredMethod("visitAppName", __signature));
        	registerVisitor("servlet-selection", this.getClass().getDeclaredMethod("visitServletSelection", __signature));
        	registerVisitor("proxy-config", this.getClass().getDeclaredMethod("visitProxyConfig", __signature));
        	
            registerVisitor("context-param", this.getClass().getDeclaredMethod("visitContextParam", __signature));
            registerVisitor("display-name", this.getClass().getDeclaredMethod("visitDisplayName", __signature));
            registerVisitor("servlet", this.getClass().getDeclaredMethod("visitServlet",  __signature));
            registerVisitor("servlet-mapping", this.getClass().getDeclaredMethod("visitServletMapping",  __signature));
            registerVisitor("session-config", this.getClass().getDeclaredMethod("visitSessionConfig",  __signature));
            // FIXME registerVisitor("security-constraint", this.getClass().getDeclaredMethod("visitSecurityConstraint",  __signature));
            // FIXME registerVisitor("login-config", this.getClass().getDeclaredMethod("visitLoginConfig",  __signature));
            // FIXME registerVisitor("security-role", this.getClass().getDeclaredMethod("visitSecurityRole",  __signature));
            registerVisitor("listener", this.getClass().getDeclaredMethod("visitListener",  __signature));
            registerVisitor("distributable", this.getClass().getDeclaredMethod("visitDistributable",  __signature));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    
    
    /** 
     * @see org.eclipse.jetty.webapp.IterativeDescriptorProcessor#start()
     */
    public void start(WebAppContext context, Descriptor descriptor)
    { 
    	if (descriptor instanceof SipDescriptor)
    		((SipAppContext) context).setSpecVersion(((SipDescriptor) descriptor).getVersion());
    }
    
    
    
    /** 
     * @see org.eclipse.jetty.webapp.IterativeDescriptorProcessor#end()
     */
    public void end(WebAppContext context, Descriptor descriptor)
    {
    }
    
    /**
     * @param context
     * @param descriptor
     * @param node
     */
    public void visitContextParam (WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
        String name = node.getString("param-name", false, true);
        String value = node.getString("param-value", false, true);
 
        context.getInitParams().put(name, value);
    
        if (Log.isDebugEnabled()) Log.debug("ContextParam: " + name + "=" + value);

    }
    

    /* ------------------------------------------------------------ */
    /**
     * @param context
     * @param descriptor
     * @param node
     */
    public void visitDisplayName(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
        context.setDisplayName(node.toString(false, true));
    }
    
    
    /**
     * @param context
     * @param descriptor
     * @param node
     */
	@SuppressWarnings("rawtypes")
    public void visitServlet(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
    	String servletName = node.getString("servlet-name", false, true);
		String servletClass = node.getString("servlet-class", false, true);
		// FIXME allow deploy with prefix: javaee:servlet-name
		SipServletHolder holder = new SipServletHolder();
		holder.setName(servletName);
		holder.setClassName(servletClass);
		
		Iterator params = node.iterator("init-param");
		
		while (params.hasNext()) 
		{
			XmlParser.Node param = (XmlParser.Node) params.next();
			String pName = param.getString("param-name", false, true);
			String pValue = param.getString("param-value", false, true);
			holder.setInitParameter(pName, pValue);
		}
		
		XmlParser.Node startup = node.get("load-on-startup");
		if (startup != null) 
		{
			String s = startup.toString(false, true);
			int order = 0; 
			if (s != null && s.trim().length() > 0) 
			{
				try 
				{
					order = Integer.parseInt(s);
				} 
				catch (NumberFormatException e) 
				{
					Log.warn("Cannot parse load-on-startup " + s);
				}
			}
			holder.setInitOrder(order);
		}
		
        // FIXME use same instance for listener and servlet
		((SipAppContext) context).addSipServlet(holder);
    }
    
    

    /**
     * @param context
     * @param descriptor
     * @param node
     */
	public void visitServletMapping(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
        String servletName = node.getString("servlet-name", false, true); 
        
        SipServletMapping mapping = new SipServletMapping();
		
		XmlParser.Node pattern = node.get("pattern");
		XmlParser.Node start = null;
		@SuppressWarnings("rawtypes")
		Iterator it = pattern.iterator();
		
		while (it.hasNext() && start == null) 
		{
			Object o = it.next();
			if (!(o instanceof XmlParser.Node)) 
				continue;

			start = (XmlParser.Node) o;
		}
        
        MatchingRule rule = initRule(start);
		mapping.setServletName(servletName);
		mapping.setMatchingRule(rule);
        
        ((SipAppContext) context).getSipServletHandler().addSipServletMapping(mapping);
    }

	@SuppressWarnings("rawtypes")
	public MatchingRule initRule(XmlParser.Node node) 
	{
		String name = node.getTag();
		if ("and".equals(name)) 
		{
			AndRule and = new AndRule();
			Iterator it = node.iterator();
			while (it.hasNext()) 
			{
				Object o = it.next();
				if (!(o instanceof XmlParser.Node)) 
					continue;
				
				and.addCriterion(initRule((XmlParser.Node) o));
			}
			return and;
		} 
		else if ("equal".equals(name)) 
		{
			String var = node.getString("var", false, true);
			String value = node.getString("value", false, true);
			boolean ignoreCase = "true".equalsIgnoreCase(node.getAttribute("ignore-case"));
			return new EqualsRule(var, value, ignoreCase);
		} 
		else if ("subdomain-of".equals(name)) 
		{
			String var = node.getString("var", false, true);
			String value = node.getString("value", false, true);
			return new SubdomainRule(var, value);
		} 
		else if ("or".equals(name)) 
		{
			OrRule or = new OrRule();
			Iterator it = node.iterator();
			while (it.hasNext()) 
			{
				Object o = it.next();
				if (!(o instanceof XmlParser.Node)) 
					continue;

				or.addCriterion(initRule((XmlParser.Node) o));
			}
			return or;
		} 
		else if ("not".equals(name)) 
		{
			NotRule not = new NotRule();
			Iterator it = node.iterator();
			while (it.hasNext()) 
			{
				Object o = it.next();
				if (!(o instanceof XmlParser.Node)) 
					continue;
				
				not.setCriterion(initRule((XmlParser.Node) o));
			}
			return not;
		} 
		else if ("contains".equals(name)) 
		{
			String var = node.getString("var", false, true);
			String value = node.getString("value", false, true);
			boolean ignoreCase = "true".equalsIgnoreCase(node.getAttribute("ignore-case"));
			return new ContainsRule(var, value, ignoreCase);
		} 
		else if ("exists".equals(name)) 
		{
			return new ExistsRule(node.getString("var", false, true));
		} 
		else 
		{
			throw new IllegalArgumentException("Unknown rule: " + name);
		}
	}
    
    
    /**
     * @param context
     * @param descriptor
     * @param node
     */
	public void visitSessionConfig(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
        XmlParser.Node tNode = node.get("session-timeout");
        if (tNode != null)
        {
            int timeout = Integer.parseInt(tNode.toString(false, true));
            ((SipAppContext) context).setSessionTimeout(timeout);
        }
    }
    
	public void visitAppName(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
    	((SipAppContext) context).setName(node.toString(false, true)); 
    }
    
	public void visitServletSelection(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
    	XmlParser.Node mainServlet = node.get("main-servlet");
		if (mainServlet != null)
			((SipAppContext) context).getSipMetaData().setMainServletName(mainServlet.toString(false, true));
		else
		{
			@SuppressWarnings("rawtypes")
			Iterator it = node.iterator("servlet-mapping");
			while (it.hasNext())
			{
				visitServletMapping(context, descriptor, (XmlParser.Node)it.next());
			}
		}
    }
    
	public void visitProxyConfig(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
    	 String s = node.getString("proxy-timeout", false, true);
         
         if (s == null)
         	s = node.getString("sequential-search-timeout", false, true);
         
         if (s != null)
         {
             try 
             {
                 int timeout = Integer.parseInt(s);
                 ((SipAppContext) context).setProxyTimeout(timeout);
             }
             catch (NumberFormatException e)
             {
                 Log.warn("Invalid sequential-search-timeout value: " + s);
             }
         }
    }
    
    /**
     * @param context
     * @param descriptor
     * @param node
     */
	public void visitListener(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
        String className = node.getString("listener-class", false, true);
        EventListener listener = null;
        try
        {
            if (className != null && className.length()> 0)
            {
            	((SipAppContext) context).getSipMetaData().addListener(className); 
            }
        }
        catch (Exception e)
        {
            Log.warn("Could not instantiate listener " + className, e);
            return;
        }
    }
    
    /**
     * @param context
     * @param descriptor
     * @param node
     */
	public void visitDistributable(WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
        // the element has no content, so its simple presence
        // indicates that the webapp is distributable...
        ((SipDescriptor)descriptor).setDistributable(true);
    }

}
