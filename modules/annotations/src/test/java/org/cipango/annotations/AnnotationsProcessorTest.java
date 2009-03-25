package org.cipango.annotations;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.annotation.SipApplication;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipServlet;

import junit.framework.TestCase;

import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.mortbay.jetty.annotations.AnnotationFinder;
import org.mortbay.jetty.annotations.ClassNameResolver;
import org.mortbay.jetty.plus.annotation.Injection;
import org.mortbay.jetty.plus.annotation.InjectionCollection;
import org.mortbay.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.mortbay.jetty.plus.annotation.RunAsCollection;
import org.mortbay.util.LazyList;

public class AnnotationsProcessorTest extends TestCase
{
	public void testAnnotations() throws Exception
	{
		AnnotationProcessor processor = getProcessor(AnnotedServlet.class, "org.cipango.kaleo", false);
		List<SipServletHolder> servlets = processor.getServlets();
		assertEquals(1, servlets.size());
		// If the name is not provided, the servlet name used will be the short 
		// name of the class annotated
		assertEquals("AnnotedServlet", servlets.get(0).getName());
		assertEquals(-1, servlets.get(0).getInitOrder());
		
		Object listeners = processor.getListenerClasses();
		assertEquals(1, LazyList.size(listeners));
		assertEquals(AnnotedServlet.class.getName(), LazyList.get(listeners, 0));
		//assertFalse(SipErrorListener.class.isAssignableFrom(listeners.get(0)));
		
		assertEquals("org.cipango.kaleo", processor.getAppName());
		
		SipApplication sipApp = processor.getSipApplication();
		assertNull(sipApp);
		
		assertNotNull(processor.getSipApplicationKeyMethod());
		
		List<Injection> injections = processor.getInjections().getFieldInjections(AnnotedServlet.class);
		assertEquals(3, injections.size());
		Iterator<Injection> it  = injections.iterator();
		while (it.hasNext())
		{
			Injection injection = it.next();
			String name = injection.getTarget().getName();
			if (name.equals("sipFactory"))
				assertEquals("sip/org.cipango.kaleo/SipFactory", injection.getJndiName());
			else if (name.equals("timerService"))
				assertEquals("sip/org.cipango.kaleo/TimerService", injection.getJndiName());
			else if (name.equals("sessionsUtil"))
				assertEquals("sip/org.cipango.kaleo/SipSessionsUtil", injection.getJndiName());
			else
				fail();
		}
	}
	
	public void testMissingAppName() throws Exception
	{
		try { getProcessor(MissingAppNameServlet.class, null, false); fail(); } catch (IllegalStateException e) {}
		
		// Use package info in servlet
		getProcessor(MissingAppNameServlet.class, "org.cipango.kaleo", false);
		
		getProcessor(MissingAppNameServlet.class, null, true);
		
		try { getProcessor(MissingAppNameServlet.class, "bad", false); fail(); } catch (IllegalStateException e) {}
	}
	
	public void testSipApplication() throws Exception
	{
		AnnotationProcessor processor = getProcessor(AnnotedServlet.class, null, true);
		SipApplication app = processor.getSipApplication();
		assertEquals("org.cipango.kaleo", app.name());
		assertEquals("Kaleo", app.displayName());
		assertEquals("main", app.mainServlet());
		
		// Default values
		assertEquals(180, app.proxyTimeout());
		assertEquals(3, app.sessionTimeout());
		assertFalse(app.distributable());
		
	}
	
	public void testApplicationKey() throws Exception
	{
		try { getProcessor(BadApplicationKey.class, null, true); fail(); } catch (IllegalStateException e) {}
		try { getProcessor(BadApplicationKey2.class, null, true); fail(); } catch (IllegalStateException e) {}		
		try { getProcessor(BadApplicationKey3.class, null, true); fail(); } catch (IllegalStateException e) {}		
		assertNotNull(getProcessor(AnnotedServlet.class, null, true).getSipApplicationKeyMethod());
	}
	
	public void testResource() throws Exception
	{
		try { getProcessor(BadRessource.class, null, true); fail(); } catch (IllegalStateException e) {}
		try { getProcessor(BadRessource2.class, null, true); fail(); } catch (IllegalStateException e) {}
	}
	
	protected AnnotationProcessor getProcessor(Class clazz, String appName, boolean includePackageInfo) throws Exception
	{
		AnnotationFinder finder = new AnnotationFinder();
		List<String> classes = new ArrayList<String>();
		classes.add(clazz.getName());
		if (includePackageInfo)
			classes.add("org.cipango.annotations.package-info");
		finder.find(classes, new ClassNameResolver()
		{

			public boolean isExcluded(String name)
			{
				return false;
			}

			public boolean shouldOverride(String name)
			{
				return true;
			}

		});
		AnnotationProcessor processor = new AnnotationProcessor(new SipAppContext(), appName, finder,
				new RunAsCollection(), new InjectionCollection(),
				new LifeCycleCallbackCollection(), new ArrayList<SipServletHolder>(), new ArrayList<Class<EventListener>>());
		processor.process();
		return processor;
	}
}

@SipServlet
class MissingAppNameServlet extends javax.servlet.sip.SipServlet 
{	
}

class BadApplicationKey
{
	@SipApplicationKey
	protected static String getSessionKey(SipServletRequest request)
	{
		return request.getCallId();
	}
}

class BadApplicationKey2
{
	@SipApplicationKey
	public static Object getSessionKey(SipServletRequest request)
	{
		return request.getCallId();
	}
}

class BadApplicationKey3
{
	@SipApplicationKey
	public static String getSessionKey(SipServletResponse response)
	{
		return response.getCallId();
	}
}

class BadRessource extends javax.servlet.sip.SipServlet
{
	@Resource
	protected static SipFactory sipFactory;
}

class BadRessource2 extends javax.servlet.sip.SipServlet
{
	@Resource
	protected final SipFactory sipFactory = null;
}
