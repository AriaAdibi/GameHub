package gui.gameComponents;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

public class Dot extends JComponent {

	private static final long	serialVersionUID			= 18918L;
	private static final Color	DEFAULT_BACKGROUND_COLOR	= new Color(40, 40,
			40);

	private Color backgroundColor;

	public Dot() {
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		init();
	}

	public Dot(Color color) {
		backgroundColor = color;
		init();
	}

	private void init() {
		Dimension defaultSize = new Dimension(10, 10);
		setMinimumSize(defaultSize);
		setPreferredSize(defaultSize);
		setSize(defaultSize);
	}

	public void setBackgroundColor(final Color color) {
		backgroundColor = color;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g = g.create();

		g.setColor(backgroundColor);
		g.fillOval(0, 0, getWidth(), getHeight());
	}
}
