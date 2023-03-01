package dev.schmieders.commands.music;

import java.awt.Color;
import java.util.concurrent.BlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import dev.schmieders.music.GuildMusicManager;
import dev.schmieders.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Queue extends ListenerAdapter {

   @Override
   public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
      if (!event.getName().equalsIgnoreCase("queue"))
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

      final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;

      EmbedBuilder embed = new EmbedBuilder();

      embed.setTitle(String.format("%d Songs in der Warteschlange", queue.size()));
      embed.setColor(Color.LIGHT_GRAY);

      for (int i = 1; i < queue.size() + 1 && i < 25; i++) {
         AudioTrackInfo info = queue.toArray(new AudioTrack[0])[i - 1].getInfo();
         embed.addField(String.format("%d. %s", i, info.title),
               String.format("von [%s](%s)", info.author, info.uri),
               false);
      }

      embed.setFooter(String.format("Seite 1 von %d (Seiten funktionieren noch nicht)", (queue.size() / 25) + 1));

      if (queue.size() > 25) {
         event.replyEmbeds(embed.build()).addActionRow(Button.primary("page_2", "➤")).queue();
      } else {
         event.replyEmbeds(embed.build()).queue();
      }
   }

}
