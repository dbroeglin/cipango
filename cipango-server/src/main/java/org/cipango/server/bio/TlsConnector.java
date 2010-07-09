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
package org.cipango.server.bio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.cipango.server.SipConnectors;
import org.cipango.server.SipMessage;

import org.eclipse.jetty.http.security.Password;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;

public class TlsConnector extends TcpConnector
{
	
	public static final int DEFAULT_PORT = 5061;
	
	/** Default value for the keystore location path. */
	public static final String DEFAULT_KEYSTORE = System.getProperty("user.home") + File.separator
			+ ".keystore";
	/** String name of key password property. */
	public static final String KEYPASSWORD_PROPERTY = "jetty.ssl.keypassword";

	/** String name of keystore password property. */
	public static final String PASSWORD_PROPERTY = "jetty.ssl.password";

	/** Default value for the cipher Suites. */
	private String _excludeCipherSuites[] = null;

	/** Default value for the keystore location path. */
	private String _keystore = DEFAULT_KEYSTORE;
	private String _keystoreType = "JKS"; // type of the key store

	/** Set to true if we require client certificate authentication. */
	private boolean _needClientAuth = false;
	private transient Password _password;
	private transient Password _keyPassword;
	private transient Password _trustPassword;
	private String _provider;
	private String _secureRandomAlgorithm; // cert algorithm
	private String _sslKeyManagerFactoryAlgorithm = (Security.getProperty("ssl.KeyManagerFactory.algorithm") == null ? "SunX509"
			: Security.getProperty("ssl.KeyManagerFactory.algorithm")); // cert
	// algorithm
	private String _sslTrustManagerFactoryAlgorithm = (Security
			.getProperty("ssl.TrustManagerFactory.algorithm") == null ? "SunX509" : Security
			.getProperty("ssl.TrustManagerFactory.algorithm")); // cert
	// algorithm

	private String _truststore;
	private String _truststoreType = "JKS"; // type of the key store

	/** Set to true if we would like client certificate authentication. */
	private boolean _wantClientAuth = false;
	private int _handshakeTimeout = 0; // 0 means use maxIdleTime

	private boolean _allowRenegotiate = false;

	@Override
	public ServerSocket newServerSocket() throws IOException
	{
		SSLServerSocketFactory factory = null;
		SSLServerSocket socket = null;

		try
		{
			factory = createFactory();

			socket = (SSLServerSocket) (getHost() == null ? factory.createServerSocket(getPort(),
					getBacklogSize()) : factory.createServerSocket(getPort(), getBacklogSize(), InetAddress
					.getByName(getHost())));

			if (_wantClientAuth)
				socket.setWantClientAuth(_wantClientAuth);
			if (_needClientAuth)
				socket.setNeedClientAuth(_needClientAuth);

			if (_excludeCipherSuites != null && _excludeCipherSuites.length > 0)
			{
				List<String> excludedCSList = Arrays.asList(_excludeCipherSuites);
				String[] enabledCipherSuites = socket.getEnabledCipherSuites();
				List<String> enabledCSList = new ArrayList<String>(Arrays.asList(enabledCipherSuites));
				Iterator<String> exIter = excludedCSList.iterator();

				while (exIter.hasNext())
				{
					String cipherName = exIter.next();
					if (enabledCSList.contains(cipherName))
					{
						enabledCSList.remove(cipherName);
					}
				}
				enabledCipherSuites = (String[]) enabledCSList.toArray(new String[enabledCSList.size()]);

				socket.setEnabledCipherSuites(enabledCipherSuites);
			}

		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			Log.warn(e.toString());
			Log.debug(e);
			throw new IOException("!JsseListener: " + e);
		}
		return socket;

	}

	protected SSLServerSocketFactory createFactory() throws Exception
	{
		if (_truststore == null)
		{
			_truststore = _keystore;
			_truststoreType = _keystoreType;
		}

		KeyManager[] keyManagers = null;
		InputStream keystoreInputStream = null;
		if (_keystore != null)
			keystoreInputStream = Resource.newResource(_keystore).getInputStream();
		KeyStore keyStore = KeyStore.getInstance(_keystoreType);
		keyStore.load(keystoreInputStream, _password == null ? null : _password.toString().toCharArray());

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(_sslKeyManagerFactoryAlgorithm);
		keyManagerFactory.init(keyStore, _keyPassword == null ? null : _keyPassword.toString().toCharArray());
		keyManagers = keyManagerFactory.getKeyManagers();

		TrustManager[] trustManagers = null;
		InputStream truststoreInputStream = null;
		if (_truststore != null)
			truststoreInputStream = Resource.newResource(_truststore).getInputStream();
		KeyStore trustStore = KeyStore.getInstance(_truststoreType);
		trustStore.load(truststoreInputStream, _trustPassword == null ? null : _trustPassword.toString()
				.toCharArray());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(_sslTrustManagerFactoryAlgorithm);
		trustManagerFactory.init(trustStore);
		trustManagers = trustManagerFactory.getTrustManagers();

		SecureRandom secureRandom = _secureRandomAlgorithm == null ? null : SecureRandom
				.getInstance(_secureRandomAlgorithm);

		SSLContext context = _provider == null ? SSLContext.getInstance(SipConnectors.TLS) : SSLContext
				.getInstance(SipConnectors.TLS, _provider);

		context.init(keyManagers, trustManagers, secureRandom);

		return context.getServerSocketFactory();
	}

