package com.gmail.filoghost.chestcommands.task;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.gmail.filoghost.chestcommands.api.Icon;

public class ExecuteCommandsTask implements Runnable {

	private Player player;
	private Icon icon;
	private ClickType click;

	public ExecuteCommandsTask(Player player, Icon icon, ClickType click) {
		this.player = player;
		this.icon = icon;
		this.click = click;
	}
	

	@Override
	public void run() {
		boolean close = icon.onClick(player, click);
		
		if (close) {
			player.closeInventory();
		}
	}

}
