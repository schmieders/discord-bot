package dev.schmieders.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import dev.schmieders.music.GuildMusicManager;
import dev.schmieders.music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Skip extends ListenerAdapter {

   @Override
   public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
      if (!event.getName().equalsIgnoreCase("skip"))
         return;

      Member member = event.getMember();
      if (member.getUser().isBot() || member.getUser().isSystem())
         return;

      GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
      if (!selfVoiceState.inAudioChannel()) {
         event.reply("Ich werde aktuell nicht verwendet.").setEphemeral(true).queue();
         return;
      }

      GuildVoiceState voiceState = member.getVoiceState();
      VoiceChannel channel = member.getVoiceState().getChannel().asVoiceChannel();
      if (!voiceState.inAudioChannel() || !selfVoiceState.getChannel().asVoiceChannel().equals(channel)) {
         event.reply("Du musst in meinem Kanal sein um einen Song zu überspringen.").setEphemeral(true).queue();
         return;
      }

      final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
      final AudioPlayer audioPlayer = musicManager.audioPlayer;

      if (audioPlayer.getPlayingTrack() == null) {
         event.reply("Es wird gerade nichts abgespielt.").setEphemeral(true).queue();
         return;
      }

      String title = audioPlayer.getPlayingTrack().getInfo().title;
      String author = audioPlayer.getPlayingTrack().getInfo().author;

      musicManager.scheduler.nextTrack();
      event.reply(String.format("**%s** von **%s** wurde übersprungen.", title, author)).queue();
   }

}
