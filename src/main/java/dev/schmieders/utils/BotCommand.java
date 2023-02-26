package dev.schmieders.utils;

public enum BotCommand {

   PLAY, STOP, PAUSE, RESUME, SKIP, CURRENT;

   public static boolean hasName(String name) {
      for (BotCommand value : BotCommand.values()) {
         if (value.name().equalsIgnoreCase(name)) {
            return true;
         }
      }
      return false;
   }

}
