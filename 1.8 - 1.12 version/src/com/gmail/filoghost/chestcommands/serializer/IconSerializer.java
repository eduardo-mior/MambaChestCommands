package com.gmail.filoghost.chestcommands.serializer;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.gmail.filoghost.chestcommands.api.Icon;
import com.gmail.filoghost.chestcommands.config.AsciiPlaceholders;
import com.gmail.filoghost.chestcommands.exception.FormatException;
import com.gmail.filoghost.chestcommands.internal.CommandsClickHandler;
import com.gmail.filoghost.chestcommands.internal.RequiredItem;
import com.gmail.filoghost.chestcommands.internal.icon.ExtendedIcon;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;
import com.gmail.filoghost.chestcommands.util.ItemStackReader;
import com.gmail.filoghost.chestcommands.util.Utils;
import com.gmail.filoghost.chestcommands.util.Validate;

public class IconSerializer {
	
	private static class Nodes {
		
		public static final
				String ID = "ID",
				DATA_VALUE = "DATA-VALUE",
				AMOUNT = "AMOUNT",
				NAME = "NAME",
				LORE = "LORE",
				ENCHANT = "ENCHANTMENT",
				HIDE_ATTRIBUTES = "HIDE-ATTRIBUTES",
				COLOR = "COLOR",
				SKULL_OWNER = "SKULL-OWNER",
				SKULL_URL = "SKULL-URL",
				LEFT = "LEFT-CLICK-COMMAND",
				RIGHT = "RIGHT-CLICK-COMMAND",
				PRICE = "PRICE",
				POINTS = "POINTS",
				EXP_LEVELS = "LEVELS",
				REQUIRED_ITEM = "REQUIRED-ITEM",
				PERMISSION = "PERMISSION",
				PERMISSION_MESSAGE = "PERMISSION-MESSAGE",
				VIEW_PERMISSION = "VIEW-PERMISSION",
				KEEP_OPEN = "KEEP-OPEN",
				SLOT = "SLOT";
	}

	public static class Slot {
	
		private Integer slot;
		
		protected Slot(Integer slot) {
			this.slot = slot;
		}
		
		public boolean isSetSlot() {
			return slot != null;
		}

		public Integer getSlot() {
			return slot;
		}
	}

