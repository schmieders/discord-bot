package dev.schmieders.spotify;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import dev.schmieders.music.PlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;;

public class SpotfiyLoader {

   private static SpotfiyLoader instance;

   private final String authUrl = "https://accounts.spotify.com/api/token";
   private final String clientName = ""; // TODO Add your spotify client
   private final String clientSecret = "";
   private final String authString = String.format("%s:%s", clientName, clientSecret);

   private String authToken;

   private SpotfiyLoader() {
      try {
         this.authToken = getAuthToken();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static SpotfiyLoader getInstance() {
      if (instance == null)
         instance = new SpotfiyLoader();

      return instance;
   }

   public void loadFromSpotify(SlashCommandInteractionEvent event, String url, boolean isPlaylist)
         throws IOException {
      String spotifyId = url.split("spotify.com/" + (isPlaylist ? "playlist/" : "track/"))[1].split("\\?si=")[0];

      String endpoint = String.format("https://api.spotify.com/v1/%s/%s",
            isPlaylist ? "playlists" : "tracks",
            spotifyId);

      URLConnection conn = new URL(endpoint).openConnection();
      conn.setRequestProperty("Method", "GET");
      conn.setRequestProperty("Authorization", "Bearer " + authToken);

      InputStream in = conn.getInputStream();
      InputStreamReader reader = new InputStreamReader(in);

      int charsRead;
      char[] charArray = new char[1024];
      StringBuffer buffer = new StringBuffer();
      while ((charsRead = reader.read(charArray)) > 0) {
         buffer.append(charArray, 0, charsRead);
      }

      JSONObject response = new JSONObject(buffer.toString());
      PlayerManager playerManager = PlayerManager.getInstance();

      if (isPlaylist) {
         JSONArray items = response.getJSONObject("tracks").getJSONArray("items");

         items.forEach(item -> {
            JSONObject track = ((JSONObject) item).getJSONObject("track");
            playerManager.loadAndPlay(event, getSearchByTrack(track), false);
         });

         event.reply("Die Spotify-Playlist **").addContent(response.getString("name"))
               .addContent("** von **")
               .addContent(response.getJSONObject("owner").getString("display_name"))
               .addContent("** wurde erfolgreich zur Warteschlange hinzugefügt.")
               .addContent(String.format(" (%d Songs)", items.length()))
               .queue();
      } else {
         playerManager.loadAndPlay(event, getSearchByTrack(response), true);
      }
   }

   private String getAuthToken() throws IOException {
      String auth = new String(Base64.encodeBase64(authString.getBytes()));
      String params = "grant_type=client_credentials";

      HttpURLConnection conn = (HttpURLConnection) new URL(authUrl).openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Basic " + auth);
      conn.setFixedLengthStreamingMode(params.getBytes().length);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Accept", "*/*");
      conn.setDoOutput(true);
      conn.setDoInput(true);

      OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
      writer.write(params);
      writer.flush();
      writer.close();

      InputStreamReader reader = new InputStreamReader(conn.getInputStream());
      int charsRead;
      char[] charArray = new char[1024];
      StringBuffer buffer = new StringBuffer();
      while ((charsRead = reader.read(charArray)) > 0) {
         buffer.append(charArray, 0, charsRead);
      }

      JSONObject response = new JSONObject(buffer.toString());

      return response.getString("access_token");
   }

   private String getSearchByTrack(JSONObject track) {
      StringBuilder search = new StringBuilder("ytsearch:");
      search.append(track.getString("name"));

      JSONArray artists = track.optJSONArray("artists");
      artists.forEach(artist -> {
         search.append(" " + ((JSONObject) artist).getString("name"));
      });

      return search.append(" audio").toString();
   }

}