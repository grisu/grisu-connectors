package org.vpac.grisu.cxf;


import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.headers.Header; 
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.phase.Phase;
import javax.xml.namespace.QName; 
import org.apache.cxf.jaxb.JAXBDataBinding;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.w3c.dom.Node;
import org.apache.log4j.Logger;


import org.globus.myproxy.CredentialInfo;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.vpac.grisu.backend.model.ProxyCredential;


public class MyProxyAuthInterceptor extends AbstractSoapInterceptor{

	static final Logger myLogger = Logger.getLogger(MyProxyAuthInterceptor.class
			.getName());


	public MyProxyAuthInterceptor() {
		super(Phase.POST_PROTOCOL);
	}

	public void handleMessage(SoapMessage message) throws Fault{
		Header header = message.getHeader(new QName("","myProxyCredentials"));
		
		try {
			MyProxyCredentials cred = null;
			ProxyCredential proxyCredential = (ProxyCredential)message.getExchange().getSession().get("credential");
			System.out.println("the credential is " + proxyCredential);
			if (proxyCredential == null || !proxyCredential.isValid()){
				
				myLogger.debug("creating new credential...");
				cred = retrieveProxyFromHeader(header);
				proxyCredential = createProxyCredential(cred.username, cred.password,cred.myproxyServer, Integer.parseInt(cred.myproxyPort), 99999);
				message.getExchange().getSession().put("credential",proxyCredential);
			}
			if (proxyCredential == null || !proxyCredential.isValid()){
				throw new Fault(new LoginException(cred.username, cred.password));
			}
			message.put("credential",proxyCredential);
			
		}
		catch (JAXBException ex){
			// should not happen...
			throw new Fault(ex);
		}
		
	}

	private MyProxyCredentials retrieveProxyFromHeader(Header h) throws JAXBException {
		Unmarshaller u = JAXBContext.newInstance(MyProxyCredentials.class).createUnmarshaller();
		Object o = u.unmarshal((org.w3c.dom.Node)(h.getObject()));
		MyProxyCredentials cred = (MyProxyCredentials)o;
		return cred;		
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
			myLogger.error("Could not create myproxy credential: "
				       + e.getLocalizedMessage());
			return null;
		}
	}
}