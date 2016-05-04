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

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
	private final static String VERSION = "0.1.0_2";
	
	private static Connection connection;
	private static String uIDOwner;
	private static String gIDGuild;
	private static String rIDPvPPart;

	public Listener(String sqlIP, String sqlUS, String sqlPW, String sqlDB) {
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader("C:/Users/Bermos/IPLBot/data.json"));
			JSONObject data = (JSONObject) obj;
			
			Listener.uIDOwner = (String) data.get("IDOwner");
			Listener.gIDGuild = (String) data.get("gIDGuild");
			Listener.rIDPvPPart = (String) data.get("IDPvPRole");
		
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
		if (event instanceof ReadyEvent) {
			System.out.println("[Info] Listener ready.");
			event.getJDA().getUserById(uIDOwner).getPrivateChannel().sendMessageAsync("[Version] " + VERSION, null);
		}
		if (event instanceof PrivateMessageReceivedEvent) {
			PrivateMessageReceivedEvent localEvent = (PrivateMessageReceivedEvent) event;
			System.out.printf("[PM] %s: %s\n", localEvent.getAuthor().getUsername(),
					localEvent.getMessage().getContent());
			
			String content = localEvent.getMessage().getContent().toLowerCase();
			Guild guild = localEvent.getJDA().getGuildById(gIDGuild);
			if (content.startsWith("/newmatch")) {
				if (guild.getRolesForUser(localEvent.getAuthor()).contains(guild.getRoleById(rIDPvPPart))) {
					newmatch(localEvent);
				} else {
					localEvent.getChannel().sendMessageAsync("[Error] you need to get a 'PvP Participant' from one of the leadership members before you can enrole in the IPL. Jump into #general to get help.", null);
				}
			}
			else if (content.startsWith("/joinmatch")) {
				if (guild.getRolesForUser(localEvent.getAuthor()).contains(guild.getRoleById(rIDPvPPart))) {
					joinmatch(localEvent);
				} else {
					localEvent.getChannel().sendMessageAsync("[Error] you need to get a 'PvP Participant' from one of the leadership members before you can enrole in the IPL. Jump into #general to get help.", null);
				}
			}
			else if (content.startsWith("/result")) {
				if (guild.getRolesForUser(localEvent.getAuthor()).contains(guild.getRoleById(rIDPvPPart))) {
					result(localEvent);
				} else {
					localEvent.getChannel().sendMessageAsync("[Error] you need to get a 'PvP Participant' from one of the leadership members before you can enrole in the IPL. Jump into #general to get help.", null);
				}
			}
		}
	}

	private void result(PrivateMessageReceivedEvent event) {
		User author = event.getAuthor();
		String mesCont = event.getMessage().getContent().replaceFirst("/result ", "").toLowerCase().trim();
		String[] mesConts = mesCont.split(", ");
		PrivateChannel privChan = event.getChannel();
		int won = 0;
		
		if (mesConts[1].equals("won") || mesConts[1].equals("win")) {
			won = 1;
		}
		try {
			int idmatch = Integer.parseInt(mesConts[0]);
			
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM iwmembers.pvpmatches WHERE idmatch = ?");
			ps.setInt	(1, idmatch);
			ResultSet resultSetMatches = ps.executeQuery();
			
			if (resultSetMatches.next() && resultSetMatches.getInt("finished") == 0) {
				User player1 = event.getJDA().getUserById(String.valueOf(resultSetMatches.getLong("player1")));
				User player2 = event.getJDA().getUserById(String.valueOf(resultSetMatches.getLong("player2")));
				
				String playerNo = "";
				String otherPlayer = "";
				if (player1.equals(player2)) {
					privChan.sendMessageAsync("[Error] It seems you have played a match against yourself. This incident is recorded.", null);
					event.getJDA().getUserById(uIDOwner).getPrivateChannel()
					.sendMessageAsync("[Warning] " + author.getUsername() + "|" + author.getId() + " | " + author.getDiscriminator() + " - played a match against himself.", null);
				}
				else if (author.equals(player1)) {
					playerNo = "resultp1";
					otherPlayer = "resultp2";
				}
				else if (author.equals(player2)) {
					playerNo = "resultp2";
					otherPlayer = "resultp1";
				} else {
					privChan.sendMessageAsync("[Error] You don't belong to this match!", null);
				}
				
				if (!playerNo.isEmpty()) {
					int otherWon = resultSetMatches.getInt(otherPlayer);
					if (!resultSetMatches.wasNull()) {
						ps = connection.prepareStatement("UPDATE iwmembers.pvpmatches SET " + playerNo + " = ? WHERE idmatch = ?");
						ps.setInt	(1, won);
						ps.setInt	(2, idmatch);
						ps.executeUpdate();
						
						if (otherWon == won) {
							String message = "[Error] Both players entered the same outcome.\n"
										   + "If you misstyped please enter the correct outcome.\n"
										   + "Should this error stay unresolved contact a leader.";
							player1.getPrivateChannel().sendMessageAsync(message, null);
							player2.getPrivateChannel().sendMessageAsync(message, null);
						} else {
						
							//Elo calculations
							//Get old elo
							ps.close();
							ps = connection.prepareStatement("SELECT elo, matches FROM iwmembers.user WHERE iduser = ?");
							ps.setLong	(1, Long.parseLong(player1.getId()));
							ResultSet resultSet = ps.executeQuery();
							resultSet.next();
							int oldEloP1 = resultSet.getInt("elo");
							int matchnoP1 = resultSet.getInt("matches");
							
							ps.close();
							ps = connection.prepareStatement("SELECT elo, matches FROM iwmembers.user WHERE iduser = ?");
							ps.setLong	(1, Long.parseLong(player2.getId()));
							resultSet.close();
							resultSet = ps.executeQuery();
							resultSet.next();
							int oldEloP2 = resultSet.getInt("elo");
							int matchnoP2 = resultSet.getInt("matches");
							
							int wonP1, wonP2;
							if(author.equals(player1)) {
								wonP1 = won; wonP2 = otherWon;
							} else {
								wonP2 = won; wonP1 = otherWon;
							}
							
							//calculate new elo
							double K = (double) 800/(matchnoP1+1);
							double exp = ((oldEloP2 - oldEloP1) / 400);
							double E = 1 / (1 + Math.pow(10, exp));
							int newEloP1 = (int) (oldEloP1 + K * (wonP1 - E));
							
							K = (double) 800/(matchnoP2+1);
							exp = ((oldEloP1 - oldEloP2) / 400);
							E = 1 / (1 + Math.pow(10, exp));
							int newEloP2 = (int) (oldEloP2 + K * (wonP2 - E));
							
							ps.close();
							ps = connection.prepareStatement("UPDATE iwmembers.user SET elo = ?, matches = ? WHERE iduser = ?");
							ps.setInt	(1, newEloP1);
							ps.setInt	(2, matchnoP1+1);
							ps.setLong	(3, Long.parseLong(player1.getId()));
							ps.executeUpdate();
							
							ps.close();
							ps = connection.prepareStatement("UPDATE iwmembers.user SET elo = ?, matches = ? WHERE iduser = ?");
							ps.setInt	(1, newEloP2);
							ps.setInt	(2, matchnoP2+1);
							ps.setLong	(3, Long.parseLong(player2.getId()));
							ps.executeUpdate();
							
							ps.close();
							ps = connection.prepareStatement("UPDATE iwmembers.pvpmatches SET finished = 1 WHERE idmatch = ?");
							ps.setInt	(1, idmatch);
							ps.executeUpdate();
							
							String message = "Match outcome confirmed by both players and the results are saved.\n"
									   + "We hope to see you again in the IW PvP-League!";
							player1.getPrivateChannel().sendMessageAsync(message, null);
							player2.getPrivateChannel().sendMessageAsync(message, null);
							
							resultSet.close();
							resultSetMatches.close();
							ps.close();
							System.out.println("");
						} //else otherWon == won
					} else {	//else 
						ps.close();
						ps = connection.prepareStatement("UPDATE iwmembers.pvpmatches SET " + playerNo + " = ? WHERE idmatch = ?");
						ps.setInt	(1, won);
						ps.setInt	(2, idmatch);
						ps.executeUpdate();
						ps.close();
						
						privChan.sendMessageAsync("Successfully stored the outcome. Waiting for confirmation from opponent.", null);
					}
				}
			} else {
				privChan.sendMessageAsync("[Error] No match with the provided id was found or the match has already been finished.", null);
			}
		} catch (NumberFormatException e) {
			privChan.sendMessageAsync("[Error] Sorry your match id seems to be invalid", null);
		} catch (SQLException e) {
			privChan.sendMessageAsync("[Error] Sorry your match outcomes couldn't be stored. Please try again later.", null);
			e.printStackTrace();
		}
	}

	private void joinmatch(PrivateMessageReceivedEvent event) {
		User player = event.getAuthor();
		String mesCont = event.getMessage().getContent().replaceFirst("/joinmatch ", "");
		PrivateChannel privChan = event.getChannel();
		
		try {
			int idmatch = Integer.parseInt(mesCont);
			
			PreparedStatement ps = connection.prepareStatement("UPDATE iwmembers.pvpmatches SET player2 = ? WHERE idmatch = ?");
			ps.setLong	(1, Long.parseLong(player.getId()));
			ps.setInt	(2, idmatch);
			int rowsManipulated = ps.executeUpdate();
			ps.close();
			
			if(rowsManipulated == 1) {
				privChan.sendMessageAsync("Your match has been created. Message your opponent to start the fight.\n"
										+ "To confirm the outcome both of you will have to write '/result <id>, <won|lost>' to me. Good luck!", null);
			} else {
				privChan.sendMessageAsync("[Error] Your match id does not exist. To create a new match type '/newmatch'", null);
			}
			
		} catch (NumberFormatException e) {
			privChan.sendMessageAsync("[Error] Sorry your match id seems to be invalid", null);
		} catch (SQLException e) {
			privChan.sendMessageAsync("[Error] Sorry your match couldn't be created. Please try again later.", null);
			e.printStackTrace();
		}
	}

	private void newmatch(PrivateMessageReceivedEvent event) {
		User player = event.getAuthor();
		PrivateChannel privChan = event.getChannel();
		
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO iwmembers.pvpmatches VALUES(default, ?, default, default, default, default, ?)",
																			PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setLong	    (1, Long.parseLong(player.getId())); 
			ps.setTimestamp (2, new Timestamp(System.currentTimeMillis()));
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			
			if (rs.next()) {
				privChan.sendMessageAsync("Your match has been created. This is your match id:\n"
										+ "```" + rs.getLong(1) + "```\n"
										+ "Tell this your opponent so he can '/joinmatch <id>'\n"
										+ "He should message you once he joined; and to confirm the outcome both of you\n"
										+ "will have to write '/result <id>, <won|lost>' to me. Good luck!", null);
			} else {
				rs.close();
				ps.close();
				throw new Error();
			}
			rs.close();
			ps.close();
		} catch (SQLException | Error e) {
			privChan.sendMessageAsync("[Error] Sorry your match couldn't be created. Please try again later.", null);
			e.printStackTrace();
		}
	}

}
