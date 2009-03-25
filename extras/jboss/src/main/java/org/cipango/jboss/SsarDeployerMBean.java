package org.cipango.jboss;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;


public interface SsarDeployerMBean {
	/** The default object name */
	   public static final ObjectName OBJECT_NAME =
		   ObjectNameFactory.create(CipangoService.MBEAN_DOMAIN + ":type=SsarDeployer");
	   
}
