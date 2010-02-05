package org.vpac.grisu.control;

import org.apache.log4j.Logger;
import org.globus.myproxy.CredentialInfo;
import org.globus.myproxy.MyProxy;
import org.ietf.jgss.GSSCredential;
import org.springframework.security.AuthenticationException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;
import org.vpac.grisu.backend.model.ProxyCredential;
import org.vpac.grisu.backend.model.User;
import org.vpac.grisu.control.serviceInterfaces.AbstractServiceInterface;
import org.vpac.grisu.settings.MyProxyServerParams;
import org.vpac.grisu.settings.ServerPropertiesManager;



public class GrisuUserDetails implements UserDetails {

	static final Logger myLogger = Logger.getLogger(GrisuUserDetails.class
			.getName());


	private final String username;
	private UsernamePasswordAuthenticationToken authentication;
	private final boolean success = true;
	private ProxyCredential proxy = null;

	private User user = null;

	public GrisuUserDetails(String username) {
		this.username = username;
	}

	private synchronized ProxyCredential createProxyCredential(String username,
			String password, String myProxyServer, int port, int lifetime) {

		//		System.out.println("Username: "+username);
		//		System.out.println("Password: "+password);

		MyProxy myproxy = new MyProxy(myProxyServer, port);
		GSSCredential proxy = null;
		try {
			proxy = myproxy.get(username, password, lifetime);

			int remaining = proxy.getRemainingLifetime();

			if (remaining <= 0) {
				throw new RuntimeException("Proxy not valid anymore.");
			}

			return new ProxyCredential(proxy);
		} catch (Exception e) {
			e.printStackTrace();
			myLogger.error("Could not create myproxy credential: "
					+ e.getLocalizedMessage());
			return null;
		}

	}

	public GrantedAuthority[] getAuthorities() {

		if (success) {
			return new GrantedAuthorityImpl[] { new GrantedAuthorityImpl("User") };
		} else {
			return null;
		}

	}

	public synchronized long getCredentialEndTime() {

		if ( authentication == null ) {
			return -1;
		}

		MyProxy myproxy = new MyProxy(MyProxyServerParams.getMyProxyServer(),
				MyProxyServerParams.getMyProxyPort());
		CredentialInfo info = null;
		try {
			String user = authentication.getPrincipal().toString();
			String password = authentication.getCredentials().toString();
			info = myproxy.info(getProxyCredential().getGssCredential(), user,
					password);
		} catch (Exception e) {
			myLogger.error(e);
			return -1;
		}

		return info.getEndTime();

	}

	public String getPassword() {

		return "dummy";
	}

	public synchronized ProxyCredential getProxyCredential() throws AuthenticationException {

		if ( authentication == null ) {
			throw new AuthenticationException("No authentication token set.") {
			};
		}

		if ( (proxy != null) && proxy.isValid() ) {

			long oldLifetime = -1;
			try {
				oldLifetime = proxy.getGssCredential().getRemainingLifetime();
				if (oldLifetime >= ServerPropertiesManager
						.getMinProxyLifetimeBeforeGettingNewProxy()) {

					//					myLogger.debug("Proxy still valid and long enough lifetime.");
					return proxy;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		ProxyCredential proxyTemp = createProxyCredential(authentication.getPrincipal().toString(), authentication.getCredentials().toString(),
				MyProxyServerParams.DEFAULT_MYPROXY_SERVER,
				MyProxyServerParams.DEFAULT_MYPROXY_PORT,
				ServerPropertiesManager.getMyProxyLifetime());

		if ((proxyTemp == null) || !proxyTemp.isValid()) {

			//			if ( proxyTemp == null ) {
			//				System.out.println("PROXYTEMP IS NULL");
			//			} else {
			//				if ( proxyTemp.getGssCredential() == null ) {
			//					System.out.println("GSSCREDENTIAL IS NULL");
			//				} else {
			//					System.out.println("GSSCREDENTIAL NO LIFETIME");
			//				}
			//			}

			throw new AuthenticationException("Could not get valid myproxy credential."){
			};
		} else {
			myLogger.info("Authentication successful.");
			this.proxy = proxyTemp;
			return this.proxy;
		}



	}

	public synchronized User getUser(AbstractServiceInterface si) {

		if ( user == null ) {
			user = User.createUser(getProxyCredential(), si);
		}

		user.setCred(getProxyCredential());
		return user;

	}

	public String getUsername() {
		return username;
	}

	public boolean isAccountNonExpired() {
		return success;
	}

	public boolean isAccountNonLocked() {
		return success;
	}

	public boolean isCredentialsNonExpired() {
		return success;
	}

	public boolean isEnabled() {
		return success;
	}

	public void setAuthentication(UsernamePasswordAuthenticationToken authentication) {
		this.authentication = authentication;
		getProxyCredential();
	}


}
