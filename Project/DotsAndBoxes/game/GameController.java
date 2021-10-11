package game;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import annotation.GraphicalResult;
import annotation.Model;
import clientSide.ClientCommunicator;
import util.ColorUtils;
import util.Pair;
import util.ServerInfo;
import game.Parser;
import gui.HintedPasswordField;
import gui.HintedTextField;
import gui.PlayersList;
import gui.PlayersStatus;
import gui.SecondaryFrame;
import gui.gameComponents.Grid;

public class GameController {

	private static final GameController gameController = new GameController();

	public static final GameController getInstance() {
		return gameController;
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		UIManager.put("Panel.background", Color.white);
		UIManager.put("Slider.background", Color.white);
		UIManager.put("List.background", Color.white);
		UIManager.put("Table.background", Color.white);
		UIManager.put("OptionPane.background", Color.white);
		UIManager.put("MenuItem.opaque", true);
		UIManager.put("MenuItem.background", Color.white);

		gameController.showSignIn();
	}

	private ClientCommunicator	client;
	@Model
	private Grid				grid;
	@Model
	private int					numberOfPlayers;
	private String				username;
	private PlayersList			players;
	private Parser				parser;
	private boolean				gameStarted;
	private JFrame				gameFrame;

	GameController() {
		parser = new Parser();
		try {
			client = new ClientCommunicator(ServerInfo.SERVER_ADDRESS,
					ServerInfo.SERVER_PORT);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	@GraphicalResult
	public void showMainWindow() {
		final JFrame frame = new JFrame("Choose a game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationByPlatform(true);

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent event) {
				int ans = JOptionPane.showConfirmDialog(frame,
						"Do you want to sign out?", "Sign Out",
						JOptionPane.YES_NO_OPTION);
				if (ans == JOptionPane.YES_OPTION) {
					try {
						String error = client.signOut();
						if (!error.equals("NoError")) {
							JOptionPane.showMessageDialog(frame, error,
									"There was an error",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						frame.dispose();
						showSignIn();
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		});

		Container pane = frame.getContentPane();
		pane.setLayout(new GridBagLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.gridheight = c.gridwidth = 1;
		c.gridx = c.gridy = 0;
		c.insets = new Insets(10, 10, 10, 10);
		c.ipadx = 0;
		c.ipady = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;

		HintedTextField name = new HintedTextField("Game's name");
		panel.add(name, c);

		c.gridy = 1;

		JSlider slider = new JSlider(2, 5, 2);
		slider.setPaintLabels(true);
		Hashtable<Integer, JLabel> tmpTable = new Hashtable<Integer, JLabel>();
		tmpTable.put(2, new JLabel("2"));
		tmpTable.put(3, new JLabel("3"));
		tmpTable.put(4, new JLabel("4"));
		tmpTable.put(5, new JLabel("5"));
		slider.setLabelTable(tmpTable);
		panel.add(slider, c);

		c.gridheight = 2;
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;

		JButton newGame = new JButton("New Game");
		newGame.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (name.getText().equals(""))
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								for (int i = 0; i < 3; i++) {
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											name.setBackground(
													ColorUtils.ERROR_COLOR);
										}
									});

									Thread.sleep(200);
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											name.setBackground(
													ColorUtils.TEXT_FIELD_DEFAULT_BACKGROUND_COLOR);
										}
									});
									Thread.sleep(200);
								}
							} catch (Exception exc) {
								exc.printStackTrace();
							}
						}
					}).start();
				else {
					try {
						Pair<Integer, String> error = client.createADLGame(
								name.getText(), slider.getValue());
						if (!error.sec.equals("NoError"))
							JOptionPane.showMessageDialog(frame, error.sec,
									"There was an error",
									JOptionPane.ERROR_MESSAGE);
						else {
							frame.dispose();
							showNewGame(error.fir, name.getText(),
									new String[] { username });
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		});
		panel.add(newGame, c);

		mainPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		mainPanel.add(panel);

		mainPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		mainPanel.add(new JSeparator(SwingConstants.VERTICAL));

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = c.gridwidth = 1;
		c.gridx = c.gridy = 0;
		c.insets = new Insets(10, 10, 10, 10);
		c.ipadx = c.ipady = 0;
		c.weightx = 1;
		c.weighty = 1;

		JList<String> games = new JList<String>();
		games.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panel.add(new JScrollPane(games), c);

		c.fill = GridBagConstraints.NONE;
		c.gridy = 1;
		c.weightx = c.weighty = 0;

		JButton join = new JButton("Join");
		join.setEnabled(false);
		join.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				if (games.isSelectionEmpty())
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								for (int i = 0; i < 3; i++) {
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											games.setBackground(
													ColorUtils.ERROR_COLOR);
										}
									});

									Thread.sleep(200);
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											games.setBackground(
													ColorUtils.LIST_DEFAULT_BACKGROUND_COLOR);
										}
									});
									Thread.sleep(200);
								}
							} catch (Exception exc) {
								exc.printStackTrace();
							}
						}
					}).start();
				else {
					try {
						Pair<Integer, String> error = client.joinTheGame(
								parser.getGameID(games.getSelectedValue()));
						if (!error.sec.equals("NoError"))
							JOptionPane.showMessageDialog(frame, error.sec,
									"There was an error",
									JOptionPane.ERROR_MESSAGE);
						else {
							frame.dispose();
							Pair<ArrayList<String>, String> error2 = client
									.getPlayers();
							if (!error2.sec.equals("NoError"))
								JOptionPane.showMessageDialog(frame, error2.sec,
										"There was an error",
										JOptionPane.ERROR_MESSAGE);
							else {
								showNewGame(error.fir, games.getSelectedValue(),
										error2.fir.toArray(new String[0]));
							}
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		});
		games.getSelectionModel()
				.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						join.setEnabled(!games.isSelectionEmpty());
					}
				});

		panel.add(join, c);

		mainPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		mainPanel.add(panel);

		mainPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		c = new GridBagConstraints(0, 0, 1, 1, 1, 0.9,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 0, 200);

		pane.add(mainPanel, c);

		c.gridy = 1;
		c.weighty = 0;
		c.ipady = 0;
		c.fill = GridBagConstraints.HORIZONTAL;

		pane.add(new JSeparator(SwingConstants.HORIZONTAL), c);

		c.gridy = 2;
		c.weighty = 0.1;
		c.ipady = 50;
		c.fill = GridBagConstraints.BOTH;

		PlayersStatus status = new PlayersStatus();
		status.setBorder(new TitledBorder("Users"));

		pane.add(status, c);

		frame.pack();
		frame.setMinimumSize(frame.getSize());
		frame.setVisible(true);
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (frame.isVisible()) {
					try {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								try {
									Pair<ArrayList<String>, String> error = client
											.getWaitingGames();
									if (!error.sec.equals("NoError"))
										JOptionPane.showMessageDialog(frame,
												error.sec, "There was an error",
												JOptionPane.ERROR_MESSAGE);
									else {
										parser.updateList(error.fir
												.toArray(new String[0]));
										String name = games.getSelectedValue();
										games.setListData(parser.getGames());
										frame.revalidate();
										frame.repaint();
										games.setSelectedValue(name, true);
									}
								} catch (Exception exc) {
									exc.printStackTrace();
								}
							}
						});
						Thread.sleep(500);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (frame.isVisible()) {
					try {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								try {
									Pair<ArrayList<String>, String> error = client
											.getUsers();
									if (!error.sec.equals("NoError"))
										JOptionPane.showMessageDialog(frame,
												error.sec, "There was an error",
												JOptionPane.ERROR_MESSAGE);
									else {
										parser.updateStatus(error.fir
												.toArray(new String[0]));
										status.setListData(parser.getPlayers(),
												parser.getPlayerStats());
										frame.revalidate();
										frame.repaint();
									}
								} catch (Exception exc) {
									exc.printStackTrace();
								}
							}
						});
						Thread.sleep(500);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		}).start();
	}

	@GraphicalResult
	private void showSignIn() {
		JFrame frame = new JFrame("Dots and Lines");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent event) {
				try {
					client.close();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});

		Container pane = frame.getContentPane();

		pane.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		HintedTextField username = new HintedTextField("Username");

		c.anchor = GridBagConstraints.SOUTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = c.gridy = 0;
		c.insets = new Insets(20, 50, 20, 50);
		c.weightx = 1;
		c.weighty = 0;
		c.ipadx = 150;
		c.gridwidth = 2;

		pane.add(username, c);

		HintedPasswordField password = new HintedPasswordField("Password");

		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 1;
		c.insets = new Insets(10, 50, 10, 50);

		pane.add(password, c);

		JButton signUp = new JButton("Sign Up");

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridy = 2;
		c.insets = new Insets(10, 50, 10, 0);
		c.ipadx = 75;
		c.gridwidth = 1;

		pane.add(signUp, c);

		JButton signIn = new JButton("Sign In");

		c.anchor = GridBagConstraints.EAST;
		c.gridx = 1;
		c.insets = new Insets(10, 0, 10, 50);

		pane.add(signIn, c);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		signUp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				if (!checkUserPass(username, password))
					return;
				gameController.showSignUp(frame);
			}
		});

		signIn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!checkUserPass(username, password))
					return;
				try {
					String error = client.signIn(username.getText(),
							String.valueOf(password.getPassword()));
					if (!error.equals("NoError")) {
						JOptionPane.showMessageDialog(frame, error,
								"There was an error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					GameController.this.username = username.getText();
					frame.dispose();
					showMainWindow();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});
	}

	@GraphicalResult
	private void showSignUp(JFrame frame) {
		frame.removeWindowListener(frame.getWindowListeners()[0]);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				showSignIn();
			}
		});

		Container pane = frame.getContentPane();

		final String username = ((HintedTextField) pane.getComponent(0))
				.getText();
		final String password = String.valueOf(
				((HintedPasswordField) pane.getComponent(1)).getPassword());

		pane.removeAll();

		pane.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		HintedTextField fullName = new HintedTextField("Full Name");

		c.anchor = GridBagConstraints.SOUTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = c.gridy = 0;
		c.insets = new Insets(20, 50, 20, 50);
		c.weightx = 1;
		c.weighty = 0;
		c.ipadx = 150;

		pane.add(fullName, c);

		HintedTextField age = new HintedTextField("Age");

		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 1;
		c.insets = new Insets(10, 50, 10, 50);

		pane.add(age, c);

		JButton signUp = new JButton("Complete Sign Up");

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.gridy = 2;
		c.insets = new Insets(10, 50, 10, 50);
		c.ipadx = 0;
		c.weightx = 0;

		pane.add(signUp, c);

		frame.revalidate();
		frame.repaint();

		signUp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					String error = client
							.createNewUser(new String[] { fullName.getText(),
									age.getText(), username, password });
					if (!error.equals("NoError")) {
						JOptionPane.showMessageDialog(frame, error,
								"There was an error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					error = client.signIn(username, password);
					if (!error.equals("NoError")) {
						JOptionPane.showMessageDialog(frame, error,
								"There was an error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					GameController.this.username = username;
					showMainWindow();
					frame.dispose();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});
	}

	@GraphicalResult
	private void showNewGame(int size, String name, String[] players) {
		gameStarted = false;
		this.players = new PlayersList(players, username);
		numberOfPlayers = players.length;
		grid = new Grid(size);

		final SecondaryFrame frame = new SecondaryFrame(name);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setLocationByPlatform(true);
		gameFrame = frame;

		Container pane = frame.getContentPane();

		pane.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = c.gridy = 0;
		c.insets = new Insets(10, 10, 10, 10);
		c.weightx = 0.8;
		c.weighty = 1;

		pane.add(grid, c);

		c.weightx = 0;
		c.gridx = 1;

		pane.add(new JSeparator(SwingConstants.VERTICAL), c);

		c.gridx = 2;
		c.weightx = 0.2;

		pane.add(new JScrollPane(this.players), c);

		c.gridy = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = c.weighty = 0;

		final JButton ready = new JButton("Ready");
		ready.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					new Thread(new Runnable() {

						@Override
						public void run() {
							Pair<Boolean, String> error;
							do {
								try {
									String error1 = client.userIsReady();
									if (!error1.equals("NoError")) {
										JOptionPane.showMessageDialog(frame,
												error1, "There was an error",
												JOptionPane.ERROR_MESSAGE);
										return;
									}
									ready.setEnabled(false);
									error = client.arePlayersReady();
									if (!error.sec.equals("NoError")) {
										JOptionPane.showMessageDialog(frame,
												error.sec, "There was an error",
												JOptionPane.ERROR_MESSAGE);
										ready.setEnabled(true);
										return;
									}
								} catch (Exception exc) {
									exc.printStackTrace();
									error = new Pair<Boolean, String>(false,
											"");
								}
								try {
									Thread.sleep(500);
								} catch (Exception exc) {
									exc.printStackTrace();
								}
							} while (!error.fir);
							try {
								String error1 = client.initTheGame();
								if (!error1.equals("NoError")) {
									JOptionPane.showMessageDialog(frame, error1,
											"There was an error",
											JOptionPane.ERROR_MESSAGE);
									ready.setEnabled(true);
									return;
								}
							} catch (Exception exc) {
								exc.printStackTrace();
							}
							changeTurn();
							pane.remove(ready);
							frame.revalidate();
							frame.repaint();
							gameStarted = true;
						}
					}).start();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});

		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("Game Options");
		menu.setMnemonic('G');

		JMenuItem item = new JMenuItem("Concede");
		item.setMnemonic('C');
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispatchEvent(
						new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		menu.add(item);

		item = new JMenuItem("Suggest a draw");
		item.setMnemonic('S');
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GameController.this.players.resetDrawAccepts();
				try {
					String error = client
							.sendTheMove(new String[] { "-1 -1", "", "" });
					if (!error.equals("NoError"))
						JOptionPane.showMessageDialog(frame, error,
								"There was an error",
								JOptionPane.ERROR_MESSAGE);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});
		menu.add(item);

		menuBar.add(menu);

		frame.setJMenuBar(menuBar);
		frame.pack();
		frame.setVisible(true);

		new Thread(new Runnable() {

			@Override
			public void run() {
				Pair<ArrayList<String>, String> error;
				while (frame.isVisible() && !gameStarted) {
					try {
						error = client.getPlayers();
						if (!error.sec.equals("NoError")) {
							JOptionPane.showMessageDialog(frame, error.sec,
									"There was an error",
									JOptionPane.ERROR_MESSAGE);
							continue;
						}
						GameController.this.players.setData(
								error.fir.toArray(new String[0]), username);
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								frame.revalidate();
								frame.repaint();
							}
						});
						Thread.sleep(500);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				Pair<Boolean, String> error;
				do {
					try {
						error = client.hasSufficientNPlayers();
						if (!error.sec.equals("NoError"))
							JOptionPane.showMessageDialog(frame, error.sec,
									"There was an error",
									JOptionPane.ERROR_MESSAGE);
					} catch (Exception exc) {
						exc.printStackTrace();
						error = new Pair<Boolean, String>(false, "");
					}
					try {
						Thread.sleep(500);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				} while (!error.fir && frame.isVisible());
				pane.add(ready, c);
				frame.revalidate();
				frame.repaint();
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (frame.isVisible()) {
					try {
						Pair<Boolean, String> error = client.gotAnyNews();
						if (!error.sec.equals("NoError")) {
							JOptionPane.showMessageDialog(frame, error.sec,
									"There was an error",
									JOptionPane.ERROR_MESSAGE);
						} else if (error.fir) {
							Pair<ArrayList<ArrayList<String>>, String> error2 = client
									.getTheNews();
							if (!error2.sec.equals("NoError")) {
								JOptionPane.showMessageDialog(frame, error2.sec,
										"There was an error",
										JOptionPane.ERROR_MESSAGE);
							} else {
								for (ArrayList<String> news : error2.fir) {
									if (news.size() == 1) {
										String name = news.get(0);
										name = name.substring(0,
												name.indexOf(" left"));
										GameController.this.players
												.playerLeft(name);
									} else {
										String pos = news.get(0);
										int x = Integer
												.parseInt(pos.split(" ")[0]);
										int y = Integer
												.parseInt(pos.split(" ")[1]);

										if (x != -1 && y != -1) {
											double time = Double
													.parseDouble(news.get(1));
											GameController.this.players
													.setElapsedTime(
															GameController.this.players
																	.getTurn(),
															time);
											set(new Point(x, y));
										} else if (x == y) {
											GameController.this.players
													.resetDrawAccepts();
											int ans = JOptionPane
													.showConfirmDialog(frame,
															"A draw was suggested, do you accept it?",
															"Draw?",
															JOptionPane.YES_NO_OPTION);
											if (ans == JOptionPane.YES_OPTION) {
												String error3 = client
														.sendTheMove(
																new String[] {
																		"-1 +1",
																		"",
																		"" });
												GameController.this.players
														.increaseDrawAccepts();
												if (!error3.equals("NoError"))
													JOptionPane
															.showMessageDialog(
																	frame,
																	error3,
																	"There was an error",
																	JOptionPane.ERROR_MESSAGE);
											}
										} else {
											GameController.this.players
													.increaseDrawAccepts();
										}
									}
								}
							}
						}
						Thread.sleep(200);
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
			}
		}).start();

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					int ans = JOptionPane.showConfirmDialog(frame,
							"Do you want to concede the game?", "Concede",
							JOptionPane.YES_NO_OPTION);
					if (ans == JOptionPane.YES_OPTION) {
						String error = client.leaveTheGame();
						if (!error.equals("NoError"))
							JOptionPane.showMessageDialog(frame, error,
									"There was an error",
									JOptionPane.ERROR_MESSAGE);
						else
							frame.dispose();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});
	}

	public void findWinner() {
		win(players.getWinner());
	}

	public void win(int winner) {
		String name = players.getPlayer(winner);
		if (name.equals(username))
			JOptionPane.showMessageDialog(gameFrame, "You win!", "The end",
					JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(gameFrame, name + " wins!", "The end",
					JOptionPane.INFORMATION_MESSAGE);

		gameFrame.dispose();
		try {
			String error = client.informTerminationOfTheGame();
			if (!error.equals("NoError"))
				JOptionPane.showMessageDialog(gameFrame, error,
						"There was an error", JOptionPane.ERROR_MESSAGE);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void draw() {
		JOptionPane.showMessageDialog(gameFrame,
				"You have all agreed on a draw", "Draw",
				JOptionPane.INFORMATION_MESSAGE);
		gameFrame.dispose();
		try {
			String error = client.informTerminationOfTheGame();
			if (!error.equals("NoError"))
				JOptionPane.showMessageDialog(gameFrame, error,
						"There was an error", JOptionPane.ERROR_MESSAGE);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	@GraphicalResult(2)
	private void removeAccount(JFrame activeWindow) {
		try {

			String error = client.removeTheUser();
			if (!error.equals("NoError")) {
				JOptionPane.showMessageDialog(activeWindow, error,
						"There was an error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (activeWindow != null)
				activeWindow.dispose();
			JOptionPane.showMessageDialog(null,
					"Your account has been successfully removed, have a nice day!",
					"Account removed", JOptionPane.INFORMATION_MESSAGE);
			showSignIn();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private boolean checkUserPass(JTextField username,
			JPasswordField password) {
		boolean[] errors = new boolean[2];
		if (username.getText().equals(""))
			errors[0] = true;
		if (password.getPassword().length == 0)
			errors[1] = true;

		if (errors[0] || errors[1]) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (username) {
						synchronized (password) {
							try {
								for (int i = 0; i < 3; i++) {
									if (errors[0])
										SwingUtilities
												.invokeLater(new Runnable() {

											@Override
											public void run() {
												username.setBackground(
														ColorUtils.ERROR_COLOR);
											}
										});
									if (errors[1])
										SwingUtilities
												.invokeLater(new Runnable() {

											@Override
											public void run() {
												password.setBackground(
														ColorUtils.ERROR_COLOR);
											}
										});

									Thread.sleep(200);
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											username.setBackground(
													ColorUtils.TEXT_FIELD_DEFAULT_BACKGROUND_COLOR);
											password.setBackground(
													ColorUtils.TEXT_FIELD_DEFAULT_BACKGROUND_COLOR);
										}
									});
									Thread.sleep(200);
								}
							} catch (Exception exc) {
								exc.printStackTrace();
							}
						}
					}
				}
			}).start();
			return false;
		}
		return true;
	}

	public boolean isPlayersTurn() {
		return players.isPlayersTurn();
	}

	public void increaseScore(int score) {
		players.increaseScore(score);
	}

	public void sendMove(JComponent c, Point edge) {
		try {
			String error = client
					.sendTheMove(
							new String[] { edge.x + " " + edge.y,
									String.valueOf(players.getElapsedTime(
											players.getPlayersIndex(username))),
					"" });
			if (!error.equals("NoError"))
				JOptionPane.showMessageDialog(c, error, "There was an error",
						JOptionPane.ERROR_MESSAGE);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void set(Point p) {
		grid.mark(p);
	}

	public String getTurnsName() {
		return String.valueOf(players.getCurrentPlayer().charAt(0));
	}

	public void changeTurn() {
		players.changeTurn();
	}
}