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
