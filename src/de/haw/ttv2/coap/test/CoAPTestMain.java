package de.haw.ttv2.coap.test;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import de.haw.ttv2.main.PlayerStatusEnum;

/**
 * Test class to test the coap put method to change the LED color on a local dummy
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 */
public class CoAPTestMain {
	
	public static void main(String[] args) throws URISyntaxException {
		URI address = new URI("coap://localhost/led");
		CoapClient client = new CoapClient(address);
		client.put("0", MediaTypeRegistry.TEXT_PLAIN);
		client.put(PlayerStatusEnum.RED.getCoapCode(), MediaTypeRegistry.TEXT_PLAIN);
		client.put(PlayerStatusEnum.BLUE.getCoapCode(), MediaTypeRegistry.TEXT_PLAIN);
		client.put("1", MediaTypeRegistry.TEXT_PLAIN);
	}

}
