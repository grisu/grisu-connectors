package org.vpac.grisu.frontend.control.cxf;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.vpac.grisu.cxf.MyProxyCredentials;

public class ClientAuthInterceptor extends SoapPreProtocolOutInterceptor {

	private final String username, password, myproxyServer;
	private final int myproxyPort;

	public ClientAuthInterceptor(String username, String password,
			String myproxyServer, int myproxyPort) {
		super();
		this.username = username;
		this.password = password;
		this.myproxyServer = myproxyServer;
		this.myproxyPort = myproxyPort;
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		/*
		 * JAXBContext jctx = JAXBContext.newInstance(MyProxyCredentials.class);
		 * JAXBDataBinding binding = new JAXBDataBinding(); binding.
		 */
		try {
			final Header cred = new Header(new QName("", "myProxyCredentials"),
					new MyProxyCredentials(myproxyServer, myproxyPort + "",
							username, password), new JAXBDataBinding(
							MyProxyCredentials.class));
			message.getHeaders().add(cred);
		} catch (final Exception ex) {
			throw new Fault(ex);
		}

	}
}