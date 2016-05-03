package essentials;

import java.io.FileReader;
import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.dv8tion.jda.JDABuilder;

public class BotLoader {
	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(new FileReader("C:/Users/Bermos/IPLBot/login.json"));
				JSONObject loginData = (JSONObject) obj;
				
				String botToken = (String) loginData.get("discordToken");				
				String sqlIP = (String) loginData.get("sqlIP");
				String sqlPW = (String) loginData.get("sqlPW");
				String sqlDB = (String) loginData.get("sqlDB");
				String sqlUS = (String) loginData.get("sqlUS");
				System.out.println(botToken);
				Listener listener = new Listener(sqlIP, sqlUS, sqlPW, sqlDB);
				
				new JDABuilder()
					.setBotToken(botToken)
					.addListener(listener)
					.buildBlocking();
				
			} catch (IOException | ParseException e) {
				System.out.println("[Error] login data could not be read/parsed.");
			} catch (LoginException e) {
				System.out.println("[Error] login data invalid");
			} catch (IllegalArgumentException e) {
				System.out.println("[Error] no login data provided");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

}
