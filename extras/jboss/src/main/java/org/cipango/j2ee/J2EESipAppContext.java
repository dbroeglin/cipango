// ========================================================================
// $Id: J2EEWebApplicationContext.java,v 1.6 2004/10/03 01:35:42 gregwilkins Exp $
// Copyright 2002-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.cipango.j2ee;

import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.SessionHandler;

import org.cipango.j2ee.session.Manager;
import org.cipango.sipapp.SipAppContext;



public class J2EESipAppContext extends SipAppContext
{

    // ----------------------------------------------------------------------------
    // DistributedHttpSession support
    // ----------------------------------------------------------------------------


    protected Manager _distributableSessionManager;

    // ----------------------------------------------------------------------------
    public J2EESipAppContext(SecurityHandler securityHandler,
            SessionHandler sessionHandler, ServletHandler servletHandler,
            ErrorHandler errorHandler)
    {
        super(securityHandler, sessionHandler, servletHandler, errorHandler);
    }


    // ----------------------------------------------------------------------------
    public void setDistributableSessionManager(Manager manager)
    {
        // _log.info("setDistributableSessionManager "+manager);
        _distributableSessionManager = (Manager) manager;
        _distributableSessionManager.setContext(this);
    }

    // ----------------------------------------------------------------------------
    public Manager getDistributableSessionManager()
    {
        return _distributableSessionManager;
    }

    // ----------------------------------------------------------------------------
    protected void doStart() throws Exception
    {
        // if (getStopGracefully() && !getStatsOn())
        // setStatsOn(true);

        super.doStart();
    }

    // ----------------------------------------------------------------------------
    public void doStop() throws Exception
    {
        super.doStop();
        _distributableSessionManager = null;
    }
}
