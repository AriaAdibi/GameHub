package gui.gameComponents;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import annotation.Model;
import game.GameController;

public class Grid extends JPanel {

	private static final long			serialVersionUID	= 1009143L;

	private int							size;

	@Model
	private Map<Point, Boolean>			edges;
	private Map<Point, Line>			lines;
	private Map<Point, GradientLabel>	labels;
	private int							cellsRemaining;

	public Grid(int size) {
		this.size = size;
		cellsRemaining = (size - 1) * (size - 1);
		edges = new HashMap<Point, Boolean>();
		lines = new HashMap<Point, Line>();
		labels = new HashMap<Point, GradientLabel>();

		setLayout(new GridBagLayout());

		GridBagConstraints dotConstraints = new GridBagConstraints();
		dotConstraints.ipadx = dotConstraints.ipady = 0;
		dotConstraints.weightx = dotConstraints.weighty = 0;
		dotConstraints.fill = GridBagConstraints.NONE;
		dotConstraints.anchor = GridBagConstraints.CENTER;

		GridBagConstraints horizontalLineConstraints = new GridBagConstraints();
		horizontalLineConstraints.ipadx = horizontalLineConstraints.ipady = 0;
		horizontalLineConstraints.weightx = 1;
		horizontalLineConstraints.weighty = 0;
		horizontalLineConstraints.fill = GridBagConstraints.HORIZONTAL;
		horizontalLineConstraints.anchor = GridBagConstraints.CENTER;

		GridBagConstraints verticalLineConstraints = new GridBagConstraints();
		verticalLineConstraints.ipadx = verticalLineConstraints.ipady = 0;
		verticalLineConstraints.weightx = 0;
		verticalLineConstraints.weighty = 1;
		verticalLineConstraints.fill = GridBagConstraints.VERTICAL;
		verticalLineConstraints.anchor = GridBagConstraints.CENTER;

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.ipadx = labelConstraints.ipady = 0;
		labelConstraints.weightx = 1;
		labelConstraints.weighty = 1;
		labelConstraints.fill = GridBagConstraints.BOTH;
		labelConstraints.anchor = GridBagConstraints.CENTER;

		int col = 0;
		int row = 0;
		Dot dot;
		Line line;
		GradientLabel label;
		Point p;
		for (int i = 0; i < size; i++) {
			col = 0;
			for (int j = 0; j < size; j++) {
				dotConstraints.gridx = col++;
				dotConstraints.gridy = row;

				dot = new Dot();
				add(dot, dotConstraints);

				if (j != size - 1) {
					horizontalLineConstraints.gridx = col++;
					horizontalLineConstraints.gridy = row;

					line = new Line(null);
					add(line, horizontalLineConstraints);

					p = new Point();
					p.x = i * size + j;
					p.y = i * size + j + 1;
					line.setPoints(p);
					lines.put(p, line);
				}
			}
			row++;

			if (i != size - 1) {
				col = 0;
				for (int j = 0; j < size; j++) {
					verticalLineConstraints.gridx = col++;
					verticalLineConstraints.gridy = row;

					line = new Line(null, Line.VERTICAL);
					add(line, verticalLineConstraints);

					p = new Point();
					p.x = i * size + j;
					p.y = (i + 1) * size + j;
					line.setPoints(p);
					lines.put(p, line);

					if (j != size - 1) {
						labelConstraints.gridx = col++;
						labelConstraints.gridy = row;

						label = new GradientLabel();
						add(label, labelConstraints);

						labels.put(p, label);
					}
				}
				row++;
			}
		}
	}

	@Model
	public boolean isValid(Point edge) {
		return edge.x >= 0 && edge.y >= 0 && edge.x <= size * size - 1
				&& edge.y <= size * size - 1;
	}

	@Model
	public boolean mark(Point edge) {
		if (!isValid(edge))
			return false;

		if (edges.getOrDefault(edge, false))
			return false;

		edges.put(edge, true);
		Line line = lines.get(edge);
		line.set();
		int score = checkNewPoints(edge);
		if (score != 0)
			GameController.getInstance().increaseScore(score);
		else {
			cellsRemaining -= score;
			if (cellsRemaining == 0)
				GameController.getInstance().findWinner();
			GameController.getInstance().changeTurn();
		}
		return true;
	}

	@Model
	private int checkNewPoints(Point edge) {
		Point tmp = new Point();
		int score = 0;

		boolean mark;
		if (edge.y - edge.x == 1) {
			tmp.x = edge.y - size;
			tmp.y = edge.y;
			mark = edges.getOrDefault(tmp, false);
			tmp.x = edge.x - size;
			tmp.y = edge.x;
			mark = mark && edges.getOrDefault(tmp, false);
			tmp.x = edge.x - size;
			tmp.y = edge.y - size;
			mark = mark && edges.getOrDefault(tmp, false);
			if (mark) {
				score++;
				tmp.x = edge.x - size;
				tmp.y = edge.x;
				labels.get(tmp)
						.fill(GameController.getInstance().getTurnsName());
			}

			tmp.x = edge.y;
			tmp.y = edge.y + size;
			mark = edges.getOrDefault(tmp, false);
			tmp.x = edge.x;
			tmp.y = edge.x + size;
			mark = mark && edges.getOrDefault(tmp, false);
			tmp.x = edge.x + size;
			tmp.y = edge.y + size;
			mark = mark && edges.getOrDefault(tmp, false);
			if (mark) {
				score++;
				tmp.x = edge.x;
				tmp.y = edge.x + size;
				labels.get(tmp)
						.fill(GameController.getInstance().getTurnsName());
			}

			return score;
		} else {
			tmp.x = edge.y - 1;
			tmp.y = edge.y;
			mark = edges.getOrDefault(tmp, false);
			tmp.x = edge.x - 1;
			tmp.y = edge.x;
			mark = mark && edges.getOrDefault(tmp, false);
			tmp.x = edge.x - 1;
			tmp.y = edge.y - 1;
			mark = mark && edges.getOrDefault(tmp, false);
			if (mark) {
				score++;
				labels.get(tmp)
						.fill(GameController.getInstance().getTurnsName());
			}

			tmp.x = edge.y;
			tmp.y = edge.y + 1;
			mark = edges.getOrDefault(tmp, false);
			tmp.x = edge.x;
			tmp.y = edge.x + 1;
			mark = mark && edges.getOrDefault(tmp, false);
			tmp.x = edge.x + 1;
			tmp.y = edge.y + 1;
			mark = mark && edges.getOrDefault(tmp, false);
			if (mark) {
				score++;
				labels.get(edge)
						.fill(GameController.getInstance().getTurnsName());
			}

			return score;
		}
	}

	public int getGridSize() {
		return size;
	}

}