	public static Icon loadIconFromSection(ConfigurationSection section, String iconName, String menuFileName, ErrorLogger errorLogger) {
		Validate.notNull(section, "A ConfigurationSection (config) nao pode ser nula");
		
		// The icon is valid even without a Material.
		ExtendedIcon icon = new ExtendedIcon();
		
		if (section.isSet(Nodes.ID)) {
			try {
				ItemStackReader itemReader = new ItemStackReader(section.getString(Nodes.ID), true);
				icon.setMaterial(itemReader.getMaterial());
				icon.setDataValue(itemReader.getDataValue());
				icon.setAmount(itemReader.getAmount());
			} catch (FormatException e) {
				errorLogger.addError("O icone \"" + iconName + "\" que esta no menu \"" + menuFileName + "\" possui um ID invalido: " + e.getMessage());
			}
		}
		
		if (section.isSet(Nodes.DATA_VALUE)) {
			icon.setDataValue((short) section.getInt(Nodes.DATA_VALUE));
		}
		
		if (section.isSet(Nodes.AMOUNT)) {
			icon.setAmount(section.getInt(Nodes.AMOUNT));
		}
		
		if (section.isSet(Nodes.HIDE_ATTRIBUTES)) {
			icon.setHideAtributes(section.getBoolean(Nodes.HIDE_ATTRIBUTES));
		}
		
		icon.setName(AsciiPlaceholders.placeholdersToSymbols(Utils.colorizeName(section.getString(Nodes.NAME))));
		icon.setLore(AsciiPlaceholders.placeholdersToSymbols(Utils.colorizeLore(section.getStringList(Nodes.LORE))));
		
		if (section.isSet(Nodes.ENCHANT)) {
			icon.setEnchantments(EnchantmentSerializer.loadEnchantments(section.getString(Nodes.ENCHANT), iconName, menuFileName, errorLogger));
		}
		
		if (section.isSet(Nodes.COLOR)) {
			try {
				icon.setColor(Utils.parseColor(section.getString(Nodes.COLOR)));
			} catch (FormatException e) {
				errorLogger.addError("O icone \"" + iconName + "\" que esta no menu \"" + menuFileName + "\" possui uma COR invalia: " + e.getMessage());
			}
		}
		
		icon.setSkullOwner(section.getString(Nodes.SKULL_OWNER));
		icon.setSkullUrl(section.getString(Nodes.SKULL_URL));
		
		icon.setPermission(section.getString(Nodes.PERMISSION));
		icon.setPermissionMessage(Utils.addColors(section.getString(Nodes.PERMISSION_MESSAGE)));
		icon.setViewPermission(section.getString(Nodes.VIEW_PERMISSION));
		
		boolean closeOnClick = !section.getBoolean(Nodes.KEEP_OPEN);
		icon.setCloseOnClick(closeOnClick);
		
		List<IconCommand> left_commands = null;
		if (section.isSet(Nodes.LEFT)) {
						
			if (section.isList(Nodes.LEFT)) {
				left_commands = Utils.newArrayList();
				
				for (String left_commandString : section.getStringList(Nodes.LEFT)) {
					if (left_commandString.isEmpty()) {
						continue;
					}
					left_commands.add(CommandSerializer.matchCommand(left_commandString));
				}
				
			} else {
				left_commands = CommandSerializer.readCommands(section.getString(Nodes.LEFT));
			}
			
		}
		
		List<IconCommand> right_commands = null;
		if (section.isSet(Nodes.RIGHT)) {
						
			if (section.isList(Nodes.RIGHT)) {
				right_commands = Utils.newArrayList();
				
				for (String right_commandString : section.getStringList(Nodes.RIGHT)) {
					if (right_commandString.isEmpty()) {
						continue;
					}
					right_commands.add(CommandSerializer.matchCommand(right_commandString));
				}
				
			} else {
				right_commands = CommandSerializer.readCommands(section.getString(Nodes.RIGHT));
			}
			
		}
		
		icon.setClickHandler(new CommandsClickHandler(left_commands, right_commands, closeOnClick));
		
		double price = section.getDouble(Nodes.PRICE);
		if (price > 0.0) {
			icon.setMoneyPrice(price);
		} else if (price < 0.0) {
			errorLogger.addError("O icone \"" + iconName + "\" que esta no menu \"" + menuFileName + "\" possui um PRECO em MONEY negativo: " + price);
		}
		
		int points = section.getInt(Nodes.POINTS);
		if (points > 0) {
			icon.setPlayerPointsPrice(points);
		} else if (points < 0) {
			errorLogger.addError("O icone \"" + iconName + "\" que esta no menu \"" + menuFileName + "\" possui um PRECO em PONOTS negativo: " + points);
		}
		
		int levels = section.getInt(Nodes.EXP_LEVELS);
		if (levels > 0) {
			icon.setExpLevelsPrice(levels);
		} else if (levels < 0) {
			errorLogger.addError("O icone \"" + iconName + "\" que esta no menu \"" + menuFileName + "\" possui um PRECO em NIVEIS de XP negativo: " + levels);
		}
		
		if (section.isSet(Nodes.REQUIRED_ITEM)) {
			try {
				ItemStackReader itemReader = new ItemStackReader(section.getString(Nodes.REQUIRED_ITEM), true);
				RequiredItem requiredItem = new RequiredItem(itemReader.getMaterial(), itemReader.getAmount());
				if (itemReader.hasExplicitDataValue()) {
					requiredItem.setRestrictiveDataValue(itemReader.getDataValue());
				}
				icon.setRequiredItem(requiredItem);
			} catch (FormatException e) {
				errorLogger.addError("O icone \"" + iconName + "\" que esta no menu \"" + menuFileName + "\" possui um REQUIRED-ITEM (Material) invalido: " + e.getMessage());
			}
		}
		
		return icon;
	}
	
	public static Slot loadCoordsFromSection(ConfigurationSection section) {
		Validate.notNull(section, "A ConfigurationSection (config) nao pode ser nula");
		
		Integer slot = null;
		
		if (section.isInt(Nodes.SLOT)) {
			slot = section.getInt(Nodes.SLOT);
		}
		
		return new Slot(slot);
	}
	
//	/**
//	 * Reads a list of strings or a single String as list.
//	 */
//	private static List<String> readAsList(ConfigurationSection section, String node) {
//		if (section.isList(node)) {
//			return section.getStringList(node);
//		} else if (section.isString(node)) {
//			return Arrays.asList(section.getString(node));
//		} else {
//			return null;
//		}
//	}
	
	public static void saveToSection(Icon icon, ConfigurationSection section) {
		Validate.notNull(icon, "O Icone nao pode ser nulo");
		Validate.notNull(section, "A ConfigurationSection (config) nao pode ser nula");
		
		section.set(Nodes.ID, serializeIconID(icon));
		
		if (icon.getEnchantments().size() > 0) {
			section.set(Nodes.ENCHANT, 1);
		}
		
		//TODO not finished
	}
	
	public static String serializeIconID(Icon icon) {
		if (icon.getMaterial() == null) {
			return "Not set";
		}
		
		StringBuilder output = new StringBuilder();
		output.append(Utils.formatMaterial(icon.getMaterial()));
		
		if (icon.getDataValue() > 0) {
			output.append(":");
			output.append(icon.getDataValue());
		}
		
		if (icon.getAmount() != 1) {
			output.append(", ");
			output.append(icon.getAmount());
		}
		
		return output.toString();
	}
	
}
