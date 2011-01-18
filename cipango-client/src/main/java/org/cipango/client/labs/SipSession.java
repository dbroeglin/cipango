/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.cipango.client.labs;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.SipSession.State;

import org.cipango.client.labs.interceptor.MessageInterceptor;

/**
 * Represents point-to-point SIP relationships. It roughly corresponds to a SIP dialog. In particular, for UAs it maintains (or is otherwise associated with) dialog state so as to be able to create subequent requests belonging to that dialog (using createRequest).
 * For UACs, SipSession extend the notion of SIP dialogs to have well-defined state before a dialog has been established and after a final non-2xx terminates an early dialog. This allows UACs to create "subsequent" requests without having an established dialog. The effect is that the subsequent request will have the same Call-ID, From and To headers (with the same From tag and without a To tag), the will exist in the same CSeq space.
 * All messages are potentially associated with a SipSession. The SipSession can be retrieved from the message by calling SipServletMessage.getSession().
 */
public interface SipSession
{
	
	public SipRequest waitRequest();
	
    /**
     * Returns a new request object. This method is used by user agents only.
     * Note that this method must not be used to create ACK or CANCEL requests. 
     * User agents create ACKs by calling SipServletResponse.createAck() and CANCELs are created by calling SipServletRequest.createCancel().
     * 
     * @throws IllegalArgumentException if method is not a syntactically valid SIP method or if it's "ACK" or "CANCEL" 
     * @throws IllegalStateException if this SipSession has been invalidated or if this SipSession is in the INITIAL state and there is an ongoing transaction or if this SipSession is in the TERMINATED state
     */
    SipRequest createRequest(java.lang.String method);

    /**
     * Returns the object bound with the specified name in this session, or null if no object is bound under the name.
     */
    java.lang.Object getAttribute(java.lang.String name);

    /**
     * Returns an Enumeration over the String objects containing the names of all the objects bound to this session.
     */
    Enumeration<java.lang.String> getAttributeNames();

    /**
     * Returns the Call-ID for this SipSession. This is the value of the Call-ID header for all messages belonging to this session.
     */
    java.lang.String getCallId();

    /**
     * Returns the time when this session was created, measured in milliseconds since midnight January 1, 1970 GMT.
     */
    long getCreationTime();

    /**
     * Returns a string containing the unique identifier assigned to this session. The identifier is assigned by the servlet container and is implementation dependent.
     */
    java.lang.String getId();
    
    /**
     * Returns true if the container will notify the application when this SipSession is in the ready-to-invalidate state. 
     * @return value of the invalidateWhenReady flag 
     * @throws IllegalStateException if this method is called on an invalidated session
     * @since 1.1
     */
    boolean getInvalidateWhenReady();
    
    /**
     * Specifies whether the container should notify the application when the SipSession 
     * is in the ready-to-invalidate state as defined above. 
     * The container notifies the application using the SipSessionListener.sessionReadyToInvalidate callback. 
     * @param invalidateWhenReady if true, the container will observe this session and 
     * notify the application when it is in the ready-to-invalidate state. 
     * The session is not observed if the flag is false. 
     * The default is true for v1.1 applications and false for v1.0 applications.
     * @throws IllegalStateException if this method is called on an invalidated session
     * @since 1.1
     */
    void setInvalidateWhenReady(boolean invalidateWhenReady);

    /**
     * Returns the last time the client sent a request associated with this session, as the number of milliseconds since midnight January 1, 1970 GMT. Actions that your application takes, such as getting or setting a value associated with the session, do not affect the access time.
     */
    long getLastAccessedTime();

    /**
     * Returns the Address identifying the local party. This is the value of the From header of locally initiated requests in this leg.
     */
    javax.servlet.sip.Address getLocalParty();


    /**
     * Returns the Address identifying the remote party. This is the value of the To header of locally initiated requests in this leg.
     */
    javax.servlet.sip.Address getRemoteParty();

    
    /**
     * Returns the current SIP dialog state, which is one of INITIAL, EARLY, CONFIRMED, or TERMINATED. These states are defined in RFC3261.
     */
    State getState();

    /**
     * Invalidates this session and unbinds any objects bound to it. A session cannot be invalidate if it is in the EARLY or CONFIRMED state, or if there exist ongoing transactions where a final response is expected. One exception is if this session has an associated unsupervised proxy, in which case the session can be invalidate even if transactions are ongoing.
     */
    void invalidate();

    /**
     * Returns true if this SipSession is valid, false otherwise. The SipSession can be invalidated by calling the method
     * on it. Also the SipSession can be invalidated by the container when either the associated
     * times out or
     * is invoked.
     */
    boolean isValid();

    /**
     * Returns true if this session is in a ready-to-invalidate state. A SipSession is in the ready-to-invalidate state under any of the following conditions:
     * 
     * 1. The SipSession transitions to the TERMINATED state.
     * 2. The SipSession transitions to the COMPLETED state when it is acting as a non-record-routing proxy.
     * 3. The SipSession acting as a UAC transitions from the EARLY state back to the INITIAL state on account of receiving a non-2xx final response and has not initiated any new requests (does not have any pending transactions).
     *  
     * @return if the session is in ready-to-invalidate state, false otherwise 
     * @throws IllegalStateException if this method is called on an invalidated session
     * @since 1.1
     */
    boolean isReadyToInvalidate();
    
    /**
     * Removes the object bound with the specified name from this session. If the session does not have an object bound with the specified name, this method does nothing.
     */
    void removeAttribute(java.lang.String name);

    /**
     * Binds an object to this session, using the name specified. If an object of the same name is already bound to the session, the object is replaced.
     */
    void setAttribute(java.lang.String name, java.lang.Object attribute);
    
    /**
     * Headers that will be added to subsequest requests and responses.
     */
    void setHeaders(Map<String,List<String>> headers);

    Map<String,List<String>> getHeaders();
    
    /**
     * Add headers set in session with {@link #setHeaders(List)} on the messsage.
     */
    public void addHeaders(SipMessage message);
    
    public void addMessageInterceptor(MessageInterceptor interceptor);

}
