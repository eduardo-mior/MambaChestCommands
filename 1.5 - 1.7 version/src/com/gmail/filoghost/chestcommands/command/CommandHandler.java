package com.gmail.filoghost.chestcommands.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.Permissions;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.task.ErrorLoggerTask;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;

@SuppressWarnings("all")
public class CommandHandler extends CommandFramework {

	public CommandHandler(String label) {
		super(label);
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE), "Você não tem permissão para utilizar este comando.");
			sender.sendMessage(ChestCommands.CHAT_PREFIX);
			sender.sendMessage(ChatColor.GREEN + "Versão: " + ChatColor.GRAY + ChestCommands.getInstance().getDescription().getVersion());
			sender.sendMessage(ChatColor.GREEN + "Desenvolvedor: " + ChatColor.GRAY + "filoghost");
			sender.sendMessage(ChatColor.GREEN + "Editor: " + ChatColor.GRAY + "RUSHyoutuber");
			sender.sendMessage(ChatColor.GREEN + "Comandos: " + ChatColor.GRAY + "/" + label + " help");
			return;
		}
		
		
		
		if (args[0].equalsIgnoreCase("help")) {
			CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + ".help"), "Você não tem permissão para utilizar este comando.");
			sender.sendMessage(ChestCommands.CHAT_PREFIX + " Comandos:");
			sender.sendMessage(ChatColor.WHITE + "/" + label + " reload" + ChatColor.GRAY + " - Recarrega o plugin.");
			sender.sendMessage(ChatColor.WHITE + "/" + label + " list" + ChatColor.GRAY + " - Mostra a lista de Menus carregados.");
			sender.sendMessage(ChatColor.WHITE + "/" + label + " open <menu> [player]" + ChatColor.GRAY + " - Abre um menu para um player.");
			return;
		}
		
		
		
		if (args[0].equalsIgnoreCase("reload")) {
			CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + ".reload"), "Você não tem permissão para utilizar este comando.");
			
			ChestCommands.closeAllMenus();
			
			ErrorLogger errorLogger = new ErrorLogger();
			ChestCommands.getInstance().load(errorLogger);
			
			ChestCommands.setLastReloadErrors(errorLogger.getSize());
			
			if (!errorLogger.hasErrors()) {
				sender.sendMessage(ChestCommands.CHAT_PREFIX + "Plugin recarregado com sucesso!");
			} else {
				new ErrorLoggerTask(errorLogger).run();
				sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "Plugin recarregado com " + errorLogger.getSize() + " erro(s).");
				if (!(sender instanceof ConsoleCommandSender)) {
					sender.sendMessage(ChestCommands.CHAT_PREFIX + ChatColor.RED + "Por favor verifique o console!");
				}
			}
			return;
		}
		
		
		
		if (args[0].equalsIgnoreCase("open")) {
			CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + ".open"), "Você não tem permissão para utilizar este comando.");
			CommandValidate.minLength(args, 2, "Use: /" + label + " open <menu> [player]");
			
			Player target = null;
			
			if (!(sender instanceof Player)) {
				CommandValidate.minLength(args, 3, "Voce deve especificar o nome de um player para poder utilizar este comando pelo console.");
				target = Bukkit.getPlayerExact(args[2]);
			} else {
				if (args.length > 2) {
					CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + ".open.others"), "Você não tem permissão para abrir menus para outras pessoas.");
					target = Bukkit.getPlayerExact(args[2]);
				} else {
					target = (Player) sender;
				}
				
			}
			
			CommandValidate.notNull(target, "Este player não esta online no momento ou não existe.");
			
			String menuName = args[1].toLowerCase().endsWith(".yml") ? args[1] : args[1] + ".yml";
			ExtendedIconMenu menu = ChestCommands.getFileNameToMenuMap().get(menuName);
			CommandValidate.notNull(menu, "O menu \"" + menuName + "\" não foi encontrado.");
			
			if (!sender.hasPermission(menu.getPermission())) {
				menu.sendNoPermissionMessage(sender);
				return;
			}

			if (sender.getName().equalsIgnoreCase(target.getName())) {
				if (!ChestCommands.getLang().open_menu.isEmpty()) {
					sender.sendMessage(ChestCommands.getLang().open_menu.replace("{menu}", menuName));
				}
			} else {
				if (!ChestCommands.getLang().open_menu_others.isEmpty()) {
					sender.sendMessage(ChestCommands.getLang().open_menu_others.replace("{menu}", menuName).replace("{player}", target.getName()));
				}
			}
			
			menu.open(target);
			return;
		}
		
		
		
		if (args[0].equalsIgnoreCase("list")) {
			CommandValidate.isTrue(sender.hasPermission(Permissions.COMMAND_BASE + ".list"), "Você não tem permissão para utilizar este comando.");
			sender.sendMessage(ChestCommands.CHAT_PREFIX + " Menus carregados:");
			for (String file : ChestCommands.getFileNameToMenuMap().keySet()) {
				sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + file);
			}
			
			return;
		}
		
		sender.sendMessage(ChatColor.RED + "Sub-comando desconecido \"" + args[0] + "\".");
	}

}
