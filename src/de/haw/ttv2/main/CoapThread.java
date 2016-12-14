package de.haw.ttv2.main;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class CoapThread implements Runnable {

	private String coapCommand;
	
	public CoapThread(String coapCommand) {
		this.coapCommand = coapCommand;
	}
	
	@Override
	public void run() {
		URI address;
		try {
			address = new URI("coap://localhost/led");
			CoapClient client = new CoapClient(address);
			client.put("0", MediaTypeRegistry.TEXT_PLAIN);
			client.put(PlayerStatusEnum.VIOLET.getCoapCode(), MediaTypeRegistry.TEXT_PLAIN);
			client.put("1", MediaTypeRegistry.TEXT_PLAIN);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}	
	}

}
