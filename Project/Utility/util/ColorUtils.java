package util;

import java.awt.Color;
import java.util.Random;

import javax.swing.JList;
import javax.swing.JTextField;

public final class ColorUtils {

	public static final Color	NO_COLOR							= new Color(
			0, 0, 0, 0);
	public static final Color	ERROR_COLOR							= new Color(
			255, 50, 80);
	public static final Color	TEXT_FIELD_DEFAULT_BACKGROUND_COLOR	= new JTextField()
			.getBackground();
	public static final Color	LIST_DEFAULT_BACKGROUND_COLOR		= new JList<Object>()
			.getBackground();
	public static final Color	CURRENT_PLAYER_ROW_COLOR			= new Color(
			0, 230, 150);
	public static final Color	PLAYER_WAITING_TEXT_COLOR			= new Color(
			10, 180, 30);
	public static final Color	PLAYER_PLAYING_TEXT_COLOR			= new Color(
			0, 80, 240);
	public static final Color	PLAYER_OFFLINE_TEXT_COLOR			= new Color(
			70, 70, 70);

	private static final Random	random								= new Random();

	public static int randomSRGBArgument() {
		return Math.abs(random.nextInt()) % 255;
	}

	public static Color randomColor() {
		return new Color(randomSRGBArgument(), randomSRGBArgument(),
				randomSRGBArgument());
	}

	public static Color getSuitingForeground(Color color) {
		if ((getBrightness(color) << 1) <= 255)
			return Color.white;
		return Color.black;
	}

	private static int getBrightness(Color color) {
		return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
	}

}
