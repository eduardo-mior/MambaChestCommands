package com.gmail.filoghost.chestcommands.internal.icon.command;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;

public class OpIconCommand extends IconCommand {

	public OpIconCommand(String command) {
		super(command);
	}

	@Override
	public void execute(Player player, ClickType click) {
		
		if (player.isOp()) {
			player.chat("/" + getParsedCommand(player, click));
			
		} else {
			player.setOp(true);
			player.chat("/" + getParsedCommand(player, click));
        	player.setOp(false);
		}
	}

}
