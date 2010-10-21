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
package org.cipango.sipatra.ruby;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * 
 * 
 */
public class JRubyRuntimePool extends GenericObjectPool 
{
	public JRubyRuntimePool(JRubyRuntimeFactory objFactory,
			GenericObjectPool.Config config) 
	{
		super(objFactory, config);
	}


	public Object borrowObject() throws Exception 
	{
		return super.borrowObject();
	}


	public void returnObject(Object obj) throws Exception 
	{
		super.returnObject(obj);
	}
}
