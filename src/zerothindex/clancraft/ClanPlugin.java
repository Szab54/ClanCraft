package zerothindex.clancraft;

import java.util.HashMap;

import zerothindex.clancraft.clan.ClanManager;
import zerothindex.clancraft.clan.ClanPlayer;
import zerothindex.clancraft.command.CommandManager;
import zerothindex.clancraft.event.ClanEventManager;

public class ClanPlugin {
	
	private static ClanPlugin instance;
	
	private String name;
	private CommandManager commandManager;
	private ClanManager clanManager;
	private ClanEventManager clanEventManager;
	
	private HashMap<String, ClanPlayer> players;
	
	/**
	 * Creates an instance of the ClanPlugin!
	 * @param name the name to appear on logs
	 */
	public ClanPlugin(String name) {
		this.name = name;
		commandManager = new CommandManager();
		clanManager = new ClanManager();
		clanEventManager = new ClanEventManager();
		players = new HashMap<String, ClanPlayer>();
		instance = this;
	}
	
	/**
	 * Safely destructs the object
	 */
	public void disable() {		
	}
	
	public static ClanPlugin getInstance() {
		return instance;
	}
	
	public CommandManager getCommandManager() {
		return commandManager;
	}
	
	public ClanManager getClanManager() {
		return clanManager;
	}
	
	public ClanEventManager getClanEventManager() {
		return clanEventManager;
	}
	
	/**
	 * Find the ClanPlayer who's name is some String
	 * @param name the name to identify by
	 * @return a ClanPlayer or NULL
	 */
	public ClanPlayer findClanPlayer(String name) {
		return players.get(name);
	}
	
	/**
	 * Get or create a ClanPlayer
	 * @param player
	 * @return never Null!
	 */
	public ClanPlayer getClanPlayer(WorldPlayer player) {
		ClanPlayer cp = ClanPlugin.getInstance().findClanPlayer(player.getName());
		if (cp == null) {
			cp = new ClanPlayer(player);
			ClanPlugin.getInstance().addClanPlayer(cp);
			cp.logIn();
		}
		log("Tracked ClanPlayers: "+players.size());
		return cp;
	}
	
	/**
	 * Adds a ClanPlayer to the game
	 * @param cp the player
	 * @return false if the name is already taken, true otherwise
	 */
	public boolean addClanPlayer(ClanPlayer cp) {
		if (players.containsKey(cp.getName())) {
			return false;
		} else {
			players.put(cp.getName(), cp);
			return true;
		}
	}
	
	public void messageAll(String string) {
		for (ClanPlayer player : players.values()) {
			player.message(string);
		}
		
	}
	
	public void log(String str) {
		System.out.println("["+name+"] "+str);
	}

}