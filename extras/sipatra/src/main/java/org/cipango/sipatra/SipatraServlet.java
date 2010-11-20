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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.cipango.sipatra.ruby.JRubyRuntimeFactory;
import org.cipango.sipatra.ruby.JRubyRuntimePool;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sipatra Main Servlet
 * 
 * @author Dominique Broeglin <dominique.broeglin@gmail.com>, Jean-Baptiste Morin
 */
public class SipatraServlet extends SipServlet 
{
	private static final Logger _log = LoggerFactory.getLogger(SipatraServlet.class);

	private static final String SIPATRA_PATH_PROPERTY 				      = "org.cipango.sipatra.script.path";
	private static final String SIPATRA_POOL_MAX_ACTIVE_PROPERTY 	  = "org.cipango.sipatra.pool.maxActive";
	private static final String SIPATRA_POOL_MAX_IDLE_PROPERTY 		  = "org.cipango.sipatra.pool.maxIdle";
	private static final String SIPATRA_POOL_MAX_WAIT_PROPERTY 		  = "org.cipango.sipatra.pool.maxWait";
	private static final String SIPATRA_POOL_MIN_IDLE_PROPERTY 		  = "org.cipango.sipatra.pool.minIdle";
	private static final String SIPATRA_POOL_MIN_EVICTABLE_PROPERTY	= "org.cipango.sipatra.pool.minEvictableIdleTimeMillis";
	private static final String SIPATRA_POOL_INIT_POOL_SIZE			    = "org.cipango.sipatra.pool.init.size";

	private ServletContext _servletContext;
	private JRubyRuntimePool _pool;

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

		_servletContext = config.getServletContext();

		String appPath = getServletContext().getRealPath("/WEB-INF/sipatra");
		String scriptPath = getStringProperty(SIPATRA_PATH_PROPERTY, null);

		if (scriptPath == null)
		{
			scriptPath = appPath + "/application.rb";
		}
		else
		{
			File file = new File(scriptPath);
			if(!file.exists())
				throw new ServletException(file.getAbsolutePath()+" does not exist!");

			if(file.isFile())
			{
				if(!file.getName().endsWith(".rb"))
					_log.warn(file.getAbsolutePath()+" is not a ruby file!");

				if(file.getParentFile() != null)
					appPath = file.getParentFile().getAbsolutePath();
				else
					_log.error(file.getAbsolutePath()+" got no parent directory!");
			}
			else if(file.isDirectory())
			{
				appPath = new File(scriptPath).getAbsolutePath();
			}
		}

		Config conf = new Config();

		conf.maxActive = getIntegerProperty(SIPATRA_POOL_MAX_ACTIVE_PROPERTY, -1);
		conf.maxIdle = getIntegerProperty(SIPATRA_POOL_MAX_IDLE_PROPERTY, -1);
		conf.maxWait = getIntegerProperty(SIPATRA_POOL_MAX_WAIT_PROPERTY, -1);
		conf.minIdle = getIntegerProperty(SIPATRA_POOL_MIN_IDLE_PROPERTY, -1);
		conf.minEvictableIdleTimeMillis = getIntegerProperty(SIPATRA_POOL_MIN_EVICTABLE_PROPERTY, -1);

		_log.info("Start pool with path: "+appPath+" ...");

		_pool = new JRubyRuntimePool(new JRubyRuntimeFactory(appPath, scriptPath), conf);

		int init_pool_size = getIntegerProperty(SIPATRA_POOL_INIT_POOL_SIZE, 0);
		for(int i = 0; i< init_pool_size; i++)
		{
			try 
			{
				_pool.addObject();
			} 
			catch (Exception e) 
			{
				_log.error("<<ERROR>>", e);
			}
		}
		_log.info("... pool started with "+init_pool_size+" JRuby Runtimes!");
	}

	private int getIntegerProperty(String name, int defaultValue)
	{
		String property = System.getProperty(name);
		if(property == null || "".equals(property))
			property = _servletContext.getInitParameter(name);
		if(property == null || "".equals(property))
			return defaultValue;
		else
		{
			try
			{
				return Integer.valueOf(property);
			}
			catch (Exception e) 
			{
				_log.warn("Property: "+name+" is not an int. Default value is used.");
				return defaultValue;
			}
		}
	}

	private String getStringProperty(String name, String defaultValue)
	{
		String property = System.getProperty(name);
		if(property == null || "".equals(property))
			property = _servletContext.getInitParameter(name);
		if(property == null || "".equals(property))
			return defaultValue;
		else
			return property;
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

	private void invokeMethod(SipServletMessage message, String methodName) 
	{
		ScriptingContainer container = null;
		try 
		{
			container = (ScriptingContainer) _pool.borrowObject();
			try 
			{
				Object app = container.runScriptlet("Sipatra::Application::new");

				container.callMethod(app, "set_bindings", new Object[] { 
						_servletContext,  
						_servletContext.getAttribute(SipServlet.SIP_FACTORY), 
						message.getSession(), 
						message});
				container.callMethod(app, methodName);
			} 
			catch(Exception e) 
			{
				_pool.invalidateObject(container);
				container = null;
			} 
			finally 
			{
				if(container != null) 
				{
					_pool.returnObject(container);
				}
			}
		} 
		catch(Exception e) 
		{
			_log.error("ERROR >> Failed to borrow a JRuby Runtime ", e);
			// TODO: failed to borrow a runtime... What should we do?
			//throw e?
		}
	}
}
