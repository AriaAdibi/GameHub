package clientSide;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import bothSidesExceptions.CommunicationProtocolException;
import bothSidesExceptions.WrongArgsException;
import util.Pair;

/**
 * @author Aria :D It is assumed that only one thread can access this. No
 *         synchronization considered.
 *
 *         A status message is always returned: 1.NoError 2.Error:<Error's
 *         message>
 *
 *         Do call close() when you are done, close() is compatible with
 *         try-with-resources.
 */
public class ClientCommunicator implements Closeable {

	private Socket			clientSocket	= null;
	private PrintWriter		pWriter			= null;
	private BufferedReader	bReader			= null;
	private Scanner			sc				= null;

	private boolean			closed			= true;

	public ClientCommunicator(String host, int port)
			throws UnknownHostException, IOException {
		this.clientSocket = new Socket(host, port);
		this.pWriter = new PrintWriter(this.clientSocket.getOutputStream(),
				true);
		this.bReader = new BufferedReader(
				new InputStreamReader(this.clientSocket.getInputStream()));
		this.sc = new Scanner(this.bReader);

		this.closed = false;
	}

	/**
	 * @param userInfo
	 *            [4] 0-name 1-age 2-username 3-password
	 * @return Was it successful? If not, what was the error? After user creates
	 *         new account, he/she must signIn.
	 * @throws WrongArgsException
	 * @throws CommunicationProtocolException
	 */
	public synchronized String createNewUser(String userInfo[])
			throws WrongArgsException, CommunicationProtocolException {
		if (userInfo.length != 4) {
			System.err.println("Error in createNewUser: size!=4");
			throw new WrongArgsException("Error in createNewUser: size!=4");
		}

		this.pWriter.println("createNewUser()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in createNewUser: theAck from server");
			throw new CommunicationProtocolException(
					"Error in createNewUser: theAck from server");
		}

		for (String str : userInfo) {
			this.pWriter.println(str);
		}

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	/**
	 * @param username
	 * @param password
	 * @return Was it successful? If not, what was the error?
	 * @throws CommunicationProtocolException
	 */
	public synchronized String signIn(String username, String password)
			throws CommunicationProtocolException {
		this.pWriter.println("signIn()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in signIn: theAck from server");
			throw new CommunicationProtocolException(
					"Error in signIn: theAck from server");
		}

