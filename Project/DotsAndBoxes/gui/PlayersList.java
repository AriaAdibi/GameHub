package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import util.Chronometer;
import util.ColorUtils;
import game.GameController;

public class PlayersList extends JTable {

	private static final long	serialVersionUID	= 3871823123L;
	private int					playersTurn;
	private String[]			players;
	private int[]				scores;
	private double[]			elapsedTimes;
	private int					turn;
	private Chronometer			chronometer;
	private boolean[]			playerLeft;
	private int					playersRemaining;
	private int					drawAccepts;

	public PlayersList(String[] players, String username) {
		super();
		setFillsViewportHeight(true);

		turn = -1;
		chronometer = new Chronometer();
		setData(players, username);
	}

	public void setData(String[] players, String username) {
		this.players = players;
		scores = new int[players.length];
		elapsedTimes = new double[players.length];
		playerLeft = new boolean[players.length];
		playersRemaining = players.length;

		Object[][] rowData = new String[players.length][3];

		int tmp = -1;
		for (int i = 0; i < players.length; i++) {
			if (players[i].equals(username))
				tmp = i;
			rowData[i][0] = players[i];
			rowData[i][1] = "0";
			rowData[i][2] = "0.00";
		}

		playersTurn = tmp;

		String[] columnsNames = new String[] { "User", "Score",
				"Total Elapsed Time" };

		DefaultTableModel model = new DefaultTableModel(rowData, columnsNames) {

			private static final long serialVersionUID = 872134872348L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		setModel(model);
		setFocusable(false);
		setRowSelectionAllowed(false);

		for (int i = 0; i < 3; i++)
			getColumnModel().getColumn(i)
					.setCellRenderer(new PlayersCellRenderer());
	}

	public void playerLeft(String name) {
		int pTurn = getPlayersIndex(name);
		playerLeft[pTurn] = true;
		playersRemaining--;
		if (playersRemaining == 1)
			GameController.getInstance().win(playersTurn);
		if (pTurn == turn)
			changeTurn();
	}

	public boolean isPlayersTurn() {
		return turn == playersTurn;
	}

	public void increaseScore(int score) {
		scores[turn] += score;
		setValueAt(String.valueOf(scores[turn]), turn, 1);
		revalidate();
		repaint();
	}

	public int getTurn() {
		return turn;
	}

	public void changeTurn() {
		if (turn != -1 && turn == playersTurn)
			increaseElapsedTime(turn, chronometer.stop());
		turn++;
		turn %= players.length;
		if (turn == playersTurn)
			chronometer.start();
		revalidate();
		repaint();
		if (playerLeft[turn])
			changeTurn();
	}

	public double getElapsedTime(int index) {
		return elapsedTimes[index];
	}

	public void increaseElapsedTime(int index, double value) {
		elapsedTimes[index] += value;
		setValueAt(String.format("%.2f", elapsedTimes[index]), index, 2);
		revalidate();
		repaint();
	}

	public void setElapsedTime(int index, double value) {
		elapsedTimes[index] = value;
		setValueAt(String.format("%.2f", elapsedTimes[index]), index, 2);
		revalidate();
		repaint();
	}

	public String getPlayer(int index) {
		return players[index];
	}

	public int getPlayersIndex(String name) {
		for (int i = 0; i < players.length; i++)
			if (players[i].equals(name))
				return i;
		return -1;
	}

	public String getCurrentPlayer() {
		return players[turn];
	}

	public int getWinner() {
		int maxScore = 0;
		int index = -1;
		for (int i = 0; i < players.length; i++) {
			if (scores[i] > maxScore) {
				index = i;
				maxScore = scores[i];
			} else if (scores[i] == maxScore
					&& elapsedTimes[i] < elapsedTimes[index]) {
				index = i;
			}
		}

		return index;
	}

	public void resetDrawAccepts() {
		drawAccepts = 1;
	}

	public void increaseDrawAccepts() {
		drawAccepts++;
		if (drawAccepts == players.length)
			GameController.getInstance().draw();
	}

	private class PlayersCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 7823872387L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);

			if (row == turn)
				c.setBackground(ColorUtils.CURRENT_PLAYER_ROW_COLOR);
			else
				c.setBackground(Color.white);
			return c;
		}
	}

}
