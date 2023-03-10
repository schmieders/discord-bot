package dev.schmieders.listeners;

import dev.schmieders.music.PlayerManager;
import dev.schmieders.music.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelUpdateListener extends ListenerAdapter {

   @Override
   public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
      GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
      if (!selfVoiceState.inAudioChannel())
         return;

      if (!selfVoiceState.isDeafened())
         event.getGuild().getSelfMember().deafen(true).queue();

      if (selfVoiceState.getChannel().getMembers().size() <= 1) {
         event.getGuild().getAudioManager().closeAudioConnection();
         TrackScheduler trackScheduler = PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler;
         trackScheduler.player.stopTrack();
         trackScheduler.queue.clear();
      }
   }

}
