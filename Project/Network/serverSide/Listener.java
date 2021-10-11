package serverSide;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import gui.HintedTextField;
import serverSide.ServerCommunicator;

public class Listener {

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		JFrame frame = new JFrame("Dots and Lines Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container pane = frame.getContentPane();
		pane.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = c.gridy = 0;
		c.ipadx = 150;
		c.weightx = 1;
		c.insets = new Insets(15, 15, 15, 15);

		HintedTextField port = new HintedTextField("Port Number");
		pane.add(port, c);

		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.ipadx = 0;
		c.weightx = 0;

		JButton start = new JButton("Start Server");
		start.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int portNumber = Integer.valueOf(port.getText());
					connect(frame, portNumber);
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame,
							"Port number must be an Integer", "NaN",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		pane.add(start, c);

		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setMinimumSize(frame.getSize());
		frame.setVisible(true);
	}

	private static void connect(JFrame frame, int port) {
		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (listening) {
				Socket socket = serverSocket.accept();
				new Thread(new ServerCommunicator(socket)).start();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, e.getMessage(),
					"Could not listen on port " + port,
					JOptionPane.ERROR_MESSAGE);
			// System.err.println("Could not listen on port " + port);
		}

	}

}