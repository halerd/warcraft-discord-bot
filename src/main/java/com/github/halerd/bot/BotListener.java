package com.github.halerd.bot;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class BotListener extends ListenerAdapter {

  private static final Logger LOGGER = Logger.getLogger(BotListener.class);
  private static final String LOCALE = "en_GB";
  private static final String[] SLOTS =
      new String[] {"head", "neck", "shoulder", "back", "chest", "wrist", "hands", "waist", "legs",
          "feet", "finger1", "finger2", "trinket1", "trinket2", "mainHand"};
  private static final String JSON_KEY_THUMBNAIL = "thumbnail";
  private static final String JSON_KEY_ITEMS = "items";
  private static final String JSON_KEY_ITEMS_NAME = "name";
  private static final String JSON_KEY_ITEMS_ITEM_LEVEL = "itemLevel";
  private static final String BLIZZ_API_FIELD_ITEMS = JSON_KEY_ITEMS;
  private static final String ITEMS_TITLE = "I T E M S";
  private static final String ARTIFACT_TITLE = "A R T I F A C T";
  private static final String ILVL_TITLE = "I T E M L E V E L";
  private static final String ARMORY_TITLE = "A R M O R Y";
  private static final String THUMBNAIL_TITLE = "T H U M B N A I L";
  private static final String LINE_SEPARATOR = "-+-+-+-+-+-+-+-+-+-++-+-+-+-+-";

  private final String blizzardApiKey;
  private final HttpClient httpClient = HttpClientBuilder.create().build();

  public BotListener(final String blizzardApiKey) {
    this.blizzardApiKey = blizzardApiKey;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    String discordUserAsMention = event.getAuthor().getAsMention();
    if (event.getMessage().getRawContent().startsWith("!gear")) {
      long startTime = System.currentTimeMillis();
      try {
        String[] split = event.getMessage().getRawContent().split(" ");
        if (split.length != 4) {
          event.getChannel()
              .sendMessage(String.format("%s - %s", discordUserAsMention, syntaxGear())).queue();
        } else {
          commandGear(event, split[1], split[2], split[3]);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      long endTime = System.currentTimeMillis();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(String.format("!gear took %dms.", (endTime - startTime)));
      }
    }
  }

  protected String syntaxGear() {
    return "Please use the following syntax:\n\n!gear [continent=eu|us] [name] [realm]";
  }

  protected void commandGear(final MessageReceivedEvent event, final String continent,
      final String name, final String realm) throws ClientProtocolException, IOException {
    String discordUserAsMention = event.getAuthor().getAsMention();
    String url = getApiUrl(continent, realm, name, BLIZZ_API_FIELD_ITEMS, LOCALE);
    event.getChannel()
        .sendMessage(String.format("%s requested gear (continent=%s, name=%s, realm=%s)...",
            discordUserAsMention, continent, name, realm))
        .queue();
    try {
      long startTime = System.currentTimeMillis();
      HttpGet request = new HttpGet(url);
      ResponseHandler<String> responseHandler = new BasicResponseHandler();
      String responseBody = httpClient.execute(request, responseHandler);
      JSONObject response = new JSONObject(responseBody);
      long endTime = System.currentTimeMillis();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(String.format("Call to Blizzard API took %dms.", (endTime - startTime)));
      }
      String message = buildMessage(response, continent, realm, name);
      event.getChannel().sendMessage(String.format("%s\n%s", discordUserAsMention, message))
          .queue();
    } catch (HttpResponseException e) {
      event.getChannel()
          .sendMessage(String.format("Sorry, %s! I wasnt able to retrieve that character.",
              event.getAuthor().getAsMention()))
          .queue();
      event.getChannel().sendMessage(syntaxGear()).queue();
    }
  }

  protected String getApiUrl(final String continent, final String realm, final String name,
      final String field, final String locale) {
    return String.format(
        "https://%s.api.battle.net/wow/character/%s/%s?fields=%s&locale=%s&apiKey=%s", continent,
        realm, name, field, locale, blizzardApiKey);
  }

  protected String buildMessage(JSONObject response, String continent, String realm, String name) {
    long startTime = System.currentTimeMillis();
    JSONObject items = (JSONObject) response.get(JSON_KEY_ITEMS);
    StringBuilder builder = new StringBuilder();
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(ITEMS_TITLE);
    builder.append("\n");
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(getGear(items));
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(ARTIFACT_TITLE);
    builder.append("\n");
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    // TODO: Artifact
    builder.append("\n");
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(ILVL_TITLE);
    builder.append("\n");
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(getItemLevel(items));
    builder.append("\n");
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(ARMORY_TITLE);
    builder.append("\n");
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(getArmoryUrl(continent, realm, name));
    builder.append("\n");
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(THUMBNAIL_TITLE);
    builder.append("\n");
    builder.append(LINE_SEPARATOR);
    builder.append("\n");
    builder.append(getThumbnailUrl(continent, response.get(JSON_KEY_THUMBNAIL).toString()));
    long endTime = System.currentTimeMillis();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("buildMessage took %dms.", (endTime - startTime)));
    }
    return builder.toString();
  }

  protected String getGear(final JSONObject items) {
    StringBuilder builder = new StringBuilder();
    for (String slot : SLOTS) {
      builder.append(getNameAndIlvl(slot, items));
      builder.append("\n");
    }
    return builder.toString();
  }

  protected String getNameAndIlvl(final String slot, final JSONObject items) {
    return String.format("%s: %s",
        ((JSONObject) items.get(slot)).get(JSON_KEY_ITEMS_NAME).toString(),
        ((JSONObject) items.get(slot)).get(JSON_KEY_ITEMS_ITEM_LEVEL).toString());
  }

  protected String getItemLevel(final JSONObject items) {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("Average (\"Bag\" ilvl): %s", items.get("averageItemLevel")));
    builder.append("\n");
    builder.append(
        String.format("Equipped (\"Real\" ilvl): %s", items.get("averageItemLevelEquipped")));
    return builder.toString();
  }

  protected String getArmoryUrl(final String continent, final String realm, final String name) {
    return String.format("http://%s.battle.net/wow/en/character/%s/%s/simple", continent, realm,
        name);
  }

  protected String getThumbnailUrl(final String continent, final String thumbnail) {
    return String.format("http://render-api-%1$s.worldofwarcraft.com/static-render/%1$s/%2$s",
        continent, thumbnail);
  }

}
