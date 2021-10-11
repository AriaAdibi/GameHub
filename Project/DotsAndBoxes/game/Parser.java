package game;

import java.util.HashMap;
import java.util.Map;

public class Parser {

	private Map<String, Integer>	games;
	private Map<String, Integer>	status;

	public Parser() {
		games = new HashMap<String, Integer>();
		games.put("The best game ever", 12);
		games.put("The worst game ever", 12);
		games.put("The melowest game ever", 12);
		games.put("The game", 12);
		status = new HashMap<String, Integer>();
	}

	public void updateStatus(String[] players) {
		status.clear();
		for (String stat : players)
			status.put(stat.split(" ")[0], Integer.valueOf(stat.split(" ")[1]));
	}

	public int getPlayerStatus(String name) {
		return status.getOrDefault(name, -10);
	}

	public String[] getPlayers() {
		return status.keySet().toArray(new String[0]);
	}

	public Integer[] getPlayerStats() {
		return status.values().toArray(new Integer[0]);
	}

	public void updateList(String[] games) {
		this.games.clear();
		for (String game : games)
			this.games.put(game.substring(0, game.lastIndexOf(' ')),
					Integer.valueOf(game.substring(game.lastIndexOf(' ') + 1)));
	}

	public int getGameID(String name) {
		return games.getOrDefault(name, -1);
	}

	public String[] getGames() {
		return games.keySet().toArray(new String[0]);
	}

}
