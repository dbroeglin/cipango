//========================================================================
//$Id: JBossWebAppContextMBean.java 1208 2006-11-13 21:38:44Z janb $
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.cipango.jboss.management;

import javax.management.ObjectName;

import org.cipango.jboss.JBossSipAppContext;
import org.mortbay.log.Log;
import org.mortbay.management.ObjectMBean;


/**
 * JBossWebApplicationContextMBean
 *
 * Provides special object name for itself so that 
 * we can integrate with jboss jsr77 management system.
 */
public class JBossSipAppContextMBean extends ObjectMBean
{
    private JBossSipAppContext _sipAppContext;
    
    public JBossSipAppContextMBean(Object managedObject)
    {
        super(managedObject);
        _sipAppContext = (JBossSipAppContext)managedObject;
    }
    
    public ObjectName getObjectName()
    {

        ObjectName oname = null;
        try
        {
            oname = new ObjectName(getMBeanContainer().getDomain()+":J2EEServer=none,J2EEApplication=none,J2EEWebModule="+_sipAppContext.getUniqueName());    
        }
        catch (Exception e)
        {
            Log.warn(e);
        }
        return oname;
    }

}
