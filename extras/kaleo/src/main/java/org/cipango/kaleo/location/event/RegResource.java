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

import java.util.List;

import org.cipango.kaleo.event.AbstractResource;
import org.cipango.kaleo.event.ResourceListener;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.event.Subscription;
import org.cipango.kaleo.event.Subscription.Reason;

public class RegResource extends AbstractResource
{
	private ReginfoDocument _content;
	
	public RegResource(String uri)
	{
		super(uri);
	}
	
	public void addListener(ResourceListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void addState(State state, int expires) {
		// TODO Auto-generated method stub
		
	}

	public void addSubscription(Subscription subscription, int expires) {
		// TODO Auto-generated method stub
		
	}

	public Content getContent() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<ResourceListener> getListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	public State getState(String etag) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Subscription> getSubscriptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public void modifyState(State state, int expires, String contentType,
			Object content) {
		// TODO Auto-generated method stub
		
	}

	public void refreshState(State state, int expires) {
		// TODO Auto-generated method stub
		
	}

	public void refreshSubscription(String id, int expires) {
		// TODO Auto-generated method stub
		
	}

	public void removeState(String etag) {
		// TODO Auto-generated method stub
		
	}

	public void removeSubscription(String id, Reason reason) {
		// TODO Auto-generated method stub
		
	}

}
