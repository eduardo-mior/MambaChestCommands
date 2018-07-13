package com.gmail.filoghost.chestcommands.internal;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.gmail.filoghost.chestcommands.api.ClickHandler;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.internal.icon.command.OpenIconCommand;

public class CommandsClickHandler implements ClickHandler {

	private List<IconCommand> left_commands;
	private List<IconCommand> right_commands;
	private boolean closeOnClick;
	
	public CommandsClickHandler(List<IconCommand> left_commands, List<IconCommand> right_commands, boolean closeOnClick) {
		this.left_commands = left_commands;
		this.right_commands  = right_commands;
		this.closeOnClick = closeOnClick;
		
		if (left_commands != null && left_commands.size() > 0) {
			for (IconCommand command : left_commands) {
				if (command instanceof OpenIconCommand) {
					// Fix GUI closing if KEEP-OPEN is not set, and a command should open another GUI.
					this.closeOnClick = false;
				}
			}
		}
		
		if (right_commands != null && right_commands.size() > 0) {
			for (IconCommand command : right_commands) {
				if (command instanceof OpenIconCommand) {
					// Fix GUI closing if KEEP-OPEN is not set, and a command should open another GUI.
					this.closeOnClick = false;
				}
			}
		}
	}
	
	@Override
	public boolean onClick(Player player, ClickType click) {
		
		if (click.toString().toUpperCase().contains("LEFT")) {
			if (left_commands != null && left_commands.size() > 0) {
				for (IconCommand command : left_commands) {
					command.execute(player, click);
				}
			}
		} 
		
		else {
			if (right_commands != null && right_commands.size() > 0) {
				for (IconCommand command : right_commands) {
					command.execute(player, click);
				}
			}
		}
		return closeOnClick;
	}
	
}
