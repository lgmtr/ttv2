package de.haw.ttv2.coap.test;

import org.eclipse.californium.core.CoapClient;

public class CoAPTestMain {
	
	public static void main(String[] args) {
		CoapClient client = new CoapClient();
		client.put("Something to Put", 0);
	}

}
