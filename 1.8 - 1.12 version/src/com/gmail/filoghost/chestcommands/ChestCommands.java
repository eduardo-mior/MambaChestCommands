package com.gmail.filoghost.chestcommands;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.chestcommands.bridge.EconomyBridge;
import com.gmail.filoghost.chestcommands.bridge.PlayerPointsBridge;
import com.gmail.filoghost.chestcommands.command.CommandFramework;
import com.gmail.filoghost.chestcommands.command.CommandHandler;
import com.gmail.filoghost.chestcommands.config.AsciiPlaceholders;
import com.gmail.filoghost.chestcommands.config.Lang;
import com.gmail.filoghost.chestcommands.config.Settings;
import com.gmail.filoghost.chestcommands.config.yaml.PluginConfig;
import com.gmail.filoghost.chestcommands.internal.BoundItem;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.internal.MenuData;
import com.gmail.filoghost.chestcommands.internal.MenuInventoryHolder;
import com.gmail.filoghost.chestcommands.listener.CommandListener;
import com.gmail.filoghost.chestcommands.listener.InventoryListener;
import com.gmail.filoghost.chestcommands.listener.JoinListener;
import com.gmail.filoghost.chestcommands.listener.SignListener;
import com.gmail.filoghost.chestcommands.nms.AttributeRemover;
import com.gmail.filoghost.chestcommands.serializer.CommandSerializer;
import com.gmail.filoghost.chestcommands.serializer.MenuSerializer;
import com.gmail.filoghost.chestcommands.task.ErrorLoggerTask;
import com.gmail.filoghost.chestcommands.task.RefreshMenusTask;
import com.gmail.filoghost.chestcommands.util.CaseInsensitiveMap;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;
import com.gmail.filoghost.chestcommands.util.Utils;
import com.gmail.filoghost.chestcommands.util.VersionUtils;

public class ChestCommands extends JavaPlugin {
	
	public static final String CHAT_PREFIX = ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "ChestCommands" + ChatColor.DARK_GREEN + "] " + ChatColor.GREEN;

	private static ChestCommands instance;
	private static Settings settings;
	private static Lang lang;
	
	private static Map<String, ExtendedIconMenu> fileNameToMenuMap;
	private static Map<String, ExtendedIconMenu> commandsToMenuMap;
	
	private static Set<BoundItem> boundItems;
	
	private static int lastReloadErrors;
	
	private static AttributeRemover attributeRemover;
	
