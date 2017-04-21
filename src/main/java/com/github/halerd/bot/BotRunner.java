package com.github.halerd.bot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class BotRunner {

  public static void main(final String[] args)
      throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
    String blizzardApiKey = System.getProperty("blizzard.api.key");
    String botApiKey = System.getProperty("app.bot.user.token");
    System.out
        .println(String.format("Blizzard API Key: %s\nBot Token: %s", blizzardApiKey, botApiKey));
    new JDABuilder(AccountType.BOT).addEventListener(new BotListener(blizzardApiKey))
        .setToken(botApiKey).buildBlocking();
  }

}
