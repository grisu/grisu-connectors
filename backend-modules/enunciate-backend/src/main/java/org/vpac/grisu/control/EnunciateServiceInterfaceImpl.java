package org.vpac.grisu.control;

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.xml.ws.soap.MTOM;

import org.apache.log4j.Logger;
import org.codehaus.enunciate.webapp.HTTPRequestContext;
import org.globus.common.CoGProperties;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.vpac.grisu.backend.model.ProxyCredential;
import org.vpac.grisu.backend.utils.LocalTemplatesHelper;
import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
import org.vpac.grisu.control.info.CachedMdsInformationManager;
import org.vpac.grisu.control.serviceInterfaces.AbstractServiceInterface;
import org.vpac.grisu.control.serviceInterfaces.LocalServiceInterface;
import org.vpac.grisu.settings.Environment;
import org.vpac.grisu.settings.ServiceTemplateManagement;
import org.vpac.grisu.utils.SeveralXMLHelpers;
import org.vpac.security.light.control.CertificateFiles;
import org.vpac.security.light.control.VomsesFiles;
import org.w3c.dom.Document;

import au.org.arcs.jcommons.interfaces.InformationManager;

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

	static final Logger myLogger = Logger
	.getLogger(EnunciateServiceInterfaceImpl.class.getName());

	private final InformationManager informationManager = CachedMdsInformationManager
	.getDefaultCachedMdsInformationManager(Environment
			.getGrisuDirectory().toString());

	private GrisuUserDetails cachedUserDetails = null;
	
	private String username;
	private char[] password;

	static {
		CoGProperties.getDefault().setProperty(
				CoGProperties.ENFORCE_SIGNING_POLICY, "false");
		
		System.out.println("ONE TIME INIT.");
		
		try {
			LocalTemplatesHelper.copyTemplatesAndMaybeGlobusFolder();
			VomsesFiles.copyVomses();
			CertificateFiles.copyCACerts();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			myLogger.error(e.getLocalizedMessage());
			// throw new
			// RuntimeException("Could not initiate local backend: "+e.getLocalizedMessage());
		}
		
	}

	public long getCredentialEndTime() {

		return getSpringUserDetails().getCredentialEndTime();
	}



	public String getTemplate(String application)
	throws NoSuchTemplateException {

		Document doc = ServiceTemplateManagement
		.getAvailableTemplate(application);

		String result;
		if (doc == null) {
			throw new NoSuchTemplateException(
					"Could not find template for application: " + application
					+ ".");
		} else {
			try {
				result = SeveralXMLHelpers.toString(doc);
			} catch (Exception e) {
				throw new NoSuchTemplateException(
						"Could not find valid xml template for application: "
						+ application + ".");
			}
		}

		return result;

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

		HttpServletRequest req = HTTPRequestContext.get().getRequest();
		req.getSession().setAttribute("credential", null);

		return "Logged out.";

	}


	private GrisuUserDetails getSpringUserDetails() {
		
		SecurityContext securityContext = SecurityContextHolder.getContext();
	    Authentication authentication = securityContext.getAuthentication();
	    if (authentication != null) {
	        Object principal = authentication.getPrincipal();
	        if ( principal instanceof GrisuUserDetails ) {
	        	return (GrisuUserDetails)principal;
	        } else {
	        	return null;
	        }
	    } else {
	    	return null;
	    }
	}

	@Override
	protected synchronized ProxyCredential getCredential() {
		
		if ( cachedUserDetails == null ) {
		
		GrisuUserDetails gud = getSpringUserDetails();
		if ( gud != null ) {
        	myLogger.debug("Found user: "+gud.getUsername());
        	cachedUserDetails = gud;
		} else {
	        myLogger.error("Couldn't find user...");
	        return null;
	    }
		
		} 
		return cachedUserDetails.getProxyCredential();

	}

}