	@Override
	public void onEnable() {
		if (instance != null) {
			getLogger().warning("Por favor nao use /reload nem use plugins como o plugman. Use \"/cc reload\" para isso.");
			return;
		}
		
		instance = this;
		fileNameToMenuMap = CaseInsensitiveMap.create();
		commandsToMenuMap = CaseInsensitiveMap.create();
		boundItems = Utils.newHashSet();
		
		settings = new Settings(new PluginConfig(this, "config.yml"));
		lang = new Lang(new PluginConfig(this, "lang.yml"));
		instance.saveResource("ajuda.yml", true);
		
		if (!EconomyBridge.setupEconomy()) {
			getLogger().warning("O vault nao encontrou nenhum plugin de economia compativel. Icones ou comandos com 'PRICE' nao irao funcionar!");
		}
		
		if (PlayerPointsBridge.setupPlugin()) {
			getLogger().info("PlayerPoints vinculado com sucesso!");
		}
		
		AttributeRemover.setup();
		
		Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
		Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
		Bukkit.getPluginManager().registerEvents(new SignListener(), this);
		
		CommandFramework.register(this, new CommandHandler("chestcommands"));
		
		ErrorLogger errorLogger = new ErrorLogger();
		load(errorLogger);
		
		lastReloadErrors = errorLogger.getSize();
		if (errorLogger.hasErrors()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new ErrorLoggerTask(errorLogger), 10L);
		}
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new RefreshMenusTask(), 2L, 2L);
	}
	
	@Override
	public void onDisable() {
		closeAllMenus();
	}
	
	public void load(ErrorLogger errorLogger) {
		fileNameToMenuMap.clear();
		commandsToMenuMap.clear();
		boundItems.clear();
		
		CommandSerializer.checkClassConstructors(errorLogger);
		
		try {
			settings.load();
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().warning("Erro I/O encontrado ao tentar utilizar os valores da config. Os valores padroes serao usados.");
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			getLogger().warning("A config.yml nao era um YAML valido!. Os valores padroes do plugin serao usados.");
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("Erro desconhecido ao tentar ler os valores da configuracao! Por favor entre em contato com um desenvolvedor!");
		}
		
		try {
			lang.load();
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().warning("Erro I/O encontrado ao tentar utilizar os valores da config. Os valores padroes serao usados.");
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			getLogger().warning("A config.yml nao era um YAML valido!. Os valores padroes do plugin serao usados.");
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("Erro desconhecido ao tentar ler os valores da configuracao! Por favor entre em contato com um desenvolvedor!");
		}
		
		try {
			AsciiPlaceholders.load(errorLogger);
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().warning("Erro I/O ao tentar ler os Placeholders. Eles nao vao funcionar!.");
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().warning("Erro desconhecido ao tentar ler os Placeholders! Por favor entre em contato com um desenvolvedor!");
		}
		
		// Load the menus.
		File menusFolder = new File(getDataFolder(), "menu");
		
		if (!menusFolder.isDirectory()) {
			// Create the directory with the default menu.
			menusFolder.mkdirs();
			Utils.saveResourceSafe(this, "menu" + File.separator + "exemplo.yml");
		}
				
		List<PluginConfig> menusList = loadMenus(menusFolder);
		for (PluginConfig menuConfig : menusList) {
			try {
				menuConfig.load();
			} catch (IOException e) {
				e.printStackTrace();
				errorLogger.addError("Erro I/O ao tentar carregar o menu \"" + menuConfig.getFileName() + "\". O arquivo esta em uso?");
				continue;
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
				errorLogger.addError("Configuracao YAML invalida no menu \"" + menuConfig.getFileName() + "\". Por favor verifique o acima ou refaca seu menu.");
				continue;
			}
			
			MenuData data = MenuSerializer.loadMenuData(menuConfig, errorLogger);
			ExtendedIconMenu iconMenu = MenuSerializer.loadMenu(menuConfig, data.getTitle(), data.getRows(), data.getType(), errorLogger);
			
			if (fileNameToMenuMap.containsKey(menuConfig.getFileName())) {
				errorLogger.addError("Dois menus possuem o mesmo nome: \"" + menuConfig.getFileName() + "\". Por favor altere os nomes ou voce tera problemas.");
			}
			fileNameToMenuMap.put(menuConfig.getFileName(), iconMenu);
			
			if (data.hasCommands()) {
				for (String command : data.getCommands()) {
					if (!command.isEmpty()) {
						if (commandsToMenuMap.containsKey(command)) {
							errorLogger.addError("O menu \"" + commandsToMenuMap.get(command).getFileName() + "\" e o menu \"" + menuConfig.getFileName() + "\" possuem o mesmo comando: \"" + command + "\". Apenas um dos menus sera aberto.");
						}
						commandsToMenuMap.put(command, iconMenu);
					}
				}
			}
			
			iconMenu.setRefreshTicks(data.getRefreshTenths());
			
			if (data.getOpenActions() != null) {
				iconMenu.setOpenActions(data.getOpenActions());
			}
			
			if (data.hasBoundMaterial() && data.getClickType() != null) {
				BoundItem boundItem = new BoundItem(iconMenu, data.getBoundMaterial(), data.getClickType());
				if (data.hasBoundDataValue()) {
					boundItem.setRestrictiveData(data.getBoundDataValue());
				}
				boundItems.add(boundItem);
			}
		}
		
		// Register the BungeeCord plugin channel.
		if (!Bukkit.getMessenger().isOutgoingChannelRegistered(this, "BungeeCord")) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		}
	}
	
	/**
	 * Loads all the configuration files recursively into a list.
	 */
	private List<PluginConfig> loadMenus(File file) {
		List<PluginConfig> list = Utils.newArrayList();
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				list.addAll(loadMenus(subFile));
			}
		} else if (file.isFile()) {
			if (file.getName().endsWith(".yml")) {
				list.add(new PluginConfig(this, file));
			}
		}
		return list;
	}
	
	public static void closeAllMenus() {
		for (Player player : VersionUtils.getOnlinePlayers()) {
			if (player.getOpenInventory() != null) {
				if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuInventoryHolder || player.getOpenInventory().getBottomInventory().getHolder() instanceof MenuInventoryHolder) {
					player.closeInventory();
				}
			}
		}
	}
	
	
	public static ChestCommands getInstance() {
		return instance;
	}
	
	public static Settings getSettings() {
		return settings;
	}
	
	public static Lang getLang() {
		return lang;
	}
	
	public static Map<String, ExtendedIconMenu> getFileNameToMenuMap() {
		return fileNameToMenuMap;
	}
	
	public static Map<String, ExtendedIconMenu> getCommandToMenuMap() {
		return commandsToMenuMap;
	}
	
	public static Set<BoundItem> getBoundItems() {
		return boundItems;
	}

	public static int getLastReloadErrors() {
		return lastReloadErrors;
	}

	public static void setLastReloadErrors(int lastReloadErrors) {
		ChestCommands.lastReloadErrors = lastReloadErrors;
	}

	public static AttributeRemover getAttributeRemover() {
		return attributeRemover;
	}
	
}
