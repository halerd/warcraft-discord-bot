# warcraft-discord-bot

## Synopsis

A Java bot for Discord using the JDA library.

Retrieves information about a given World of Warcraft character

## Example

Build and run the bot

```
mvn clean install
java -Dapp.bot.user.token=YOUR_APP_BOT_USER_TOKEN -Dblizzard.api.key=YOUR_BLIZZARD_API_KEY -jar target\discord-bot-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

* https://discordapp.com/developers/applications/me
* https://dev.battle.net/

## Discord Setup

* Start creating an application: https://discordapp.com/developers/applications/me/create
* Enter a suitable `APP NAME` e.g. Warcraft Discord Bot
* Click `Create App`
* `Next screen is displayed`
* Click `Create a Bot User`
* Click `Yes, do it!`
* Copy `Client ID` value (YOUR_CLIENT_ID_HERE)
* Click `Token: click to reveal` and copy value (YOUR_APP_BOT_USER_TOKEN)
* Add the bot to your server: https://discordapp.com/oauth2/authorize?&client_id=YOUR_CLIENT_ID_HERE&scope=bot
* Select a server in the dropdown
* Click `Authorize`