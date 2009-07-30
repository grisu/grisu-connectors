

package org.vpac.grisu.client.control.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.handler.AbstractHandler;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This handler puts the MyProxy username & password into the http header.
 * 
 * Look at the xfire documentation if you need to know more...
 * 
 * @author Markus Binsteiner
 *
 */
public class ClientAuthenticationHandler extends AbstractHandler {
	
private String username = null;
private String password = null;
private String myProxyServer = null;
private String myProxyPort = null;


public ClientAuthenticationHandler() {
}

public ClientAuthenticationHandler(String username,String password, String myProxyServer, String myProxyPort) {
    this.username = username;
    this.password = password;
    this.myProxyServer = myProxyServer;
    this.myProxyPort = myProxyPort;
}

public void setUsername(String username) {
    this.username = username;
}

public void setPassword(String password) {
    this.password = password;
}

public void setMyProxyServer(String myProxyServer) {
	this.myProxyServer = myProxyServer;
}

public void setMyProxyPort(String myProxyPort) {
	this.myProxyPort = myProxyPort;
}

public void invoke(MessageContext context) throws Exception {

    final Namespace ns = Namespace.getNamespace("http://grisu.vpac.org/grisu-ws");
    Element el = new Element("header",ns);
    context.getOutMessage().setHeader(el);

    Element auth = new Element("AuthenticationToken", ns);
    Element username_el = new Element("Username",ns);
    username_el.addContent(username);
    Element password_el = new Element("Password",ns);
    password_el.addContent(password);
    Element myProxyServer_el = new Element("MyProxyServer",ns);
    myProxyServer_el.addContent(myProxyServer);
    Element myProxyPort_el = new Element("MyProxyPort",ns);
    myProxyPort_el.addContent(myProxyPort);
    auth.addContent(username_el);
    auth.addContent(password_el);
    auth.addContent(myProxyServer_el);
    auth.addContent(myProxyPort_el);
    el.addContent(auth);

}
}
