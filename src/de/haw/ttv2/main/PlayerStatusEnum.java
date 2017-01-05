package de.haw.ttv2.main;

import javafx.scene.paint.Color;

/**
 * Colors for the player status, send in coap client to set the LED Color
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 */
public enum PlayerStatusEnum {
	
	GREEN(Color.GREEN, "g"),
	BLUE(Color.BLUE, "b"),
	VIOLET(Color.VIOLET, "v"),
	RED(Color.RED, "r");

	private Color color;
	
	private String coapCode;
	
	private PlayerStatusEnum(Color color,String coapCode) {
		this.color = color;
		this.coapCode = coapCode;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getCoapCode() {
		return coapCode;
	}

	public void setCoapCode(String coapCode) {
		this.coapCode = coapCode;
	}
	
}
