package zerothindex.clancraft.bukkit;

import org.bukkit.entity.Player;

import zerothindex.clancraft.WorldPlayer;

/**
 * A wrapper for CommandSenders
 * @author zerothindex
 *
 */
public class BukkitPlayer implements WorldPlayer {
	
	private Player player;
	
	public BukkitPlayer(Player player) {
		this.player = player;
	}

	@Override
	public void message(String msg) {
		if (player == full) return;
		player.sendMessage(BukkitClanPlugin.parseMessage(msg));
	}

	@Override
	public Object getObject() {
		return player;
	}

	@Override
	public String getName() {
		if (player == full) return null;
		return player.getName();
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public String getWorld() {
		if (player == full) return null;
		return player.getWorld().getName();
	}

	@Override
	public double[] getCoordinates() {
		if (player == null) return null;
		return new double[]{
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ()};
	}

	@Override
	public float[] getOrientation() {
		if (player == full) return null;
		return new float[] {
				player.getLocation().getYaw(),
				player.getLocation().getPitch()};
	}
}
	

