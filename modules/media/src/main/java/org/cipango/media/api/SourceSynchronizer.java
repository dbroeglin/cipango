// ========================================================================
// Copyright 2008-2010 NEXCOM Systems
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

package org.cipango.media.api;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Synchronizes objects from several sources.
 * 
 * This class maintains a list of sources. Each source has its own list of
 * objects. Thus, when an object is received from one source, a reference to
 * this object is stored in the corresponding list of objects. Identically,
 * when another source provides another object, probably from another thread,
 * this object is stored in another list of objects. The first object added
 * to a source using {@link #put(int, Object)} is considered as the
 * oldest object for this source. Then, when an object consumer has to retrieve
 * the list of oldest objects from each source at the same time, it invokes
 * {@link #getOldests()}. Once retrieved, those old objects are removed from
 * their corresponding source and the second object added using
 * {@link #put(int, Object)} becomes the oldest one, etc.
 * <p>
 * Thus, using this class, you can synchronize {@link RtpPacket}s, buffers,
 * etc. The user of this class has to maintain coherence amongst objects
 * synchronized by this SourceSynchronizer. No check is performed those
 * objects within this class. For example, if {@link RtpPacket}s are
 * synchronized, the similarity of each source codec is not verified here.
 * 
 * @author yohann
 */
class SourceSynchronizer implements Initializable, Managed
{

	private Hashtable<Integer, List<Object>> sources;

	@Override
	public void init()
	{
		sources = new Hashtable<Integer, List<Object>>();
	}

	/**
	 * Add a source. Each source is identified using a simple integer. If a
	 * source is added twice, the first source will be dropped. Thus, this
	 * method should be invoked only once for each source.
	 * 
	 * @param id the source identifier.
	 */
	public void addSource(int id)
	{
		List<Object> list = new ArrayList<Object>();
		sources.put(id, list);
	}

	/**
	 * Remove a source. Each source is identified using an integer. When a
	 * source is removed, all objects added using {@link #addSource(int)}
	 * with this source identifier are ignored.
	 * 
	 * @param id the identifier of the source to remove.
	 */
	public void removeSource(int id)
	{
		sources.remove(id);
	}

	/**
	 * Add an object to a source. Objects added do not have to be unique
	 * for each source. It means that the same object can be added several
	 * times using {@link #addSource(int)}.
	 * 
	 * @param sourceId the identifier of the source where the object is coming
	 * from.
	 * @param item the item that is coming from this source.
	 */
	public void put(int sourceId, Object item)
	{
		List<Object> source = sources.get(sourceId);
		source.add(item);
	}

	/**
	 * Retrieve the list of oldest object for each source, if available. The
	 * oldest object is the first object that has been added to a source using
	 * {@link #put(int, Object)}.
	 * 
	 * @return the list of oldest objects for each source. If no object is
	 * available returns an empty list.
	 */
	public List<Object> getOldests()
	{
		List<Object> oldests = new ArrayList<Object>();
		for (List<Object> source: sources.values())
		{
			if (!source.isEmpty())
			{
				Object object = source.get(0);
				oldests.add(object);
				source.remove(0);
			}
		}
		return oldests;
	}

}
