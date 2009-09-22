package org.vpac.grisu.control;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.xml.ws.soap.MTOM;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileTypeSelector;
import org.apache.log4j.Logger;
import org.codehaus.enunciate.modules.spring_app.HTTPRequestContext;
import org.codehaus.xfire.annotations.EnableMTOM;
import org.globus.myproxy.CredentialInfo;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.vpac.grisu.backend.hibernate.JobDAO;
import org.vpac.grisu.backend.hibernate.MultiPartJobDAO;
import org.vpac.grisu.backend.hibernate.UserDAO;
import org.vpac.grisu.backend.model.ProxyCredential;
import org.vpac.grisu.backend.model.RemoteFileTransferObject;
import org.vpac.grisu.backend.model.User;
import org.vpac.grisu.backend.model.job.Job;
import org.vpac.grisu.backend.model.job.JobSubmissionManager;
import org.vpac.grisu.backend.model.job.JobSubmitter;
import org.vpac.grisu.backend.model.job.MultiPartJob;
import org.vpac.grisu.backend.model.job.gt4.GT4DummySubmitter;
import org.vpac.grisu.backend.model.job.gt4.GT4Submitter;
import org.vpac.grisu.backend.utils.CertHelpers;
import org.vpac.grisu.backend.utils.FileContentDataSourceConnector;
import org.vpac.grisu.backend.utils.FileSystemStructureToXMLConverter;
import org.vpac.grisu.control.exceptions.JobPropertiesException;
import org.vpac.grisu.control.exceptions.JobSubmissionException;
import org.vpac.grisu.control.exceptions.MultiPartJobException;
import org.vpac.grisu.control.exceptions.NoSuchJobException;
import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
import org.vpac.grisu.control.exceptions.NoValidCredentialException;
import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
import org.vpac.grisu.control.info.CachedMdsInformationManager;
import org.vpac.grisu.control.serviceInterfaces.AbstractServiceInterface;
import org.vpac.grisu.control.serviceInterfaces.LocalServiceInterface;
import org.vpac.grisu.model.MountPoint;
import org.vpac.grisu.model.dto.DtoActionStatus;
import org.vpac.grisu.model.dto.DtoApplicationDetails;
import org.vpac.grisu.model.dto.DtoApplicationInfo;
import org.vpac.grisu.model.dto.DtoDataLocations;
import org.vpac.grisu.model.dto.DtoFile;
import org.vpac.grisu.model.dto.DtoFolder;
import org.vpac.grisu.model.dto.DtoGridResources;
import org.vpac.grisu.model.dto.DtoHostsInfo;
import org.vpac.grisu.model.dto.DtoJob;
import org.vpac.grisu.model.dto.DtoJobProperty;
import org.vpac.grisu.model.dto.DtoJobs;
import org.vpac.grisu.model.dto.DtoMountPoints;
import org.vpac.grisu.model.dto.DtoMultiPartJob;
import org.vpac.grisu.model.dto.DtoStringList;
import org.vpac.grisu.model.dto.DtoSubmissionLocations;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;
import org.vpac.grisu.settings.Environment;
import org.vpac.grisu.settings.MyProxyServerParams;
import org.vpac.grisu.settings.ServerPropertiesManager;
import org.vpac.grisu.settings.ServiceTemplateManagement;
import org.vpac.grisu.utils.SeveralXMLHelpers;
import org.vpac.security.light.voms.VO;
import org.vpac.security.light.voms.VOManagement.VOManagement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.org.arcs.grid.grisu.matchmaker.MatchMakerImpl;
import au.org.arcs.grid.sched.MatchMaker;
import au.org.arcs.jcommons.constants.Constants;
import au.org.arcs.jcommons.constants.JobSubmissionProperty;
import au.org.arcs.jcommons.interfaces.GridResource;
import au.org.arcs.jcommons.interfaces.InformationManager;
import au.org.arcs.jcommons.utils.JsdlHelpers;
import au.org.arcs.jcommons.utils.SubmissionLocationHelpers;

