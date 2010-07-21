package org.vpac.grisu.control.serviceInterfaces;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebService;

import org.apache.commons.lang.StringUtils;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.annotations.EnableMTOM;
import org.codehaus.xfire.service.invoker.AbstractInvoker;
import org.ietf.jgss.GSSException;
import org.vpac.grisu.backend.model.ProxyCredential;
import org.vpac.grisu.backend.model.User;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.XFireServiceInterface;
import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
import org.vpac.grisu.control.exceptions.NoValidCredentialException;
import org.vpac.grisu.settings.ServerPropertiesManager;
import org.vpac.grisu.settings.ServiceTemplateManagement;

/**
 * This class implements a {@link ServiceInterface} to use for a web service.
 * The credential is written into the session so that it can be retrieved easily
 * when needed.
 * 
 * @author Markus Binsteiner
 * 
 */
@EnableMTOM
@WebService(endpointInterface = "org.vpac.grisu.control.ServiceInterface", targetNamespace = "http://grisu.vpac.org/grisu-ws")
public class WsServiceInterface extends AbstractServiceInterface implements
		XFireServiceInterface {

	private ProxyCredential credential = null;

	private static boolean triedToCopySetupFiles = false;
	private static String hostname = null;

	/**
	 * Gets the credential from memory or the session context if the one from
	 * memory is already expired or about to expire.
	 * 
	 * @return the credential
	 * @throws NoValidCredentialException
	 */
	@Override
	protected synchronized ProxyCredential getCredential()
			throws NoValidCredentialException {

		MessageContext context = AbstractInvoker.getContext();
		// MessageContext context = MessageContextHelper.getContext();

		if ((this.credential == null) || !this.credential.isValid()) {
			myLogger.debug("No valid credential in memory. Fetching it from session context...");
			this.credential = (ProxyCredential) (context.getSession()
					.get("credential"));
			if ((this.credential == null) || !this.credential.isValid()) {
				throw new NoValidCredentialException(
						"Could not get credential from session context.");
			}
			getUser().cleanCache();
		} else {
			// check whether min lifetime as configured in server config file is
			// reached
			try {
				long oldLifetime = this.credential.getGssCredential()
						.getRemainingLifetime();
				if (oldLifetime < ServerPropertiesManager
						.getMinProxyLifetimeBeforeGettingNewProxy()) {
					myLogger.debug("Credential reached minimum lifetime. Getting new one from session. Old lifetime: "
							+ oldLifetime);
					this.credential = (ProxyCredential) (context.getSession()
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
			} catch (GSSException e) {
				myLogger.error("Could not read remaining lifetime from GSSCredential. Retrieving new one from session context.");
				if ((this.credential == null) || !this.credential.isValid()) {
					throw new NoValidCredentialException(
							"Could not get credential from session context.");
				}
				this.credential = (ProxyCredential) (context.getSession()
						.get("credential"));
				getUser().cleanCache();
			}
		}

		return this.credential;
	}

	public long getCredentialEndTime() {

		MessageContext context = AbstractInvoker.getContext();
		long endTime = (Long) (context.getSession().get("credentialEndTime"));
		return endTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.vpac.grisu.control.ServiceInterface#getTemplate(java.lang.String)
	 */
	public final String getTemplate(final String application)
			throws NoSuchTemplateException {
		String temp = ServiceTemplateManagement.getTemplate(application);

		if (StringUtils.isBlank(temp)) {
			throw new NoSuchTemplateException(
					"Could not find template for application: " + application
							+ ".");
		}
		return temp;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// * org.vpac.grisu.control.ServiceInterface#getTemplate(java.lang.String,
	// * java.lang.String)
	// */
	// private String getTemplate(String application, String version)
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

	@Override
	public String getInterfaceInfo(String key) {

		if ("HOSTNAME".equalsIgnoreCase(key)) {
			if (hostname == null) {
				try {
					InetAddress addr = InetAddress.getLocalHost();
					byte[] ipAddr = addr.getAddress();
					hostname = addr.getHostName();
				} catch (UnknownHostException e) {
					hostname = "Unavailable";
				}
			}
		} else if ("VERSION".equalsIgnoreCase(key)) {
			return ServiceInterface.INTERFACE_VERSION;
		} else if ("NAME".equalsIgnoreCase(key)) {
			return "Webservice (SOAP/xfire) interface running on: " + hostname;
		}
		return null;
	}

}
