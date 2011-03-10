// ========================================================================
// Copyright 2010 NEXCOM Systems
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
package org.cipango.console.printer;

import java.io.IOException;
import java.io.Writer;

import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.cipango.console.printer.generic.HtmlPrinter;

public class OamPrinter implements HtmlPrinter
{
	private static final String[] SIMPLE_SIGNATURE = new String[] { "java.io.Writer" };
	private static final String[] COMPLEX_SIGNATURE = new String[] { "java.io.Writer", "javax.servlet.http.HttpServletRequest" };
	private static final String[] HANDLE = new String[] { "javax.servlet.http.HttpServletRequest" };
	
	private MBeanServerConnection _connection;
	private HttpServletRequest _request;
	private ObjectName _objectName;
	
	public OamPrinter(MBeanServerConnection connection, HttpServletRequest request, ObjectName objectName) throws  IOException, JMException
	{
		_connection = connection;
		_request = request;
		_objectName = objectName;

		MBeanInfo info = _connection.getMBeanInfo(_objectName);
		for (int i = 0; i < info.getOperations().length; i++)
		{
			MBeanOperationInfo operation = info.getOperations()[i];
			if ("handle".equals(operation.getName()))
			{
				MBeanParameterInfo[] signatures =  operation.getSignature();
				if (signatures.length == 1 && signatures[0].getType().equals(HANDLE[0]))
				{
					_connection.invoke(_objectName, "handle", new Object[] { _request }, HANDLE);
				}
			}
		}
	}
	
	public void print(Writer out) throws Exception
	{
		MBeanInfo info = _connection.getMBeanInfo(_objectName);
		boolean foundSimple = false;
		boolean foundComplex = false;
		for (int i = 0; i < info.getOperations().length; i++)
		{
			MBeanOperationInfo operation = info.getOperations()[i];
			if ("print".equals(operation.getName()))
			{
				MBeanParameterInfo[] signatures =  operation.getSignature();
				if (signatures.length == 1 && signatures[0].getType().equals(SIMPLE_SIGNATURE[0]))
					foundSimple = true;
				else if (signatures.length == 2 
						&& signatures[0].getType().equals(COMPLEX_SIGNATURE[0])
						&& signatures[1].getType().equals(COMPLEX_SIGNATURE[1]))
					foundComplex = true;	
			}
		}
		if (foundComplex)
			_connection.invoke(_objectName, "print", new Object[] { out, _request }, COMPLEX_SIGNATURE);
		else if (foundSimple)
			_connection.invoke(_objectName, "print", new Object[] { out }, SIMPLE_SIGNATURE);
		else
			throw new IllegalArgumentException("The object name " + _objectName + " does not expose " +
					"the method 'print(java.io.Writer)' or 'print(java.io.Writer, javax.servlet.http.HttpServletRequest)'");
	}
	
	/**
	 * These are the methods that can be exposed by JMX.
	 */
	public interface OamPage
	{		
		/**
		 * Returns the HTML title of the page.
		 */
		public String getTitle();
		
		/**
		 * Returns the title menu.
		 */
		public String getMenuTitle();
		
		/**
		 * Returns a subPages object names.
		 */
		public ObjectName[] getSubPages();

		/**
		 * Allows some preprocessing by the application before dispatching to JSP.
		 * @param request
		 */
		public void handle(HttpServletRequest request) throws Exception;
		
		public void print(Writer writer) throws Exception;
		
		public void print(Writer writer, HttpServletRequest request) throws Exception;

	}

}
