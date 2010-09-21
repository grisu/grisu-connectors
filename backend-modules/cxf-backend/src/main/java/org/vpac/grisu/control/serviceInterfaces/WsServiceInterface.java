package org.vpac.grisu.control.serviceInterfaces;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.StringUtils;
import org.ietf.jgss.GSSException;
import org.vpac.grisu.backend.model.ProxyCredential;
import org.vpac.grisu.backend.model.User;
import org.vpac.grisu.control.CXFServiceInterface;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
import org.vpac.grisu.control.exceptions.NoValidCredentialException;
import org.vpac.grisu.settings.ServerPropertiesManager;
import org.vpac.grisu.settings.ServiceTemplateManagement;

@WebService(endpointInterface = "org.vpac.grisu.control.CXFServiceInterface", serviceName = "grisu")
public class WsServiceInterface extends AbstractServiceInterface implements
		CXFServiceInterface {

	private static boolean triedToCopySetupFiles = false;

	@Resource
	private WebServiceContext ctx;

	private ProxyCredential credential = null;

	private static String hostname = null;

	@Override
	protected synchronized ProxyCredential getCredential()
			throws NoValidCredentialException {
		final MessageContext context = ctx.getMessageContext();

		if ((this.credential == null) || !this.credential.isValid()) {
			myLogger.debug("No valid credential in memory. Fetching it from session context...");
			this.credential = (ProxyCredential) (context.get("credential"));
			if ((this.credential == null) || !this.credential.isValid()) {
				throw new NoValidCredentialException(
						"Could not get credential from session context.");
			}
			getUser().cleanCache();
		} else {
			// check whether min lifetime as configured in server config file is
			// reached
			try {
				final long oldLifetime = this.credential.getGssCredential()
						.getRemainingLifetime();
				if (oldLifetime < ServerPropertiesManager
						.getMinProxyLifetimeBeforeGettingNewProxy()) {
					myLogger.debug("Credential reached minimum lifetime. Getting new one from session. Old lifetime: "
							+ oldLifetime);
					this.credential = (ProxyCredential) (context
							.get("credential"));
					if ((this.credential == null) || !this.credential.isValid()) {
						throw new NoValidCredentialException(
								"Could not get credential from session context.");
					}
					getUser().cleanCache();
					myLogger.debug("Success. New lifetime: "
							+ this.credential.getGssCredential()
									.getRemainingLifetime());
				}
			} catch (final GSSException e) {
				myLogger.error("Could not read remaining lifetime from GSSCredential. Retrieving new one from session context.");
				if ((this.credential == null) || !this.credential.isValid()) {
					throw new NoValidCredentialException(
							"Could not get credential from session context.");
				}
				this.credential = (ProxyCredential) (context.get("credential"));
				getUser().cleanCache();
			}

		}
		return this.credential;
	}

	public long getCredentialEndTime() {

		final MessageContext context = ctx.getMessageContext();
		final long endTime = (Long) (context.get("credentialEndTime"));
		return endTime;
	}

	@Override
	public String getInterfaceInfo(String key) {

		if ("HOSTNAME".equalsIgnoreCase(key)) {
			if (hostname == null) {
				try {
					final InetAddress addr = InetAddress.getLocalHost();
					final byte[] ipAddr = addr.getAddress();
					hostname = addr.getHostName();
				} catch (final UnknownHostException e) {
					hostname = "Unavailable";
				}
			}
		} else if ("VERSION".equalsIgnoreCase(key)) {
			return ServiceInterface.INTERFACE_VERSION;
		} else if ("NAME".equalsIgnoreCase(key)) {
			return "Webservice (SOAP/cxf) interface running on: " + hostname;
		}
		return null;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// * org.vpac.grisu.control.ServiceInterface#getTemplate(java.lang.String,
	// * java.lang.String)
	// */
	// public String getTemplate(String application, String version)
	// throws NoSuchTemplateException {
	//
	// Document doc = ServiceTemplateManagement
	// .getAvailableTemplate(application);
	//
	// if (doc == null) {
	// throw new NoSuchTemplateException(
	// "Could not find template for application: " + application
	// + ", version " + version);
	// }
	//
	// return SeveralXMLHelpers.toString(doc);
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vpac.grisu.control.ServiceInterface#getTemplate(java.lang.String)
	 */
	public final String getTemplate(final String application)
			throws NoSuchTemplateException {
		final String temp = ServiceTemplateManagement.getTemplate(application);

		if (StringUtils.isBlank(temp)) {
			throw new NoSuchTemplateException(
					"Could not find template for application: " + application
							+ ".");
		}
		return temp;
	}

	@Override
	protected User getUser() {

		throw new RuntimeException("Needs to be implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.vpac.grisu.control.ServiceInterface#listHostedApplications()
	 */
	public String[] listHostedApplicationTemplates() {
		return ServiceTemplateManagement.getAllAvailableApplications();
	}

	// not needed here because username and password is already in the http
	// header
	public void login(String username, String password)
			throws NoValidCredentialException {

		// nothing to do here anymore because all the myproxy stuff is now
		// in the inhandler of the web service

	}

	// only destroys the session. maybe I should do more here?
	public String logout() {

		myLogger.debug("Exiting...");
		this.credential.destroy();
		return null;
	}
}