package org.vpac.grisu.cxf;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * contains all necessary information to login to grisu: username, password,
 * myproxy port and myproxy host
 **/

@XmlRootElement
public class MyProxyCredentials {
	@XmlElement
	public String username;
	@XmlElement
	public String password;
	@XmlElement
	public String myproxyServer;
	@XmlElement
	public String myproxyPort;

	/*
	 * public String getUsername(){ return username;} public String
	 * getPassword(){ return password;} public String getMyproxyServer(){ return
	 * myproxyServer;} public String getMyproxyPort(){ return myproxyPort;}
	 * 
	 * public void setUsername(String username){ this.username = username; }
	 * 
	 * public void setPassword(String password){ this.password = password; }
	 * 
	 * public void setMyproxyServer(String myproxyServer){ this.myproxyServer =
	 * myproxyServer; }
	 * 
	 * public void setMyproxyPort(String myproxyPort){ this.myproxyPort =
	 * myproxyPort; }
	 */

	public MyProxyCredentials() {
	}

	public MyProxyCredentials(String myproxyServer, String myproxyPort,
			String username, String password) {
		this.username = username;
		this.password = password;
		this.myproxyServer = myproxyServer;
		this.myproxyPort = myproxyPort;
	}
}