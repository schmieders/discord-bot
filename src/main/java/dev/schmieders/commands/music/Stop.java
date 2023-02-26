package dev.schmieders.commands.music;

import dev.schmieders.music.PlayerManager;
import dev.schmieders.music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Stop extends ListenerAdapter {

   @Override
   public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
      if (!event.getName().equalsIgnoreCase("stop"))
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
         event.reply("Du musst in meinem Kanal sein um die Musik zu beenden.").setEphemeral(true).queue();
         return;
      }

      TrackScheduler trackScheduler = PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler;
      trackScheduler.player.stopTrack();
      trackScheduler.queue.clear();
      event.getGuild().getAudioManager().closeAudioConnection();
      event.reply("Die wiedergabe wurde erfolgreich beendet.").queue();
   }
}
