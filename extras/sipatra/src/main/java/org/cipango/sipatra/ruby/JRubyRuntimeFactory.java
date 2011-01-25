// ========================================================================
// Copyright 2003-2011 the original author or authors.
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
package org.cipango.sipatra.ruby;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool.PoolableObjectFactory;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

/**
 * 
 *
 */
public class JRubyRuntimeFactory implements PoolableObjectFactory
{
	private String _appPath;
	private String _scriptPath;

	public JRubyRuntimeFactory(String appPath, String scriptPath) 
	{
		_appPath = appPath;
		_scriptPath = scriptPath;
	}

	public Object makeObject() 
	{
		ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
		// TODO: handle RUBY LOAD PATH to allow non JRuby dev
		List<String> loadPaths = new ArrayList<String>();
		loadPaths.add(_appPath);
		container.getProvider().setLoadPaths(loadPaths);
		container.runScriptlet("ENV['SIPATRA_PATH'] = '" + _appPath.replaceAll("'", "\'") + "'");
		container.runScriptlet(PathType.CLASSPATH, "sipatra.rb");

		if(_scriptPath != null)
		{
			File file = new File(_scriptPath);
			if(file.isFile())
				container.runScriptlet(PathType.ABSOLUTE, file.getAbsolutePath());
			else if(file.isDirectory())
			{
				for(File f : file.listFiles())
				{
					if(f.getName().endsWith(".rb"))
						container.runScriptlet(PathType.ABSOLUTE, f.getAbsolutePath());
				}
			}
		}
		return container;
	}

	public void destroyObject(Object obj) {}

	public boolean validateObject(Object obj) 
	{
		return true;
	}

	public void activateObject(Object obj) {}

	public void passivateObject(Object obj) 
	{
		if (obj instanceof ScriptingContainer) 
		{
			((ScriptingContainer) obj).clear();
		}
	}
}
