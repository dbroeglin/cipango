package org.cipango.kaleo.presence;

import java.util.LinkedList;

import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.cipango.kaleo.Constants;
import org.cipango.kaleo.event.ContentHandler;
import org.cipango.kaleo.event.EventPackage;
import org.cipango.kaleo.event.Resource;
import org.cipango.kaleo.event.Subscription.State;

public abstract class NotificationsManager 
implements EventPackage<Presentity>
{
	private final static Logger __log = Logger.getLogger(NotificationsManager.class);

	private LinkedList<Notification> _notifications = new LinkedList<Notification>();

	/**
	 * Create and post notification
	 * @param subscription
	 * @param expires
	 */
	public void createNotification(SipSession session, Resource resource,
			int expires, Resource.Content content, State state, String reason)  
	{
		Notification notification = new Notification(session, resource, expires, content, state, reason);
		synchronized(_notifications) 
		{
			_notifications.addLast(notification);
			_notifications.notifyAll();
		}
	}

	class Notifier implements Runnable 
	{
		public void run() 
		{
			Thread.currentThread().setName("Notifier");
			Notification notification = null;
			while (true) 
			{
				try 
				{
					synchronized (_notifications) 
					{
						while (_notifications.isEmpty()) 
						{
							_notifications.wait();
						}
						notification = _notifications.removeFirst();
					}
					sendNotify(notification);
				} 
				catch (InterruptedException e) 
				{

				}
			}
		}
	}

	private void sendNotify(Notification notification) 
	{
		try 
		{
			SipSession session = notification.getSession();
			//FIXME
			if(session.isValid())
			{
				SipServletRequest notify = session.createRequest("NOTIFY");
				String type = notification.getContent().getType();
				ContentHandler handler = getContentHandler(type);
				byte[] b = handler.getBytes(notification.getContent().getValue());
				notify.addParameterableHeader(Constants.SUBSCRIPTION_STATE, notification.getSubscriptionStateHeader(), true);
				notify.addHeader(Constants.EVENT, getName());
				notify.setContent(b, type);
				notify.send();
			}
			else
			{
				//FIXME
				notification.getResource().eraseSubscription(notification.getSession().getId());
			}
		} 
		catch (Throwable e) 
		{
			__log.error("Error while sending notifies",e);
		}
	}

	class Notification 
	{
		private SipSession _session;

		private Resource _ressource;

		private int _expires;

		private Resource.Content _content;

		private String _reason;

		private State _state;

		public Notification(
				SipSession session,
				Resource resource,
				int expires,
				Resource.Content content,
				State state,
				String reason) 
		{
			_session = session;
			_ressource = resource;
			_expires = expires;
			_content = content;
			_reason = reason;
			_state = state;
		}

		/**
		 * TODO: improve Subscription-State management
		 * @return
		 * @throws ServletParseException 
		 */
		public Parameterable getSubscriptionStateHeader() throws ServletParseException 
		{
			/*
			 * Very basic
			 */
			SipFactory factory = (SipFactory) getSession().getServletContext().getAttribute(SipServlet.SIP_FACTORY);

			Parameterable header = factory.createParameterable(_state.getName());

			if(_state.equals(State.ACTIVE))
			{
				/*
   If the "Subscription-State" header value is "active", it means that
   the subscription has been accepted and (in general) has been
   authorized.  If the header also contains an "expires" parameter, the
   subscriber SHOULD take it as the authoritative subscription duration
   and adjust accordingly.  The "retry-after" and "reason" parameters
   have no semantics for "active".
				 */
				header.setParameter(Constants.EXPIRES, new Integer(_expires).toString());

			}
			else if (_state.equals(State.PENDING))
			{
				/*
   If the "Subscription-State" value is "pending", the subscription has
   been received by the notifier, but there is insufficient policy
   information to grant or deny the subscription yet.  If the header
   also contains an "expires" parameter, the subscriber SHOULD take it
   as the authoritative subscription duration and adjust accordingly.
   No further action is necessary on the part of the subscriber.  The
   "retry-after" and "reason" parameters have no semantics for
   "pending".
				 */
				header.setParameter(Constants.EXPIRES, new Integer(_expires).toString());
			}
			else if(_state.equals(State.TERMINATED))
			{
				/*
If the "Subscription-State" value is "terminated", the subscriber
   should consider the subscription terminated.  The "expires" parameter
   has no semantics for "terminated".  If a reason code is present, the
   client should behave as described below.  If no reason code or an
   unknown reason code is present, the client MAY attempt to re-
   subscribe at any time (unless a "retry-after" parameter is present,
   in which case the client SHOULD NOT attempt re-subscription until
   after the number of seconds specified by the "retry-after"
   parameter).  The defined reason codes are:

   deactivated: The subscription has been terminated, but the subscriber
      SHOULD retry immediately with a new subscription.  One primary use
      of such a status code is to allow migration of subscriptions
      between nodes.  The "retry-after" parameter has no semantics for
      "deactivated".

   probation: The subscription has been terminated, but the client
      SHOULD retry at some later time.  If a "retry-after" parameter is
      also present, the client SHOULD wait at least the number of
      seconds specified by that parameter before attempting to re-
      subscribe.

   rejected: The subscription has been terminated due to change in
      authorization policy.  Clients SHOULD NOT attempt to re-subscribe.
      The "retry-after" parameter has no semantics for "rejected".

   timeout: The subscription has been terminated because it was not
      refreshed before it expired.  Clients MAY re-subscribe
      immediately.  The "retry-after" parameter has no semantics for
      "timeout".

   giveup: The subscription has been terminated because the notifier
      could not obtain authorization in a timely fashion.  If a "retry-
      after" parameter is also present, the client SHOULD wait at least
      the number of seconds specified by that parameter before
      attempting to re-subscribe; otherwise, the client MAY retry
      immediately, but will likely get put back into pending state.

   noresource: The subscription has been terminated because the resource
      state which was being monitored no longer exists.  Clients SHOULD
      NOT attempt to re-subscribe.  The "retry-after" parameter has no
      semantics for "noresource".
				 */
				if(_reason != null)
					header.setParameter(Constants.REASON, _reason);

			}
			return header;
		}

		public SipSession getSession() {
			return _session;
		}

		public Resource getResource() {
			return _ressource;
		}

		public Resource.Content getContent() 
		{
			return _content;
		}

		public int getExpires()
		{
			return _expires;
		}
	}
}