	@Override
	public void accept(int acceptorId) throws IOException, InterruptedException
	{
		try
		{
			Socket socket = getServerSocket().accept();

			TlsConnection connection = new TlsConnection((SSLSocket) socket);
			addConnection(socket.getInetAddress(), socket.getPort(), connection);
			connection.dispatch();
		}
		catch (SSLException e)
		{
			Log.warn(e);
			try
			{
				stop();
			}
			catch (Exception e2)
			{
				Log.warn(e2);
				throw new IllegalStateException(e2.getMessage());
			}
		}
	}

	@Override
	public void process(SipMessage message)
	{        
        TlsConnection tlsConnection = (TlsConnection) message.getConnection();
        SSLSocket sslSocket = tlsConnection.getSocket();
        
        try
        {
            SSLSession sslSession = sslSocket.getSession();
            X509Certificate[] certs = (X509Certificate[]) sslSession.getValue(X509Certificate.class.getName());
            if (certs == null)
            {
                certs = getCertChain(sslSession);
                if (certs == null)
                	certs = new X509Certificate[0];
                sslSession.putValue(X509Certificate.class.getName(), certs);
            }

            if (certs.length > 0)
                message.setAttribute("javax.servlet.request.X509Certificate", certs);
            else if (_needClientAuth) // Sanity check
                throw new IllegalStateException("no client auth");

        }
        catch (Exception e)
        {
            Log.warn(Log.EXCEPTION, e);
        }
        
    	super.process(message);

	}
	
    /**
     * Return the chain of X509 certificates used to negotiate the SSL Session.
     * <p>
     * Note: in order to do this we must convert a javax.security.cert.X509Certificate[], as used by
     * JSSE to a java.security.cert.X509Certificate[],as required by the Servlet specs.
     * 
     * @param sslSession the javax.net.ssl.SSLSession to use as the source of the cert chain.
     * @return the chain of java.security.cert.X509Certificates used to negotiate the SSL
     *         connection. <br>
     *         Will be null if the chain is missing or empty.
     */
    private static X509Certificate[] getCertChain(SSLSession sslSession)
    {
        try
        {
            javax.security.cert.X509Certificate javaxCerts[] = sslSession.getPeerCertificateChain();
            if (javaxCerts == null || javaxCerts.length == 0)
                return null;

            int length = javaxCerts.length;
            X509Certificate[] javaCerts = new X509Certificate[length];

            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
            for (int i = 0; i < length; i++)
            {
                byte bytes[] = javaxCerts[i].getEncoded();
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                javaCerts[i] = (X509Certificate) cf.generateCertificate(stream);
            }

            return javaCerts;
        }
        catch (SSLPeerUnverifiedException pue)
        {
            return null;
        }
        catch (Exception e)
        {
            Log.warn(Log.EXCEPTION, e);
            return null;
        }
    }

	@Override
	public boolean isSecure()
	{
		return true;
	}
	
	@Override
	public int getDefaultPort()
	{
		return DEFAULT_PORT;
	}
	
	@Override
	public int getTransportOrdinal() 
	{
		return SipConnectors.TLS_ORDINAL;
	}
	
	@Override
	protected TcpConnection newConnection(InetAddress addr, int port) throws IOException
	{
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(addr, port);
		return new TlsConnection(sslsocket);
	}
	
	/* ------------------------------------------------------------ */
	public String[] getExcludeCipherSuites()
	{
		return _excludeCipherSuites;
	}

	/* ------------------------------------------------------------ */
	public String getKeystore()
	{
		return _keystore;
	}

	/* ------------------------------------------------------------ */
	public String getKeystoreType()
	{
		return (_keystoreType);
	}

	/* ------------------------------------------------------------ */
	public boolean getNeedClientAuth()
	{
		return _needClientAuth;
	}

	/* ------------------------------------------------------------ */
	public String getProvider()
	{
		return _provider;
	}

	/* ------------------------------------------------------------ */
	public String getSecureRandomAlgorithm()
	{
		return _secureRandomAlgorithm;
	}

	/* ------------------------------------------------------------ */
	public String getSslKeyManagerFactoryAlgorithm()
	{
		return _sslKeyManagerFactoryAlgorithm;
	}

	/* ------------------------------------------------------------ */
	public String getSslTrustManagerFactoryAlgorithm()
	{
		return _sslTrustManagerFactoryAlgorithm;
	}

	/* ------------------------------------------------------------ */
	public String getTruststore()
	{
		return _truststore;
	}

