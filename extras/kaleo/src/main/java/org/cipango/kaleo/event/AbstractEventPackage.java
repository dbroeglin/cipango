// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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

package org.cipango.kaleo.event;

import org.cipango.kaleo.AbstractResourceManager;

public abstract class AbstractEventPackage<T extends AbstractEventResource> extends AbstractResourceManager<T> implements EventPackage<T>
{
	private int _minExpires = 60;
	private int _maxExpires = 3600;
	private int _defaultExpires = 3600;
	
	public int getMinExpires()
	{
		return _minExpires; 
	}

	public int getMaxExpires()
	{
		return _maxExpires;
	}

	public int getDefaultExpires()
	{
		return _defaultExpires;
	}
	
	public void setMinExpires(int minExpires)
	{
		_minExpires = minExpires;
	}
	
	public void setMaxExpires(int maxExpires)
	{
		_maxExpires = maxExpires;
	}
	
	public void setDefaultExpires(int defaultExpires)
	{
		_defaultExpires = defaultExpires;
	}	
}