		this.pWriter.println(username);
		this.pWriter.println(password);

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	/**
	 * @return Was it successful? If not, what was the error?
	 * 
	 *         When this method invoked, it is assumed that the
	 *         serverCommunicator knows the user (that user signedIn before). If
	 *         not error is returned.
	 * 
	 * @throws CommunicationProtocolException
	 */
	public synchronized String signOut() throws CommunicationProtocolException {
		this.pWriter.println("signOut()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in signOut: theAck from server");
			throw new CommunicationProtocolException(
					"Error in signOut: theAck from server");
		}

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	/**
	 * @return Was it successful? If not, what was the error?
	 * 
	 *         When this method invoked, it is assumed that the
	 *         serverCommunicator knows the user (that user signedIn before). If
	 *         not error is returned.
	 * 
	 * @throws CommunicationProtocolException
	 */
	public synchronized String removeTheUser()
			throws CommunicationProtocolException {
		this.pWriter.println("removeTheUser()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in removeTheUser: theAck from server");
			throw new CommunicationProtocolException(
					"Error in removeTheUser: theAck from server");
		}

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	/**
	 * @return .first <username-state> (- is space) state: -1 = offline 0 =
	 *         playing 1 = waiting .second Was it successful? If not, what was
	 *         the error? If controllable error occurs in server, server still
	 *         will send info to satisfy nUsers messages. But the Error is
	 *         returned.
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<ArrayList<String>, String> getUsers()
			throws CommunicationProtocolException {
		this.pWriter.println("getUsers()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in getUsers: theAck from server");
			throw new CommunicationProtocolException(
					"Error in getUsers: theAck from server");
		}

		ArrayList<String> users = new ArrayList<String>();
		int nUsers = this.sc.nextInt();
		this.sc.nextLine();
		for (int i = 0; i < nUsers; i++) {
			String username = this.sc.next();
			String state = this.sc.nextLine().trim();
			users.add(username + " " + state);
		}

		String errorMessage = this.sc.nextLine();

		return new Pair<ArrayList<String>, String>(users, errorMessage);
	}

	/**
	 * @return .first <name-id> (- is space) .second Was it successful? If not,
	 *         what was the error? If controllable error occurred in server,
	 *         server still send info to satisfy nWGames messages. But the Error
	 *         is returned.
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<ArrayList<String>, String> getWaitingGames()
			throws CommunicationProtocolException {
		this.pWriter.println("getWaitingGames()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in getWaitingGames: theAck from server");
			throw new CommunicationProtocolException(
					"Error in getWaitingGames: theAck from server");
		}

		ArrayList<String> waitingGames = new ArrayList<String>();
		int nWGames = this.sc.nextInt();
		this.sc.nextLine();
		for (int i = 0; i < nWGames; i++) {
			String name = this.sc.next();
			String id = this.sc.nextLine();
			waitingGames.add(name + " " + id);
		}

		String errorMessage = this.sc.nextLine();

		return new Pair<ArrayList<String>, String>(waitingGames, errorMessage);
	}

	/**
	 * @return .first n (-1 if not successful) .second Was it successful? If
	 *         not, what was the error?
	 *
	 *         When this method invoked, it is assumed that the
	 *         serverCommunicator knows the user (that user signedIn before). If
	 *         not error is returned.
	 * 
	 *         for getting information about players use "getPlayers()"
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<Integer, String> joinTheGame(int gameID)
			throws CommunicationProtocolException {
		this.pWriter.println("joinTheGame()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in joinTheGame: theAck from server");
			throw new CommunicationProtocolException(
					"Error in joinTheGame: theAck from server");
		}

		this.pWriter.println(gameID);

		int n = this.sc.nextInt();
		this.sc.nextLine();
		String errorMessage = this.sc.nextLine();

		return new Pair<Integer, String>(n, errorMessage);
	}

	/**
	 * When this method invoked, it is assumed that the serverCommunicator knows
	 * the user (that user signedIn before). If not error is returned.
	 * 
	 * @param nameOfTheGame
	 * @param requiredNPlayers
	 * @return .first n .second Was it successful? If not, what was the error?
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<Integer, String> createADLGame(
			String nameOfTheGame, int requiredNPlayers)
					throws CommunicationProtocolException {
		this.pWriter.println("createAGame()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in createAGame: theAck from server");
			throw new CommunicationProtocolException(
					"Error in createAGame: theAck from server");
		}

		this.pWriter.println(nameOfTheGame);
		this.pWriter.println(requiredNPlayers);

		int n = this.sc.nextInt();
		this.sc.nextLine();
		String errorMessage = this.sc.nextLine();

		return new Pair<Integer, String>(n, errorMessage);
	}

	/**
	 * @return .first Answer to "Has sufficient number of players?" .second Was
	 *         it successful? If not, what was the error? It is assumed that
	 *         server knows the user and the game which this user is in. If
	 *         either is empty error is returned.
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<Boolean, String> hasSufficientNPlayers()
			throws CommunicationProtocolException {
		this.pWriter.println("hasSufficientNPlayers()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println(
					"Error in hasSufficientNPlayers: theAck from server");
			throw new CommunicationProtocolException(
					"Error in hasSufficientNPlayers: theAck from server");
		}

		boolean b = this.sc.nextBoolean();
		this.sc.nextLine();
		String errorMessage = this.sc.nextLine();

		return new Pair<Boolean, String>(b, errorMessage);
	}

	/**
	 * @return Was it successful? If not, what was the error? It is assumed that
	 *         server knows the user and the game which this user is in. If
	 *         either is empty error is returned.
	 * @throws CommunicationProtocolException
	 */
	public synchronized String leaveTheGame()
			throws CommunicationProtocolException {
		this.pWriter.println("leaveTheGame()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in leaveTheGame: theAck from server");
			throw new CommunicationProtocolException(
					"Error in leaveTheGame: theAck from server");
		}

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	/**
	 * @return Was it successful? If not, what was the error? It is assumed that
	 *         server knows the user and the game which this user is in. If
	 *         either is empty error is returned.
	 * 
	 *         If the number of players is not sufficient, proper error will
	 *         returned.
	 * 
	 *         This method must called when all players say they are ready.
	 *         (NOTE: The ready buttons must appear only if sufficient number of
	 *         players are present.)
	 * @throws CommunicationProtocolException
	 */
	public synchronized String initTheGame()
			throws CommunicationProtocolException {
		this.pWriter.println("initTheGame()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in initTheGame: theAck from server");
			throw new CommunicationProtocolException(
					"Error in initTheGame: theAck from server");
		}

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	/**
	 * @return Was it successful? If not, what was the error?
	 * 
	 *         When this method invoked, it is assumed that the
	 *         serverCommunicator knows the user (that user signedIn before). If
	 *         not error is returned.
	 * 
	 * @throws CommunicationProtocolException
	 */
	public synchronized String userIsReady()
			throws CommunicationProtocolException {
		this.pWriter.println("userIsReady()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in userIsReady: theAck from server");
			throw new CommunicationProtocolException(
					"Error in userIsReady: theAck from server");
		}

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	/**
	 * @return .first Answer to "Are players ready?". .second Was it successful?
	 *         If not, what was the error?
	 * 
	 *         It is assumed that server knows the user and the game which this
	 *         user is in. If either is empty error is returned.
	 * 
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<Boolean, String> arePlayersReady()
			throws CommunicationProtocolException {
		this.pWriter.println("arePlayersReady()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in arePlayersReady: theAck from server");
			throw new CommunicationProtocolException(
					"Error in arePlayersReady: theAck from server");
		}

		boolean b = this.sc.nextBoolean();
		this.sc.nextLine();
		String errorMessage = this.sc.nextLine();

		return new Pair<Boolean, String>(b, errorMessage);
	}

	/**
	 * @return .first List of players .second Was it successful? If not, what
	 *         was the error?
	 * 
	 *         It is assumed that server knows the user and the game which this
	 *         user is in. If either is empty error is returned.
	 * 
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<ArrayList<String>, String> getPlayers()
			throws CommunicationProtocolException {
		this.pWriter.println("getPlayers()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in getPlayers: theAck from server");
			throw new CommunicationProtocolException(
					"Error in getPlayers: theAck from server");
		}

		ArrayList<String> players = new ArrayList<String>();
		int nPlayers = this.sc.nextInt();
		this.sc.nextLine();
		for (int i = 0; i < nPlayers; i++) {
			String username = this.sc.nextLine();
			players.add(username);
		}

		String errorMessage = this.sc.nextLine();

		return new Pair<ArrayList<String>, String>(players, errorMessage);
	}

	/**
	 * @return .first Answer to "Got any news?". .second Was it successful? If
	 *         not, what was the error?
	 * 
	 *         It is assumed that server knows the user and the game which this
	 *         user is in. If either is empty error is returned.
	 * 
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<Boolean, String> gotAnyNews()
			throws CommunicationProtocolException {
		this.pWriter.println("gotAnyNews()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in gotAnyNews: theAck from server");
			throw new CommunicationProtocolException(
					"Error in gotAnyNews: theAck from server");
		}

		boolean b = this.sc.nextBoolean();
		this.sc.nextLine();
		String errorMessage = this.sc.nextLine();

		return new Pair<Boolean, String>(b, errorMessage);
	}

	/**
	 * @return .first List of News' items. .second Was it successful? If not,
	 *         what was the error?
	 * 
	 *         It is assumed that server knows the user and the game which this
	 *         user is in. If either is empty error is returned.
	 * 
	 *         Check in with gotAnyNews precedent to invoking this.
	 * 
	 *         2 kinds of news: 1. someone left in <username-left> (- is space)
	 *         2. information of the moves.
	 *
	 * @throws CommunicationProtocolException
	 */
	public synchronized Pair<ArrayList<ArrayList<String>>, String> getTheNews()
			throws CommunicationProtocolException {
		this.pWriter.println("getTheNews()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in getTheNews: theAck from server");
			throw new CommunicationProtocolException(
					"Error in getTheNews: theAck from server");
		}

