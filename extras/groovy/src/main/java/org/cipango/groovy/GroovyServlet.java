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
package org.cipango.groovy;

import groovy.lang.Closure;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.codehaus.groovy.runtime.GroovyCategorySupport;

/**
 * This servlet will run Groovy scripts as SIP Groovlets.
 *
 *
 * This servlet use two scripts:
 * <ul>
 *   <li>One for handling <code>javax.servlet.sip.SipServletRequest</code>. If the 
 *   init parameter <code>request.script</code> is set, then the script is relative 
 *   to <code>/WEB-INF/groovy/</code>. Else the default location <code>/WEB-INF/groovy/requests.groovy</code>
 *   is used</li>
 *   <li>One for handling <code>javax.servlet.sip.SipServletResponse</code>. If the 
 *   init parameter <code>response.script</code> is set, then the script is relative 
 *   to <code>/WEB-INF/groovy/</code>. Else the default location <code>/WEB-INF/groovy/responses.groovy</code>
 *   is used</li>
 * </ul>
 *
 * <p>To make your SIP application more groovy, you must add the GroovyServlet
 * to your application's sip.xml configuration.  Here is the
 * sip.xml entry:
 *
 * <pre>
 *    &lt;servlet>
 *      &lt;servlet-name>Groovy&lt;/servlet-name>
 *      &lt;servlet-class>org.cipango.groovy.GroovyServlet&lt;/servlet-class>
 *      &lt;load-on-startup/>
 *    &lt;/servlet>
 * </pre>
 *
 * @see SipServletBinding
 * @see SipServletRequestBinding
 * @see SipServletResponseBinding
 */
public class GroovyServlet extends SipServlet implements ResourceConnector
{

	private static final String DEFAULT_REQUEST_SCRIPT = "requests.groovy";
	private static final String DEFAULT_RESPONSE_SCRIPT = "responses.groovy";

	private ServletContext _servletContext;

	/**
	 * Controls almost all log output.
	 */
	private boolean _verbose = false;
	
	private String _requestScript = DEFAULT_REQUEST_SCRIPT;
	private String _responseScript = DEFAULT_RESPONSE_SCRIPT;
	
	/**
	 * The script engine executing the Groovy scripts for this servlet
	 */
	private GroovyScriptEngine _gse;

	/**
	 * Initialize the GroovyServlet.
	 * 
	 * @throws ServletException
	 *             if this method encountered difficulties
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		_servletContext = config.getServletContext();

		// Get verbosity hint.
		String value = config.getInitParameter("verbose");
		if (value != null)
			_verbose = Boolean.valueOf(value).booleanValue();
		
		value = config.getInitParameter("request.script");
		if (value != null)
			_requestScript = value;
		
		try 
		{
			getResourceConnection(_requestScript);
		}
		catch (Exception e) 
		{
			throw new ServletException("Request script not found", e);
		}
		
		value = config.getInitParameter("response.script");
		if (value != null)
			_responseScript = value;
		
		try 
		{
			getResourceConnection(_responseScript);
		}
		catch (Exception e) 
		{
			_responseScript = null;
			if (!_responseScript.equals(DEFAULT_RESPONSE_SCRIPT))
				throw new ServletException("Response script defined and not found", e);
		}
		

		if (_verbose)
		{
			log("Request script: " + _requestScript);
			log("Response script: " + _responseScript);
		}

		// Set up the scripting engine
		_gse = createGroovyScriptEngine();

		_servletContext.log("Groovy servlet initialized on " + _gse + ".");
	}

	/**
	 * Handle web requests to the GroovyServlet
	 */
	@Override
	public void doRequest(SipServletRequest request) throws IOException
	{
		// Set up the script context
		SipServletBinding binding = new SipServletRequestBinding(request, _servletContext);

		// Run the script
		try
		{
			runScript(binding, _requestScript);
		}
		catch (RuntimeException runtimeException)
		{
			StringBuffer error = new StringBuffer("GroovyServlet Error: ");
			error.append(" script: '");
			error.append(_requestScript);
			error.append("': ");
			Throwable e = runtimeException.getCause();
			/*
			 * Null cause?!
			 */
			if (e == null)
			{
				error.append(" Script processing failed.");
				error.append(runtimeException.getMessage());
				if (runtimeException.getStackTrace().length > 0)
					error.append(runtimeException.getStackTrace()[0].toString());
				_servletContext.log(error.toString());
				System.err.println(error.toString());
				runtimeException.printStackTrace(System.err);
				throw runtimeException;
			}
			/*
			 * Resource not found.
			 */
			if (e instanceof ResourceException)
			{
				error.append(" Script not found, sending 404.");
				_servletContext.log(error.toString());
				System.err.println(error.toString());
				request.createResponse(SipServletResponse.SC_NOT_FOUND);
				return;
			}
			/*
			 * Other internal error. Perhaps syntax?!
			 */
			_servletContext.log("An error occurred processing the request", runtimeException);
			error.append(e.getMessage());
			if (e.getStackTrace().length > 0)
				error.append(e.getStackTrace()[0].toString());
			_servletContext.log(e.toString());
			System.err.println(e.toString());
			runtimeException.printStackTrace(System.err);
			throw runtimeException;
		}
	}
	
