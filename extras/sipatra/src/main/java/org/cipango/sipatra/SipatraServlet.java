// ========================================================================
// Copyright 2003-2010 the original author or authors.
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
package org.cipango.sipatra;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.LocalContextScope;
import org.jruby.javasupport.JavaEmbedUtils.EvalUnit;

/**
 * @author Dominique Broeglin <dominique.broeglin@gmail.com>
 */
public class SipatraServlet extends SipServlet 
{
	private ScriptingContainer _container;
	private ServletContext _servletContext;
  private ThreadLocal _localContainer = new ThreadLocal();
  String _appPath;
  
  public ScriptingContainer getContainer() {
    if (_localContainer.get() == null) {
  		ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
  		List<String> loadPaths = new ArrayList<String>();

      // TODO: handle RUBY LOAD PATH to allow non JRuby dev
  		loadPaths.add(_appPath);
  		
  		container.getProvider().setLoadPaths(loadPaths);
  		container.runScriptlet("ENV['SIPATRA_PATH'] = '" + _appPath.replaceAll("'", "\'") + "'");
  		container.runScriptlet(PathType.CLASSPATH, "sipatra.rb");
  		container.runScriptlet(PathType.ABSOLUTE, _appPath + "/application.rb");
  		
  		_localContainer.set(container);
    }
    return (ScriptingContainer)_localContainer.get();
  }

	/**
	 * Initialize the jrubyServlet.
	 * 
	 * @throws ServletException
	 *             if this method encountered difficulties
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

	  _appPath = getServletContext().getRealPath("/WEB-INF/sipatra");
		_servletContext = config.getServletContext();
	}

	@Override
	public void doRequest(SipServletRequest request) throws IOException
	{
	  invokeMethod(request, "do_request");
	}

	@Override
	public void doResponse(SipServletResponse response) throws IOException
	{
	  invokeMethod(response, "do_response");
	}	
	
	private void invokeMethod(SipServletMessage message, String methodName) {
	  ScriptingContainer container = getContainer();
	  
	  Object app = container.runScriptlet("Sipatra::Application::new");

		container.callMethod(app, "set_bindings", new Object[] { 
		  _servletContext,  
		  _servletContext.getAttribute(SipServlet.SIP_FACTORY), 
		  message.getSession(), 
		  message});
	  container.callMethod(app, methodName);
	}
}