import com.sun.xml.ws.developer.StreamingAttachment;


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
@WebService(endpointInterface = "org.vpac.grisu.control.EnunciateServiceInterface")
@MTOM(enabled = true)
@StreamingAttachment(parseEagerly = true, memoryThreshold = 40000L)
@EnableMTOM
public class EnunciateServiceInterfaceImpl extends AbstractServiceInterface implements EnunciateServiceInterface {

	static final Logger myLogger = Logger
			.getLogger(EnunciateServiceInterfaceImpl.class.getName());

	private InformationManager informationManager = CachedMdsInformationManager
			.getDefaultCachedMdsInformationManager(Environment
					.getGrisuDirectory().toString());


	private String username;
	private char[] password;
	
	/**
	 * This method has to be implemented by the endpoint specific
	 * ServiceInterface. Since there are a few different ways to get a proxy
	 * credential (myproxy, just use the one in /tmp/x509..., shibb,...) this
	 * needs to be implemented differently for every single situation.
	 * 
	 * @return the proxy credential that is used to contact the grid
	 */
	protected synchronized ProxyCredential getCredential() {
		
		
		if ( this.credential == null || ! this.credential.isValid() ) {
			myLogger.debug("No valid credential in memory. Fetching it from session context...");
			this.credential = getCredentialJaxWs(); 
			if ( this.credential == null || ! this.credential.isValid() ) {
				throw new NoValidCredentialException("Could not get credential from session context.");
			}
			getUser().cleanCache();
		} else {
			// check whether min lifetime as configured in server config file is reached
			try {
				long oldLifetime = this.credential.getGssCredential().getRemainingLifetime();
				if ( oldLifetime < ServerPropertiesManager.getMinProxyLifetimeBeforeGettingNewProxy() ) {
					myLogger.debug("Credential reached minimum lifetime. Getting new one from session. Old lifetime: "+oldLifetime);
					this.credential = getCredentialJaxWs();
					if ( this.credential == null || ! this.credential.isValid() ) {
						throw new NoValidCredentialException("Could not get credential from session context.");
					}
					getUser().cleanCache();
					myLogger.debug("Success. New lifetime: "+this.credential.getGssCredential().getRemainingLifetime());
				}
			} catch (GSSException e) {
				myLogger.error("Could not read remaining lifetime from GSSCredential. Retrieving new one from session context.");
				if ( this.credential == null || ! this.credential.isValid() ) {
					throw new NoValidCredentialException("Could not get credential from session context.");
				}
				this.credential = getCredentialJaxWs(); 
				getUser().cleanCache();
			}
			
		}
			
		return credential;
	}
	
	private ProxyCredential credential = null;
	
//	/**
//	 * Gets the credential from memory or the session context if the one from memory is already expired or about to expire.
//	 * 
//	 * @return the credential
//	 * @throws NoValidCredentialException
//	 */
//	protected synchronized ProxyCredential getCredentialXfire() throws NoValidCredentialException {
//
//		MessageContext context = AbstractInvoker.getContext();
//		
//		if ( this.credential == null || ! this.credential.isValid() ) {
//			myLogger.debug("No valid credential in memory. Fetching it from session context...");
//			this.credential = (ProxyCredential)(context.getSession().get("credential")); 
//			if ( this.credential == null || ! this.credential.isValid() ) {
//				throw new NoValidCredentialException("Could not get credential from session context.");
//			}
//			getUser().cleanCache();
//		} else
//			// check whether min lifetime as configured in server config file is reached
//			try {
//				long oldLifetime = this.credential.getGssCredential().getRemainingLifetime();
//				if ( oldLifetime < ServerPropertiesManager.getMinProxyLifetimeBeforeGettingNewProxy() ) {
//					myLogger.debug("Credential reached minimum lifetime. Getting new one from session. Old lifetime: "+oldLifetime);
//					this.credential = (ProxyCredential)(context.getSession().get("credential")); 
//					if ( this.credential == null || ! this.credential.isValid() ) {
//						throw new NoValidCredentialException("Could not get credential from session context.");
//					}
//					getUser().cleanCache();
//					myLogger.debug("Success. New lifetime: "+this.credential.getGssCredential().getRemainingLifetime());
//				}
//			} catch (GSSException e) {
//				myLogger.error("Could not read remaining lifetime from GSSCredential. Retrieving new one from session context.");
//				if ( this.credential == null || ! this.credential.isValid() ) {
//					throw new NoValidCredentialException("Could not get credential from session context.");
//				}
//				this.credential = (ProxyCredential)(context.getSession().get("credential")); 
//				getUser().cleanCache();
//			}
//		
//		return this.credential;
//	}
	
