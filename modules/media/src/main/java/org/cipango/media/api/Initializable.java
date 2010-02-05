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

/**
 * Something that can be initialized.
 * 
 * This will generally correspond to class variable instantiation in objects.
 * 
 * @author yohann
 */
public interface Initializable
{

	/**
	 * Initialize this object.
	 * 
	 * In most classes of this API, this will instantiate objects referenced
	 * in class variables. Classes are instantiated providing parameters, but
	 * no internal object is created when constructors are invoked. Those
	 * internal objects creation is postponed up to this {@link #init()} method
	 * invocation.
	 */
	public void init();

}