	/* ------------------------------------------------------------ */
	public String getTruststoreType()
	{
		return _truststoreType;
	}

	/* ------------------------------------------------------------ */
	public boolean getWantClientAuth()
	{
		return _wantClientAuth;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @author Tony Jiang
	 */
	public void setExcludeCipherSuites(String[] cipherSuites)
	{
		this._excludeCipherSuites = cipherSuites;
	}

	/* ------------------------------------------------------------ */
	public void setKeyPassword(String password)
	{
		_keyPassword = Password.getPassword(KEYPASSWORD_PROPERTY, password, null);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param keystore
	 *            The resource path to the keystore, or null for built in
	 *            keystores.
	 */
	public void setKeystore(String keystore)
	{
		_keystore = keystore;
	}

	/* ------------------------------------------------------------ */
	public void setKeystoreType(String keystoreType)
	{
		_keystoreType = keystoreType;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Set the value of the needClientAuth property
	 * 
	 * @param needClientAuth
	 *            true iff we require client certificate authentication.
	 */
	public void setNeedClientAuth(boolean needClientAuth)
	{
		_needClientAuth = needClientAuth;
	}

	/* ------------------------------------------------------------ */
	public void setPassword(String password)
	{
		_password = Password.getPassword(PASSWORD_PROPERTY, password, null);
	}

	/* ------------------------------------------------------------ */
	public void setTrustPassword(String password)
	{
		_trustPassword = Password.getPassword(PASSWORD_PROPERTY, password, null);
	}

	/* ------------------------------------------------------------ */
	public void setProvider(String _provider)
	{
		this._provider = _provider;
	}

	/* ------------------------------------------------------------ */
	public void setSecureRandomAlgorithm(String algorithm)
	{
		this._secureRandomAlgorithm = algorithm;
	}

	/* ------------------------------------------------------------ */
	public void setSslKeyManagerFactoryAlgorithm(String algorithm)
	{
		this._sslKeyManagerFactoryAlgorithm = algorithm;
	}

	/* ------------------------------------------------------------ */
	public void setSslTrustManagerFactoryAlgorithm(String algorithm)
	{
		this._sslTrustManagerFactoryAlgorithm = algorithm;
	}

	public void setTruststore(String truststore)
	{
		_truststore = truststore;
	}

	public void setTruststoreType(String truststoreType)
	{
		_truststoreType = truststoreType;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Set the value of the _wantClientAuth property. This property is used when
	 * {@link #newServerSocket(SocketAddress, int) opening server sockets}.
	 * 
	 * @param wantClientAuth
	 *            true iff we want client certificate authentication.
	 * @see SSLServerSocket#setWantClientAuth
	 */
	public void setWantClientAuth(boolean wantClientAuth)
	{
		_wantClientAuth = wantClientAuth;
	}

	/**
	 * Set the time in milliseconds for so_timeout during ssl handshaking
	 * 
	 * @param msec
	 *            a non-zero value will be used to set so_timeout during ssl
	 *            handshakes. A zero value means the maxIdleTime is used
	 *            instead.
	 */
	public void setHandshakeTimeout(int msec)
	{
		_handshakeTimeout = msec;
	}

	public int getHandshakeTimeout()
	{
		return _handshakeTimeout;
	}
	
	public class TlsConnection extends TcpConnection
	{
		private SSLSocket _socket;

		public TlsConnection(SSLSocket socket) throws IOException
		{
			super(socket);
			_socket = socket;
		}
		
		public SSLSocket getSocket()
		{
			return _socket;
		}

		public void run()
		{
			try
			{
				int handshakeTimeout = getHandshakeTimeout();
				int oldTimeout = _socket.getSoTimeout();
				if (handshakeTimeout > 0)
					_socket.setSoTimeout(handshakeTimeout);

				_socket.addHandshakeCompletedListener(new HandshakeCompletedListener()
				{
					boolean handshook = false;

					public void handshakeCompleted(HandshakeCompletedEvent event)
					{
						if (handshook)
						{
							if (!_allowRenegotiate)
							{
								Log.warn("SSL renegotiate denied: " + _socket);
								try
								{
									_socket.close();
								}
								catch (IOException e)
								{
									Log.warn(e);
								}
							}
						}
						else
							handshook = true;
					}
				});
				_socket.startHandshake();

				if (handshakeTimeout > 0)
					_socket.setSoTimeout(oldTimeout);

				super.run();
			}
			catch (SSLException e)
			{
				Log.warn(e);
				try
				{
					close();
				}
				catch (IOException e2)
				{
					Log.ignore(e2);
				}
			}
			catch (IOException e)
			{
				Log.debug(e);
				try
				{
					close();
				}
				catch (IOException e2)
				{
					Log.ignore(e2);
				}
			}
		}
				
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append("TLS Connection ");
			sb.append(getLocalAddr()).append(":").append(getLocalPort());
			sb.append(" - ");
			sb.append(getRemoteAddr()).append(":").append(getRemotePort());
			return sb.toString();
		}

	}

}
