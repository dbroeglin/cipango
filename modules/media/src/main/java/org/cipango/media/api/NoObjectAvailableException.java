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
 * This exception is thrown when no more instance of managed object type
 * is available. It keeps a reference to the class of the {@link Managed}
 * type that could not be instantiated correctly. This object class can
 * be accessed using {@link #getObjectClass()}.
 * 
 * @author yohann
 */
public class NoObjectAvailableException extends Exception
{

	private static final long serialVersionUID = 1L;

	private Class<? extends Managed> _class;

	public NoObjectAvailableException(Class<? extends Managed> clazz)
	{
		_class = clazz;
	}

	public NoObjectAvailableException(Class<? extends Managed> clazz,
			String message, Throwable throwable)
	{
		super(message, throwable);
		_class = clazz;
	}

	public NoObjectAvailableException(Class<? extends Managed> clazz,
			String message)
	{
		super(message);
		_class = clazz;
	}

	public NoObjectAvailableException(Class<? extends Managed> clazz,
			Throwable throwable)
	{
		super(throwable);
		_class = clazz;
	}

	/**
	 * Retrieve the reference to the class that could not be instantiated.
	 * 
	 * @return the Class that could not be instantiated correctly.
	 */
	public Class<? extends Managed> getObjectClass()
	{
		return _class;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer(super.toString());
		buf.insert(0, "cannot instantiate " + _class.getName() + "\n");
		return buf.toString();
	}

}
