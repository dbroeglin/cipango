package org.cipango.media.api;

/**
 * Something that can be open or closed.
 * 
 * @author yohann
 */
public interface Door
{

	/**
	 * Opens an internal resource, such as file, socket, etc.
	 */
	public void open();

	/**
	 * Closes an internal resource, such as file, socket, etc.
	 */
	public void close();

}
