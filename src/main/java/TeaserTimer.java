import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class TeaserTimer implements Runnable {
	ChatColor[] colors = ChatColor.values();
	List<?> teaserTexts = ManyThingsPlugin.teaserTexts;
	int executed;
	
	@Override
	public void run() { // will be executed every x minutes
		if (executed < teaserTexts.size()-1)
			executed++;
		else
			executed = 0;
		if (ManyThingsPlugin.config.getBoolean("display-teasers") && Bukkit.getOnlinePlayers().size() > 0
				&& executed < teaserTexts.size()) {
			int which = ThreadLocalRandom.current().nextInt(15);
			Bukkit.broadcastMessage(colors[which] + (String) teaserTexts.get(executed));
		}
	}
}