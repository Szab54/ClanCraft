package zerothindex.clancraft.bukkit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import zerothindex.clancraft.ClanPlugin;
import zerothindex.clancraft.WorldPlayer;
import zerothindex.clancraft.clan.Clan;
import zerothindex.clancraft.clan.ClanManager;
import zerothindex.clancraft.clan.ClanPlayer;

/**
 * The "main" class of the Bukkit plugin. Because this plugin aims to convert
 * to the official server API when it is available, we will try to keep all
 * Bukkit interaction as separate from the core of the plugin as possible.
 * 
 * This class will try to be THE ONLY WAY that Bukkit and Clan classes will 
 * interact with each other!
 * 
 * @author zerothindex
 *
 */
public class BukkitClanPlugin extends JavaPlugin {
	
	private static BukkitClanPlugin instance;
	private static ClanPlugin clanPlugin;
	private static WorldGuardPlugin worldGuard;
	
	private HashMap<String,String> tagMap;
	
	private Gson gson;
	
	/**
	 * Called when the plugin is enabled.
	 */
	public void onEnable() {
		instance = this;
		clanPlugin = new ClanPlugin(this.getDescription().getName());
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new EntityListener(), this);
		
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	    // WorldGuard may not be loaded
	    if (plugin != null && (plugin instanceof WorldGuardPlugin)) {
	    	worldGuard = (WorldGuardPlugin) plugin;
	    } else {
	    	ClanPlugin.getInstance().log("WorldGuard not found! Disabling...");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    }
	    
	    tagMap = new HashMap<String,String>();
	    tagMap.put("<r>", ChatColor.DARK_RED.toString());
	    tagMap.put("<g>", ChatColor.DARK_GREEN.toString());
	    tagMap.put("<b>", ChatColor.DARK_AQUA.toString());
	    tagMap.put("<n>", ChatColor.WHITE.toString());
	    tagMap.put("<t>", ChatColor.GOLD.toString());
	    tagMap.put("<m>", ChatColor.GRAY.toString());
	    //tagMap.put("<*>", ChatColor.MAGIC.toString());
	    
	    // load an arraylist of savestateclan's into clanManager
 		gson = new GsonBuilder().setPrettyPrinting().create();
 		Reader reader;
 		try {
 			reader = new InputStreamReader(new FileInputStream("ClanData.json"));
 			/* skip warning comment at start of file
 			int next = reader.read();
 			while(next != (int)'\n') {
 				next = reader.read();
 			}*/
 			Type type = new TypeToken<ArrayList<SaveStateClan>>(){}.getType();
 			ArrayList<SaveStateClan> clanStates = gson.fromJson(reader, type);
 			ClanPlugin.getInstance().log("Loading "+clanStates.size()+" clans...");
 			for (SaveStateClan clanState : clanStates) {
 				Clan clan = clanState.toClan();
 				ClanPlugin.getInstance().getClanManager().addClan(clan);
 				if (clan.getPlot() != null) {
 					clan.getPlot().recalculate();
 				}
 			}
 			reader.close();
 		} catch (FileNotFoundException e) {
 			ClanPlugin.getInstance().log("Saves not found.");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
		
	}
	
	/**
	 * Called when the plugin is disabled.
	 */
	public void onDisable() {
		clanPlugin.disable();
		// save clanmanager to json
		ArrayList<SaveStateClan> clanList = new ArrayList<SaveStateClan>();
		for (Clan clan : ClanPlugin.getInstance().getClanManager().getClans()) {
			clanList.add(new SaveStateClan(clan));
		}
		Writer writer;
		try {
			writer = new OutputStreamWriter(new FileOutputStream("ClanData.json"));
			//writer.write("### DO NOT EDIT THIS FILE MANUALLY ###\n");
		    gson.toJson(clanList, writer);
		    
		    //writer = new OutputStreamWriter(new FileOutputStream("ClanSettings.json"));
		    //gson.toJson(PluginSettings.class, writer);
		    
		    writer.close();
		} catch (FileNotFoundException e) {
			ClanPlugin.getInstance().log("SEVERE ERROR: Could not save ClanPlugin, file not found.");
			e.printStackTrace();
		} catch (IOException e) {
			ClanPlugin.getInstance().log("SEVERE ERROR: Could not save ClanPlugin, IO error.");
			e.printStackTrace();
		}
	}
	
	/**
	 * @return world guard
	 */
	public static WorldGuardPlugin getWorldGuardPlugin() {
		return worldGuard;
	}
	
	public static BukkitClanPlugin getInstance() {
		return instance;
	}
	
	/**
	 * Called when a command is made
	 */
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
    	if(cmd.getName().equalsIgnoreCase("c")){
    		if (sender instanceof Player) 
    			ClanPlugin.getInstance().getCommandManager().handle(new BukkitPlayer((Player)sender), args);
    		else
    			ClanPlugin.getInstance().getCommandManager().handle(new BukkitCommandSender(sender), args);
    	}
    	return true; 
    }
    
    /**
     * Gets an existing or creates a new ClanPlayer for the given Player
     * @param p a Player
     * @return a ClanPlayer
     */
    public ClanPlayer getClanPlayer(Player p) {
    	ClanPlayer cp = ClanPlugin.getInstance().findClanPlayer(p.getName());
		if (cp == null) {
			cp = new ClanPlayer(new BukkitPlayer(p));
			ClanPlugin.getInstance().addClanPlayer(cp);
		}
		return cp;
    }
    
    /**
     * @param str A message to send to a player or console
     * @return a parsed string with color chars
     */
    public static String parseMessage(String str) {
    	String out = str;
    	
    	Iterator<Entry<String,String>> iter = getInstance().tagMap.entrySet().iterator();
    	
    	while(iter.hasNext()) {
    		Entry<String, String> tag = iter.next();
    		out = out.replaceAll(tag.getKey(), tag.getValue());
    	}
    	return out;
    }
    
    /**
     * @param str A message to strip the tags of
     * @return a formatless string
     */
    public static String stripMessage(String str) {
    	String out = str;
    	
    	Iterator<Entry<String,String>> iter = getInstance().tagMap.entrySet().iterator();
    	
    	while(iter.hasNext()) {
    		Entry<String, String> tag = iter.next();
    		out = out.replaceAll(tag.getKey(), "");
    	}
    	return out;
    }

}