	@Override
	public void doResponse(SipServletResponse response) throws IOException
	{
		if (_responseScript == null)
			return;
		
		// Set up the script context
		SipServletBinding binding = new SipServletResponseBinding(response, _servletContext);

		// Run the script
		try
		{
			runScript(binding, _responseScript);
		}
		catch (RuntimeException runtimeException)
		{
			StringBuffer error = new StringBuffer("GroovyServlet Error: ");
			error.append(" script: '");
			error.append(_responseScript);
			error.append("': ");
			throw runtimeException;
		}
	}
	
	private void runScript(final SipServletBinding binding, final String script)
	{
		Closure closure = new Closure(_gse)
		{

			@Override
			public Object call()
			{
				try
				{
					return ((GroovyScriptEngine) getDelegate()).run(script, binding);
				}
				catch (ResourceException e)
				{
					throw new RuntimeException(e);
				}
				catch (ScriptException e)
				{
					throw new RuntimeException(e);
				}
			}

		};
		GroovyCategorySupport.use(ServletCategory.class, closure);	
	}
	
	/**
	 * Hook method to setup the GroovyScriptEngine to use.<br/>
	 * Subclasses may override this method to provide a custom engine.
	 */
	protected GroovyScriptEngine createGroovyScriptEngine()
	{
		return new GroovyScriptEngine(this);
	}
	
	/**
	 * Interface method for ResourceContainer. This is used by the
	 * GroovyScriptEngine.
	 */
	public URLConnection getResourceConnection(String name) throws ResourceException
	{
		String basePath = _servletContext.getRealPath("/");
		if (name.startsWith(basePath))
			name = name.substring(basePath.length());

		name = name.replaceAll("\\\\", "/");

		// remove the leading / as we are trying with a leading / now
		if (name.startsWith("/"))
			name = name.substring(1);

		/*
		 * Try to locate the resource and return an opened connection to it.
		 */
		try
		{
			String tryScriptName = "/" + name;
			URL url = _servletContext.getResource(tryScriptName);
			if (url == null)
			{
				tryScriptName = "/WEB-INF/groovy/" + name;
				url = _servletContext.getResource(tryScriptName);
			}
			if (url == null)
				throw new ResourceException("Resource \"" + name + "\" not found!");
			else
				url = new URL("file", "", _servletContext.getRealPath(tryScriptName));

			return url.openConnection();
		}
		catch (IOException e)
		{
			throw new ResourceException("Problems getting resource named \"" + name + "\"!", e);
		}
	}

	public boolean isVerbose()
	{
		return _verbose;
	}

	public void setVerbose(boolean verbose)
	{
		_verbose = verbose;
	}

	public String getRequestScript()
	{
		return _requestScript;
	}

	public void setRequestScript(String requestScript)
	{
		_requestScript = requestScript;
	}

	public String getResponseScript()
	{
		return _responseScript;
	}

	public void setResponseScript(String responseScript)
	{
		_responseScript = responseScript;
	}
}
