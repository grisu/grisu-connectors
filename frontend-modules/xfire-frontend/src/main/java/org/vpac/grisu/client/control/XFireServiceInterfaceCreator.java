package org.vpac.grisu.client.control;

import java.net.URL;
import java.util.Arrays;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.TrustMaterial;
import org.apache.log4j.Logger;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;
import org.codehaus.xfire.transport.http.HttpTransport;
import org.vpac.grisu.client.control.xfire.ClientAuthenticationHandler;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.ServiceInterfaceCreator;
import org.vpac.grisu.control.exceptions.NoValidCredentialException;
import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
import org.vpac.grisu.settings.CaCertManager;
import org.vpac.grisu.settings.ClientPropertiesManager;

public class XFireServiceInterfaceCreator implements ServiceInterfaceCreator {

	static final Logger myLogger = Logger
			.getLogger(XFireServiceInterfaceCreator.class.getName());

	public static String DEFAULT_SERVICE_INTERFACE = "https://grisu.vpac.org/grisu-ws/services/grisu";

	public ServiceInterface create(String interfaceUrl, String username,
			char[] password, String myProxyServer, String myProxyPort,
			Object[] otherOptions) throws ServiceInterfaceException {

		String httpProxy = null;
		int httpProxyPort = -1;
		String httpProxyUsername = null;
		char[] httpProxyPassword = null;

		if (otherOptions == null && otherOptions.length == 4) {
			try {
				httpProxy = (String) otherOptions[0];
				httpProxyPort = (Integer) otherOptions[1];
				httpProxyUsername = (String) otherOptions[2];
				httpProxyPassword = (char[]) otherOptions[3];
			} catch (ClassCastException cce) {
				throw new ServiceInterfaceException(
						"Could not create serviceInterface: "
								+ cce.getLocalizedMessage(), cce);
			}
		}

		// Technique similar to
		// http://juliusdavies.ca/commons-ssl/TrustExample.java.html
		HttpSecureProtocol protocolSocketFactory;
		try {
			protocolSocketFactory = new HttpSecureProtocol();

			TrustMaterial trustMaterial = null;

			// "/thecertificate.cer" can be PEM or DER (raw ASN.1). Can even
			// be several PEM certificates in one file.

			String cacertFilename = System.getProperty("grisu.cacert");
			URL cacertURL = null;

			try {
				if (cacertFilename != null && !"".equals(cacertFilename)) {
					cacertURL = XFireServiceInterfaceCreator.class
							.getResource("/" + cacertFilename);
					if (cacertURL != null) {
						myLogger.debug("Using cacert " + cacertFilename
								+ " as configured in the -D option.");
					}
				}
			} catch (Exception e) {
				// doesn't matter
				myLogger
						.debug("Couldn't find specified cacert. Using default one.");
			}

			if (cacertURL == null) {

				cacertFilename = new CaCertManager()
						.getCaCertNameForServiceInterfaceUrl(interfaceUrl);
				if (cacertFilename != null && cacertFilename.length() > 0) {
					myLogger
							.debug("Found url in map. Trying to use this cacert file: "
									+ cacertFilename);
					cacertURL = XFireServiceInterfaceCreator.class
							.getResource("/" + cacertFilename);
					if (cacertURL == null) {
						myLogger
								.debug("Didn't find cacert. Using the default one.");
						// use the default one
						cacertURL = XFireServiceInterfaceCreator.class
								.getResource("/cacert.pem");
					} else {
						myLogger.debug("Found cacert. Using it. Good.");
					}
				} else {
					myLogger
							.debug("Didn't find any configuration for a special cacert. Using the default one.");
					// use the default one
					cacertURL = XFireServiceInterfaceCreator.class
							.getResource("/cacert.pem");
				}

			}

			trustMaterial = new TrustMaterial(cacertURL);

			// We can use setTrustMaterial() instead of addTrustMaterial()
			// if we want to remove
			// HttpSecureProtocol's default trust of TrustMaterial.CACERTS.
			protocolSocketFactory.addTrustMaterial(trustMaterial);

			// Maybe we want to turn off CN validation (not recommended!):
			protocolSocketFactory.setCheckHostname(false);

			Protocol protocol = new Protocol("https",
					(ProtocolSocketFactory) protocolSocketFactory, 443);
			Protocol.registerProtocol("https", protocol);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
//			e1.printStackTrace();
			throw new ServiceInterfaceException(
					"Unspecified error while trying to establish secure connection.", e1);
		}

		Service serviceModel = new ObjectServiceFactory().create(
				ServiceInterface.class, null, "http://grisu.vpac.org/grisu-ws",
				null);

		XFireProxyFactory serviceFactory = new XFireProxyFactory();

		ServiceInterface serviceInterface = null;
		try {
			serviceInterface = (ServiceInterface) serviceFactory.create(
					serviceModel, interfaceUrl);
			Client client = Client.getInstance(serviceInterface);

			// timeout
			Long timeout = ClientPropertiesManager.getConnectionTimeoutInMS();
			client.setProperty(CommonsHttpMessageSender.HTTP_TIMEOUT, timeout
					.toString());
			// enable file transfer for bigger files
			client.setProperty(HttpTransport.CHUNKING_ENABLED, "true");
			client.setProperty("mtom-enabled", "true");

			client.addOutHandler(new ClientAuthenticationHandler(username,
					new String(password), myProxyServer, myProxyPort));

			if (httpProxy != null && !"".equals(httpProxy) && httpProxyPort > 0) {
				client.setProperty(CommonsHttpMessageSender.HTTP_PROXY_HOST,
						httpProxy);
				client.setProperty(CommonsHttpMessageSender.HTTP_PROXY_PORT,
						new Integer(httpProxyPort).toString());
				if (httpProxyUsername != null && !"".equals(httpProxyUsername)
						&& httpProxyPassword != null) {
					client.setProperty(
							CommonsHttpMessageSender.HTTP_PROXY_USER,
							httpProxyUsername);
					client.setProperty(
							CommonsHttpMessageSender.HTTP_PROXY_PASS,
							new String(httpProxyPassword));
					Arrays.fill(httpProxyPassword, 'x');
				}
				client.setProperty(
						CommonsHttpMessageSender.DISABLE_PROXY_UTILS, true);
			}
		} catch (Exception e) {
//			e.printStackTrace();
			throw new ServiceInterfaceException(
					"Unspecified error while connecting to web service.", e);
		}

		try {
			serviceInterface.login(username, new String(password));
		} catch (NoValidCredentialException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			throw new ServiceInterfaceException(
					"Could not create & upload proxy to the myproxy server. Probably because of a wrong private key passphrase or network problems.",
					e);
		} catch (Exception e1) {
//			e1.printStackTrace();
			if (e1.getCause() != null) {
				if (e1.getCause().getCause() instanceof com.ctc.wstx.exc.WstxUnexpectedCharException) {
					throw new ServiceInterfaceException(
							"Remote web service seems to be down.", e1);
				} else if (e1.getCause().getCause() != null
						&& e1.getCause().getCause().getCause() != null
						&& e1.getCause().getCause().getCause() instanceof java.net.UnknownHostException) {
					throw new ServiceInterfaceException(
							"Can't connect to host: "
									+ e1.getCause().getCause().getCause()
											.getLocalizedMessage(), e1);
				} else if ( e1.getCause() instanceof XFireFault ) {
//					e1.getCause().printStackTrace();
					throw new ServiceInterfaceException("Probably because of a wrong password or the MyProxy credentials you want to use are expired/do not exist.", e1);
				}
			}
			throw new ServiceInterfaceException("Unspecified error while login to web service.", e1);
		}

		Arrays.fill(password, 'x');

		try {
			if ( ! serviceInterface.getInterfaceVersion().equals(ServiceInterface.INTERFACE_VERSION)) {
				throw new ServiceInterfaceException(
						"Remote Grisu service publishes interface version: "
								+ serviceInterface.getInterfaceVersion()
								+ ". This client only supports version "
								+ ServiceInterface.INTERFACE_VERSION
								+ ". Please download a new version of the Grisu client from http://grisu.arcs.org.au",
						null);
			}
		} catch (XFireRuntimeException xfre) {
			throw new ServiceInterfaceException(
					"Remote Grisu service doesn't publish interface version which means it's outdated."
							+ "Please tell your administrator to update it.",
					xfre);
		}

		return serviceInterface;

	}

	public boolean canHandleUrl(String url) {
		if (url != null) {
			return url.startsWith("http");
		} else {
			return false;
		}
	}

}
