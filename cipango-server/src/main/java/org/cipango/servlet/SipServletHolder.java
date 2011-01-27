// ========================================================================
// Copyright 2008-2010 NEXCOM Systems
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
package org.cipango.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;




/* --------------------------------------------------------------------- */
/** Servlet Instance and Context Holder.
 * Holds the name, params and some state of a javax.servlet.Servlet
 * instance. It implements the ServletConfig interface.
 * This class will organise the loading of the servlet when needed or
 * requested.
 *
 * 
 */
public class SipServletHolder extends ServletHolder
{
	
    private transient long _unavailable;
    private transient UnavailableException _unavailableEx;
	
    /* ---------------------------------------------------------------- */
    /** Constructor .
     */
    public SipServletHolder()
    {
    }
    
    
    /* ---------------------------------------------------------------- */
    /** Constructor for existing servlet.
     */
    public SipServletHolder(Servlet servlet)
    {
        super(servlet);
    }

    /* ---------------------------------------------------------------- */
    /** Constructor for existing servlet.
     */
    public SipServletHolder(Class<? extends Servlet> servlet)
    {
        super(servlet);
    }

    
    /** Get the servlet.
     * @return The servlet
     */
    @Override
    public synchronized Servlet getServlet()
        throws ServletException
    {
        // Handle previous unavailability
        if (_unavailable!=0)
        {
            if (_unavailable<0 || _unavailable>0 && System.currentTimeMillis()<_unavailable)
                throw _unavailableEx;
            _unavailable=0;
            _unavailableEx=null;
        }

        return super.getServlet();
    }
    
    @Override
    public boolean isAvailable()
    {
        if (isStarted()&& _unavailable==0)
            return super.isAvailable();
        try 
        {
            getServlet();
        }
        catch(Exception e)
        {
            Log.ignore(e);
        }

        return isStarted()&& _unavailable==0 && super.isAvailable();
    }
    
    /* ------------------------------------------------------------ */
    /** Service a request with this servlet.
     */
    public void handle(ServletRequest request,
                       ServletResponse response)
        throws ServletException,
               UnavailableException,
               IOException
    {
        if (_class==null)
            throw new UnavailableException("Servlet Not Initialized");
        
        Servlet servlet = getServletInstance();
        synchronized(this)
        {
            if (!isAvailable() || !isSetInitOrder())
                servlet=getServlet();
            if (servlet==null)
                throw new UnavailableException("Could not instantiate "+_class);
        }
        
        // Service the request
        boolean servlet_error=true;
        
        try
        {            
            servlet.service(request,response);
            servlet_error=false;
        }
        catch(UnavailableException e)
        {
            makeUnavailable(e);
            throw getUnavailableException();
        }
        finally
        {
            // Handle error params.
            if (servlet_error && request != null)
                request.setAttribute("javax.servlet.error.servlet_name",getName());
        }
    }
    
    private void makeUnavailable(UnavailableException e)
    {
        if (_unavailableEx==e && _unavailable!=0)
            return;

        _servletHandler.getServletContext().log("unavailable",e);

        _unavailableEx=e;
        _unavailable=-1;
        if (e.isPermanent())   
            _unavailable=-1;
        else
        {
            if (_unavailableEx.getUnavailableSeconds()>0)
                _unavailable=System.currentTimeMillis()+1000*_unavailableEx.getUnavailableSeconds();
            else
                _unavailable=System.currentTimeMillis()+5000; // TODO configure
        }
    }
    
    @Override
    public UnavailableException getUnavailableException()
    {
    	if (_unavailableEx != null)
    		return _unavailableEx;
    	return super.getUnavailableException();
    }
}