		ArrayList<ArrayList<String>> news = new ArrayList<ArrayList<String>>();
		int nNews = this.sc.nextInt();
		this.sc.nextLine();
		for (int i = 0; i < nNews; i++) {
			int len = this.sc.nextInt();
			this.sc.nextLine();
			ArrayList<String> aNewsItem = new ArrayList<String>();
			for (int j = 0; j < len; j++) {
				String str = this.sc.nextLine();
				aNewsItem.add(str);
			}
			news.add(aNewsItem);
		}

		String errorMessage = this.sc.nextLine();

		return new Pair<ArrayList<ArrayList<String>>, String>(news,
				errorMessage);
	}

	/**
	 * @param moveInfo
	 *            [3] 0-the line 1-elapse time 2-mouse info Client side must
	 *            check the validity of the move.
	 *
	 *            It is assumed that server knows the user and the game which
	 *            this user is in. If either is empty error is returned.
	 *
	 * @return Was it successful? If not, what was the error?
	 * @throws WrongArgException
	 * @throws CommunicationProtocolException
	 */
	public synchronized String sendTheMove(String moveInfo[])
			throws WrongArgsException, CommunicationProtocolException {
		if (moveInfo.length != 3) {
			System.err.println("Error in sendTheMove: size!=3");
			throw new WrongArgsException("Error in sendTheMove: size!=3");
		}

		this.pWriter.println("sendTheMove()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println("Error in sendTheMove: theAck from server");
			throw new CommunicationProtocolException(
					"Error in sendTheMove: theAck from server");
		}

		for (String str : moveInfo) {
			this.pWriter.println(str);
		}

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	/**
	 * Every remaining players should invoke this when the game finished. It is
	 * assumed that server knows the user and the game which this user is in. If
	 * either is empty error is returned.
	 * 
	 * @throws CommunicationProtocolException
	 */
	public synchronized String informTerminationOfTheGame()
			throws CommunicationProtocolException {
		this.pWriter.println("informTerminationOfTheGame()");
		String theAck = this.sc.nextLine();
		if (!theAck.equals("OK")) {
			System.err.println(
					"Error in informTerminationOfTheGame: theAck from server");
			throw new CommunicationProtocolException(
					"Error in informTerminationOfTheGame: theAck from server");
		}

		String errorMessage = this.sc.nextLine();

		return errorMessage;
	}

	@Override
	public synchronized void close() throws IOException {
		if (!this.closed) {
			this.pWriter.println("close()");
			String theAck = this.sc.nextLine();
			if (!theAck.equals("OK")) {
				System.err.println("Error in close: theAck from server");
				System.exit(-1);
			}

			this.sc.close();
			this.bReader.close();
			this.pWriter.close();
			this.clientSocket.close();
			this.closed = true;
		}
	}

}