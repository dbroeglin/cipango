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
package org.cipango.sipatra;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.cipango.sipatra.properties.Properties;
import org.cipango.sipatra.properties.PropertyUtils;
import org.cipango.sipatra.ruby.JRubyRuntimeFactory;
import org.slf4j.Logger;

public class DefaultContextLoader implements ServletContextListener
{
	private static final Logger _log = org.slf4j.LoggerFactory.getLogger(DefaultContextLoader.class);

	public void contextInitialized(ServletContextEvent sce) 
	{
		ServletContext servletContext = sce.getServletContext();

		String appPath = servletContext.getRealPath("/WEB-INF/sipatra");
		String scriptPath = PropertyUtils.getStringProperty(Properties.SIPATRA_PATH_PROPERTY, null, servletContext);

		if (scriptPath == null)
		{
			scriptPath = appPath + "/application.rb";
		}
		else
		{
			File file = new File(scriptPath);
			if(!file.exists())
			{
				_log.error(file.getAbsolutePath()+" does not exist!");
				scriptPath = null;
			}

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

		conf.maxActive = PropertyUtils.getIntegerProperty(Properties.SIPATRA_POOL_MAX_ACTIVE_PROPERTY, -1, servletContext);
		conf.maxIdle = PropertyUtils.getIntegerProperty(Properties.SIPATRA_POOL_MAX_IDLE_PROPERTY, -1, servletContext);
		conf.maxWait = PropertyUtils.getIntegerProperty(Properties.SIPATRA_POOL_MAX_WAIT_PROPERTY, -1, servletContext);
		conf.minIdle = PropertyUtils.getIntegerProperty(Properties.SIPATRA_POOL_MIN_IDLE_PROPERTY, -1, servletContext);
		conf.minEvictableIdleTimeMillis = PropertyUtils.getLongProperty(Properties.SIPATRA_POOL_MIN_EVICTABLE_PROPERTY, 1000L*60L*30L, servletContext);
		conf.lifo = PropertyUtils.getBooleanProperty(Properties.SIPATRA_POOL_LIFO, false, servletContext);
		conf.numTestsPerEvictionRun = PropertyUtils.getIntegerProperty(Properties.SIPATRA_POOL_TEST_EVICTION_RUN, 3, servletContext);
		conf.softMinEvictableIdleTimeMillis = PropertyUtils.getLongProperty(Properties.SIPATRA_POOL_SOFT_MIN_EVICTABLE, -1L, servletContext);
		conf.testOnBorrow = PropertyUtils.getBooleanProperty(Properties.SIPATRA_POOL_TEST_BORROW, false, servletContext);
		conf.testOnReturn = PropertyUtils.getBooleanProperty(Properties.SIPATRA_POOL_TEST_RETURN, false, servletContext);
		conf.testWhileIdle = PropertyUtils.getBooleanProperty(Properties.SIPATRA_POOL_TEST_IDLE, false, servletContext);
		conf.timeBetweenEvictionRunsMillis = PropertyUtils.getLongProperty(Properties.SIPATRA_POOL_TIME_EVICTION, -1L, servletContext);
		
		GenericObjectPool pool = new GenericObjectPool(new JRubyRuntimeFactory(appPath, scriptPath), conf);
		startPool(pool, PropertyUtils.getIntegerProperty(Properties.SIPATRA_POOL_INIT_POOL_SIZE, 0, servletContext));
		servletContext.setAttribute(Attributes.POOL, pool);
	}
	
	public void contextDestroyed(ServletContextEvent sce) 
	{
		GenericObjectPool pool = (GenericObjectPool) sce.getServletContext().getAttribute(Attributes.POOL);
		stopPool(pool);
	}
	
	protected void startPool(GenericObjectPool pool, int init_pool_size) 
	{
		for(int i = 0; i< init_pool_size; i++)
		{
			try 
			{
				pool.addObject();
			} 
			catch (Exception e) 
			{
				_log.error("<<ERROR>>", e);
			}
		}
		_log.info("Pool started with "+init_pool_size+" JRuby Runtimes!");
	}

	protected void stopPool(GenericObjectPool pool) 
	{
		pool.clear();
		try 
		{
			pool.close();
		} 
		catch (Exception e) 
		{
			_log.error("ERROR >> Failed to close pool ", e);
		}
	}
}