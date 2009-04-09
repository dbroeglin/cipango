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

package org.cipango.kaleo.presence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cipango.kaleo.event.ContentHandler;
import org.cipango.kaleo.event.EventPackage;
import org.cipango.kaleo.presence.pidf.PidfHandler;

/**
 * Manage Presentities and Notifications
 *
 */
public class PresenceEventPackage 
implements EventPackage<Presentity>
{
	public static final String NAME = "presence";
	
	public static final String PIDF = "application/pidf+xml";

	public int _minExpires = 60;
	public int _maxExpires = 3600;
	public int _defaultExpires = 3600;

	private Map<String, Presentity> _presentities = new HashMap<String, Presentity>();
	
	private PidfHandler _pidfHandler = new PidfHandler();

	public String getName()
	{
		return NAME;
	}

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

	public List<String> getSupportedContentTypes()
	{
		return Collections.singletonList(PIDF);
	}

	public Presentity getResource(String uri) 
	{
		synchronized (_presentities)
		{
			Presentity presentity = _presentities.get(uri);
			if (presentity == null)
			{
				presentity = new Presentity(uri, this);
				_presentities.put(uri, presentity);
			}
			return presentity;
		}
	}

	public Iterator<String> getResourceUris()
	{
		synchronized (_presentities)
		{
			return _presentities.keySet().iterator();
		}
	}

	public ContentHandler<?> getContentHandler(String contentType)
	{
		if (PIDF.equals(contentType))
			return _pidfHandler;
		else
			return null;
	}
}
