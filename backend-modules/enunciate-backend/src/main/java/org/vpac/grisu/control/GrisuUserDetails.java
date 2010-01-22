package org.vpac.grisu.control;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;



public class GrisuUserDetails implements UserDetails {

	private String username;
	private String password;
	private boolean success;

	public GrisuUserDetails(String username, String password, boolean success) {
		this.username = username;
		this.password = password;
		this.success = success;
	}

	public GrantedAuthority[] getAuthorities() {

		if (success) {
			return new GrantedAuthorityImpl[] { new GrantedAuthorityImpl("User") };
		} else {
			return null;
		}

	}

	public String getPassword() {
		return password;
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

}
