package gui;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import game.GameController;

public class SecondaryFrame extends JFrame {

	private static final long serialVersionUID = 89143817410L;

	public SecondaryFrame(String title) {
		super(title);
		setIconImage(new ImageIcon(getClass().getResource("/static/icon.png"))
				.getImage());
		getContentPane().setBackground(Color.white);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent event) {
				GameController.getInstance().showMainWindow();
			}
		});
	}

}
