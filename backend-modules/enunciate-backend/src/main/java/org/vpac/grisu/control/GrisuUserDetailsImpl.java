package org.vpac.grisu.control;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.enunciate.webapp.HTTPRequestContext;
import org.globus.common.CoGProperties;
import org.globus.myproxy.MyProxy;
import org.ietf.jgss.GSSCredential;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.vpac.grisu.backend.model.ProxyCredential;
import org.vpac.grisu.settings.MyProxyServerParams;
import org.vpac.grisu.settings.ServerPropertiesManager;

public class GrisuUserDetailsImpl implements UserDetailsService {

	static final Logger myLogger = Logger.getLogger(GrisuUserDetailsImpl.class
			.getName());

	static {
		CoGProperties.getDefault().setProperty(
				CoGProperties.ENFORCE_SIGNING_POLICY, "false");
	}

	public UserDetails loadUserByUsername(String arg0)
			throws UsernameNotFoundException, DataAccessException {

		myLogger.debug("Authenticating....");
		return new GrisuUserDetails(arg0);

	}

}
