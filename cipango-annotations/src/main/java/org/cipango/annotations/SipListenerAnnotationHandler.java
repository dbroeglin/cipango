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

import java.util.Iterator;
import java.util.List;

import org.cipango.sipapp.SipAppContext;
import org.cipango.sipapp.SipXmlProcessor;
import org.eclipse.jetty.annotations.AnnotationParser.AnnotationHandler;
import org.eclipse.jetty.annotations.AnnotationParser.Value;
import org.eclipse.jetty.util.log.Log;

public class SipListenerAnnotationHandler implements AnnotationHandler
{
	private SipAppContext _sac;
	private SipXmlProcessor _processor;
	
	public SipListenerAnnotationHandler(SipAppContext context, SipXmlProcessor processor)
	{
		_processor = processor;
		_sac = context;
	}
	
	public void handleClass(String className, int version, int access, String signature, String superName,
			String[] interfaces, String annotation, List<Value> values)
	{
		
		Iterator<Value> it = values.iterator();
		while (it.hasNext())
		{
			Value value = it.next();
			if ("applicationName".equals(value.getName()))
			{
				if (_sac.getName() != null && !_sac.getName().equals(value.getValue()))
	    			throw new IllegalStateException("App-name in sip.xml: " + _sac.getName() 
	    					+ " does not match with SipApplication annotation: " + value.getValue());
				_sac.setName((String) value.getValue());
			}
		}
		_processor.addListenerClass(className);
	}

	public void handleMethod(String className, String methodName, int access, String desc, String signature,
			String[] exceptions, String annotation, List<Value> values)
	{
		Log.warn ("@SipListener annotation ignored on method: "+className+"."+methodName+" "+signature);
	}

	public void handleField(String className, String fieldName, int access, String fieldType,
			String signature, Object value, String annotation, List<Value> values)
	{
		Log.warn ("@SipListener annotation not applicable for fields: "+className+"."+fieldName);
	}

}
