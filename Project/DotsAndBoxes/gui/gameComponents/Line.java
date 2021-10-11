package gui.gameComponents;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import annotation.Model;
import util.ColorUtils;
import game.GameController;

public class Line extends JComponent {

	private static final ArrayList<Line> allLines;

	static {
		allLines = new ArrayList<Line>();
		Timer timer = new Timer(1_000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				for (Line line : allLines)
					if (line.state.equals("out") && !line.changing) {
						line.setBackgroundColor(ColorUtils.NO_COLOR);
					}
			}
		});

		timer.start();
	}

	public static final int		VERTICAL					= 1;
	public static final int		HORIZONTAL					= 4;

	private static final long	serialVersionUID			= 1821381278L;
	private static final Color	DEFAULT_BACKGROUND_COLOR	= new Color(80, 80,
			80);

	private Color				backgroundColor;
	private boolean				currentColorPainted;
	private String				state;
	private boolean				changing;
	private boolean				set;

	@Model
	private Point				points;

	public Line() {
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		init(HORIZONTAL);
	}

	public Line(int direction) {
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		init(direction);
	}

	public Line(Color color) {
		backgroundColor = color;
		init(HORIZONTAL);
	}

	public Line(Color color, int direction) {
		backgroundColor = color;
		init(direction);
	}

	private void init(int direction) {
		state = "out";
		changing = false;
		set = false;

		if (backgroundColor == null)
			backgroundColor = ColorUtils.NO_COLOR;

		currentColorPainted = false;

		Dimension defaultSize;
		if (direction == HORIZONTAL)
			defaultSize = new Dimension(50, 2);
		else
			defaultSize = new Dimension(2, 50);
		setMinimumSize(defaultSize);
		setPreferredSize(defaultSize);
		setSize(defaultSize);

		addMouseListener(new LineMouseListener());

		setCursor(new Cursor(Cursor.HAND_CURSOR));

		allLines.add(this);
	}

	@Model
	public void setPoints(Point points) {
		this.points = points;
	}

	@Model
	public Point getPoints() {
		return points;
	}

	public void setBackgroundColor(Color color) {
		if (currentColorPainted) {
			changing = true;
			currentColorPainted = false;
			final ColorChangeHolderWithState holder = new ColorChangeHolderWithState(
					backgroundColor, color, 250, state);
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (!holder.isOver()) {
							if (!state.equals(holder.getState()))
								return;
							backgroundColor = holder.next();
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									repaint();
								}
							});
							Thread.sleep(1);
						}
						changing = false;
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}).start();
		} else
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

		if (backgroundColor == null)
			return;
		g.setColor(backgroundColor);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (!currentColorPainted)
			currentColorPainted = true;
	}

	public boolean isSet() {
		return set;
	}

	public void set() {
		if (set)
			return;
		state = "fixed";
		set = true;
		setCursor(Cursor.getDefaultCursor());
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
		repaint();
		revalidate();
	}

	private class LineMouseListener extends MouseAdapter {

		private Color original;

		@Override
		public void mouseEntered(MouseEvent event) {
			if (!set) {
				state = "in";
				original = getBackgroundColor();
				setBackgroundColor(ColorUtils.randomColor());
				repaint();
				revalidate();
			}
		}

		@Override
		public void mouseExited(MouseEvent event) {
			if (!set) {
				state = "out";
				setBackgroundColor(original);
				original = null;
				repaint();
				revalidate();
			}
		}

		@Override
		public void mouseClicked(MouseEvent event) {
			if (event.getButton() != MouseEvent.BUTTON1
					|| !GameController.getInstance().isPlayersTurn())
				return;

			GameController.getInstance().set(points);
			GameController.getInstance().sendMove(Line.this, points);
		}
	}

}
