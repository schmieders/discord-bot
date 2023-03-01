package dev.schmieders.utils;

import java.util.List;

import dev.schmieders.commands.music.Current;
import dev.schmieders.commands.music.Pause;
import dev.schmieders.commands.music.Play;
import dev.schmieders.commands.music.Queue;
import dev.schmieders.commands.music.Resume;
import dev.schmieders.commands.music.Skip;
import dev.schmieders.commands.music.Stop;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandManager {

   private static CommandManager instance;
   private JDA jda;

   public static CommandManager getInstance(JDA jda) {
      if (instance == null) {
         instance = new CommandManager(jda);
      }
      return instance;
   }

   private CommandManager(JDA jda) {
      this.jda = jda;
   }

   public void registerCommands() throws RateLimitedException {
      this.removeDeletedCommands();
      this.upsertApplicationCommands();
   }

   private void removeDeletedCommands() throws RateLimitedException {
      List<Command> existingCommands = this.jda.retrieveCommands().complete(true);

      existingCommands.forEach(command -> {
         if (!BotCommand.hasName(command.getName())) {
            command.delete().queue();
         }
      });
   }

   private void upsertApplicationCommands() {
      this.upsertMusicCommands();
      this.upsertPresetCommands();
   }

   private void upsertMusicCommands() {
      this.jda
            .upsertCommand("play", "Fügt einen neuen Song zur Warteschlange hinzu.")
            .addOption(OptionType.STRING, "suchbegriff", "Titel, Autor oder anderer Suchbegriff", true).queue();
      this.jda.addEventListener(new Play());

      this.jda.upsertCommand("stop", "Beendet die Wiedergabe.").queue();
      this.jda.addEventListener(new Stop());

      this.jda.upsertCommand("pause", "Pausiert die Wiedergabe").queue();
      this.jda.addEventListener(new Pause());

      this.jda.upsertCommand("resume", "Startet die Wiedergabe").queue();
      this.jda.addEventListener(new Resume());

      this.jda.upsertCommand("skip", "Überspringt den aktuellen Song").queue();
      this.jda.addEventListener(new Skip());

      this.jda.upsertCommand("current", "Zeigt den aktuellen Song an.").queue();
      this.jda.addEventListener(new Current());

      this.jda.upsertCommand("queue", "Zeigt die aktuelle Warteschlange an.").queue();
      this.jda.addEventListener(new Queue());
   }

   private void upsertPresetCommands() {

   }

}
