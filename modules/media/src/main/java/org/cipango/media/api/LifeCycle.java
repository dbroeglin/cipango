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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A LifeCycle is something that can be started and stopped.
 * 
 * Objects implementing this interface will probably trigger tasks on
 * background threads, either using {@link ExecutorService} or
 * {@link ScheduledExecutorService} referenced by {@link MediaFactory}.
 * 
 * @author yohann
 */
public interface LifeCycle
{

	/**
	 * Starts this LifeCycle.
	 */
	public void start();

	/**
	 * Stops this LifeCycle.
	 */
	public void stop();

}
