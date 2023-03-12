package dev.schmieders.commands.music;

import java.io.IOException;

import dev.schmieders.music.PlayerManager;
import dev.schmieders.spotify.SpotfiyLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Play extends ListenerAdapter {

   @Override
   public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
      if (!event.getName().equalsIgnoreCase("play") && !event.getName().equalsIgnoreCase("p"))
         return;

      Member member = event.getMember();
      if (member.getUser().isBot() || member.getUser().isSystem())
         return;

      GuildVoiceState voiceState = member.getVoiceState();
      if (!voiceState.inAudioChannel()) {
         event.reply("Du musst in einem Kanal sein um Musik abzuspielen.").queue();
         return;
      }

      VoiceChannel channel = member.getVoiceState().getChannel().asVoiceChannel();
      if (!channel.canTalk()) {
         event.reply("Ich kann in deinem Kanal nichts abspielen.").queue();
         return;
      }

      Guild guild = event.getGuild();
      Member selfMember = guild.getSelfMember();
      GuildVoiceState selfVoiceState = selfMember.getVoiceState();
      if (selfVoiceState.inAudioChannel() && !selfVoiceState.getChannel().asVoiceChannel().equals(channel)) {
         event.reply("Ich werde bereits in einem anderen Kanal verwendet.").queue();
         return;
      }

      event.getGuild().getAudioManager().openAudioConnection(channel);

      String search = event.getOption("suchbegriff").getAsString();

      if (isSpotifyURL(search)) {
         event.deferReply();

         SpotfiyLoader loader = SpotfiyLoader.getInstance();
         try {
            loader.loadFromSpotify(event, search, isSpotifyPlaylistURL(search));
         } catch (IOException e) {
            e.printStackTrace();
            event.reply("Die angegebene Spotify-Playlist konnte nicht geladen werden.").queue();
         }
      } else {
         PlayerManager.getInstance().loadAndPlay(
               event,
               search.startsWith("https://") ? search : String.format("ytsearch:%s audio", search),
               true);
      }
   }

   private boolean isSpotifyURL(String url) {
      url = url.toLowerCase();
      if (!url.contains("https://"))
         return false;

      if (!url.contains("spotify.com/"))
         return false;

      return true;
   }

   private boolean isSpotifyPlaylistURL(String url) {
      if (!url.toLowerCase().contains("spotify.com/playlist"))
         return false;

      return true;
   }

}
