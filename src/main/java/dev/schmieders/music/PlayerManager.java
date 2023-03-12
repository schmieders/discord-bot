package dev.schmieders.music;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl, boolean reply) {
        final GuildMusicManager musicManager = this.getMusicManager(event.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.scheduler.queue(audioTrack);

                if (reply) {
                    event.reply("**").addContent(audioTrack.getInfo().title)
                            .addContent("** von **")
                            .addContent(audioTrack.getInfo().author)
                            .addContent("** wurde erfolgreich zur Warteschlange hinzugefügt.")
                            .addContent(String.format(
                                    " (%s)",
                                    new SimpleDateFormat("mm:ss").format(audioTrack.getDuration())))
                            .queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();

                if (!tracks.isEmpty()) {
                    musicManager.scheduler.queue(tracks.get(0));

                    if (reply)
                        event.reply("**")
                                .addContent(tracks.get(0).getInfo().title)
                                .addContent("** von **")
                                .addContent(tracks.get(0).getInfo().author)
                                .addContent("** wurde erfolgreich zur Warteschlange hinzugefügt.")
                                .queue();
                } else {
                    System.err.println("Empty playlist was loaded.");

                    if (reply)
                        event.reply("Ich konnte diesen Song leider nicht finden...").queue();
                }
            }

            @Override
            public void noMatches() {
                System.err.println("Couldn't find song for search: " + trackUrl);

                if (reply)
                    event.reply("Ich konnte leider keine Ergebinsse finden...").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                System.err.println("Couldn't load song for search: " + trackUrl);
                System.err.println(e.getMessage());

                if (reply)
                    event.reply("Die Suchergebnisse sind leider nicht erreichbar...").queue();
            }

        });
    }

    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

}
