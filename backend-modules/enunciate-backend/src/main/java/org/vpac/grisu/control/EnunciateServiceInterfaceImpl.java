package org.vpac.grisu.control;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jws.WebService;
import javax.ws.rs.Path;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.vpac.grisu.backend.model.ProxyCredential;
import org.vpac.grisu.backend.model.User;
import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
import org.vpac.grisu.control.serviceInterfaces.AbstractServiceInterface;
import org.vpac.grisu.control.serviceInterfaces.LocalServiceInterface;
import org.vpac.grisu.settings.Environment;
import org.vpac.grisu.settings.ServiceTemplateManagement;

/**
 * This abstract class implements most of the methods of the
 * {@link ServiceInterface} interface. This way developers don't have to waste
 * time to implement the whole interface just for some things that are site/grid
 * specific. Currently there are two classes that extend this abstract class:
 * {@link LocalServiceInterface} and WsServiceInterface (which can be found in
 * the grisu-ws module).
 * 
 * The {@link LocalServiceInterface} is used to work with a small local database
 * like hsqldb so a user has got the whole grisu framework on his desktop. Of
 * course, all required ports have to be open from the desktop to the grid. On
 * the other hand no web service server is required.
 * 
 * The WsServiceInterface is the main one and it is used to set up a web service
 * somewhere. So light-weight clients can talk to it.
 * 
 * @author Markus Binsteiner
 * 
 */

@Path("/grisu")
@WebService(endpointInterface = "org.vpac.grisu.control.ServiceInterface")
@MTOM(enabled = true)
// @StreamingAttachment(parseEagerly = true, memoryThreshold = 40000L)
public class EnunciateServiceInterfaceImpl extends AbstractServiceInterface
		implements ServiceInterface {

	static {
		// System.out.println("INHERITABLETHREAD");
		SecurityContextHolder
				.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}

	static final Logger myLogger = Logger
			.getLogger(EnunciateServiceInterfaceImpl.class.getName());

	private String username;
	private char[] password;

	private static String hostname = null;

	@Override
	protected synchronized ProxyCredential getCredential() {

		final GrisuUserDetails gud = getSpringUserDetails();
		if (gud != null) {
			myLogger.debug("Found user: " + gud.getUsername());
			return gud.getProxyCredential();
		} else {
			myLogger.error("Couldn't find user...");
			return null;
		}

	}

	public long getCredentialEndTime() {

		return getSpringUserDetails().getCredentialEndTime();
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
			return "Webservice (REST/SOAP) interface";
		}
		return null;
	}

	private GrisuUserDetails getSpringUserDetails() {

		final SecurityContext securityContext = SecurityContextHolder
				.getContext();
		final Authentication authentication = securityContext
				.getAuthentication();

		if (authentication != null) {
			final Object principal = authentication.getPrincipal();
			if (principal instanceof GrisuUserDetails) {
				return (GrisuUserDetails) principal;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getTemplate(String name) throws NoSuchTemplateException {

		final File file = new File(
				Environment.getAvailableTemplatesDirectory(), name
						+ ".template");

		String temp;
		try {
			temp = FileUtils.readFileToString(file);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		return temp;

	}

	@Override
	protected User getUser() {

		final GrisuUserDetails gud = getSpringUserDetails();
		if (gud != null) {
			// myLogger.debug("Found user: "+gud.getUsername());
			return gud.getUser(this);
		} else {
			myLogger.error("Couldn't find user...");
			throw new RuntimeException("Can't get user for session.");
		}

	}

	public String[] listHostedApplicationTemplates() {
		return ServiceTemplateManagement.getAllAvailableApplications();
	}

	public void login(String username, String password) {

		this.username = username;
		this.password = password.toCharArray();

		getCredential();

	}

	public String logout() {

		myLogger.debug("Logging out user: " + getDN());

		new Thread() {
			@Override
			public void run() {
				myLogger.debug("Closing filesystems for user " + getDN()
						+ "...");
				getUser().closeFileSystems();
				myLogger.debug("Filesystems closed for user " + getDN());
			}
		}.start();

		// HttpServletRequest req = HTTPRequestContext.get().getRequest();
		// req.getSession().setAttribute("credential", null);

		return "Logged out.";

	}
}
