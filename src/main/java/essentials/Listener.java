package essentials;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdateNameEvent;
import net.dv8tion.jda.events.channel.text.TextChannelUpdateTopicEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.events.guild.role.GuildRoleCreateEvent;
import net.dv8tion.jda.events.guild.role.GuildRoleDeleteEvent;
import net.dv8tion.jda.events.guild.role.GuildRoleUpdateNameEvent;
import net.dv8tion.jda.events.guild.role.GuildRoleUpdatePositionEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
	private final static String VERSION = "0.1.0_2";
	
	private static Connection connection;
	private static String uIDOwner;
	private static String gIDGuild;

	public Listener(String sqlIP, String sqlUS, String sqlPW, String sqlDB) {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("./data.json"));
			JSONObject data = (JSONObject) obj;
			
			Listener.uIDOwner = (String) data.get("IDOwner");
			Listener.gIDGuild = (String) data.get("gIDGuild");
		
		System.out.println("[Info] Connecting to DB...");
			Listener.connection = DriverManager.getConnection("jdbc:mysql://" + sqlIP + "/" + sqlDB + "?user=" + sqlUS + "&password=" + sqlPW);
			System.out.println("[Info] Connection to SQL DB established.");
		} catch (SQLException e) {
			System.out.println("[Error] There was an error while trying to connect to the DB.");
			e.printStackTrace();
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}
	}
	
	public void onEvent(Event event)
	{
		try {
			if (event instanceof ReadyEvent) {
				System.out.println("[Info] Listener ready.");
				event.getJDA().getUserById(uIDOwner).getPrivateChannel().sendMessageAsync("[Version] " + VERSION, null);
				
				updateDB((ReadyEvent) event);
			}
			// Messages
			if (event instanceof GuildMessageReceivedEvent) {
				guildMessageReceived((GuildMessageReceivedEvent) event);
			}
			if (event instanceof GuildMessageDeleteEvent) {
				guildMessageDelete((GuildMessageDeleteEvent) event);
			}
			if (event instanceof GuildMessageUpdateEvent) {
				guildMessageUpdate((GuildMessageUpdateEvent) event);
			}
			// Text channels
			if (event instanceof TextChannelCreateEvent) {
				textChannelCreate((TextChannelCreateEvent) event);
			}
			if (event instanceof TextChannelUpdateNameEvent) {
				textChannelUpdateName((TextChannelUpdateNameEvent) event);
			}
			if (event instanceof TextChannelDeleteEvent) {
				textChannelDelete((TextChannelDeleteEvent) event);
			}
			if (event instanceof TextChannelUpdateTopicEvent) {
				textChannelUpdateTopic((TextChannelUpdateTopicEvent) event);
			}
			// Roles
			if (event instanceof GuildRoleCreateEvent) {
				guildRoleCreate((GuildRoleCreateEvent) event);
			}
			if (event instanceof GuildRoleUpdateNameEvent) {
				guildRoleUpdateName((GuildRoleUpdateNameEvent) event);
			}
			if (event instanceof GuildRoleUpdatePositionEvent) {
				guildRoleUpdatePosition((GuildRoleUpdatePositionEvent) event);
			}
			if (event instanceof GuildRoleDeleteEvent) {
				guildRoleDelete((GuildRoleDeleteEvent) event);
			}
			// Users
			if (event instanceof GuildMemberJoinEvent) {
				guildMemberJoin((GuildMemberJoinEvent) event);
			}
			if (event instanceof GuildMemberLeaveEvent) {
				guildMemberLeaveEvent((GuildMemberLeaveEvent) event);
			}
			if (event instanceof GuildMemberRoleAddEvent) {
				guildMemberRoleAdd((GuildMemberRoleAddEvent) event);
			}
			if (event instanceof GuildMemberRoleRemoveEvent) {
				guildMemberRoleRemove((GuildMemberRoleRemoveEvent) event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void guildMessageReceived(GuildMessageReceivedEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void guildMessageDelete(GuildMessageDeleteEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void guildMessageUpdate(GuildMessageUpdateEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void textChannelCreate(TextChannelCreateEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void textChannelUpdateName(TextChannelUpdateNameEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void textChannelDelete(TextChannelDeleteEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void textChannelUpdateTopic(TextChannelUpdateTopicEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void guildRoleCreate(GuildRoleCreateEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void guildRoleUpdateName(GuildRoleUpdateNameEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void guildRoleUpdatePosition(GuildRoleUpdatePositionEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void guildRoleDelete(GuildRoleDeleteEvent event) {
		// TODO Auto-generated method stub
		
	}

	private void guildMemberRoleRemove(GuildMemberRoleRemoveEvent event) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("DELETE FROM user_roles WHERE iduser = ? AND idroles = ?");
		ps.setLong(1, Long.parseLong(event.getUser().getId()));
		ps.setLong(2, Long.parseLong(event.getRoles().get(0).getId()));
		ps.executeUpdate();

		ps.close();
	}

	private void guildMemberRoleAdd(GuildMemberRoleAddEvent event) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("SELECT iduser_roles FROM user_roles WHERE iduser = ? AND idroles = ?");
		ps.setLong(1, Long.parseLong(event.getUser().getId()));
		ps.setLong(2, Long.parseLong(event.getRoles().get(0).getId()));
		ResultSet rs = ps.executeQuery();
		
		if (!rs.next()) {
			ps.close();
			
			ps = connection.prepareStatement("INSERT INTO user_roles VALUES (default, ?, ?)");
			ps.setLong(1, Long.parseLong(event.getUser().getId()));
			ps.setLong(2, Long.parseLong(event.getRoles().get(0).getId()));
			ps.executeUpdate();
			ps.close();
		}
	}

	private void guildMemberLeaveEvent(GuildMemberLeaveEvent event) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("UPDATE user SET onlinestatus = -1 WHERE iduser = ?");
		ps.setLong(1, Long.parseLong(event.getUser().getId().trim()));
		ps.executeUpdate();
		ps.close();
	}

	private void guildMemberJoin(GuildMemberJoinEvent event) throws NumberFormatException, SQLException {
		User author = event.getUser();
		
		PreparedStatement ps = connection.prepareStatement("SELECT iduser FROM user WHERE iduser = ?");
		ps.setLong(1, Long.parseLong(event.getUser().getId().trim()));
		ResultSet rs = ps.executeQuery();

		String role = event.getGuild().getRolesForUser(author).isEmpty() ? "No membership" : event.getGuild().getRolesForUser(author).get(0).getName();
		
		if (!rs.next()) {
			ps.close();
			ps = connection.prepareStatement("INSERT INTO user VALUES(?, ?, ?, ?, ?, ?, ?, default, default, default, default, default, default)");
			ps.setLong		(1, Long.parseLong(event.getUser().getId().trim()));
			ps.setString	(2, author.getUsername());
			ps.setString	(3, role);
			ps.setInt		(4, author.getOnlineStatus().ordinal());
			ps.setTimestamp	(5, new Timestamp(System.currentTimeMillis()));
			ps.setTimestamp	(6, new Timestamp(System.currentTimeMillis()));
			ps.setString	(7, author.getAvatarUrl());
			ps.executeUpdate();
			
			rs.close();
			ps.close();
		} else {
			ps.close();
			ps = connection.prepareStatement("UPDATE user SET username = ?, role = ?, onlinestatus = ?, lastonline = ?, ppurl = ? WHERE iduser = ?");
			ps.setString	(1, author.getUsername());
			ps.setString	(2, role);
			ps.setInt		(3, author.getOnlineStatus().ordinal());
			ps.setTimestamp	(4, new Timestamp(System.currentTimeMillis()));
			ps.setString	(5, author.getAvatarUrl());
			ps.setLong		(6, Long.parseLong(event.getUser().getId().trim()));
			ps.executeUpdate();
			
			rs.close();
			ps.close();
		}
	}

	private void updateDB(ReadyEvent event) {
		
	}
}
