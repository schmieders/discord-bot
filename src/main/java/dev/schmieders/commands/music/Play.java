package dev.schmieders.commands.music;

import dev.schmieders.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Play extends ListenerAdapter {

   @Override
   public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
      if (!event.getName().equalsIgnoreCase("play"))
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
      PlayerManager.getInstance().loadAndPlay(event,
            search.startsWith("https://") ? search : String.format("ytsearch:%s audio", search));
   }

   @Override
   public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
      if (!event.getMember().equals(event.getGuild().getSelfMember()))
         return;

      if (event.getGuild().getSelfMember().getVoiceState().inAudioChannel())
         event.getGuild().getSelfMember().deafen(true).queue();
   }

}
