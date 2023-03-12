package dev.schmieders.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import dev.schmieders.music.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Current extends ListenerAdapter {

   @Override
   public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
      if (!event.getName().equalsIgnoreCase("current"))
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

      final AudioPlayer audioPlayer = PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer;

      if (audioPlayer.getPlayingTrack() == null) {
         event.reply("Es wird gerade nichts abgespielt.").setEphemeral(true).queue();
         return;
      }

      AudioTrackInfo track = audioPlayer.getPlayingTrack().getInfo();
      event.reply(String.format("Aktuell wird **%s** von **%s** abgespielt.", track.title, track.author)).queue();
   }

}
