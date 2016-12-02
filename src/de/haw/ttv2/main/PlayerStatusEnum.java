package de.haw.ttv2.main;

import javafx.scene.paint.Color;

public enum PlayerStatusEnum {
	
	GREEN(Color.GREEN),
	BLUE(Color.BLUE),
	VIOLET(Color.VIOLET),
	RED(Color.RED);

	private Color color;
	
	private PlayerStatusEnum(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	
	
}
