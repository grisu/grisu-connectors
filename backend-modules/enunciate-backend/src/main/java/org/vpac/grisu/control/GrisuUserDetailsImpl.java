package org.vpac.grisu.control;

import org.apache.log4j.Logger;
import org.globus.common.CoGProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

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