	protected ProxyCredential getCredentialJaxWs() {

		if ( username != null && password != null ) {
			
			ProxyCredential proxy = createProxyCredential(username, new String(password),
					MyProxyServerParams.getMyProxyServer(),
					MyProxyServerParams.getMyProxyPort(),
					ServerPropertiesManager.getMyProxyLifetime());
			
			return proxy;
			
		}
		
		
		HttpServletRequest req = null;
		req = HTTPRequestContext.get().getRequest();

		ProxyCredential sessionProxy = (ProxyCredential) (req.getSession()
				.getAttribute("credential"));

		if (sessionProxy != null && sessionProxy.isValid()) {

			myLogger.debug("Auth: Using old proxy!!");
			return sessionProxy;

		} else {

			myLogger.debug("Auth: No Proxy in session. Creating new one.");
			String auth_head = req.getHeader("authorization");

			if (auth_head != null && auth_head.startsWith("Basic")) {
				String usernpass = new String(
						org.apache.commons.codec.binary.Base64
								.decodeBase64((auth_head.substring(6)
										.getBytes())));
				String user = usernpass.substring(0, usernpass.indexOf(":"));
				String password = usernpass
						.substring(usernpass.indexOf(":") + 1);

				ProxyCredential proxy = createProxyCredential(user, password,
						MyProxyServerParams.getMyProxyServer(),
						MyProxyServerParams.getMyProxyPort(),
						ServerPropertiesManager.getMyProxyLifetime());

				boolean success = true;

				if (proxy == null || !proxy.isValid()) {
					success = false;
					myLogger.debug("Auth: authentication not successful!");
					return null;
				}

				req.getSession().setAttribute("credential", proxy);

				myLogger.debug("Auth: Authentication successful!");

				return proxy;
			} else {
				return null;
			}
		}

	}

	private ProxyCredential createProxyCredential(String username,
			String password, String myProxyServer, int port, int lifetime) {
		MyProxy myproxy = new MyProxy(myProxyServer, port);
		GSSCredential proxy = null;
		try {
			proxy = myproxy.get(username, password, lifetime);

			int remaining = proxy.getRemainingLifetime();

			if (remaining <= 0)
				throw new RuntimeException("Proxy not valid anymore.");

			return new ProxyCredential(proxy);
		} catch (Exception e) {
			e.printStackTrace();
			myLogger.error("Could not create myproxy credential: "
					+ e.getLocalizedMessage());
			return null;
		}

	}

	public long getCredentialEndTime() {

		MyProxy myproxy = new MyProxy(MyProxyServerParams.getMyProxyServer(),
				MyProxyServerParams.getMyProxyPort());
		CredentialInfo info = null;
		try {
			HttpServletRequest req = HTTPRequestContext.get().getRequest();
			String auth_head = req.getHeader("authorization");
			String usernpass = new String(
					org.apache.commons.codec.binary.Base64
							.decodeBase64((auth_head.substring(6).getBytes())));
			String user = usernpass.substring(0, usernpass.indexOf(":"));
			String password = usernpass.substring(usernpass.indexOf(":") + 1);
			info = myproxy.info(getCredential().getGssCredential(), user,
					password);
		} catch (MyProxyException e) {
			myLogger.error(e);
			return -1;
		}

		return info.getEndTime();
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


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
