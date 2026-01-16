# DiscordWebhook

![GitHub release (latest by date)](https://img.shields.io/github/v/release/jemsire/DiscordWebhook)
![GitHub stars](https://img.shields.io/github/stars/jemsire/DiscordWebhook?style=social)
![GitHub issues](https://img.shields.io/github/issues/jemsire/DiscordWebhook)
![GitHub pull requests](https://img.shields.io/github/issues-pr/jemsire/DiscordWebhook)
![GitHub license](https://img.shields.io/github/license/jemsire/DiscordWebhook)

A Hytale server plugin that links in-game events to a Discord channel using a webhook. This plugin automatically sends player join, leave, and chat messages to your Discord server.

## Current Features

- **Player Join Events**: Sends an embed notification when a player joins the server
- **Player Leave Events**: Sends an embed notification when a player leaves the server
- **Player Death Events**: Sends an embed notification when a player dies on the server
- **Player Chat**: Forwards all player chat messages to Discord
- **Easy Configuration**: Simple webhook URL configuration
- **Update check**: Check Github releases for updates and notifies you if there is one
- **Hot Reload**: Reload configuration without restarting the server using `/dw-reload`

## Planned Features
- **Most other events from the game**: Dont currently have all events but want to add most the main ones like deaths.
- **Toggling events**: Add toggles for events
- **Message customization**: Want to add customization for type of message(embed/raw) and its contents.

## Installation

1. Download the latest release from the [releases page](https://github.com/jemsire/DiscordWebhook/releases)
2. Place the `DiscordWebhook-x.x.x.jar` file into your Hytale server's `mods` folder
3. Start your server to generate the configuration file
4. Edit the `Jemsire_DiscordWebhook/WebhookConfig.json` file in your mods folder and add your Discord webhook URL
5. In-game type `/dw-reload` to hot reload the config to start the plugin.

## Configuration

After first launch, a `Jemsire_DiscordWebhook/WebhookConfig.json` file will be created in your mods folder. Edit this file and replace `PUT-WEBHOOK-URL-HERE` with your Discord webhook URL.

### Getting a Discord Webhook URL

1. Open your Discord server settings
2. Go to **Integrations** → **Webhooks**
3. Click **New Webhook** or select an existing webhook
4. Copy the webhook URL
5. Paste it into your `WebhookConfig.json` file

### Configuration File Structure

```json
{
  "WebhookLink": "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL_HERE",
  "Version": 1,
  "UpdateCheck": true
}
```

## Screenshots

### Configuration File
![Configuration File](assets/images/ConfigFile.png)

### Discord Messages
![Discord Messages](assets/images/DiscordExample.png)

## Commands

- `/dw-reload` - Reloads the plugin configuration without restarting the server
  - **Permission**: `discordwebhook.reload`
  - **Usage**: Only reloads if the configuration has changed

## How It Works

### Architecture

The plugin follows a modular architecture:

- **Main Plugin Class** (`DiscordWebhook.java`): Handles plugin initialization, event registration, and configuration management
- **Event Handlers**: Listen for Hytale server events and format messages for Discord
- **Webhook Sender** (`DiscordWebhookSender.java`): Handles HTTP requests to Discord's webhook API
- **Configuration System**: Manages webhook URL storage and reloading

### Event Flow

1. **Player Join** (`OnPlayerReadyEvent.java`):
   - Listens for `PlayerReadyEvent`
   - Sends a green embed with player name and join message

2. **Player Leave** (`OnPlayerDisconnectEvent.java`):
   - Listens for `PlayerDisconnectEvent`
   - Sends a red embed with player name and leave message

3. **Player Death** (`OnPlayerDeathEvent.java`):
    - Listens for `DeathSystems.OnDeathSystem` events
    - Sends a gray embed with player name and death message

4. **Player Chat** (`OnPlayerChatEvent.java`):
   - Listens for `PlayerChatEvent`
   - Sends a formatted message with player name and chat content

### Message Formatting

- **Join/Leave/Death Events**: Sent as Discord embeds with colored borders (green for join, red for leave)
- **Chat Messages**: Sent as plain text messages with player name and content
- **JSON Escaping**: All messages are properly escaped to prevent JSON injection

## Building from Source

### Prerequisites

- Java Development Kit (JDK) 25 or higher
- Gradle 8.0 or higher

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/jemsire/DiscordWebhook.git
   cd DiscordWebhook
   ```

2. Ensure `HytaleServer.jar` is in the `libs/` directory

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. The compiled JAR will be in `build/libs/DiscordWebhook-x.x.x.jar`

## Project Structure

```
DiscordWebhook/
├── src/main/java/com/jemsire/
│   ├── commands/
│   │   └── ReloadCommand.java      # Command handler for /dw-reload
│   ├── config/
│   │   └── WebhookConfig.java      # Configuration data class
│   ├── events/
│   │   ├── OnPlayerChatEvent.java      # Handles player chat messages
│   │   ├── OnPlayerDisconnectEvent.java # Handles player disconnections
│   │   ├── OnPlayerDeathEvent.java     # Handles player deaths
│   │   └── OnPlayerReadyEvent.java     # Handles player joins
│   ├── plugin/
│   │   └── DiscordWebhook.java     # Main plugin class
│   └── utils/
│       ├── DiscordWebhookSender.java   # Webhook HTTP client
│       ├── UpdateChecker.java          # Checks for updates
│       └── Logger.java                 # Logging utility
├── src/main/resources/
│   └── manifest.json                # Plugin metadata
├── build.gradle.kts                 # Gradle build configuration
└── settings.gradle.kts              # Gradle project settings
```

## Technical Details

### Dependencies

- **HytaleServer.jar**: Provided at compile time, required at runtime
- **Java Standard Library**: Uses `java.net` for HTTP connections and `javax.net.ssl` for HTTPS

### Security

- Webhook URLs are stored in configuration files (not in code)
- JSON content is properly escaped to prevent injection attacks
- Permission-based command access (`discordwebhook.reload`)

### Error Handling

- Validates webhook URL is set before sending messages
- Logs errors to server console if webhook requests fail
- Gracefully handles network errors without crashing the server

## Troubleshooting

### Messages Not Appearing in Discord

1. **Check Webhook URL**: Ensure the URL in `WebhookConfig.json` is correct
2. **Verify Webhook Status**: Check if the webhook is still active in Discord
3. **Check Server Logs**: Look for error messages in the server console
4. **Reload Config**: Use `/dw-reload` after updating the configuration

### Permission Errors

- Ensure you have the `discordwebhook.reload` permission to use the reload command
- Check your server's permission system configuration

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

**TinyTank800**

- Website: [https://jemsire.com/DiscordWebhook](https://jemsire.com/DiscordWebhook)

## Support

For issues, feature requests, or questions, please open an [Issue](https://github.com/jemsire/discordwebhook/issues).
