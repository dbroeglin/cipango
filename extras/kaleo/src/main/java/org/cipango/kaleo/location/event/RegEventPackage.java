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

package org.cipango.kaleo.location.event;

import java.util.Collections;
import java.util.List;

import org.cipango.kaleo.event.AbstractEventPackage;
import org.cipango.kaleo.event.ContentHandler;

public class RegEventPackage extends AbstractEventPackage<RegResource>
{
	public static final String NAME = "reg";
	public static final String REGINFO = "application/reginfo+xml";
	
	private ReginfoHandler _handler = new ReginfoHandler();
	
	public ContentHandler<?> getContentHandler(String contentType) 
	{
		if (REGINFO.equals(contentType))
			return _handler;
		else
			return null;
	}

	public String getName() 
	{
		return NAME;
	}
	
	protected RegResource newResource(String uri)
	{
		return new RegResource(uri);
	}

	public List<String> getSupportedContentTypes() 
	{
		return Collections.singletonList(REGINFO);
	}
}
