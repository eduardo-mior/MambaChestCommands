package com.gmail.filoghost.chestcommands.internal.icon.command;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;

public class PlayerIconCommand extends IconCommand {

	public PlayerIconCommand(String command) {
		super(command);
	}

	@Override
	public void execute(Player player, ClickType click) {
		player.chat('/' + getParsedCommand(player, click));
	}

}
