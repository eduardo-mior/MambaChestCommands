package com.gmail.filoghost.chestcommands.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.Permissions;

public class JoinListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		
		if (event.getPlayer().hasPermission(Permissions.SEE_ERRORS) && ChestCommands.getLastReloadErrors() > 0) {
			event.getPlayer().sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "O plugin encontrou " + ChestCommands.getLastReloadErrors() + " erro(s) a ultima vez que foi carregado. Recomendamos que você digite \"/cc reload\" e observe o console!");
		}
	}
	
}
