package serverSide;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import bothSidesExceptions.WrongArgsException;
import serverSide.Game;
import serverSide.SomethingIsWrongException;
import serverSide.User;

public class ServerCommunicator implements Runnable, Closeable {

	private Socket				socket				= null;

	private PrintWriter			pWriter				= null;
	private BufferedReader		bReader				= null;
	private Scanner				sc					= null;

	private User				theUser				= null;

	private static final int	USER_INFO_LENGTH	= 4;
	private static final int	MOVE_INFO_LENGTH	= 3;

	private boolean				closed				= true;

	public ServerCommunicator(Socket socket) {
		this.socket = socket;

		try {
			this.pWriter = new PrintWriter(this.socket.getOutputStream(), true);
			this.bReader = new BufferedReader(
					new InputStreamReader(this.socket.getInputStream()));
			this.sc = new Scanner(bReader);
		} catch (IOException e) {
			System.err.println("Can not create reader/writer.");
			e.printStackTrace();
			System.exit(-1);
		}

		this.theUser = null;
		this.closed = false;
	}

	private void sendTheErrorMessage(String errorMessage) {
		if (errorMessage == null)
			errorMessage = "NoError";
		else
			errorMessage = "Error: " + errorMessage;

		this.pWriter.println(errorMessage);
	}

	private void hCreateNewUser() {
		String errorMessage = null;

		ArrayList<String> userInfo = new ArrayList<String>(USER_INFO_LENGTH);
		for (int i = 0; i < USER_INFO_LENGTH; i++)
			userInfo.add(this.sc.nextLine());

		try {
			User.createNewUser(userInfo.toArray(new String[USER_INFO_LENGTH]));
		} catch (WrongArgsException e) {
			System.err.println("WrongArgsEX in hCreateNewUser");
			e.printStackTrace();
			errorMessage = e.getMessage();
		} catch (SomethingIsWrongException e) {
			System.err.println("SomethingIsWrongEX in hCreateNewUser");
			e.printStackTrace();
			errorMessage = e.getMessage();
		} catch (IOException e) {
			System.err.println("IOEX in hCreateNewUser");
			e.printStackTrace();
			errorMessage = e.getMessage();
		}

		sendTheErrorMessage(errorMessage);
	}

	private void hSignIn() {
		String errorMessage = null;

		String username = this.sc.nextLine();
		String password = this.sc.nextLine();

		try {
			this.theUser = User.signIn(username, password);// TODO caution
		} catch (SomethingIsWrongException e) {
			System.err.println("SomethingIsWrongEX in hSignIn");
			e.printStackTrace();
			errorMessage = e.getMessage();
		}

		sendTheErrorMessage(errorMessage);
	}

