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

package org.cipango.diameter;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

import org.mortbay.util.LazyList;

/**
 * A list of AVPs. Used for diameter messages and AVP of Grouped type.
 *
 */
public class AVPList extends AbstractList<AVP<?>>
{
	private ArrayList<AVP<?>> _avps = new ArrayList<AVP<?>>();

	@SuppressWarnings("unchecked")
	public <T> AVP<T> get(Type<T> type)
	{
		for (AVP<?> avp : _avps)
		{
			if (avp.getType() == type)
				return (AVP<T>) avp;
		}
		return null;
	}
	
	public <T> T getValue(Type<T> type)
	{
		AVP<T> avp = get(type);
		if (avp == null)
			return null;
		return avp.getValue();
	}
	
	@Override
	public void add(int index, AVP<?> avp)
	{
		_avps.add(index, avp);
	}
	
	public <T> void add(Type<T> type, T value)
	{
		AVP<T> avp = new AVP<T>(type);
		avp.setValue(value);
		add(avp);
	}
	
	public AVP<?> get(int index)
	{
		return _avps.get(index);
	}

	public int size()
	{
		return _avps.size();
	}	
	
	
	@SuppressWarnings("unchecked")
	public <T> Iterator<AVP<T>> getAVPs(Type<T> type)
	{
		Object avps = null;
		for (int i = 0; i < _avps.size(); i++)
		{
			AVP avp = _avps.get(i);
			if (avp.getType() == type)
				avps = LazyList.add(avps, avp);
		}
		return LazyList.iterator(avps);
	}	

}
