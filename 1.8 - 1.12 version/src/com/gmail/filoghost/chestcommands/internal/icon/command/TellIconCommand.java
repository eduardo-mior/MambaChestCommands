package com.gmail.filoghost.chestcommands.internal.icon.command;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.Utils;

public class TellIconCommand extends IconCommand {

	public TellIconCommand(String command) {
		super(Utils.addColors(command));
	}

	@Override
	public void execute(Player player, ClickType click) {
		player.sendMessage(getParsedCommand(player, click));
	}

}
