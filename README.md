# JDAdvanced
JDAvanced is an extension created by fans(me) that enhances the functionality of the popular Java Discord API Wrapper, 
[JDA](https://github.com/discord-jda/JDA). It provides additional features to simplify bot building, event creation, and more.
> ❗️Disclaimer: As mentioned earlier, this project is solely a "fan-made" creation of mine
> Please note that not everything may be optimized to the highest standard, but I hope you still enjoy using it.

## License
This project is licensed under the **Apache 2.0** license.<br>
**Copyright (c)** 2023 ShuraBlack<br>
For more information about the license, see [apache.org](https://www.apache.org/licenses/LICENSE-2.0)
or check the <b>LICENSE</b> file in the project

## Dependencies
❤️ Thanks to all the great programmers out there, which made all of this possible

This project requires Java 11+ [SDK](https://www.oracle.com/java/technologies/downloads/)<br>
All dependencies are managed by [Maven](https://maven.apache.org)
- [JDA](https://github.com/discord-jda/JDA) - **6.4.1**
- [Discord-Webhooks](https://github.com/MinnDevelopment/discord-webhooks) - **0.8.2**
- [SLF4J API](https://github.com/qos-ch/slf4j) - **2.0.17** *(bring your own SLF4J implementation, e.g. Logback or Log4j2)*
- [Cron4J](https://github.com/Takuto88/cron4j) - **2.2.5**
- [Json](https://github.com/stleary/JSON-java) - **20231013**
- [Classgraph](https://github.com/classgraph/classgraph) - **4.8.179**
- [JUnit](https://github.com/junit-team/junit5) - **5.13.4** *(Test scope)*
- [Mockito](https://github.com/mockito/mockito) - **5.14.2** *(Test scope)*

## Packages
- **core** -> Main classess of the project
- **listener** -> Default implementation of Listener
- **mapping** -> Maps multiple Identifiers to one and gives an easier way to interact with commands
- **localization** -> Language and timezone handling

## Download
JDAdvanced is available via [JitPack](https://jitpack.io/#ShuraBlack/JDAdvanced). Replace `VERSION` with a release tag (e.g. `1.1.0`).

**Maven:**
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ShuraBlack</groupId>
    <artifactId>JDAdvanced</artifactId>
    <version>VERSION</version>
</dependency>
```

**Gradle:**
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.ShuraBlack:JDAdvanced:VERSION'
}
```

Alternatively, the **.jar** is attached to each [GitHub Release](https://github.com/ShuraBlack/JDAdvanced/releases).


## Creating the JDAUtil Object
To create the JDAUtil object, you can utilize the UtilBuilder. Prior to that, you should create a "config.properties" file in the root folder (development) or next to the **.jar** file (deployment)
that includes at least a property for the "access_token". Copy the [config.example.properties](config.example.properties) to get all available options. Afterward, you can call the init function, which will load the config file, initialize the AssetPool, and LocalData.
> **HINT:** Set `commands.auto-upload=true` to upload your slash and context commands on launch. Only commands that changed compared to the ones registered at Discord get uploaded.
> In the console, `upload` does the same on demand and `upload force` uploads everything.

This builder requires a JDABuilder and an EventHandler object. For more information on building the JDA object, please refer to the [JDA](https://github.com/discord-jda/JDA) documentation.<br>

**Example:**

```java
// Initialize the UtilBuilder
// Optional: pass your base packages, e.g. UtilBuilder.init("de.mybot"), to only scan those for workers, cron tasks and commands
UtilBuilder.init();

// Create a JDABuilder object to build the Discord bot
final JDABuilder builder = JDABuilder.createDefault(Config.getConfig("access_token"));

// Create an EventHandler object to handle events in the Discord bot
final EventHandler handler = EventHandler.create();

// Use the UtilBuilder to create a JDAUtil object for the Discord bot
final JDAUtil util = UtilBuilder.create(builder, handler).build();
```

### Configuration
Furthermore, you have the option to add commands to the command line.<br>

**Example:**

```java
final JDAUtil util = UtilBuilder.create(builder, handler)
  // Adds another commandline action to the console
  .setCommandLineAction(
    new CommandAction("say-hello", "Says 'Hello, World!'", (String args) -> System.out.println("Hello, World!"))
    ...
  )
  .build();
```

## Creating the EventHandler Object
The EventHandler validates incoming events (bots, channel restrictions, cooldowns, permissions) and hands them off to the corresponding EventWorker.
The `NativeEventHandler` runs the workers on the JDA event thread, the `DispatchEventHandler` on a thread pool (make sure your workers are thread safe).
Workers and command handlers are declared with annotations and discovered on startup.

**Example:**

```java
final EventHandler handler = DispatchEventHandler
    // Message prefix, ignore bots, ephemeral error feedback, admin bypass
    .create("!", true, true, false)
    .registerEvent(InteractionSet.fromAnnotation());
```

Cooldowns (in seconds), channel restrictions and required permissions can be set per process.
Permissions are checked in the channel of the event (channel overrides count) and deny the event outside of guilds.
```java
@EventProcess(identifier = "ban", userCooldown = 5, requiredPermissions = Permission.BAN_MEMBERS)
@Override
public void processGuildSlashEvent(final Member member, final MessageChannelUnion channel, final String name, final SlashCommandInteractionEvent event) {
    // Only reached by members with the ban permission
}
```

## Write Listener
Listeners should maintain a reference to the EventHandler in order to pass the JDA events. Default implementations can be found in the _de.shurablack.listener_ package.

**Example:**

```java
public class DefaultInteractionReceiver extends ListenerAdapter {

    private final EventHandler handler;

    public DefaultInteractionReceiver(EventHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        handler.onModalEvent(event.getModalId(), event);
    }
}
```

## Write EventWorker
Your Worker class should extend the EventWorker and override the processes it handles. Annotate each with `@EventProcess`, `InteractionSet.fromAnnotation()` picks them up.

**Example:**

```java
public class Worker extends EventWorker {

  @EventProcess(identifier = "confirm")
  @Override
  public void processButtonEvent(final Member member, final MessageChannelUnion channel, final String compID, final ButtonInteractionEvent event) {
    // Your Implementation
  }
}
```

### Auto Complete
Mark an option with `autoComplete = true` and suggest values while the user types. The identifier is the command name.
Discord accepts up to 25 choices within 3 seconds. They are only suggestions, so validate the submitted value in the command itself.

```java
@CommandOption(type = OptionType.STRING, name = "song", description = "The song to play", autoComplete = true)

@EventProcess(identifier = "play")
@Override
public void processAutoCompleteEvent(final User user, final MessageChannelUnion channel, final String option, final CommandAutoCompleteInteractionEvent event) {
    final String typed = event.getFocusedOption().getValue();
    event.replyChoiceStrings(songs.search(typed, 25)).queue();
}
```

### Command Handler
Instead of switching over the sub command name, annotate one method per command or sub command. Options get converted to the parameter type,
everything else (`SlashCommandInteractionEvent`, `Member`, `User`, `Guild`, `MessageChannelUnion`, `DiscordLocale`) gets injected.
Options which werent provided are `null`. The class doesnt need to extend `EventWorker` and gets discovered by `InteractionSet.fromAnnotation()`.

```java
@SlashCommand(name = "music", description = "Music commands", subCommands = {
    @SubCommand(name = "play", description = "Plays a song", options = {
        @CommandOption(type = OptionType.STRING, name = "title", description = "The title", required = true),
        @CommandOption(type = OptionType.INTEGER, name = "volume", description = "The volume")
    })
})
public class MusicCommands {

    @CommandHandler(command = "music", sub = "play", userCooldown = 5)
    void play(SlashCommandInteractionEvent event, Member member, @Option("title") String title, @Option("volume") Integer volume) {
        event.reply("Playing " + title).queue();
    }
}
```
Commands without sub commands declare their options directly on `@SlashCommand` (Discord doesnt allow both) and leave out `sub`:
```java
@SlashCommand(name = "ping", description = "Pings a user", options = {
    @CommandOption(type = OptionType.USER, name = "target", description = "Who to ping")
})
public class PingCommand {

    @CommandHandler(command = "ping")
    void ping(SlashCommandInteractionEvent event, @Option("target") User target) { ... }
}
```
Cooldowns, channel restrictions and permissions apply per handler. The handlers get validated against the `@SlashCommand` declaration on startup:
an unknown option, a type that doesnt fit the option type or a primitive for an optional option throws an `ImplementationException` right away.

## Localization
Load one `.properties` file per language, named after the language (`de.properties`) or a Discord locale tag (`en-US.properties`, `pt-BR.properties`).
`Language.get(event, "code")` picks the language of the user who triggered the interaction. It falls back key by key: full locale (`en-US`) -> base language (`en`) -> default language.

```java
Language.loadLanguageFile("lang/en.properties");
Language.loadLanguageFile("lang/de.properties");
Language.setDefaultLanguage("en");

event.reply(Language.get(event, "welcome")).queue();
```

The error messages of the event handlers (cooldown, wrong channel, ...) can be translated with the keys
`validation.is_bot`, `validation.no_event`, `validation.wrong_channel`, `validation.on_cooldown` and `validation.no_permission`.
Without a translation the english default is used.
