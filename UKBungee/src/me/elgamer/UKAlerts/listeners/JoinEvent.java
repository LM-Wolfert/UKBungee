package me.elgamer.UKAlerts.listeners;

import me.elgamer.UKAlerts.Main;
import me.elgamer.UKAlerts.sql.PlayerData;
import me.elgamer.UKAlerts.sql.PublicBuilds;
import me.elgamer.UKAlerts.utils.Points;
import me.elgamer.UKAlerts.utils.Weekly;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinEvent implements Listener {

	@EventHandler
	public void onPostLogin(PostLoginEvent e) {
		
		ProxiedPlayer p = e.getPlayer();
		
		Main instance = Main.getInstance();
		
		String uuid = e.getPlayer().getUniqueId().toString();
		String name = e.getPlayer().getName();
		
		TextComponent joinmessage;
		
		if (PlayerData.userExists(uuid)) {
			PlayerData.updateName(uuid, name);
			joinmessage = new TextComponent(name + " joined UKnet");
			joinmessage.setColor(ChatColor.YELLOW);
		} else {
			PlayerData.createUser(uuid, name);
			joinmessage = new TextComponent(name + " joined UKnet for the first time!");
			joinmessage.setColor(ChatColor.YELLOW);
		}
		
		for (ProxiedPlayer pl : instance.getProxy().getPlayers()) {
			
			pl.sendMessage(joinmessage);
		}
			
		if (p.hasPermission("group.reviewer")) {
			
			if (PublicBuilds.reviewExists(p.getUniqueId().toString())) {
				TextComponent message = new TextComponent("There is a plot available for review on the building server!");
				message.setColor(ChatColor.GREEN);
				p.sendMessage(message);
			}
			
		}
		
		Weekly w = new Weekly();
		Points pt = new Points();
		
		pt.createUserIfNew(uuid);
		w.createUserIfNew(uuid);
		w.updateDay();
		w.updateLeader();
		
	}
	
	
}
