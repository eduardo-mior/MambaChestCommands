package com.gmail.filoghost.chestcommands.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

public class VersionUtils {
	
	private static boolean setup;
	private static Method oldGetOnlinePlayersMethod;
	
	public static Collection<? extends Player> getOnlinePlayers() {
		try {
			
			if (!setup) {
				oldGetOnlinePlayersMethod = Bukkit.class.getDeclaredMethod("getOnlinePlayers");		
				setup = true;
			}
			
			Player[] playersArray = (Player[]) oldGetOnlinePlayersMethod.invoke(null);
			return ImmutableList.copyOf(playersArray);
			
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
}
