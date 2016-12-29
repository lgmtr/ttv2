package de.haw.ttv2.main;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

/**
 * 
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 */
public class CoapThread implements Runnable {

	private PlayerStatusEnum coapCommand;

	public CoapThread(PlayerStatusEnum coapCommand) {
		this.coapCommand = coapCommand;
	}

	@Override
	public void run() {
		URI address;
		try {
			address = new URI("coap://localhost/led");
			CoapClient client = new CoapClient(address);
			client.put("0", MediaTypeRegistry.TEXT_PLAIN);
			switch (coapCommand) {
			case RED:
				client.put(PlayerStatusEnum.RED.getCoapCode(), MediaTypeRegistry.TEXT_PLAIN);
				break;
			case BLUE:
				client.put(PlayerStatusEnum.BLUE.getCoapCode(), MediaTypeRegistry.TEXT_PLAIN);
				break;
			case GREEN:
				client.put(PlayerStatusEnum.GREEN.getCoapCode(), MediaTypeRegistry.TEXT_PLAIN);
				break;
			case VIOLET:
				client.put(PlayerStatusEnum.RED.getCoapCode(), MediaTypeRegistry.TEXT_PLAIN);
				client.put(PlayerStatusEnum.BLUE.getCoapCode(), MediaTypeRegistry.TEXT_PLAIN);
				break;
			}
		} catch (URISyntaxException e) {
			//e.printStackTrace();
		}
	}

}
