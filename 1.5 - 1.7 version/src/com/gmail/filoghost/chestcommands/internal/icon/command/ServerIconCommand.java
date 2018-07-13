package com.gmail.filoghost.chestcommands.internal.icon.command;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.gmail.filoghost.chestcommands.bridge.bungee.BungeeCordUtils;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;

public class ServerIconCommand extends IconCommand {

	public ServerIconCommand(String command) {
		super(command);
	}

	@Override
	public void execute(Player player, ClickType click) {
		BungeeCordUtils.connect(player, command);
	}

}
