package gui.gameComponents;

import java.awt.Color;

public class ColorChangeHolderWithState extends ColorChangeHolder {

	private String state;

	public ColorChangeHolderWithState(Color srcColor, Color destColor,
			int steps, String state) {
		super(srcColor, destColor, steps);
		this.state = state;
	}

	public String getState() {
		return state;
	}

}
