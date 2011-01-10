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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;

import javax.annotation.PostConstruct;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;

import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.DiscoveredAnnotation;

public class SipApplicationKeyAnnotation extends DiscoveredAnnotation
{
    public SipApplicationKeyAnnotation (SipAppContext context, String className)
    {
        super(context, className);
    }

    /** 
     * @see org.eclipse.jetty.annotations.ClassAnnotation#apply()
     */
    @SuppressWarnings("rawtypes")
	public void apply()
    {
        Class clazz = getTargetClass();
        
        if (clazz == null)
        {
            Log.warn(_className+" cannot be loaded");
            return;
        }
        
        Method[] methods = clazz.getDeclaredMethods();
        for (int i=0; i<methods.length; i++)
        {
            Method m = (Method)methods[i];
            if (m.isAnnotationPresent(SipApplicationKey.class))
            {
            	if (!Modifier.isStatic(m.getModifiers()))
            		throw new IllegalStateException(m.getName() + " must be static");
            	
            	if (!Modifier.isPublic(m.getModifiers()))
            		throw new IllegalStateException(m.getName() + " must be public");
            	
            	if (m.getParameterTypes().length != 1)
            		throw new IllegalStateException(m.getName() + " argument must have a single argument");
            	
            	if (m.getParameterTypes()[0] != SipServletRequest.class)
            		throw new IllegalStateException(m.getName() + " argument must be of type SipServletRequest");
            	
            	if (m.getReturnType() != String.class)
            		throw new IllegalStateException(m.getName() + " must return a String");
            	
            	((SipAppContext) _context).setSipApplicationKeyMethod(m);
            }
        }
    }
}
