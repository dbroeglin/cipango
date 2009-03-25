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
package org.cipango.jonas;

import javax.naming.Context;
import javax.naming.NameNotFoundException;

import org.mortbay.jetty.plus.naming.NamingEntry;
import org.mortbay.jetty.plus.naming.NamingEntryUtil;
import org.mortbay.log.Log;

public class EnvConfiguration extends org.mortbay.jetty.plus.webapp.EnvConfiguration
{
	@Override
	public void deconfigureWebApp() throws Exception
	{
		//get rid of any bindings for comp/env for webapp
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getWebAppContext().getClassLoader());
        //compCtx.destroySubcontext("env");
        
        //unbind any NamingEntries that were configured in this webapp's name space
        try
        {
            Context scopeContext = NamingEntryUtil.getContextForScope(getWebAppContext());
            scopeContext.destroySubcontext(NamingEntry.__contextName);
        }
        catch (NameNotFoundException e)
        {
            Log.ignore(e);
            Log.debug("No naming entries configured in environment for webapp "+getWebAppContext());
        }
        Thread.currentThread().setContextClassLoader(oldLoader);
	}
	

}
