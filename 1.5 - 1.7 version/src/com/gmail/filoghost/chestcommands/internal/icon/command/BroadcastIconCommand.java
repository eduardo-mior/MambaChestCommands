package com.gmail.filoghost.chestcommands.internal.icon.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.Utils;

public class BroadcastIconCommand extends IconCommand {

	public BroadcastIconCommand(String command) {
		super(Utils.addColors(command));
	}

	@Override
	public void execute(Player player, ClickType click) {
		Bukkit.broadcastMessage(getParsedCommand(player, click));
	}
}
