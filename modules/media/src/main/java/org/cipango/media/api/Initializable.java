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
