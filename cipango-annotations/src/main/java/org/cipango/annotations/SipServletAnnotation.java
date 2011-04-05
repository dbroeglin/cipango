// ========================================================================
// Copyright 2010 NEXCOM Systems
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

import javax.servlet.sip.annotation.SipServlet;

import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.DiscoveredAnnotation;

public class SipServletAnnotation extends DiscoveredAnnotation
{
    
    public SipServletAnnotation (SipAppContext context, String className)
    {
        super(context, className);
    }

    /** 
     * @see org.eclipse.jetty.annotations.ClassAnnotation#apply()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void apply()
    {
        //TODO check this algorithm with new rules for applying descriptors and annotations in order
        Class clazz = getTargetClass();
        
        if (clazz == null)
        {
            Log.warn(_className+" cannot be loaded");
            return;
        }
        
        //Servlet Spec 8.1.1
        if (!javax.servlet.sip.SipServlet.class.isAssignableFrom(clazz))
        {
            Log.warn(clazz.getName()+" is not assignable from javax.servlet.sip.SipServlet");
            return;
        }
        
        SipServlet annotation = (SipServlet) clazz.getAnnotation(SipServlet.class);
        
        
        SipServletHolder holder = new SipServletHolder();
        
        SipAppContext context = (SipAppContext) _context;
		
        if (!Util.isEmpty(annotation.applicationName()))
        {
        	
        	if (context.getName() != null && !context.getName().equals(annotation.applicationName()))
    			throw new IllegalStateException("App-name in sip.xml: " + context.getName() 
    					+ " does not match with SipApplication annotation: " + annotation.applicationName());
        	context.getSipMetaData().setAppName(annotation.applicationName());
        }
        
        if (annotation.name() != null && !"".equals(annotation.name()))
        	holder.setName(annotation.name());
        else
        	holder.setName(_className.substring(_className.lastIndexOf('.') + 1));
        
        holder.setInitOrder(annotation.loadOnStartup());
        holder.setDisplayName(annotation.description());
        holder.setClassName(_className);
        
        context.addSipServlet(holder);
    }
}
