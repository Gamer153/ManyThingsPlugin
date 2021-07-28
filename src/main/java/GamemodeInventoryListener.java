import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GamemodeInventoryListener implements Listener {
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemStack clicked = event.getCurrentItem();
		Inventory inventory = event.getInventory();
		if (inventory.equals(ManyThingsPlugin.gamemodeMenu1)) {
			if (clicked.getType() == Material.APPLE) {
				
			}
			switch (clicked.getType()) {
			case APPLE:
				player.setGameMode(GameMode.SURVIVAL);
				break;
			case GOLD_BLOCK:
				player.setGameMode(GameMode.CREATIVE);
				break;
			case BARRIER:
				player.setGameMode(GameMode.SPECTATOR);
				break;
			case DIAMOND_SWORD:
				player.setGameMode(GameMode.ADVENTURE);
				break;
			default:
				break;
			}
			event.setCancelled(true);
			player.closeInventory();
		}
	}
}
