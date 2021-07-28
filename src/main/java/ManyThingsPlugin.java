import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManyThingsPlugin extends JavaPlugin {
	public static HashMap<Player, boolean[]> canWalk;
	public static List<?> invseeAllowedPlayers;
	public static YamlConfiguration config;
	public static Inventory gamemodeMenu1;
	public static int teaserTaskID;
	public static List<String> teaserTexts;
	public static Player temp_player;
	public static ItemStack[] old_inv;
	public static ItemStack[] troll_inv;
	
	public static ManyThingsPlugin Instance;
	public static TitleManagerAPI tMAPI;
	
	public YamlConfiguration getConfig() {
		return config;
	}
	
	public void onEnable() {
		Instance = this;
		this.saveResource("config.yml", false);
		config = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
		invseeAllowedPlayers = this.getConfig().getList("invsee-allowed-players");
		teaserTexts = this.getConfig().getStringList("teaser-texts");
		
		canWalk = new HashMap<Player, boolean[]>();
		boolean[] temp = {true};
		for (Player player : Bukkit.getOnlinePlayers()) {
			canWalk.put(player, temp);
		}
		
		PluginManager pM = this.getServer().getPluginManager();
		CustomListener listener = new CustomListener();
		listener.addServerPingPacketListener();
		GamemodeInventoryListener gminvListener = new GamemodeInventoryListener();
		pM.registerEvents(listener, this);
		pM.registerEvents(gminvListener, this);
		
		gamemodeMenu1 = Bukkit.createInventory(null, 9*3, ChatColor.GOLD + "Wähle deinen Spielmodus!");
		loadGamemodeMenu1();

		if (this.getConfig().getBoolean("display-teasers")) {
			double delayMinutes = this.getConfig().getDouble("teaser-time");
			teaserTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, (Runnable) new TeaserTimer(), 0L, (long)( delayMinutes *60*20));
		}
		
		this.getLogger().info(this.getConfig().getString("loaded-message") + " " + getName());
		getCommand("gamemodemenu").setTabCompleter((arg0, arg1, arg2, arg3) -> new ArrayList<String>());
		getCommand("fakesay").setTabCompleter((arg0, arg1, arg2, arg3) -> new ArrayList<String>());
		getCommand("manythings-reload-config").setTabCompleter((arg0, arg1, arg2, arg3) -> new ArrayList<String>());
		getCommand("killall").setTabCompleter((arg0, arg1, arg2, arg3) -> new ArrayList<String>());
		getCommand("troll").setTabCompleter((arg0, arg1, arg2, args) -> {
			ArrayList<String> returns = new ArrayList<>();
			if (args.length == 1) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					returns.add(player.getName());
				}
			}
			return returns;
		});
		getCommand("writewithcolor").setTabCompleter((sender, cmd, commandLabel, args) -> {
			ArrayList<String> returns = new ArrayList<>();
			if (args.length == 1) {
				returns.add("red");
				returns.add("yellow");
				returns.add("blue");
				returns.add("green");
			}
			return returns;
		});
		getCommand("move").setTabCompleter((sender, cmd, commandLabel, args) -> {
			ArrayList<String> returns = new ArrayList<>();
			switch (args.length) {
			case 1:
				returns.add("on");
				returns.add("off");
				break;
			case 2:
				for (Player player : Bukkit.getOnlinePlayers()) {
					returns.add(player.getName());
				}
			}
			return returns;
		});
		getCommand("teaser").setTabCompleter((sender, cmd, commandLabel, args) -> {
			ArrayList<String> returns = new ArrayList<>();
			switch (args.length) {
			case 1:
				returns.add("mode");
				returns.add("time");
				returns.add("add");
				returns.add("remove");
				returns.add("list-all");
				returns.add("help");
				break;
			case 2:
				switch (args[0].trim().toLowerCase()) {
				case "mode":
					returns.add("on");
					returns.add("off");
					break;
				case "time":
					returns.add("10");
					returns.add("30");
					returns.add("60");
					returns.add("120");
					break;
				case "remove":
					if (teaserTexts.size() < 2);
						//sender.sendMessage("Keine Texte entfernbar!");
					else
						for (int i = 0; i < teaserTexts.size(); returns.add(String.valueOf(i + 1)), i++);
				}
				break;
			}
			return returns;
		});
		getCommand("listinfo").setTabCompleter((sender, cmd, commandLabel, args) -> {
			ArrayList<String> returns = new ArrayList<>();
			if (args.length == 1) {
				returns.add("set");
				returns.add("mode");
				returns.add("hover");
				returns.add("help");
			} else if (args.length == 2 && args[0].trim().equalsIgnoreCase("mode")) {
				returns.add("on");
				returns.add("off");
			}
			return returns;
		});
		getCommand("serveroffline").setTabCompleter((commandSender, command, s, args) -> {
			List<String> returns = new ArrayList<>();
			switch (args.length) {
				case 1:
					returns.add("joining");
					returns.add("message");
					returns.add("help");
					break;
				case 2:
					if (args[0].trim().equalsIgnoreCase("joining")) {
						returns.add("on");
						returns.add("off");
					}
					break;
			}
			return returns;
		});
	}
	
	private static void loadGamemodeMenu1() {
		ItemStack survival = new ItemStack(Material.APPLE, 1);		setName(survival, "Überleben");
		ItemStack creative = new ItemStack(Material.GOLD_BLOCK, 1);	setName(creative, "Kreativ");
		ItemStack spectator = new ItemStack(Material.BARRIER, 1);	setName(spectator, "Beobachter");
		ItemStack adventure = new ItemStack(Material.DIAMOND_SWORD, 1);		setName(adventure, "Abenteuer");
		gamemodeMenu1.setItem(8+2, survival);      // #########
		gamemodeMenu1.setItem(8+4, creative);      // #s#c#s#a# --> s = 8+2 ; c = 8+4 ; ...
		gamemodeMenu1.setItem(8+6, spectator);     // #########
		gamemodeMenu1.setItem(8+8, adventure);
		gamemodeMenu1.setMaxStackSize(1);
	}

	public void onDisable() {
		
	}
	
	public boolean onCommand(@Nonnull CommandSender sender, Command command, @Nonnull String label, @Nonnull String[] args) {
		if (command.getName().equalsIgnoreCase("move")) {
			if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender || sender instanceof Player) {
				if (args.length == 2 || (sender instanceof Player && args.length > 0)) {
					if (!(args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
						sender.sendMessage(ChatColor.RED + "Usage: " + command.getUsage());
						return true;
					}
					Player player = null;
					if (sender instanceof Player && args.length == 1)
						player = (Player) sender;
					else
						player = Bukkit.getPlayer(args[1]);
					return move(player, args[0]);
				} else {
					sender.sendMessage("Too little or too many arguments!");
					return false;
				}
			} else {
				this.getServer().broadcastMessage("/move was executed by " + sender.getClass().toString() + ", resulting in an error!");
				return false;
			}
		} else if (command.getName().equalsIgnoreCase("killall")) {
			return killall();
		} else if (command.getName().equalsIgnoreCase("writewithcolor")) {
			if (sender instanceof Player)
				return writewithcolor((Player) sender, args[0], args);
			else
				sender.sendMessage("You can only execute this as a player!");
			return true;
		} else if (command.getName().equalsIgnoreCase("gamemodemenu")) {
			if (sender instanceof Player)
				return gamemodemenu((Player) sender);
			else
				sender.sendMessage("You can only execute this as a player!");
			return true;
		} else if (command.getName().equalsIgnoreCase("teaser")) {                // Command: teaser
			//this.getLogger().warning(ChatColor.DARK_RED + "Länge :" + String.valueOf(args.length) + " Arg 1: |" + args[0] + "| Arg 2: |"/* + args[1]*/);
			if (args[0].equalsIgnoreCase("mode") && args.length == 2
			&& (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("off"))) {
				teaser(args[0], args[1], sender);
			return true;
			} else if (args[0].equalsIgnoreCase("time") && args.length == 2) {
				teaser(args[0], args[1], sender);
				return true;
			} else if (args[0].equalsIgnoreCase("add") && args.length >= 2) {
				teaser_add(args, sender);
				return true;
			} else if (args[0].equalsIgnoreCase("help") && args.length == 1) {
				teaser(args[0], null, sender);
				return true;
			} else if (args[0].equalsIgnoreCase("list-all") && args.length == 1) {
				teaser(args[0], null, sender);
				return true;
			} else if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
				teaser(args[0], args[1], sender);
				return true;
			} else {
				sender.sendMessage("/teaser help --> Shows the help for /teaser.");
				return true;
			}
		} else if(command.getName().equalsIgnoreCase("manythings-reload-config")){// Command: manythings-reload-config
			mtrc(sender);
			return true;
		} else if (command.getName().equalsIgnoreCase("troll")) {                 // Command: troll
			if (args.length > 0)
				troll(sender, Bukkit.getPlayer(args[0]));
			else
				return usage(command, sender);
			return true;
		} else if(command.getName().equalsIgnoreCase("fakesay")) {                 // Command: fakesay
			if (args.length > 1)
				fakesay(sender, Bukkit.getPlayer(args[0]), args);
			else
				return usage(command, sender);
			return true;
		} else if (command.getName().equalsIgnoreCase("listinfo")) {
			if (args.length > 0) {
				listinfo(sender, args);
			} else {
				return usage(command, sender);
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("serveroffline")) {
			if (args.length > 0) {
				serveroffline(sender, args);
			} else {
				return usage(command, sender);
			}
			return true;
		}  else {
			return false;
		}
	}

	private void serveroffline(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("joining")) {
			if (args.length > 1) {
				switch (args[1]) {
					case "on":
						if (getConfig().getBoolean("currently-offline")) {
							getConfig().set("currently-offline", false);
							sender.sendMessage("Joining is activated.");
						} else {
							sender.sendMessage("Nothing changed.");
						}
						break;
					case "off":
						if (!getConfig().getBoolean("currently-offline")) {
							getConfig().set("currently-offline", true);
							sender.sendMessage("Joining is deactivated.");
						} else {
							sender.sendMessage("Nothing changed.");
						}
						break;
					default:
						sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + "/serveroffline joining <on|off>");
						break;
				}
				saveConfig();
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + "/serveroffline joining <on|off>");
			}
		} else if (args[0].equalsIgnoreCase("message")) {
			if (args.length > 1) {
				StringBuilder builder = new StringBuilder();
				for (int i = 1; i < args.length; i++) {
					builder.append(args[i]);
					if (i < args.length - 1)
						builder.append(" ");
				}
				String message = builder.toString();
				getConfig().set("offline-message", message);
				saveConfig();
				sender.sendMessage("Message when trying to join is now: " + message);
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + "/serveroffline message <message>");
			}
		} else if (args[0].equalsIgnoreCase("help")) {
			String[] helps = new String[] {"/serveroffline help:", "/serveroffline joining <on|off> - Activate or deactivate joining.", "/serveroffline message <message> - Message to join when joining is blocked."};
			sender.sendMessage(helps);
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "Help: " + ChatColor.RED + "/serveroffline help");
		}
	}

	private void listinfo(CommandSender sender, String[] args) {
//		returns.add("set");
//		returns.add("mode");
//		returns.add("hover");
//		returns.add("help");
		switch (args[0]) {
			case "set":
				if (args.length > 1) {
					StringBuilder builder = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						builder.append(args[i]);
						if (i < args.length - 1)
							builder.append(" ");
					}
					String message = builder.toString();
					getConfig().set("list-version-display", message);
					sender.sendMessage("Version info displayed in list is now: " + message);
					saveConfig();
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + "/listinfo set <version info>");
				}
				break;
			case "mode":
				if (args.length > 1) {
					switch (args[1]) {
						case "on":
							if (!getConfig().getBoolean("change-list-info")) {
								getConfig().set("change-list-info", true);
								sender.sendMessage("Custom version display is activated.");
							} else {
								sender.sendMessage("Nothing changed.");
							}
							break;
						case "off":
							if (getConfig().getBoolean("change-list-info")) {
								getConfig().set("change-list-info", false);
								sender.sendMessage("Custom version display is deactivated.");
							} else {
								sender.sendMessage("Nothing changed.");
							}
							break;
						default:
							sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + "/listinfo mode <on|off>");
							break;
					}
					saveConfig();
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + "/listinfo mode <on|off>");
				}
				break;
			case "hover":
				if (args.length > 1) {
					StringBuilder builder = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						builder.append(args[i]);
						if (i < args.length - 1)
							builder.append(" ");
					}
					String message = builder.toString();
					getConfig().set("list-version-hover", message);
					sender.sendMessage("Hover text displayed in list is now: " + message);
					saveConfig();
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + "/listinfo hover <hover text>");
				}
				break;
			case "help":
				String[] helps = {"/listinfo help:", "/listinfo mode <on|off> - Deactivate or activate custom version display.", "/listinfo set <version info> - Version info to display.", "/listinfo hover <hover text> - Text to display when hovering the custom version text."};
				sender.sendMessage(helps);
				break;
		}
	}

	private void fakesay(CommandSender sender, Player player, String[] args) {
		StringBuilder msg_builder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			msg_builder.append(args[i]).append(" ");
		}
		String msg = msg_builder.toString();
		if (player != null)
			player.chat(msg);
		else
			sender.sendMessage(ChatColor.RED + "Player " + args[0] + " not online!");
	}

	public static void setName(ItemStack is, String name) {
		if (is != null) {
			ItemMeta m = is.getItemMeta();
			if (m != null) {
				m.setDisplayName(name);
			}
			is.setItemMeta(m);
		}
	}
	
	public boolean usage(Command command, CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.RED + command.getUsage());
		return true;
	}

	private void troll(CommandSender sender, Player player) {
		if (Bukkit.getOnlinePlayers().contains(player)) {
			temp_player = player;
			old_inv = player.getInventory().getContents();
			for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
			    player.getInventory().setItem(slot, new ItemStack(Material.ROTTEN_FLESH, 64));
			}
			int seconds = 10;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			     public void run() {
			    	 ManyThingsPlugin.temp_player.getInventory().clear();
			    	 if (ManyThingsPlugin.old_inv != null)
			    		 ManyThingsPlugin.temp_player.getInventory().setContents(ManyThingsPlugin.old_inv);
			    	 else
			    		 ManyThingsPlugin.temp_player.getInventory().clear();
			     }
			}, (seconds * 20));
			player.sendMessage(sender.getName() + " hat dich getrollt!");
			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 10));
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Player " + player.getName() +" not online!");
		}
	}
	
	private void mtrc(CommandSender sender) {
		Bukkit.getScheduler().cancelTask(teaserTaskID);
		this.reloadConfig();
		this.onEnable();
		sender.sendMessage("ManyThingsPlugin Config reloaded!");
	}
	
	public void teaser(@Nonnull String config, String arg, CommandSender sender) {
		if (config.equalsIgnoreCase("mode")) {
			if (arg.equalsIgnoreCase("on")) {
				this.getConfig().set("display-teasers", true);
			} else {
				this.getConfig().set("display-teasers", false);
			}
			this.saveConfig();
			sender.sendMessage("Teaser texts were turned " + arg + "!");
		} else if (config.equalsIgnoreCase("time")) {
			double new_delay;
			try {
				new_delay = Double.parseDouble(arg);
			}
			catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Not a valid number!");
				return;
			}
			this.getConfig().set("teaser-time", new_delay);
			this.saveConfig();
			Bukkit.getScheduler().cancelTask(teaserTaskID);
			teaserTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, (Runnable) new TeaserTimer(), 0L,
					(long)( new_delay *20));
			sender.sendMessage("Teasers are now displayed every " + arg + " second(s)!");
		} else if (config.equalsIgnoreCase("list-all")) {
			int i = 1;
			for (Object tTxt : this.getConfig().getStringList("teaser-texts").toArray()) {
				sender.sendMessage(ChatColor.BOLD + "" + ChatColor.ITALIC + String.valueOf(i) + ". " + ChatColor.RESET
						+ String.valueOf(tTxt));
				i++;
			}
		} else if (config.equalsIgnoreCase("remove")) {
			int number;
			try {
				number = Integer.parseInt(arg);
				if (number > teaserTexts.size())
					throw new NumberFormatException();
			}
			catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Number not valid or too big!");
				return;
			}
			String toDelete = teaserTexts.get(number-1);
			teaserTexts.remove(number-1);
			if (teaserTexts.size() == 0) {
				teaserTexts.add(toDelete);
				sender.sendMessage(ChatColor.BOLD + "Das letzte Element darf nicht entfernt werden!");
				return;
			}
			this.getConfig().set("teaser-texts", teaserTexts);
		    this.saveConfig();
		    sender.sendMessage("Teaser text \"" + toDelete + "\" deleted!");
		} else {
			String[] teaser_help = {ChatColor.UNDERLINE + "" + ChatColor.BOLD + "" + ChatColor.ITALIC +"Possible options:",
					"/teaser mode <on|off> --> Turns teaser texts on or off.",
					"/teaser time <number> --> Sets the interval for displaying teasers. (can be decimal)",
					"/teaser add <text> --> Adds a teaser text.",
					"/teaser remove <number> --> Removes the <number> teaser text. (see list-all)",
					"/teaser list-all --> Lists all teaser texts with numbers.",
					"/teaser help --> Shows this help."};
			sender.sendMessage(teaser_help);
		}
	}
	public void teaser_add(String[] args, CommandSender sender) {
		StringBuilder teaser_builder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			teaser_builder.append(args[i]);
			if (i < args.length-1)
				teaser_builder.append(" ");
		}
		String new_teaser = teaser_builder.toString();
		List<String> new_t_l = new ArrayList<String>();
		new_t_l.add(new_teaser);
		teaserTexts.addAll(new_t_l);
	    this.getConfig().set("teaser-texts", teaserTexts);
	    this.saveConfig();
	    sender.sendMessage("Teaser \"" + new_t_l.toArray()[0].toString() + "\" added!");
	}
	
	public boolean gamemodemenu(Player player) {
		player.openInventory(gamemodeMenu1);
		return true;
	}

	private boolean writewithcolor(Player player, String color, String args[]) {
		if (args.length > 1) {
			StringBuffer textBuilder = new StringBuffer();
			for (int i = 1; i < args.length; i++) {
				textBuilder.append(args[i] + " ");
			}
			String text = textBuilder.toString();
            switch (color) {
                case "red": {
                    player.chat(ChatColor.DARK_RED + text);
                    return true;
                }
                case "blue": {
                    player.chat(ChatColor.BLUE + text);
                    return true;
                }
                case "yellow": {
                    player.chat(ChatColor.YELLOW + text);
                    return true;
                }
                case "green": {
                    player.chat(ChatColor.GREEN + text);
                    return true;
                }
                default:
                    break;
            }
            player.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Available color options: red, yellow, blue, green!");
            return true;
        } else {
            player.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Not enough arguments!");
            return false;
        }
	}
	
	private boolean killall() {
		this.getServer().dispatchCommand
				(this.getServer().getConsoleSender(), "minecraft:kill @e[type=!minecraft:player,type=!minecraft:item_frame]");
		return true;
	}

	public static boolean move(Player player, String arg) {
		boolean newState;
		if (arg.equalsIgnoreCase("on")) {
			newState = true;
		} else {
			newState = false;
		}
		boolean[] temp = {newState};
		
		if (canWalk.containsKey(player)) {
			canWalk.replace(player, temp);
		} else {
			canWalk.put(player, temp);
		}
		return true;
	}
}
