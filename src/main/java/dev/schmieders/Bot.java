package dev.schmieders;

import dev.schmieders.listeners.ChannelUpdateListener;
import dev.schmieders.listeners.JoinListener;
import dev.schmieders.utils.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

public class Bot {

   private final JDA JDA;

   private final String TOKEN = "YOUR_BOT_TOKEN"; // TODO replace YOUR_BOT_TOKEN

   public Bot() throws InterruptedException {
      this.JDA = JDABuilder
            .createDefault(TOKEN)
            .setActivity(Activity.listening("ySimon#3112")) // TODO set custom activity
            .setAutoReconnect(true)
            .build();

      this.JDA.awaitReady();

      try {
         CommandManager.getInstance(this.JDA).registerCommands();
      } catch (RateLimitedException e) {
         System.err.println("Could not execute command registration due to rate limit.");
      }

      this.registerEventListeners();
   }

   private void registerEventListeners() {
      this.JDA.addEventListener(new JoinListener());
      this.JDA.addEventListener(new ChannelUpdateListener());
   }

}
