package org.cipango.annotations;

import javax.annotation.Resource;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipListener;

@javax.servlet.sip.annotation.SipServlet (applicationName="org.cipango.kaleo")
@SipListener (applicationName="org.cipango.kaleo")
public class AnnotedServlet extends SipServlet implements TimerListener
{
	@Resource
	protected SipFactory sipFactory;
	
	@Resource
	protected TimerService timerService;
	
	@Resource
	protected SipSessionsUtil sessionsUtil;
	
	public void timeout(ServletTimer arg0)
	{
		
	}
	
	@SipApplicationKey
	public static String getSessionKey(SipServletRequest request)
	{
		return request.getCallId();
	}

}
