// ========================================================================
// Copyright 2009 NEXCOM Systems
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.log.Log;

public class Router extends AbstractLifeCycle implements DiameterRouter
{

	private Map<String, Peer> _peers = new HashMap<String, Peer>();
	
	private Node _node;
	
	public void addPeer(Peer peer)
	{
		Peer[] olds = getPeers().toArray(new Peer[] {});
		synchronized (_peers)
		{
			_peers.put(peer.getHost(), peer);
			peer.setNode(_node);
		}
		if (_node != null && _node.getServer() != null)
			_node.getServer().getContainer().update(this, olds, getPeers().toArray(new Peer[] {}), "peers");
	}
	

	public void removePeer(Peer peer)
	{
		Peer[] olds = getPeers().toArray(new Peer[] {});
		synchronized (_peers)
		{
			_peers.remove(peer.getHost());
		}
		if (_node != null && _node.getServer() != null)
			_node.getServer().getContainer().update(this, olds, getPeers().toArray(new Peer[] {}), "peers");

	}
	
	public List<Peer> getPeers()
	{
		synchronized (_peers)
		{
			return new ArrayList<Peer>(_peers.values());
		}
	}

	public Peer getPeer(String realm, String host)
	{
		synchronized (_peers)
		{
			return _peers.get(host);
		}
	}

	public Peer getRoute(DiameterRequest request)
	{
		return getPeer(request.getDestinationRealm(), request.getDestinationHost());
	}

	public Node getNode()
	{
		return _node;
	}

	public void setNode(Node node)
	{
		_node = node;
	}

	@Override
	protected void doStart() throws Exception
	{
		synchronized (_peers)
		{	
			Iterator<Peer> it = _peers.values().iterator();
			while (it.hasNext())
			{
				Peer peer = it.next();
				peer.start();
				Log.debug("Peer: {} started", peer);
			}
		}
	}

	@Override
	protected void doStop() throws Exception
	{
		synchronized (_peers)
		{	
			Iterator<Peer> it = _peers.values().iterator();
			while (it.hasNext())
			{
				Peer peer = it.next();
				peer.stop();
				Log.debug("Peer: {} stopped", peer);
			}
			// Wait at most 1 seconds for DPA reception
			try {
				boolean allClosed = false;
				int iter = 20;
				while (iter-- > 0 && allClosed)
				{
					allClosed = true;
					it = _peers.values().iterator();
					while (it.hasNext()) 
					{
						Peer peer = it.next();
						if (!peer.isClosed())
						{
							allClosed = false;
							Log.info("Wait 50ms for " + peer + " closing");
							Thread.sleep(50);
							break;
						}
					} 
				}
			} 
			catch (Exception e) {
				Log.ignore(e);
			}
		}
	}


}
