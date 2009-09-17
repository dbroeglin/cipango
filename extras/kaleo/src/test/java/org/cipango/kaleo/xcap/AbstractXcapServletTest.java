package org.cipango.kaleo.xcap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.cipango.kaleo.web.XcapServlet;
import org.cipango.kaleo.xcap.dao.FileXcapDao;
import org.cipango.kaleo.xcap.dao.XcapDao;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.servlet.BasicServletTestCaseAdapter;

public abstract class AbstractXcapServletTest extends BasicServletTestCaseAdapter {

	private XcapServlet _xcapServlet;
	private File _xcapRoot;
	
	public AbstractXcapServletTest()
	{
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
		
		_xcapServlet = (XcapServlet) createServlet(XcapServlet.class);	
		if (_xcapServlet.getXcapService().getDao() instanceof FileXcapDao)
		{
			FileXcapDao dao = (FileXcapDao) _xcapServlet.getXcapService().getDao();
			_xcapRoot = new File("target/test-data");
			_xcapRoot.mkdirs();
			dao.setBaseDir(_xcapRoot);
		}

		response = getWebMockObjectFactory().getMockResponse();
		request = getWebMockObjectFactory().getMockRequest();

	}
	
	protected void setContent(String xcapUri) throws Exception
	{
		
		XcapDao dao = _xcapServlet.getXcapService().getDao();
		XcapUri uri = new XcapUri(xcapUri, _xcapServlet.getXcapService().getRootName());
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
	
	public void copyFile(String source, String destination) throws IOException {
		File sourceFile = new File(xcapRoot, source);
		InputStream is = new FileInputStream(sourceFile);
		File outputFile = new File(xcapRoot, destination);
		FileOutputStream os = new FileOutputStream(outputFile);
		int read;
		byte[] buffer = new byte[1024];
		while ((read = is.read(buffer)) != -1) {
			os.write(buffer, 0, read);
		}
		os.close();
		is.close();
	}
	
	public byte[] getResourceAsBytes(String resourceName) throws IOException {
		InputStream is = AbstractXcapServletTest.class.getResourceAsStream(resourceName);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read;
		while ((read = is.read(buffer)) != -1) {
			os.write(buffer, 0, read);
		}
		return os.toByteArray();
	}

	
	public void doPut() {
		request.setRequestURL(
				XcapServiceTest.HEAD + request.getRequestURI());
		super.doPut();
	}
	
	public void doGet() {
		request.setRequestURL(
				XcapServiceTest.HEAD + request.getRequestURI());
		super.doGet();
	}
	
	public void doDelete() {
		request.setRequestURL(
				XcapServiceTest.HEAD + request.getRequestURI());
		super.doDelete();
	}
	
	protected MockHttpServletResponse response;
	protected MockHttpServletRequest request;
	protected File xcapRoot;
}
