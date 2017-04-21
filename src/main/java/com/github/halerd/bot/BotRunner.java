package com.github.halerd.bot;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class BotRunner {

  private static final Logger LOGGER = Logger.getLogger(BotRunner.class);

  public static void main(final String[] args)
      throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
    String blizzardApiKey = System.getProperty("blizzard.api.key");
    String botUserToken = System.getProperty("app.bot.user.token");
    if (blizzardApiKey == null) {
      throw new RuntimeException(
          "Please provide a Blizzard API key using -Dblizzard.api.key system property");
    }
    if (botUserToken == null) {
      throw new RuntimeException(
          "Please provide a Discord App Bot User token using -Dapp.bot.user.token system property");
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          String.format("\nBlizzard API Key: %s\nBot Token: %s", blizzardApiKey, botUserToken));
    }
    new JDABuilder(AccountType.BOT).addEventListener(new BotListener(blizzardApiKey))
        .setToken(botUserToken).buildBlocking();
  }

}
