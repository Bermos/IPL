package essentials;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;

public class Listener {
	private final static String VERSION = "0.0.1_1";
	
	private static Connection connection;
	private static String uIDOwner;
	private static String rIDPvPPart;

	public Listener(String sqlIP, String sqlUS, String sqlPW, String sqlDB) {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:/Users/Bermos/IPLBot/data.json"));
			JSONObject data = (JSONObject) obj;
			
			Listener.uIDOwner = (String) data.get("IDOwner");
			Listener.rIDPvPPart = (String) data.get("IDPvPRole");
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("[Info] Connecting to DB...");
		try {
			Listener.connection = DriverManager.getConnection("jdbc:mysql://" + sqlIP + "/" + sqlDB + "?" + "user=" + sqlUS + "&password=" + sqlPW);
			System.out.println("[Info] Connection to SQL DB established.");
		} catch (SQLException e) {
			System.out.println("[Error] There was an error while trying to connect to the DB.");
			e.printStackTrace();
		}
	}
	
	public void onEvent(Event event)
	{
		if (event instanceof ReadyEvent) {
			System.out.println("[Info] Listener ready.");
			event.getJDA().getUserById(uIDOwner).getPrivateChannel().sendMessageAsync("[Version] " + VERSION, null);
		}
		if (event instanceof PrivateMessageReceivedEvent) {
			PrivateMessageReceivedEvent localEvent = (PrivateMessageReceivedEvent) event;
			System.out.printf("[PM] %s: %s\n", localEvent.getAuthor().getUsername(),
					localEvent.getMessage().getContent());
		}
	}

}
