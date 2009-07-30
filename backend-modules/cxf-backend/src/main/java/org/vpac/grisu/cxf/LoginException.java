package org.vpac.grisu.cxf;

public class LoginException extends Exception{
	
	public String username,password;

	public LoginException(String username, String password){
		super("login with user " + username + " failed.");
		this.username = username;
		this.password = password;
	}
}