	private void hSignOut() {
		String errorMessage = null;

		if (this.theUser == null) {
			System.err.println("User is unkown - hSignOut");
			errorMessage = "User is unkown - hSignOut";
		} else {
			try {
				this.theUser.signOut();
			} catch (SomethingIsWrongException e) {
				System.err.println("SomethingIsWrongEX in hSignOut");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		sendTheErrorMessage(errorMessage);
	}

	private void hRemoveTheUser() {
		String errorMessage = null;

		if (this.theUser == null) {
			System.err.println("User is unkown in hRemoveTheUser");
			errorMessage = "User is unkown in hRemoveTheUser";
		} else {
			try {
				this.theUser.removeUser();
			} catch (SomethingIsWrongException e) {
				System.err.println("SomethingIsWrongEX in hRemoveTheUser");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		sendTheErrorMessage(errorMessage);
	}

	private void hGetUsers() {
		String errorMessage = null;

		Collection<User> uC = User.getUsers();

		this.pWriter.println(uC.size());
		for (User u : uC)
			this.pWriter.println(u.getUsername() + " " + u.getState());

		sendTheErrorMessage(errorMessage);
	}

	private void hGetWaitingGames() {
		String errorMessage = null;

		Collection<Game> gC = Game.getWaitingGames();

		this.pWriter.println(gC.size());
		for (Game g : gC)
			this.pWriter.println(g.getName() + " " + g.getID());

		sendTheErrorMessage(errorMessage);
	}

	private void hJoinTheDLGame() {
		String errorMessage = null;

		DotsAndBoxesGame theGame = null;
		int gameID = this.sc.nextInt();
		this.sc.nextLine();

		if (this.theUser == null) {
			System.err.println("User is unkown - hJoinTheGame");
			errorMessage = "User is unkown - hJoinTheGame";
		} else {
			try {
				this.theUser.joinTheGame(gameID);

				theGame = (DotsAndBoxesGame) this.theUser.getGame();// TODO
			} catch (SomethingIsWrongException e) {
				System.err.println("SomethingIsWrongEX in hJoinTheGame");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		if (theGame == null) // just send some int ( -1 )
			this.pWriter.println(-1);
		else
			this.pWriter.println(theGame.getN());
		sendTheErrorMessage(errorMessage);
	}

	private void hCreateADLGame() {
		String errorMessage = null;

		DotsAndBoxesGame theGame = null;
		String nameOfTheGame = this.sc.nextLine();
		int requiredNPlayers = this.sc.nextInt();
		this.sc.nextLine();

		if (this.theUser == null) {
			System.err.println("User is unkown - hCreateAGame");
			errorMessage = "User is unkown - hCreateAGame";
		} else {
			try {
				this.theUser.createADLGame(nameOfTheGame, requiredNPlayers);

				theGame = (DotsAndBoxesGame) this.theUser.getGame();// TODO
			} catch (SomethingIsWrongException e) {
				System.err.println("SomethingIsWrongEX in hCreateADLGame");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		if (theGame == null) // just send some int ( -1 )
			this.pWriter.println(-1);
		else
			this.pWriter.println(theGame.getN());
		sendTheErrorMessage(errorMessage);
	}

	private void hHasSufficientNPlayers() {
		String errorMessage = null;

		int hasSNP = -1; // 0-false 1-true
		if (this.theUser == null) {
			System.err.println("User is unkown - hHasSufficientNPlayers");
			errorMessage = "User is unkown - hHasSufficientNPlayers";
		} else {
			if (this.theUser.getGame() == null) {
				System.err.println(
						"User is in no game! - hHasSufficientNPlayers");
				errorMessage = "User is in no game! - hHasSufficientNPlayers";
			} else {
				Game theGame = this.theUser.getGame();
				try {
					if (theGame.hasSufficientNPlayers())
						hasSNP = 1;
					else
						hasSNP = 0;
				} catch (SomethingIsWrongException e) {
					System.err.println(
							"SomethingIsWrongEX in hHasSufficientNPlayers");
					e.printStackTrace();
					errorMessage = e.getMessage();
				}
			}
		}

		if (hasSNP == -1) // just send some boolean ( false )
			this.pWriter.println(false);
		else if (hasSNP == 0)
			this.pWriter.println(false);
		else if (hasSNP == 1)
			this.pWriter.println(true);
		else {
			System.err.println("Error in hHasSufficientNPlayers!");
			System.exit(-1);
		}
		sendTheErrorMessage(errorMessage);
	}

	private void hLeaveTheGame() {
		String errorMessage = null;

		if (this.theUser == null) {
			System.err.println("User is unkown - hLeaveTheGame");
			errorMessage = "User is unkown - hLeaveTheGame";
		} else {
			try {
				this.theUser.leaveTheGame();
			} catch (SomethingIsWrongException e) {
				System.err.println("SomethingIsWrongEX in hLeaveTheGame");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		sendTheErrorMessage(errorMessage);
	}

	private void hInitTheGame() {
		String errorMessage = null;

		if (this.theUser == null) {
			System.err.println("User is unkown - hInitTheGame");
			errorMessage = "User is unkown - hInitTheGame";
		} else {
			if (this.theUser.getGame() == null) {
				System.err.println("User is in no game! - hInitTheGame");
				errorMessage = "User is in no game! - hInitTheGame";
			} else {
				Game theGame = this.theUser.getGame();
				try {
					theGame.initTheGame();
				} catch (SomethingIsWrongException e) {
					System.err.println("SomethingIsWrongEX in hInitTheGame");
					e.printStackTrace();
					errorMessage = e.getMessage();
				}
			}
		}

		sendTheErrorMessage(errorMessage);
	}

	private void hUserIsReady() {
		String errorMessage = null;

		if (this.theUser == null) {
			System.err.println("User is unkown - hUserIsReady");
			errorMessage = "User is unkown - hUserIsReady";
		} else {
			try {
				this.theUser.setIsReady();
			} catch (SomethingIsWrongException e) {
				System.err.println("SomethingIsWrongEX in hUserIsReady");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		sendTheErrorMessage(errorMessage);
	}

	private void hArePlayersReady() {
		String errorMessage = null;

		int arePsReady = -1; // 0-false 1-true
		if (this.theUser == null) {
			System.err.println("User is unkown - hArePlayersReady");
			errorMessage = "User is unkown - hArePlayersReady";
		} else {
			if (this.theUser.getGame() == null) {
				System.err.println("User is in no game! - hArePlayersReady");
				errorMessage = "User is in no game! - hArePlayersReady";
			} else {
				Game theGame = this.theUser.getGame();
				if (theGame.arePlayersReady())
					arePsReady = 1;
				else
					arePsReady = 0;
			}
		}

		if (arePsReady == -1) // just send some boolean ( false )
			this.pWriter.println(false);
		else if (arePsReady == 0)
			this.pWriter.println(false);
		else if (arePsReady == 1)
			this.pWriter.println(true);
		else {
			System.err.println("Error in hArePlayersReady!");
			System.exit(-1);
		}
		sendTheErrorMessage(errorMessage);
	}

	private void hGetPlayers() {
		String errorMessage = null;

		Collection<User> pC = null;
		if (this.theUser == null) {
			System.err.println("User is unkown - hGetPlayers");
			errorMessage = "User is unkown - hGetPlayers";
		} else {
			if (this.theUser.getGame() == null) {
				System.err.println("User is in no game! - hGetPlayers");
				errorMessage = "User is in no game! - hGetPlayers";
			} else {
				Game theGame = this.theUser.getGame();
				pC = theGame.getPlayers();
			}
		}

		if (pC == null) { // just write something
			this.pWriter.println(-1);
			// no need for names due to -1.
		} else {
			this.pWriter.println(pC.size());
			for (User u : pC)
				this.pWriter.println(u.getUsername());
		}
		sendTheErrorMessage(errorMessage);
	}

	private void hGotAnyNews() {
		String errorMessage = null;

		int gotNews = -1; // 0-false 1-true
		if (this.theUser == null) {
			System.err.println("User is unkown - hGotAnyNews");
			errorMessage = "User is unkown - hGotAnyNews";
		} else {
			try {
				if (this.theUser.gotAnyNews())
					gotNews = 1;
				else
					gotNews = 0;
			} catch (SomethingIsWrongException e) {
				System.err.println("SomethingIsWrongEX in hGotAnyNews");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		if (gotNews == -1) // just send some boolean ( false )
			this.pWriter.println(false);
		else if (gotNews == 0)
			this.pWriter.println(false);
		else if (gotNews == 1)
			this.pWriter.println(true);
		else {
			System.err.println("Error in hGotAnyNews!");
			System.exit(-1);
		}
		sendTheErrorMessage(errorMessage);
	}

	private void hGetTheNews() {
		String errorMessage = null;

		Collection<ArrayList<String>> newsC = null;
		if (this.theUser == null) {
			System.err.println("User is unkown - hGetTheNews");
			errorMessage = "User is unkown - hGetTheNews";
		} else {
			try {
				newsC = this.theUser.getTheNews();
			} catch (SomethingIsWrongException e) {
				System.err.println("SomethingIsWrongEX in hGetTheNews");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		if (newsC == null) { // just write something
			this.pWriter.println(-1);
			// no need for other due to -1.
		} else {
			this.pWriter.println(newsC.size());
			for (ArrayList<String> aNewsItem : newsC) {
				this.pWriter.println(aNewsItem.size());
				for (String str : aNewsItem)
					this.pWriter.println(str);
			}
		}
		sendTheErrorMessage(errorMessage);
	}

	private void hSendTheMove() {
		String errorMessage = null;

		ArrayList<String> theMoveInfo = new ArrayList<String>(MOVE_INFO_LENGTH);
		for (int i = 0; i < MOVE_INFO_LENGTH; i++)
			theMoveInfo.add(this.sc.nextLine());

		if (this.theUser == null) {
			System.err.println("User is unkown - hSendTheMove");
			errorMessage = "User is unkown - hSendTheMove";
		} else {
			if (this.theUser.getGame() == null) {
				System.err.println("User is in no game! - hSendTheMove");
				errorMessage = "User is in no game! - hSendTheMove";
			} else {
				Game theGame = this.theUser.getGame();
				try {
					theGame.echoTheMoveInOrder(this.theUser,
							theMoveInfo.toArray(new String[MOVE_INFO_LENGTH]));
				} catch (WrongArgsException e) {
					System.err.println("SomethingIsWrongEX in hSendTheMove");
					e.printStackTrace();
					errorMessage = e.getMessage();
				}
			}
		}

		sendTheErrorMessage(errorMessage);
	}

	private void hInformTerminationOfTheGame() {
		String errorMessage = null;

		if (this.theUser == null) {
			System.err.println("User is unkown - hInformTerminationOfTheGame");
			errorMessage = "User is unkown - hInformTerminationOfTheGame";
		} else {
			try {
				this.theUser.informTerminationOfTheGame();
			} catch (SomethingIsWrongException e) {
				System.err.println(
						"SomethingIsWrongEX in hInformTerminationOfTheGame");
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		}

		sendTheErrorMessage(errorMessage);
	}

	@Override
	public void run() {
		W:
		while (this.sc.hasNextLine()) {
			String req = this.sc.nextLine(); // this do the waiting
			switch (req) {
				case "createNewUser()":
					this.pWriter.println("OK");
					hCreateNewUser();
					break;

				case "signIn()":
					this.pWriter.println("OK");
					hSignIn();
					break;

				case "signOut()":
					this.pWriter.println("OK");
					hSignOut();
					break;

				case "removeTheUser()":
					this.pWriter.println("OK");
					hRemoveTheUser();
					break;

				case "getUsers()":
					this.pWriter.println("OK");
					hGetUsers();
					break;

				case "getWaitingGames()":
					this.pWriter.println("OK");
					hGetWaitingGames();
					break;

				case "joinTheGame()":
					this.pWriter.println("OK");
					hJoinTheDLGame();
					break;

				case "createAGame()":
					this.pWriter.println("OK");
					hCreateADLGame();
					break;

				case "hasSufficientNPlayers()":
					this.pWriter.println("OK");
					hHasSufficientNPlayers();
					break;

				case "leaveTheGame()":
					this.pWriter.println("OK");
					hLeaveTheGame();
					break;

				case "initTheGame()":
					this.pWriter.println("OK");
					hInitTheGame();
					break;

				case "userIsReady()":
					this.pWriter.println("OK");
					hUserIsReady();
					break;

				case "arePlayersReady()":
					this.pWriter.println("OK");
					hArePlayersReady();
					break;

				case "getPlayers()":
					this.pWriter.println("OK");
					hGetPlayers();
					break;

				case "gotAnyNews()":
					this.pWriter.println("OK");
					hGotAnyNews();
					break;

				case "getTheNews()":
					this.pWriter.println("OK");
					hGetTheNews();
					break;

				case "sendTheMove()":
					this.pWriter.println("OK");
					hSendTheMove();
					break;

				case "informTerminationOfTheGame()":
					this.pWriter.println("OK");
					hInformTerminationOfTheGame();
					break;

				case "close()":
					this.pWriter.println("OK");
					break W;

				default:
					this.pWriter.println("NotOK");
					break;
			}
		}

		try {
			close();
		} catch (IOException e) {
			System.err.println("Can not close resources in ServerCommunicator");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public void close() throws IOException {
		if (!this.closed) {
			this.sc.close();
			this.bReader.close();
			this.pWriter.close();
			this.socket.close();
			this.closed = true;
		}
	}

}