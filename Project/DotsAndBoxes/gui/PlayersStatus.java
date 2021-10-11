package gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;

import util.ColorUtils;

public class PlayersStatus extends JList<String> {

	private static final long	serialVersionUID	= 571729293L;

	private Integer[]			stats;

	public PlayersStatus() {
		setSelectionModel(new NoSelectionModel());
		setCellRenderer(new PlayersStatusRenderer());
	}

	public void setListData(String[] listData, Integer[] stats) {
		setListData(listData);
		this.stats = stats;
	}

	private class PlayersStatusRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 751815387318L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);

			switch (stats[index]) {
				case -1:
					c.setForeground(ColorUtils.PLAYER_OFFLINE_TEXT_COLOR);
					break;
				case 0:
					c.setForeground(ColorUtils.PLAYER_PLAYING_TEXT_COLOR);
					break;
				default:
					c.setForeground(ColorUtils.PLAYER_WAITING_TEXT_COLOR);
					break;
			}
			return c;
		}
	}

	private static class NoSelectionModel extends DefaultListSelectionModel {

		private static final long serialVersionUID = 1471438713487L;

		@Override
		public void setSelectionInterval(int start, int ed) {
			super.setSelectionInterval(-1, -1);
		}
	}

}
