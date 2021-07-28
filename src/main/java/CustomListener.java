import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;

import api.TitleAPI;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.*;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;

public class CustomListener implements Listener {
	@EventHandler(priority = EventPriority.HIGH)
	public void onJoinEvent(PlayerJoinEvent event) {
		event.setJoinMessage(ChatColor.GOLD + ManyThingsPlugin.config.getString("player-join-message")
				.replace("%n", event.getPlayer().getDisplayName()));
		ManyThingsPlugin.move(event.getPlayer(), "on");

		if (ManyThingsPlugin.Instance.getConfig().getBoolean("currently-offline")) event.getPlayer().kickPlayer(ManyThingsPlugin.Instance.getConfig().getString("offline-message"));
	}

	public void addServerPingPacketListener() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(ManyThingsPlugin.Instance, ListenerPriority.HIGH, Collections.singletonList(PacketType.Status.Server.SERVER_INFO)) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (!(ManyThingsPlugin.Instance.isEnabled())) return;
				if (!ManyThingsPlugin.Instance.getConfig().getBoolean("change-list-info")) return;
				final WrappedServerPing ping = event.getPacket().getServerPings().read(0);
				ping.setVersionProtocol(-1);
				ping.setVersionName(ManyThingsPlugin.Instance.getConfig().getString("list-version-display"));
				List<WrappedGameProfile> list = new ArrayList<>();
				String playerName = ManyThingsPlugin.Instance.getConfig().getString("list-version-hover");
				list.add(new WrappedGameProfile(new UUID(0L, 0L), playerName));
				ping.setPlayers(list);
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMoveEvent(PlayerMoveEvent event) {
		event.setCancelled(!ManyThingsPlugin.canWalk.get(event.getPlayer())[0]);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onCommandExecute(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().startsWith("/say")||event.getMessage().startsWith("/invsee") // !!!!!!!!! remove /say !!!!!!!
				|| event.getMessage().startsWith("/essentials:invsee")) {
			if (!ManyThingsPlugin.invseeAllowedPlayers.contains(event.getPlayer().getName())) {
				try {
					Bukkit.broadcastMessage(ChatColor.DARK_RED + "Achtung! " + ChatColor.RED + event.getPlayer().getDisplayName()
							+ " wollte in das Inventar von " + event.getMessage().split(" ")[1] + " schauen!");
					event.setCancelled(true);
				} catch (ArrayIndexOutOfBoundsException exception) {}
			}
		} else if ((!(event.getMessage().startsWith("/gmm") || event.getMessage().startsWith("/gamemodem") || event.getMessage().startsWith("/many-things-plugin")) && (event.getMessage().startsWith("/gm") || event.getMessage().startsWith("/gamemode") || event.getMessage().startsWith("/essentials:gm") || event.getMessage().startsWith("/minecraft:gamemode"))) && ManyThingsPlugin.config.getBoolean("show-gmmenu-hint"))
			TitleAPI.sendTitle(event.getPlayer(), 50, 100, 50, "Bitte benutze /gmm, /gmmenu", "oder /gamemodemenu für eine schönere Auswahl!");
	}
}