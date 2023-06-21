# JDAdvanced
JDAvanced is an extension created by fans(me) that enhances the functionality of the popular Java Discord API Wrapper, 
[JDA](https://github.com/discord-jda/JDA). It provides additional features to simplify bot building, event creation, and database/SQL handling, among others.
> ❗️Disclaimer: As mentioned earlier, this project is solely a "fan-made" creation of mine. I am an eighth-semester student pursuing a degree in applied computer science.
> Please note that not everything may be optimized to the highest standard, but I hope you still enjoy using it.

## License
This project is licensed under the **Apache 2.0** license.<br>
**Copyright (c)** 2023 ShuraBlack<br>
For more information about the license, see [apache.org](https://www.apache.org/licenses/LICENSE-2.0)
or check the <b>LICENSE</b> file in the project

## Dependencies
❤️ Thanks to all the great programmers out there, which made all of this possible

This project requires Java 8+ [SDK](https://www.oracle.com/java/technologies/downloads/)<br>
All dependencies are managed by [Maven](https://maven.apache.org)
- [JDA](https://github.com/discord-jda/JDA) - **5.0.0-beta.10**
- [Discord-Webhooks](https://github.com/MinnDevelopment/discord-webhooks) - **0.8.2**
- [Log4j Core](https://github.com/apache/logging-log4j2) - **2.20.0**
- [Log4j API](https://github.com/apache/logging-log4j2) - **2.0.5**
- [Log4J Simple](https://github.com/apache/logging-log4j2) - **2.0.5**
- [JCABI-Log](https://github.com/jcabi/jcabi-log) - **0.22.0**
- [Commons-DBUtils](commons-dbutils) - **1.7**
- [Cron4J](https://github.com/Takuto88/cron4j) - **2.2.5**
- [Json](https://github.com/stleary/JSON-java) - **20230618**

## Packages
- **core** -> Main classess of the project
- **listener** -> Default implementation of Listener
- **mapping** -> Maps multiple Identifiers to one and gives an easier way to interact with commands
- **sql** -> FluentSQL, SQLRequest & Connectionpool

## Download
Currently this project only supports [GitHub Realse](https://github.com/ShuraBlack/JDAdvanced/releases) with a **.jar**.


## Creating the JDAUtil Object
To create the JDAUtil object, you can utilize the UtilBuilder. Prior to that, you should create a "config.properties" file in the root folder (development) or next to the **.jar** file (deployment)
that includes at least a property for the "access_token". Afterward, you can call the init function, which will load the config file, initialize the AssetPool, and LocalData.
> **HINT:** If you plan to utilize the implemented connection pool, you will also need to specify the "db_url", "db_username", "db_password", and "db_poolsize".
This builder requires a JDABuilder and an EventHandler object. For more information on building the JDA object, please refer to the [JDA](https://github.com/discord-jda/JDA) documentation.<br>

**Example:**

```java
// Initialize the UtilBuilder
UtilBuilder.init();

// Create a JDABuilder object to build the Discord bot
final JDABuilder builder = JDABuilder.createDefault(Config.getConfig("access_token"));

// Create an EventHandler object to handle events in the Discord bot
final EventHandler handler = EventHandler.create();

// Use the UtilBuilder to create a JDAUtil object for the Discord bot
final JDAUtil util = UtilBuilder.create(builder, handler).build();
```

### Configuration
Furthermore, you have the option to add commands to the command line and activate the database connection pool.<br>

**Example:**

```java
final JDAUtil util = UtilBuilder.create(builder, handler)
  // Activates the pool with provided credentials
  .addDataBase()
  // Adds another commandline action to the console
  .setCommandLineAction(
    new CommandAction("say-hello", "Says 'Hello, World!'", (String args) -> System.out.println("Hello, World!"))
    ...
  )
  .build();
```

## Creating the EventHandler Object
The EventHandler is responsible for managing JDA events and handing them off to the corresponding EventWorker. 
If any handling is missing, you can extend the class and implement your own function.
Each event will be dispatched and processed by a separate thread (make sure to create your EventWorker in a concurrent manner).
To declare functions of an EventWorker as active, you can utilize the Interaction and InteractionSet.<br>

**Example:**

```java
final EventHandler handler = EventHandler.create()
  // Set Prefix for Messages and boolean for ingoreBotRequests
  .set(EventHandler.DEFAULT_PREFIX, true)
  .registerEvent(
    InteractionSet().create(
      new EventWorkerA(),
      Interaction.create(GLOBAL_SLASH, "identifier").setGlobalCD(30),
      Interaction.create(GUILD_SLASH, "identifier")
    ),
    InteractionSet().create(
      new EventWorkerB(),
      Interaction.create(GLOBAL_SLASH, "identifier").setUserCD(10),
      Interaction.create(GUILD_SLASH, "identifier").setChannelRestriction(List.of(...))
    )
  );
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
Your Worker class should extend the EventWorker and implement all the active Interactions. Once implemented, you can register them in the EventHandler as an InteractionSet.

**Example:**

```java
public Worker extends EventWorker {

  @Override
  public void processButtonEvent(final Member member, final MessageChannelUnion channel, final String compID, final ButtonInteractionEvent event)
    // Your Implementation
}
```
