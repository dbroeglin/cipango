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
import org.jruby.javasupport.JavaEmbedUtils.EvalUnit;

/**
 * @author Dominique Broeglin <dominique.broeglin@gmail.com>
 */
public class SipatraServlet extends SipServlet 
{
	private ScriptingContainer _container;
	private ServletContext _servletContext;

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

		String appPath = getServletContext().getRealPath("/WEB-INF/sipatra");
		List<String> loadPaths = new ArrayList<String>();

		loadPaths.add(appPath);
		_container = new ScriptingContainer();
		_container.getProvider().setLoadPaths(loadPaths);
		_servletContext = config.getServletContext();

		// TODO: derive it from the servlets package name
		_container.runScriptlet(PathType.CLASSPATH, "/org/cipango/sipatra/base.rb");
		_container.runScriptlet(PathType.ABSOLUTE, appPath + "/application.rb");

	}

	private Object getSipatraApp() {
		return _container.runScriptlet("Sipatra::Application::new");
	}

	@Override
	public void doRequest(SipServletRequest request) throws IOException
	{
		_container.getVarMap().clear();
		Object app = getSipatraApp();

		setBindings(app, request);
		_container.callMethod(app, "do_request");
	}

	@Override
	public void doResponse(SipServletResponse response) throws IOException
	{
		Object app = getSipatraApp();

		_container.getVarMap().clear();
		setBindings(app, response);
		_container.callMethod(app, "do_response");
	}	

	private void setBindings(Object app, SipServletMessage message) {
		_container.callMethod(app, "set_bindings", new Object[] { 
		  _servletContext,  
		  _servletContext.getAttribute(SipServlet.SIP_FACTORY), 
		  message.getSession(), 
		  message});
	}
}
