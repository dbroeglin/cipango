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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;

import org.cipango.plus.sipapp.Configuration;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.AnnotationParser.Value;
import org.eclipse.jetty.plus.annotation.Injection;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;

public class ResourceAnnotationHandler extends org.eclipse.jetty.annotations.ResourceAnnotationHandler
{
	protected List<Injection> _sipInjections = new ArrayList<Injection>();
		
	public ResourceAnnotationHandler(WebAppContext wac)
	{
		super(wac);
	}
	
	protected SipAppContext getSipAppCtx()
	{
		return (SipAppContext) _wac;
	}
	
	@Override
	public void handleField(String className, String fieldName, int access, String fieldType, String signature, Object value, String annotation,
            List<Value> values)
	{
		String jndiName = getSipResourceJndiName(fieldType);
		if (jndiName != null)
		{
			// The Sip resources are injected in JNDI latter (in plus config), so could not check for availability.
			
			//JavaEE Spec 5.2.3: Field cannot be static
            if ((access & org.objectweb.asm.Opcodes.ACC_STATIC) > 0)
            {
                Log.warn("Skipping Resource annotation on "+className+"."+fieldName+": cannot be static");
                return;
            }

            //JavaEE Spec 5.2.3: Field cannot be final
            if ((access & org.objectweb.asm.Opcodes.ACC_FINAL) > 0)
            {
                Log.warn("Skipping Resource annotation on "+className+"."+fieldName+": cannot be final");
                return;
            }
			
            //   Make the Injection for it if the binding succeeded
            Injection injection = new Injection();
            injection.setTarget(className, fieldName, null);
            injection.setJndiName(jndiName);
            _injections.add(injection); 
            _sipInjections.add(injection);
		}
		else		
			super.handleField(className, fieldName, access, fieldType, signature, value, annotation, values);
	}

	/**
	 * As application name may not be set when resource is detected, the JNDI could need to be updated.
	 */
	public void normalizeSipInjections()
	{
		Iterator<Injection> it = _sipInjections.iterator();
		while (it.hasNext())
		{
			Injection injection = (Injection) it.next();
			String name = injection.getJndiName();
			name = name.replace("/null/", "/" + getSipAppCtx().getName() + "/");
			injection.setJndiName(name);
		}
	}
	
	private String getSipResourceJndiName(String className)
    {
		// FIXME: the app name may have not been set yet...
		className = AnnotationParser.normalize(className);
		
    	if (className.equals(SipFactory.class.getName()))
        {
        	Log.info("Detect SipFactory Resource from annotation");
        	return Configuration.JNDI_SIP_PREFIX + getSipAppCtx().getName() + Configuration.JNDI_SIP_FACTORY_POSTFIX;
        } 
        else if (className.equals(SipSessionsUtil.class.getName()))
        {
        	Log.info("Detect SipSessionsUtil Resource from annotation");
        	return Configuration.JNDI_SIP_PREFIX + getSipAppCtx().getName() + Configuration.JNDI_SIP_SESSIONS_UTIL_POSTFIX;
        } 
        else if (className.equals(TimerService.class.getName()))
        {
        	Log.info("Detect TimerService Resource from annotation");
        	return Configuration.JNDI_SIP_PREFIX + getSipAppCtx().getName() + Configuration.JNDI_TIMER_SERVICE_POSTFIX;
        } else
        {
        	return null;
        }
    }
}
