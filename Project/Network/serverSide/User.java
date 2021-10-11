package serverSide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import bothSidesExceptions.WrongArgsException;
import serverSide.Game;
import serverSide.SomethingIsWrongException;
import serverSide.User;

public class User {

	private String							name			= null;
	private int								age				= 0;
	private String							username		= null,
			password = null;												// usernames
																			// must
																			// be
																			// unique

	private Game							game			= null;
	private ArrayList<ArrayList<String>>	news			= null;			// TODO
	private final Object					lockGameInfo	= new Object();

	/**
	 * -1 = offline 0 = playing 1 = waiting
	 */
	private int								state			= -1;

	private boolean							isReady			= false;

	public User(String name, int age, String username, String password)
			throws IOException {
		this.name = name;
		this.age = age;
		this.username = username;
		this.password = password;

		this.game = null;
		this.news = null;
		this.state = -1;
		this.isReady = false;
	}

	public synchronized String getName() {
		return this.name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized int getAge() {
		return this.age;
	}

	public synchronized void setAge(int age) {
		this.age = age;
	}

	public synchronized String getUsername() {
		return this.username;
	}

	public synchronized void setUsername(String username) {
		this.username = username;
	}

	public synchronized String getPassword() {
		return this.password;
	}

	public synchronized void setPassword(String password) {
		this.password = password;
	}

	public synchronized Game getGame() {
		synchronized (this.lockGameInfo) {
			return this.game;
		}
	}

	public synchronized boolean getIsReady() {
		return isReady;
	}

	public synchronized void setIsReady() throws SomethingIsWrongException {
		if (this.game == null)// TODO maybe no need
			throw new SomethingIsWrongException(
					"Error occurred in setting the ready status: "
							+ "User have not joined any game yet! - username= "
							+ this.username);
		this.isReady = true;
	}

	public synchronized void resetIsReady() {
		this.isReady = false;// TODO check this.game
	}

	public synchronized int getState() {
		return this.state;
	}

	public synchronized void setState(int s) {
		if (s < -1 || s > 1) {
			System.err.println("Error in setState: Worng state number");
			System.exit(-1);
		}
		this.state = s;
	}

	// not checked if it's in users or not.
	public synchronized void signOut() throws SomethingIsWrongException {
		if (this.state == -1)
			throw new SomethingIsWrongException(
					"Error occurred in signing out: "
							+ "User is already offline - username= "
							+ this.username);
		this.state = -1;
	}

	// user must not be in any game.
	// I am careful about deadlock; I don't break the order.
	public synchronized void removeUser() throws SomethingIsWrongException {
		if (this.game != null)
			throw new SomethingIsWrongException(
					"Error occurred in removing an account: "
							+ "User must not be in any game - username= "
							+ this.username);
		if (User.doesTheUserExist(this.username)) { // locks the User.users
			synchronized (User.users) {
				if (!User.users.remove(this))
					throw new SomethingIsWrongException(
							"Error occurred in removing an account: "
									+ "Can not remove - username= "
									+ this.username);
			}
		} else
			throw new SomethingIsWrongException(
					"Error occurred in removing an account: "
							+ "Username not found - username= "
							+ this.username);
	}

	// I am careful about deadlock; I don't break the order.
	public synchronized void leaveTheGame() throws SomethingIsWrongException {
		synchronized (this.lockGameInfo) {
			int nP = this.game.getNPlayers();
			if (nP < 1)
				throw new SomethingIsWrongException(
						"Error occurred in leaving the game: "
								+ "Number of players in the game is less than 1!! - username= "
								+ this.username);
			else if (nP == 1)
				this.informTerminationOfTheGame();
			else {
				if (!this.game.removeThePlayer(this))
					throw new SomethingIsWrongException(
							"Error occurred in leaving the game: "
									+ "Can not leave - username= "
									+ this.username);
				else {
					this.isReady = false;
					this.state = 1;
					this.game = null;
					this.news = null;
				}
			}
		}
	}

	// I am careful about deadlock; I don't break the order.
	public synchronized void informTerminationOfTheGame()
			throws SomethingIsWrongException {
		synchronized (this.lockGameInfo) {
			if (this.game == null)
				throw new SomethingIsWrongException(
						"Error occured in terminating the game: "
								+ "User has not joined in any game! - username= "
								+ this.username);

			if (!this.game.terminateTheGame())
				throw new SomethingIsWrongException(
						"Error occured in terminating the game: "
								+ "Can not terminate - username= "
								+ this.username);
			else {
				this.isReady = false;
				this.state = 1;
				this.game = null;
				this.news = null;
			}
		}
	}

	// I am careful about deadlock; I don't break the order.
	public synchronized void createADLGame(String name, int requiredNPlayers)
			throws SomethingIsWrongException {
		synchronized (this.lockGameInfo) {
			if (this.game != null)
				throw new SomethingIsWrongException(
						"Error occured in creating new game: "
								+ "User is in a game - username= "
								+ this.username);

			Game theGame = Game.createADLGame(this, name, requiredNPlayers);

			this.game = theGame;
			this.news = new ArrayList<ArrayList<String>>();// TODO
		}
	}

	// I am careful about deadlock; I don't break the order.
	public synchronized void joinTheGame(int gameID)
			throws SomethingIsWrongException {
		synchronized (this.lockGameInfo) {
			if (this.game != null)
				throw new SomethingIsWrongException(
						"Error occured in joining the game: "
								+ "User have already joined a game - username= "
								+ this.username);

			Game theGame = Game.findTheGame(gameID);
			if (theGame == null)
				throw new SomethingIsWrongException(
						"Error occured in joining the game: "
								+ "Wrong game ID - username= " + this.username);
			synchronized (theGame) {
				if (theGame.hasSufficientNPlayers())
					throw new SomethingIsWrongException(
							"Error occured in joining the game: "
									+ "Game is full - username= "
									+ this.username);

				theGame.addAPlayer(this);
				this.game = theGame;
				this.news = new ArrayList<ArrayList<String>>();
			}
		}
	}

	public boolean gotAnyNews() throws SomethingIsWrongException {
		synchronized (this.lockGameInfo) {
			if (this.game == null)// TODO maybe no need
				throw new SomethingIsWrongException(
						"Error occurred in checking for news: "
								+ "User have not joined any game yet! - username= "
								+ this.username);
			return (!this.news.isEmpty());
		}
	}

	public ArrayList<ArrayList<String>> getTheNews()
			throws SomethingIsWrongException {
		synchronized (this.lockGameInfo) {
			if (this.game == null)// TODO maybe no need
				throw new SomethingIsWrongException(
						"Error occurred in getting the news: "
								+ "User have not joined any game yet! - username= "
								+ this.username);
			@SuppressWarnings("unchecked")
			ArrayList<ArrayList<String>> theNews = (ArrayList<ArrayList<String>>) this.news
					.clone(); // TODO
			this.news.clear();
			return theNews;
		}
	}

	public boolean addANewsItem(ArrayList<String> theNewsItem) {
		synchronized (this.lockGameInfo) {
			return this.news.add(theNewsItem);
		}
	}

	// statics:
	public static Collection<User> users = new ArrayList<User>(); // TODO

	// @param userInfo [4] 0-name 1-age 2-username 3-password
	// I am careful about deadlock; I don't break the order.
	public static void createNewUser(String userInfo[])
			throws WrongArgsException, SomethingIsWrongException, IOException {
		if (userInfo.length != 4)
			throw new WrongArgsException(
					"User.createNewUser: userInfo.length != 4");

		if (!User.doesTheUserExist(userInfo[2])) {
			User newUser = new User(userInfo[0], Integer.parseInt(userInfo[1]),
					userInfo[2], userInfo[3]);
			synchronized (User.users) {
				User.users.add(newUser);
			}
		} else
			throw new SomethingIsWrongException(
					"Error occured in creating new user: "
							+ "This username already exists.");
	}

	// I am careful about deadlock; I don't break the order.
	public static User signIn(String username, String password)
			throws SomethingIsWrongException {
		User theUser = User.findTheUser(username);
		if (theUser == null)
			throw new SomethingIsWrongException("Error occured in signing in: "
					+ "This username does not exist.");

		synchronized (theUser) {
			if (!theUser.getPassword().equals(password))
				throw new SomethingIsWrongException(
						"Error occured in signing in: " + "Wrong Password.");
			theUser.state = 1;
			return theUser;
		}
	}

	@SuppressWarnings("unchecked")
	public static Collection<User> getUsers() {
		synchronized (User.users) {
			return (Collection<User>) ((ArrayList<User>) User.users).clone();// TODO
		}
	}

	// I am careful about deadlock.
	public static boolean doesTheUserExist(String username) {
		synchronized (User.users) {
			Iterator<User> itUser = User.users.iterator();
			while (itUser.hasNext()) {
				if (itUser.next().username.equals(username))
					return true;
			}
			return false;
		}
	}

	// I am careful about deadlock.
	public static User findTheUser(String username) {
		synchronized (User.users) {
			Iterator<User> itUser = User.users.iterator();
			while (itUser.hasNext()) {
				User user = itUser.next();
				if (user.username.equals(username))
					return user;
			}
			return null;
		}
	}
}