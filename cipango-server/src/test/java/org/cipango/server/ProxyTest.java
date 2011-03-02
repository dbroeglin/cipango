// ========================================================================
// Copyright 2007-2008 NEXCOM Systems
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
package org.cipango.server;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.sip.SipApplicationSessionListener;

import org.junit.Ignore;

@Ignore
public class ProxyTest 
{
	static class ServletHolder implements InvocationHandler
	{
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			// TODO Auto-generated method stub
			//System.out.println("Proxy: " + proxy);
			System.out.println("method " + method);
			return null;
		}
	}
	
	public static void main(String[] args) {
		ServletHolder holder = new ServletHolder();
		
		SipApplicationSessionListener listener = (SipApplicationSessionListener)
			Proxy.newProxyInstance(holder.getClass().getClassLoader(),
				new Class[] { SipApplicationSessionListener.class },
				holder);
		
		listener.sessionCreated(null);
	}
}
