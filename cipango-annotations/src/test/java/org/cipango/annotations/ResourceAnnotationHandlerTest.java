package org.cipango.annotations;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.annotation.SipApplication;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;

import junit.framework.TestCase;

import org.cipango.annotations.resources.AnnotedServlet;
import org.cipango.servlet.SipServletHandler;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.cipango.sipapp.SipXmlProcessor;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.ResourcesAnnotationHandler;
import org.eclipse.jetty.plus.annotation.Injection;
import org.eclipse.jetty.plus.annotation.InjectionCollection;
import org.eclipse.jetty.plus.annotation.LifeCycleCallbackCollection;
import org.eclipse.jetty.plus.annotation.RunAsCollection;
import org.eclipse.jetty.util.LazyList;

public class ResourceAnnotationHandlerTest extends TestCase
{
	/*	
	public void testAnnotations() throws Exception
	{
		SipAppContext sac = new SipAppContext();
		
		ArrayList<String> classNames = new ArrayList<String>();
		classNames.add(AnnotedServlet.class.getName());
		AnnotationParser parser = new AnnotationParser();
        ResourcesAnnotationHandler handler = new ResourcesAnnotationHandler(wac);
        parser.registerAnnotationHandler("javax.annotation.Resources", handler);
        parser.parse(classNames, new ClassNameResolver()
        {
            public boolean isExcluded(String name)
            {
                return false;
            }

            public boolean shouldOverride(String name)
            {
                return false;
            }
        });
		
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
		
	}*/
	
	private SipAppContext _sac;
	private AnnotationParser _parser;
	private InjectionCollection _injections;
	private SipXmlProcessor _processor;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_sac = new SipAppContext();
		_sac.setServletHandler(new org.cipango.plus.servlet.SipServletHandler());
		_parser = new AnnotationParser();
		_injections = new InjectionCollection();
		_sac.setAttribute(InjectionCollection.INJECTION_COLLECTION, _injections);
		_parser.registerAnnotationHandler("javax.annotation.Resource", new ResourceAnnotationHandler (_sac));
		_processor = new SipXmlProcessor(_sac);
		_parser.registerAnnotationHandler("javax.servlet.sip.annotation.SipListener", new SipListenerAnnotationHandler(_sac, _processor));
	}
			
	public void testBadResource() throws Exception
	{
		_parser.parse(BadRessource.class.getName(), new SimpleResolver());
		assertNull(_injections.getInjections(BadRessource.class.getName()));
		
		_parser.parse(BadRessource2.class.getName(), new SimpleResolver());
		assertNull(_injections.getInjections(BadRessource2.class.getName()));
	}
	
	public void testSipFactory() throws Exception
	{
		_parser.parse(ListenerRessource.class.getName(), new SimpleResolver());
	
		List<Injection> injections = _injections.getInjections(ListenerRessource.class.getName());
		assertEquals(1, injections.size());
		assertEquals("sip/org.cipango.kaleo/SipFactory", injections.get(0).getJndiName());
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

@SipListener (applicationName="org.cipango.kaleo")
class ListenerRessource implements SipServletListener
{
	@Resource
	protected SipFactory sipFactory ;

	public void servletInitialized(SipServletContextEvent arg0)
	{
	}
}
