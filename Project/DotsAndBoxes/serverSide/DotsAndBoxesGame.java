package serverSide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import bothSidesExceptions.WrongArgsException;
import serverSide.Game;
import serverSide.SomethingIsWrongException;
import serverSide.User;

public class DotsAndBoxesGame extends Game {

	private int n = -1;

	public DotsAndBoxesGame(User initiator, String name, int requiredNPlayers) {
		super(initiator, name, requiredNPlayers);
		Random rand = new Random(); // TODO
		this.n = rand.nextInt(4) + 6;
	}

	public synchronized int getN() {
		return this.n;
	}

	/*
	 * @see serverSide.Game#initTheGame() Client checked the requisitions.
	 */
	@Override
	public void initTheGame() throws SomethingIsWrongException {
		synchronized (super.isWaiting) {
			if (!super.hasSufficientNPlayers())
				throw new SomethingIsWrongException(
						"Error occurred in initiating the game: "
								+ "Number of players is not sufficient.");

			if (super.isWaiting) {
				synchronized (super.players) {
					if (!super.arePlayersReady())
						throw new SomethingIsWrongException(
								"Error occurred in initiating the game: "
										+ "Some users are not ready");

					for (User u : super.players)
						u.setState(0);
					super.isWaiting = false;
				}
			}
		}
	}

	/**
	 * @param mover
	 * @param theMoveInfo
	 *            [2] 0-theLine 1-elpaseTime 2-mouse info Handling of
	 *            "who's Turn is this?" is responsibility of ClientSide.
	 * @return
	 * @throws WrongArgsException
	 */
	public void echoTheMoveInOrder(User mover, String theMoveInfo[])
			throws WrongArgsException {
		synchronized (super.lockEcho) {
			if (theMoveInfo.length != 3)
				throw new WrongArgsException("echo: theMoveInfo.length != 3");

			Game theGame = mover.getGame();
			Collection<User> players = theGame.getPlayers();
			for (User u : players) {
				if (u.getUsername().equals(mover.getUsername()))
					continue;
				ArrayList<String> newsItem = new ArrayList<String>();
				for (int i = 0; i < theMoveInfo.length; i++)
					newsItem.add(theMoveInfo[i]);
				u.addANewsItem(newsItem);
			}
		}
	}

}