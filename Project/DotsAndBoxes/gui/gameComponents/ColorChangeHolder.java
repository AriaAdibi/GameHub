package gui.gameComponents;

import java.awt.Color;

class ColorChangeHolder {

	private final int	steps;
	private final Color	destColor;
	private int			currentStep;
	private double		redStep, greenStep, blueStep, alphaStep;
	private double		red, green, blue, alpha;
	private boolean		isOver;

	public ColorChangeHolder(Color srcColor, Color destColor, int steps) {
		if (srcColor.getAlpha() == 0) {
			srcColor = new Color(destColor.getRed(), destColor.getGreen(),
					destColor.getBlue(), 0);
		} else if (destColor.getAlpha() == 0) {
			destColor = new Color(srcColor.getRed(), srcColor.getGreen(),
					srcColor.getBlue(), 0);
		}
		this.destColor = destColor;
		this.steps = steps;
		currentStep = 0;

		red = srcColor.getRed();
		green = srcColor.getGreen();
		blue = srcColor.getBlue();
		alpha = srcColor.getAlpha();

		redStep = destColor.getRed() - srcColor.getRed();
		greenStep = destColor.getGreen() - srcColor.getGreen();
		blueStep = destColor.getBlue() - srcColor.getBlue();
		alphaStep = destColor.getAlpha() - srcColor.getAlpha();

		redStep /= steps;
		greenStep /= steps;
		blueStep /= steps;
		alphaStep /= steps;

		isOver = false;
	}

	public Color next() {
		currentStep++;
		if (currentStep == steps) {
			isOver = true;
			return destColor;
		}

		red += redStep;
		green += greenStep;
		blue += blueStep;
		alpha += alphaStep;

		if (red < 0)
			red = 0;

		if (green < 0)
			green = 0;

		if (blue < 0)
			blue = 0;

		if (alpha < 0)
			alpha = 0;

		return new Color((int) red, (int) green, (int) blue, (int) alpha);
	}

	public boolean isOver() {
		return isOver;
	}
}