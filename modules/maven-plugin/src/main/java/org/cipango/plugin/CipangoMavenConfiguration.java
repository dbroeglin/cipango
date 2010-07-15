//========================================================================
//$Id: Jetty6MavenConfiguration.java 3766 2008-10-08 07:59:53Z janb $
//Copyright 2000-2005 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.cipango.plugin;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

import org.cipango.annotations.Configuration;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

public class CipangoMavenConfiguration extends Configuration 
{
    private List<File> classPathFiles;
    private boolean annotationsEnabled;
   
    public CipangoMavenConfiguration()
    {
        super();
    }

    public void setClassPathConfiguration(List<File> classPathFiles)
    {
        this.classPathFiles = classPathFiles;
    }
    
    
    /** Set up the classloader for the webapp, using the various parts of the Maven project
     * @see org.mortbay.jetty.webapp.Configuration#configureClassLoader()
     */
    public void configureClassLoader() throws Exception 
    {
        if (classPathFiles != null)
        {
            Log.debug("Setting up classpath ...");

            //put the classes dir and all dependencies into the classpath
            Iterator<File> itor = classPathFiles.iterator();
            while (itor.hasNext())
                ((WebAppClassLoader)getWebAppContext().getClassLoader()).addClassPath((itor.next()).getCanonicalPath());

            if (Log.isDebugEnabled())
                Log.debug("Classpath = "+LazyList.array2List(((URLClassLoader)getWebAppContext().getClassLoader()).getURLs()));
        }
        else
        {
            super.configureClassLoader();
        }

        // knock out environmental maven and plexus classes from webAppContext
        String[] existingServerClasses = getWebAppContext().getServerClasses();
        String[] newServerClasses = new String[2+(existingServerClasses==null?0:existingServerClasses.length)];
        newServerClasses[0] = "-org.apache.maven.";
        newServerClasses[1] = "-org.codehaus.plexus.";
        System.arraycopy( existingServerClasses, 0, newServerClasses, 2, existingServerClasses.length );
        
        getWebAppContext().setServerClasses( newServerClasses );
    }
    
	public boolean isAnnotationsEnabled()
	{
		return annotationsEnabled;
	}

	public void setAnnotationsEnabled(boolean annotationsEnabled)
	{
		this.annotationsEnabled = annotationsEnabled;
	}

	@Override
	public void parseAnnotations() throws Exception
	{
		if (annotationsEnabled)
			super.parseAnnotations();
		else
			Log.info("Annotations parsing is disabled");
	}

}
