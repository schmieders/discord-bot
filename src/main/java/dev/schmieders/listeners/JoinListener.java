package dev.schmieders.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JoinListener extends ListenerAdapter {

   @Override
   public void onGuildMemberJoin(GuildMemberJoinEvent event) {
      Member member = event.getMember();
      if (member.getUser().isBot() || member.getUser().isSystem())
         return;

      try {
         // TODO replace YOUR_ROLE_ID
         event.getGuild().addRoleToMember(event.getUser(), event.getGuild().getRoleById("YOUR_ROLE_ID")).queue();
      } catch (Exception e) {
         // Safety catch
      }
   }
}
