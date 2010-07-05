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
package org.cipango.deploy.providers;

import java.io.File;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.deploy.util.FileID;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

public class SipAppProvider extends WebAppProvider
{
	
	private String _defaultsSipDescriptor;
	
	@Override
    public ContextHandler createContextHandler(final App app) throws Exception
    {
        Resource resource = Resource.newResource(app.getOriginId());
        File file = resource.getFile();
        if (!resource.exists())
            throw new IllegalStateException("App resouce does not exist "+resource);

        String context = file.getName();
        
        if (file.isDirectory())
        {
            // must be a directory
        }
        else if (FileID.isWebArchiveFile(file))
        {
            // Context Path is the same as the archive.
            context = context.substring(0,context.length() - 4);
        }
        else 
        {
            throw new IllegalStateException("unable to create ContextHandler for "+app);
        }
        
        // special case of archive (or dir) named "root" is / context
        if (context.equalsIgnoreCase("root") || context.equalsIgnoreCase("root/")) 
        {
            context = URIUtil.SLASH;
        }

        // Ensure "/" is Prepended to all context paths.
        if (context.charAt(0) != '/') 
        {
            context = "/" + context;
        }

        // Ensure "/" is Not Trailing in context paths.
        if (context.endsWith("/") && context.length() > 0) 
        {
            context = context.substring(0,context.length() - 1);
        }

        SipAppContext sac = new SipAppContext();
        sac.setContextPath(context);
        sac.setWar(file.getAbsolutePath());
        if (getDefaultsDescriptor() != null) 
            sac.setDefaultsDescriptor(getDefaultsDescriptor());
        if (getDefaultsSipDescriptor() != null) 
            sac.setDefaultsSipDescriptor(getDefaultsSipDescriptor());
        sac.setExtractWAR(isExtractWars());
        sac.setParentLoaderPriority(isParentLoaderPriority());
        if (getConfigurationClasses() != null) 
        {
            sac.setConfigurationClasses(getConfigurationClasses());
        }

        if (getTempDir() != null)
        {
            /* Since the Temp Dir is really a context base temp directory,
             * Lets set the Temp Directory in a way similar to how WebInfConfiguration does it,
             * instead of setting the
             * WebAppContext.setTempDirectory(File).  
             * If we used .setTempDirectory(File) all webapps will wind up in the
             * same temp / work directory, overwriting each others work.
             */
            sac.setAttribute(WebAppContext.BASETEMPDIR, getTempDir());
        }
        return sac; 
    }

	public String getDefaultsSipDescriptor()
	{
		return _defaultsSipDescriptor;
	}

	public void setDefaultsSipDescriptor(String defaultsSipDescriptor)
	{
		_defaultsSipDescriptor = defaultsSipDescriptor;
	}
}
