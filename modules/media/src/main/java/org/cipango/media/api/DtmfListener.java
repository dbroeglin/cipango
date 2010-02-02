package org.cipango.media.api;

/**
 * A DtmfListener is an object that needs to receive DTMF events.
 * 
 * DtmfListener implementations will be invoked when {@link DtmfFilter}
 * detect that a combination of DTMF RTP packets corresponds to one key
 * pressed event. To be invoked, a DtmfListener must be added to a DtmfFilter
 * using its {@link DtmfFilter#addDtmfListener(DtmfListener)} method.
 * 
 * @author yohann
 */
public interface DtmfListener
{

	/**
	 * Notifies this DtmfListener that a new DTMF has been detected.
	 * 
	 * @param c the character corresponding to the DTMF event.
	 */
	public void dtmfReceived(char c);

}
