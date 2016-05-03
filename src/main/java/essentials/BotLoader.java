package essentials;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BotLoader {
	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(new FileReader("C:/Users/Bermos/IPLBot/login.json"));
				JSONObject loginData = (JSONObject) obj;
				
				System.out.println((String) loginData.get("discordToken"));
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
	}

}
