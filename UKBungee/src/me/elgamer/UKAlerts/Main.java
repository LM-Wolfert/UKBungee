package me.elgamer.UKAlerts;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import me.elgamer.UKAlerts.commands.AddPoints;
import me.elgamer.UKAlerts.commands.PointsCommand;
import me.elgamer.UKAlerts.commands.RemovePoints;
import me.elgamer.UKAlerts.listeners.JoinEvent;
import me.elgamer.UKAlerts.listeners.QuitEvent;
import me.elgamer.UKAlerts.sql.PublicBuilds;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Main extends Plugin {

	static Main instance;

	//MySQL
	private Connection connection_publicbuilds, connection_points;
	public String host, database_publicbuilds, database_points, username, password, plotData, submitData, 
	playerData, pointsData, weeklyData, data;
	public int port;

	@Override
	public void onEnable() {

		//Config Setup
		Main.instance = this;

		try {
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//MySQL		
		mysqlSetup();

		//Commands
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new AddPoints());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new PointsCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new RemovePoints());
		
		//Listeners
		getProxy().getPluginManager().registerListener(this, new JoinEvent());
		getProxy().getPluginManager().registerListener(this, new QuitEvent());

		//1 minute timer
		getProxy().getScheduler().schedule(this, new Runnable() {

			@Override
			public void run() {

				getPublicBuilds();
				
				if (PublicBuilds.newSubmit()) {
					TextComponent message = new TextComponent("A plot has been submitted on the building server!");
					message.setColor(ChatColor.GREEN);

					for (ProxiedPlayer p : getProxy().getPlayers()) {
						if (p.hasPermission("group.reviewer")) {
							p.sendMessage(message);
						}
					}
				}

			}

		}, 0L, 60L, TimeUnit.SECONDS);


	}

	@SuppressWarnings("deprecation")
	public void onDisable() {
		
		//MySQL
		try {
			if (connection_publicbuilds != null && !connection_publicbuilds.isClosed()) {

				connection_publicbuilds.close();
				Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
				BungeeCord.getInstance().getConsole().sendMessage("MySQL disconnected from " + config.getString("database"));
			}
			
			if (connection_points != null && !connection_points.isClosed()) {

				connection_points.close();
				Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
				BungeeCord.getInstance().getConsole().sendMessage("MySQL disconnected from " + config.getString("database"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//Creates the mysql connection.
	@SuppressWarnings("deprecation")
	public void mysqlSetup() {

		Configuration config;
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			host = config.getString("host");
			port = config.getInt("port");
			
			database_publicbuilds = config.getString("database_publicbuilds");
			database_points = config.getString("database_points");
			
			username = config.getString("username");
			password = config.getString("password");
			
			plotData = config.getString("plotData");
			submitData = config.getString("submitData");
			
			playerData = config.getString("playerData");
			pointsData = config.getString("pointsData");
			weeklyData = config.getString("weeklyData");
			data = config.getString("data");

			try {

				synchronized (this) {
					if (connection_publicbuilds == null || connection_publicbuilds.isClosed()) {
						Class.forName("com.mysql.jdbc.Driver");
						setPublicBuilds(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" 
								+ this.port + "/" + this.database_publicbuilds + "?&useSSL=false&", this.username, this.password));

						BungeeCord.getInstance().getConsole().sendMessage("MySQL connected to " + config.getString("database_publicbuilds"));
					}
					
					if (connection_points == null || connection_points.isClosed()) {
						Class.forName("com.mysql.jdbc.Driver");
						setPoints(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" 
								+ this.port + "/" + this.database_points + "?&useSSL=false&", this.username, this.password));

						BungeeCord.getInstance().getConsole().sendMessage("MySQL connected to " + config.getString("database_points"));
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	//Returns the mysql publicbuilds connection.
	public Connection getPublicBuilds() {

		try {
			if (connection_publicbuilds == null || connection_publicbuilds.isClosed()) {
				mysqlSetup();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection_publicbuilds;
	}
	
	//Returns the mysql points connection.
		public Connection getPoints() {

			try {
				if (connection_points == null || connection_points.isClosed()) {
					mysqlSetup();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return connection_points;
		}

	//Sets the mysql connection as the variable 'connection'.
	public void setPublicBuilds(Connection connection) {
		this.connection_publicbuilds = connection;
	}

	public void setPoints(Connection connection) {
		this.connection_points = connection;
	}
	
	//Returns an instance of the plugin.
	public static Main getInstance() {
		return instance;
	}

}