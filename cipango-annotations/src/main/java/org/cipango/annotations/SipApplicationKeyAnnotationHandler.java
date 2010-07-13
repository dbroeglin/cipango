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
import java.util.List;

import javax.servlet.sip.SipServletRequest;

import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.annotations.AnnotationParser.AnnotationHandler;
import org.eclipse.jetty.annotations.AnnotationParser.Value;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.log.Log;

public class SipApplicationKeyAnnotationHandler implements AnnotationHandler
{
	private SipAppContext _sac;

	public SipApplicationKeyAnnotationHandler(SipAppContext context)
	{
		_sac = context;
	}

	public void handleClass(String className, int version, int access, String signature, String superName,
			String[] interfaces, String annotation, List<Value> values)
	{
		Log.warn("@SipApplicationKey annotation not applicable to classes: " + className);
	}

	public void handleMethod(String className, String methodName, int access, String desc, String signature,
			String[] exceptions, String annotation, List<Value> values)
	{
		if (_sac.getSipApplicationKeyMethod() != null)
			throw new IllegalStateException("Found multiple SipApplicationKey annotations");

		org.objectweb.asm.Type[] args = org.objectweb.asm.Type.getArgumentTypes(desc);

		if ((access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0)
			throw new IllegalStateException(methodName + " must be static");

		if ((access & org.objectweb.asm.Opcodes.ACC_PUBLIC) == 0)
			throw new IllegalStateException(methodName + " must be public");

		if (args.length != 1)
			throw new IllegalStateException(methodName + " argument must have a single argument");

		if (!SipServletRequest.class.getName().equals(args[0].getClassName()))
			throw new IllegalStateException(methodName + " argument must be of type SipServletRequest");

		if (!String.class.getName().equals(org.objectweb.asm.Type.getReturnType(desc).getClassName()))
			throw new IllegalStateException(methodName + " must return a String");
		try
		{
			Class targetClass = Loader.loadClass(null, className);
			Method method = targetClass.getDeclaredMethod(methodName, new Class[] { SipServletRequest.class });

			_sac.setSipApplicationKeyMethod(method);
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e);
		}

	}

	public void handleField(String className, String fieldName, int access, String fieldType,
			String signature, Object value, String annotation, List<Value> values)
	{
		Log.warn("@SipApplication annotation not applicable for fields: " + className + "." + fieldName);
	}

}
