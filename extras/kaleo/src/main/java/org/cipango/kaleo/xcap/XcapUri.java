package org.cipango.kaleo.xcap;

import javax.servlet.http.HttpServletResponse;

import org.cipango.kaleo.xcap.util.RequestUtil;

public class XcapUri
{
	public static final String NODE_SELECTOR_SEPARATOR = "/~~";
	public static final String USERS = "users";
	public static final String GLOBAL = "global";
	
	private String _nodeSelector;
	private String _documentSelector;
	private String _auid;
	private boolean _global;
	private String _user;
	private String _resourceId;
	
	public XcapUri(String auid, String user, String document, String nodeSelector) throws XcapException
	{
		_auid = auid;
		_user = user;
		_documentSelector = _auid + '/' + USERS + '/' + user + '/' + document;
		_nodeSelector = nodeSelector;
		_global = false;
	}
	
	public XcapUri(String requestUri, String rootName) throws XcapException
	{
		requestUri = getRequestUriWithoutRootName(RequestUtil.URLDecode(requestUri), rootName);

		int separator = requestUri.indexOf(NODE_SELECTOR_SEPARATOR);

		if (separator == -1)
			_documentSelector = requestUri;
		else
		{
			_documentSelector = requestUri.substring(0, separator);
			_nodeSelector = requestUri.substring(separator + NODE_SELECTOR_SEPARATOR.length());
		}
		
		if (_documentSelector.indexOf('/') == -1)
		{
			throw new XcapException("Request URI " + requestUri
					+ " does not contains a second '/'",
					HttpServletResponse.SC_NOT_FOUND);
		}
		
		String[] docParts = _documentSelector.split("/");
		_auid = docParts[0];
		_resourceId = _documentSelector.substring(_auid.length() + 1).replaceAll(":", "%3A").replaceAll("/", "%2F");

		if (GLOBAL.equals(docParts[1]))
			_global = true;
		else if (USERS.equals(docParts[1]))
		{
			if (docParts.length >= 3)
			{
				_user = docParts[2];
			}
			_global = false;
		}
		else
			throw new XcapException("Request URI " + requestUri
					+ " is not in subtree global or users",
					HttpServletResponse.SC_BAD_REQUEST);
		

	}
	
	private String getRequestUriWithoutRootName(String requestUri, String rootName)
	throws XcapException
	{
		if (!requestUri.startsWith(rootName))
		{
			throw new XcapException("Request URI " + requestUri
					+ " does not start with '" + rootName  + "'",
					HttpServletResponse.SC_NOT_FOUND);
		}
		return requestUri.substring(rootName.length());
	}
	
	public String getNodeSelector()
	{
		return _nodeSelector;
	}
	public void setNodeSelector(String nodeSelector)
	{
		_nodeSelector = nodeSelector;
	}
	public boolean hasNodeSeparator()
	{
		return _nodeSelector != null;
	}
	public String getDocumentSelector()
	{
		return _documentSelector;
	}
	public void setDocumentSelector(String documentSelector)
	{
		_documentSelector = documentSelector;
	}
	public String getAuid()
	{
		return _auid;
	}
	public void setAuid(String auid)
	{
		_auid = auid;
	}
	/**
	 * Returns <code>true</code> if the selected document is global.
	 * i.e. if the subtree name after the auid is <code>global</code>.
	 * @return <code>true</code> if the selected document is global.
	 */
	public boolean isGlobal()
	{
		return _global;
	}
	public void setGlobal(boolean global)
	{
		_global = global;
	}
	public String getUser()
	{
		return _user;
	}
	public void setUser(String user)
	{
		_user = user;
	}
	public String getResourceId()
	{
		return _resourceId;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(_documentSelector);
		if (hasNodeSeparator())
			sb.append(NODE_SELECTOR_SEPARATOR).append(_nodeSelector);
		
		return sb.toString();
	}
	
	
}
