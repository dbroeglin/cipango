package org.cipango.kaleo.presence;

import java.util.LinkedList;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.cipango.kaleo.Constants;
import org.cipango.kaleo.event.ContentHandler;
import org.cipango.kaleo.event.EventPackage;
import org.cipango.kaleo.event.Resource;
import org.cipango.kaleo.event.Subscription;

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
	public void createNotification(Subscription subscription,
			int expires, Resource.Content content) 
	{
		Notification notification = new Notification(subscription, expires, content);
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
			SipSession session = notification.getSubscription().getSession();
			SipServletRequest notify = session.createRequest("NOTIFY");
			String type = notification.getContent().getType();
			ContentHandler handler = getContentHandler(type);
			byte[] b = handler.getBytes(notification.getContent().getValue());
			//Subscription-State: active;expires=543
			notify.addHeader(Constants.SUBSCRIPTION_STATE, notification.getSubscriptionStateHeader());
			notify.addHeader(Constants.EVENT, getName());
			notify.setContent(b, type);
			notify.send();
			__log.info("Sent: " + notify);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}

	class Notification 
	{
		private Subscription _subscription;

		private int _expires;

		private Resource.Content _content;

		public Notification(
				Subscription subscription,
				int expires,
				Resource.Content content) 
		{
			_expires = expires;
			_subscription = subscription;
			_content = content;
		}

		/**
		 * TODO: improve Subscription-State management
		 * @return
		 */
		public String getSubscriptionStateHeader() 
		{
			/*
			 * Very basic
			 */
			if(_expires != 0)
			{
				return Constants.ACTIVE+";"+"expires="+_expires;
			}
			else
			{
				return Constants.TERMINATED+";"+"reason=timeout";
			}
		}

		public Subscription getSubscription() 
		{
			return _subscription;
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
