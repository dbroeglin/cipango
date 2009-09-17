package org.cipango.kaleo.xcap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.cipango.kaleo.xcap.dao.FileXcapDao;
import org.cipango.kaleo.xcap.dao.XcapDao;
import org.cipango.kaleo.xcap.dao.XmlResource;
import org.cipango.kaleo.xcap.util.XcapUtil;
import org.w3c.dom.Attr;

public class XcapServiceTest extends TestCase {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(XcapServiceTest.class);

	private XcapService _xcapService;
	private File _xcapRoot;
	
	public XcapServiceTest() throws Exception
	{
		_xcapService = new XcapService();
		if (_xcapService.getDao() instanceof FileXcapDao)
		{
			FileXcapDao dao = (FileXcapDao) _xcapService.getDao();
			_xcapRoot = new File("target/test-data");
			_xcapRoot.mkdirs();
			dao.setBaseDir(_xcapRoot);
		}
		
		_xcapService.init();
		_xcapService.setRootName("/");
		
		setContent(VALID_PRES_RULES_URI_2);
		setContent(VALID_PRES_RULES_URI_1);
		
		
	}
		
	public void setUp() throws Exception 
	{
		
	}
	
	private void setContent(String xcapUri) throws Exception
	{
		
		XcapDao dao = _xcapService.getDao();
		XcapUri uri = new XcapUri(xcapUri, _xcapService.getRootName());
		if (dao instanceof FileXcapDao)
		{
			File sourceFile = new File("target/test-classes/xcap-root", uri.getDocumentSelector());
			InputStream is = new FileInputStream(sourceFile);
			File outputFile = new File(_xcapRoot, uri.getDocumentSelector().replace("@", "%40"));
			outputFile.getParentFile().mkdirs();
			FileOutputStream os = new FileOutputStream(outputFile);
			int read;
			byte[] buffer = new byte[1024];
			while ((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
			}
			os.close();
			is.close();
		}
		/*
		XMLResource resource = ((ExistXmlResource) dao.getDocument(uri, true)).getXmlResource();
		
		InputStream is = getClass().getResourceAsStream("/xcap-root/" + uri.getDocumentSelector());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read = 0;
		while ((read = is.read(buffer)) != -1)
			os.write(buffer, 0, read);
		resource.setContent(new String(os.toByteArray()));
		resource.getParentCollection().storeResource(resource);
		*/
	}
	
	public void testValid() throws Exception {
		XcapResourceImpl resource = _xcapService.getResource(VALID_PRES_RULES_URI, false, HEAD, null);
		XmlResource xmlResource = resource.getSelectedResource();

		log.info("Selected node:\n" + new String(xmlResource.getBytes()));
		assertEquals(EXPECTED_RESULT,  new String(xmlResource.getBytes()));
		assertEquals("thomas@cipango.org", resource.getXcapUri().getUser());
	}
	
	public void testValid2() throws Exception {
		XcapResourceImpl resource = _xcapService.getResource(VALID_PRES_RULES_URI_3, false, HEAD, null);
		XmlResource xmlResource = resource.getSelectedResource();
		log.info("Selected node:\n" + new String(xmlResource.getBytes()));
		assertEquals("allow",  xmlResource.getDom().getFirstChild().getNodeValue());
	}
	
	public void testGetAttribute() throws Exception {
		XcapResourceImpl resource = _xcapService.getResource(GET_ATTRIBUTE, false, HEAD, null);
		XmlResource xmlResource = resource.getSelectedResource();
		//log.info("Selected attribute:\n" + xmlResource.getContent());
		assertEquals("a", ((Attr) xmlResource.getDom()).getNodeValue());
	}
	
	/*public void testGetNamespaceBindings() throws Exception {
		XcapResourceImpl resource = _xcapService.getResource(GET_NAMESPACE_BINDINGS, false, HEAD, null);
		List nodes = resource.getNodes();
		log.info("Selected binding:" 
				+ "\n\t" + ((Node) nodes.get(0)).asXML()
				+ "\n\t" + ((Node) nodes.get(1)).asXML()
				+ "\n\t" + ((Node) nodes.get(2)).asXML());
		assertEquals("xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"", ((Node) nodes.get(0)).asXML());
		assertEquals("xmlns:cr=\"urn:ietf:params:xml:ns:common-policy\"", ((Node) nodes.get(1)).asXML());
		assertEquals("xmlns:pr=\"urn:ietf:params:xml:ns:pres-rules\"", ((Node) nodes.get(2)).asXML());
	}*/
	
	public void testMissingFirstSlash() throws Exception {
		try {
			_xcapService.getResource(VALID_PRES_RULES_URI.substring(1), false, HEAD, null);
			fail();
		} catch (XcapException e) {
			assertEquals(404, e.getStatusCode());
			log.debug(e.getMessage());
		}
	}
	
	public void testBadAuid() throws Exception {
		try {
			_xcapService.getResource("/unknown/users/nicolas@nexcom.fr/index/~~/conditions", false, HEAD, null);
			fail();
		} catch (XcapException e) {
			assertEquals(404, e.getStatusCode());
			log.debug(e.getMessage());
		}
	}
	
	public void testUnknownUser() throws Exception {
		try {
			_xcapService.getResource("/pres-rules/users/unknown@nexcom.fr/index/~~/conditions", false, HEAD, null);
			fail();
		} catch (XcapException e) {
			assertEquals(404, e.getStatusCode());
			log.debug(e.getMessage());
		}
	}
	

//	public void testDoubleTilde() throws Exception {
//		try {
//			_xcapService.getResource("/pres-rules/users/nicolas@nexcom.fr/index/~~/~~/conditions", false, HEAD, null);
//			fail();
//		} catch (XcapException e) {
//			assertEquals(500, e.getStatusCode());
//			log.debug(e.getMessage());
//		}
//	}
	
	
	public void testInsertDefaultNamespace() {
		String nodeSelector = XcapUtil.insertDefaultNamespace("/ruleset/rule[@id=\"a\"]/conditions",
				"cp");
		assertEquals("/cp:ruleset/cp:rule[@id=\"a\"]/cp:conditions", nodeSelector);
		nodeSelector = XcapUtil.insertDefaultNamespace(nodeSelector,
				"cp");
		assertEquals("/cp:ruleset/cp:rule[@id=\"a\"]/cp:conditions", nodeSelector);

		nodeSelector = XcapUtil.insertDefaultNamespace("/ruleset/@id", "cp");
		assertEquals("/cp:ruleset/@id", nodeSelector);
	}
	
	public static final String VALID_PRES_RULES_URI_2 =
		"/pres-rules/users/nicolas@cipango.org/index/~~/ruleset/rule[@id='a']";
	
	public static final String VALID_PRES_RULES_URI_1 =
		"/pres-rules/users/thomas@cipango.org/index/~~/ruleset/rule%5b@id=%22a%22%5d/conditions";

	public static final String VALID_PRES_RULES_URI =
		"/pres-rules/users/thomas@cipango.org/index/~~/cr:ruleset/cr:rule%5b@id=%22a%22%5d/cr:conditions";

	public static final String VALID_PRES_RULES_URI_3 =
		"/pres-rules/users/thomas@cipango.org/index/~~/cr:ruleset/cr:rule%5b@id=%22a%22%5d/cr:actions/pr:sub-handling";

	public static final String GET_ATTRIBUTE =
		"/pres-rules/users/thomas@cipango.org/index/~~/cr:ruleset/cr:rule/@id";

	public static final String GET_NAMESPACE_BINDINGS =
		"/pres-rules/users/thomas@cipango.org/index/~~/cr:ruleset/namespace::*";

	public static final String GET_ALL_PRES_RULES_DOC =
		"/pres-rules/users/thomas@cipango.org/index";
	
	public static final String NEW_PRES_RULES_DOC =
		"/pres-rules/users/put/newDocument2";

	private static final String OS_SEPARATOR = System.getProperty("line.separator");
	
	public static final String EXPECTED_RESULT =
		"<cr:conditions xmlns:cr=\"urn:ietf:params:xml:ns:common-policy\">" + OS_SEPARATOR
		+ "    <cr:identity>" + OS_SEPARATOR
		+ "     <cr:one id=\"sip:user@cipango.org\"/>" + OS_SEPARATOR
		+ "    </cr:identity>" + OS_SEPARATOR
		+ "   </cr:conditions>";
	
	public static final String PRES_RULES_PROCESSOR_CLASS =
		"com.nexcom.sipapps.presence.rules.PresRulesProcessor";
	
	public static final String HEAD =
		"http://aloha:8080";
	
}
