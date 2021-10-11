package serverSide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import bothSidesExceptions.WrongArgsException;
import serverSide.SomethingIsWrongException;
import serverSide.User;

public abstract class Game {

	protected int					requiredNPlayers	= -1;
	protected int					id					= -1;
	protected String				name				= null;

	protected Collection<User>		players				= null;

	protected Boolean				isDone				= false;
	protected Boolean				isWaiting			= false;

	protected static final Object	lockCreatingAGame	= new Object();
	protected static final int		PAUSE				= 50;
	protected final Object			lockEcho			= new Object();

	public Game(User initiator, String name, int requiredNPlayers) {
		this.requiredNPlayers = requiredNPlayers;
		this.name = name;

		this.players = new ArrayList<User>();// TODO
		this.players.add(initiator);

		this.id = -1;
		this.isDone = false;
		this.isWaiting = false;
	}

	public synchronized int getID() {
		return this.id;
	}

	public synchronized void setID(int id) {
		this.id = id;
	}

	public synchronized String getName() {
		return this.name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}

	public synchronized boolean getIsWaiting() {
		return this.isWaiting;
	}

	public synchronized void setIsWaiting() {
		this.isWaiting = true;
	}

	public synchronized void resetIsWaiting() {
		this.isWaiting = false;
	}

	public synchronized int getNPlayers() {
		return this.players.size();
	}

	public synchronized void addAPlayer(User newPlayer)
			throws SomethingIsWrongException {
		char newFL = newPlayer.getUsername().charAt(0);
		for (User u : this.players) {
			char fL = u.getUsername().charAt(0);
			if (fL == newFL)
				throw new SomethingIsWrongException(
						"Error occured in adding a player: "
								+ "First letter already exists - username= "
								+ newPlayer.getUsername() + "game= "
								+ this.name);
		}

		if (!this.players.add(newPlayer))
			throw new SomethingIsWrongException(
					"Error occured in adding a player: "
							+ "Can not add - username= "
							+ newPlayer.getUsername() + "game= " + this.name);
	}

	public boolean hasSufficientNPlayers() throws SomethingIsWrongException {
		if (requiredNPlayers == players.size())
			return true;
		else if (requiredNPlayers < players.size())
			throw new SomethingIsWrongException(
					"Error occured in hasSufficientNPlayers: "
							+ "requiredNPlayers < players.size - game= "
							+ this.name);
		else
			return false;
	}

	public boolean removeThePlayer(User theUser) {
		synchronized (theUser) {
			synchronized (players) { // players must never be null
				if (this.players.remove(theUser)) {
					for (User u : this.players) {
						ArrayList<String> newsItem = new ArrayList<String>();
						newsItem.add(theUser.getUsername() + " left");
						u.addANewsItem(newsItem);
						if (this.isWaiting)
							u.resetIsReady();
					}
					return true;
				} else
					return false;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<User> getPlayers() {
		synchronized (players) { // players must never be null
			return (Collection<User>) ((ArrayList<User>) this.players).clone();// TODO
		}
	}

	public boolean arePlayersReady() {
		synchronized (players) { // players must never be null
			for (User p : this.players)
				if (!p.getIsReady())
					return false;
			return true;
		}
	}

	public abstract void initTheGame() throws SomethingIsWrongException;

	public abstract void echoTheMoveInOrder(User mover, String theMoveInfo[])
			throws WrongArgsException;

	// I am careful about deadlock; I don't break the order.
	public boolean terminateTheGame() {
		synchronized (this.isDone) {
			synchronized (Game.games) {
				if (!isDone) {
					if (Game.games.remove(this)) {
						while (!Game.ids.remove(this.id)) {
							try {
								Thread.sleep(Game.PAUSE);
							} catch (InterruptedException e) {
								System.err.println(
										"Intrupt error in terminateTheGame");
								e.printStackTrace();
								System.exit(-1);
							}
						}
						this.isDone = true;
						return true;
					}
					return false;
				} else
					return true;
			}
		}
	}

	// statics:
	public static HashSet<Integer>	ids		= new HashSet<Integer>();	// TODO
	public static Collection<Game>	games	= new ArrayList<Game>();	// TODO

	public static Game createADLGame(User initiator, String name,
			int requiredNPlayers) {
		synchronized (Game.lockCreatingAGame) {
			Game newGame = null;
			synchronized (ids) {
				Random rand = new Random();
				int theID = -1;
				do {
					theID = rand.nextInt(Integer.MAX_VALUE);
				} while (Game.ids.contains(theID));

				ids.add(theID);

				newGame = new DotsAndBoxesGame(initiator, name,
						requiredNPlayers);
				newGame.setID(theID);
				newGame.setIsWaiting();
			}
			synchronized (Game.games) {
				Game.games.add(newGame);
			}
			return newGame;
		}
	}

	public static Collection<Game> getWaitingGames() {
		synchronized (Game.games) {
			@SuppressWarnings("unchecked")
			Collection<Game> wGames = (Collection<Game>) ((ArrayList<Game>) Game.games)
					.clone();// TODO
			Iterator<Game> iterator = wGames.iterator();
			Game g;
			while (iterator.hasNext()) {
				g = iterator.next();
				if (!g.getIsWaiting())
					iterator.remove();
			}
			return wGames;
		}
	}

	public static Game findTheGame(int gameID) {
		synchronized (Game.games) {
			Iterator<Game> itGame = Game.games.iterator();
			while (itGame.hasNext()) {
				Game game = itGame.next();
				if (game.id == gameID)
					return game;
			}
			return null;
		}
	}

}