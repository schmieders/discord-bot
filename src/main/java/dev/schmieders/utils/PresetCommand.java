package dev.schmieders.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class PresetCommand {

   public static boolean executePreset(SlashCommandInteractionEvent event) {
      Member member = event.getMember();
      if (member.getUser().isBot() || member.getUser().isSystem())
         return false;

      GuildVoiceState voiceState = member.getVoiceState();
      if (!voiceState.inAudioChannel()) {
         event.reply("Du musst in einem Kanal sein um Musik abzuspielen.").queue();
         return false;
      }

      VoiceChannel channel = member.getVoiceState().getChannel().asVoiceChannel();
      if (!channel.canTalk()) {
         event.reply("Ich kann in deinem Kanal nichts abspielen.").queue();
         return false;
      }

      Guild guild = event.getGuild();
      Member selfMember = guild.getSelfMember();
      GuildVoiceState selfVoiceState = selfMember.getVoiceState();
      if (selfVoiceState.inAudioChannel() && !selfVoiceState.getChannel().asVoiceChannel().equals(channel)) {
         event.reply("Ich werde bereits in einem anderen Kanal verwendet.").queue();
         return false;
      }

      if (!voiceState.inAudioChannel()) {
         AudioManager audioManager = event.getGuild().getAudioManager();
         audioManager.openAudioConnection(channel);
      }

      return true;
   }
}
