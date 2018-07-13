package com.gmail.filoghost.chestcommands.serializer;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;

import com.gmail.filoghost.chestcommands.api.Icon;
import com.gmail.filoghost.chestcommands.config.yaml.PluginConfig;
import com.gmail.filoghost.chestcommands.exception.FormatException;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.internal.MenuData;
import com.gmail.filoghost.chestcommands.serializer.IconSerializer.Slot;
import com.gmail.filoghost.chestcommands.util.ClickType;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;
import com.gmail.filoghost.chestcommands.util.ItemStackReader;
import com.gmail.filoghost.chestcommands.util.Utils;

public class MenuSerializer {
	
	private static class Nodes {
		
		public static final String MENU_NAME = "menu-settings.name";
		public static final String MENU_ROWS = "menu-settings.rows";
		public static final String MENU_COMMAND = "menu-settings.command";
		public static final String MENU_TYPE = "menu-settings.type";
		
		public static final String OPEN_ACTION = "menu-settings.open-action";
		
		public static final String OPEN_ITEM_MATERIAL = "menu-settings.open-with-item.id";
		public static final String OPEN_ITEM_LEFT_CLICK = "menu-settings.open-with-item.left-click";
		public static final String OPEN_ITEM_RIGHT_CLICK = "menu-settings.open-with-item.right-click";
		
		public static final String AUTO_REFRESH = "menu-settings.auto-refresh";
		
	}
	
	public static ExtendedIconMenu loadMenu(PluginConfig config, String title, int rows, String type, ErrorLogger errorLogger) {
		ExtendedIconMenu iconMenu = new ExtendedIconMenu(title, rows, type, config.getFileName());
		
		for (String subSectionName : config.getKeys(false)) {
			if (subSectionName.equals("menu-settings")) {
				continue;
			}
			
			ConfigurationSection iconSection = config.getConfigurationSection(subSectionName);
			
			Icon icon = IconSerializer.loadIconFromSection(iconSection, subSectionName, config.getFileName(), errorLogger);
			Slot slot = IconSerializer.loadCoordsFromSection(iconSection);
			
			if (!slot.isSetSlot()) {
				errorLogger.addError("O icone \"" + subSectionName + "\" que esta no menu \"" + config.getFileName() + " nao possui SLOT definido.");
				continue;
			}
			
			if (iconMenu.getIcon(slot.getSlot()) != null) {
				errorLogger.addError("O icone \"" + subSectionName + "\" que esta no menu \"" + config.getFileName() + " esta passando por cima de outro icone na mesma posicao do menu.");
			}
			
			iconMenu.setIcon(slot.getSlot(), icon);
		}
		
		return iconMenu;
	}
	
	/**
	 * Reads all the settings of a menu. It will never return a null title, even if not set.
	 */
	public static MenuData loadMenuData(PluginConfig config, ErrorLogger errorLogger) {
		
		String title = Utils.addColors(config.getString(Nodes.MENU_NAME));
		String type = config.getString(Nodes.MENU_TYPE);
		int rows;
		
		if (type == null) {
			errorLogger.addError("O menu \"" + config.getFileName() + "\" nao o seu tipo definido, por padrao o tipo do container sera o CHEST (bau).");
			type = "CHEST";
		}
		
		if (!EnumUtils.isValidEnum(InventoryType.class, type.toUpperCase())) {
			errorLogger.addError("O menu \"" + config.getFileName() + "\" nao possui um tipo de container valido, por padrao o tipo de container sera o CHEST (bau).");
			type = "CHEST";
		}
		
		if (title == null) {
			errorLogger.addError("O menu \"" + config.getFileName() + "\" nao possui o titulo definido.");
			title = ChatColor.DARK_RED + "Titulo indefinido";
		}
		
		if (title.length() > 32) {
			title = title.substring(0, 32);
		}
		
		if (config.isInt(Nodes.MENU_ROWS)) {
			rows = config.getInt(Nodes.MENU_ROWS);
			
			if (rows < 1 || rows > 6) {
				rows = 1;
			}
			
		} else {
			rows = 6; // Defaults to 6 rows.
			errorLogger.addError("O menu \"" + config.getFileName() + "\" nao possui o seu numero de linhas (rows) definidas, por padrao o numero de linhas sera 6.");
		}
		
		MenuData menuData = new MenuData(title, rows, type);
		
		if (config.isSet(Nodes.MENU_COMMAND)) {
			menuData.setCommands(config.getString(Nodes.MENU_COMMAND).replace(" ", "").split(";"));
		}
		
		if (config.isSet(Nodes.OPEN_ACTION)) {
			menuData.setOpenActions(CommandSerializer.readCommands(config.getString(Nodes.OPEN_ACTION)));
		}
		
		if (config.isSet(Nodes.OPEN_ITEM_MATERIAL)) {
			try {
				ItemStackReader itemReader = new ItemStackReader(config.getString(Nodes.OPEN_ITEM_MATERIAL), false);
				menuData.setBoundMaterial(itemReader.getMaterial());
				
				if (itemReader.hasExplicitDataValue()) {
					menuData.setBoundDataValue(itemReader.getDataValue());
				}
			} catch (FormatException e) {
				errorLogger.addError("O item \""+ config.getString(Nodes.OPEN_ITEM_MATERIAL) + "\" usado para abrir o menu \"" + config.getFileName() + "\" e invalido: " + e.getMessage());
			}
			
			boolean leftClick = config.getBoolean(Nodes.OPEN_ITEM_LEFT_CLICK);
			boolean rightClick = config.getBoolean(Nodes.OPEN_ITEM_RIGHT_CLICK);
			
			if (leftClick || rightClick) {
				menuData.setClickType(ClickType.fromOptions(leftClick, rightClick));
			}
		}
		
		if (config.isSet(Nodes.AUTO_REFRESH)) {
			int tenthsToRefresh = (int) (config.getDouble(Nodes.AUTO_REFRESH) * 10.0);
			if (tenthsToRefresh < 1) {
				tenthsToRefresh = 1;
			}
			menuData.setRefreshTenths(tenthsToRefresh);
		}
		
		return menuData;
	}

}
