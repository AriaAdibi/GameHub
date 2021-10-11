package gui.gameComponents;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import util.ColorUtils;
import util.DoubleUtils;

public class GradientLabel extends JLabel {

	private static final long	serialVersionUID	= 8912821832982138L;
	private static Random		random				= new Random();

	private Point[]	direction;
	private float[]	bounds;
	private Color[]	colors;
	private boolean	filled;
	private boolean	filling;

	public GradientLabel() {
		super("", SwingConstants.CENTER);
		setBackground(null);

		direction = new Point[2];
		for (int i = 0; i < 2; i++)
			direction[i] = new Point();

		bounds = new float[2];
		bounds[0] = 0f;

		colors = new Color[2];
		colors[1] = Color.white;

		filled = false;
		filling = false;

		setFont(new Font("Open Sans Light", Font.BOLD, 50));

		addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent e) {
				adaptFont();
			}
		});
	}

	private void adaptFont() {
		int stringWidth = getFontMetrics(getFont()).stringWidth(getText());
		int componentWidth = getWidth();

		double widthRatio = (double) componentWidth / (double) stringWidth;

		int newFontSize = (int) (getFont().getSize() * widthRatio);
		int componentHeight = getHeight() / 2;

		int fontSizeToUse = Math.min(newFontSize, componentHeight);

		setFont(getFont().deriveFont(getFont().getStyle(), fontSizeToUse));
	}

	private void setDirection() {
		switch (Math.abs(random.nextInt()) % 4) {
			case 0:
				direction[0].x = 0;
				direction[0].y = 0;

				direction[1].x = 0;
				direction[1].y = getHeight();
				return;
			case 1:
				direction[0].x = 0;
				direction[0].y = 0;

				direction[1].x = getWidth();
				direction[1].y = 0;
				return;
			case 2:
				direction[0].x = getWidth();
				direction[0].y = 0;

				direction[1].x = 0;
				direction[1].y = 0;
				return;
			case 3:
				direction[0].x = 0;
				direction[0].y = getHeight();

				direction[1].x = 0;
				direction[1].y = 0;
				return;
		}
	}

	public void fill(String text) {
		if (filling || filled)
			return;
		filling = true;

		setDirection();

		bounds[1] = 0.000001f;

		colors[0] = ColorUtils.randomColor();
		setForeground(ColorUtils.getSuitingForeground(colors[0]));

		final Timer timer = new Timer(1, null);
		timer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (1 - bounds[1] < DoubleUtils.EPS) {
					filled = true;
					filling = false;
					setOpaque(true);
					setBackground(colors[0]);
					setText(text);
					timer.stop();
				} else
					bounds[1] += 0.005f;
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						repaint();
					}
				});
			}
		});
		timer.start();
	}

	@Override
	public void paint(Graphics g) {
		if (filling && !filled) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			LinearGradientPaint paint = new LinearGradientPaint(direction[0],
					direction[1], bounds, colors);
			g2d.setPaint(paint);

			g2d.fillRect(0, 0, getWidth(), getHeight());

			g2d.dispose();
		}

		super.paint(g);
	}

}
