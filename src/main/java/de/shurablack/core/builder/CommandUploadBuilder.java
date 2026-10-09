package de.shurablack.core.builder;

import de.shurablack.core.event.annotation.command.*;
import de.shurablack.core.util.ClassScan;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandUploadBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUploadBuilder.class);

    private CommandUploadBuilder() {
        // Private constructor to prevent instantiation
    }

    public static void uploadInteractionCommands(JDA jda) {
        uploadInteractionCommands(jda, false);
    }

    /**
     * @param jda the bot
     * @param onlyChanged true to skip targets (global/guild) whose commands are already registered unchanged
     */
    public static void uploadInteractionCommands(JDA jda, boolean onlyChanged) {
        upload(jda, jda::getGuildById, onlyChanged);
    }

    /**
     * Uploads via the ready shards. Guilds are looked up across all shards
     * @param manager the {@link ShardManager} with all shards ready
     * @param onlyChanged true to skip targets (global/guild) whose commands are already registered unchanged
     */
    public static void uploadInteractionCommands(ShardManager manager, boolean onlyChanged) {
        // Global commands are application wide, any shard can register them
        upload(manager.getShards().get(0), manager::getGuildById, onlyChanged);
    }

    private static void upload(JDA jda, Function<String, Guild> guilds, boolean onlyChanged) {
        Map<String, List<CommandData>> commands = buildCommands();

        List<CommandData> global = commands.remove("GLOBAL");
        if (global != null) {
            push("global", global, jda.updateCommands(), jda.retrieveCommands(true), onlyChanged);
        }

        for (Map.Entry<String, List<CommandData>> entry : commands.entrySet()) {
            String guildId = entry.getKey();
            Guild guild = guilds.apply(guildId);
            if (guild == null) {
                LOGGER.warn("Guild with ID {} not found! Skipping command registration for this guild.", guildId);
                continue;
            }
            push("guild " + guildId, entry.getValue(), guild.updateCommands(), guild.retrieveCommands(true), onlyChanged);
        }
    }

    private static void push(String target, List<CommandData> local, CommandListUpdateAction update,
                             RestAction<List<Command>> registered, boolean onlyChanged) {
        if (!onlyChanged) {
            send(target, local, update);
            return;
        }
        registered.queue(remote -> {
            List<CommandData> remoteData = remote.stream().map(CommandData::fromCommand).collect(Collectors.toList());
            if (isUpToDate(remoteData, local)) {
                LOGGER.info("Commands for {} are up to date, skipping upload", target);
            } else {
                send(target, local, update);
            }
        }, e -> LOGGER.error("Failed to retrieve registered commands for {}!", target, e));
    }

    private static void send(String target, List<CommandData> local, CommandListUpdateAction update) {
        LOGGER.info("Registering {} commands for {}...", local.size(), target);
        update.addCommands(local).queue(
                r -> LOGGER.info("Successfully registered {} commands for {}!", local.size(), target),
                e -> LOGGER.error("Failed to register commands for {}!", target, e));
    }

    /**
     * Compares the serialized commands, independent of command and field order
     * @return true if both lists describe the same commands
     */
    static boolean isUpToDate(List<CommandData> registered, List<CommandData> local) {
        if (registered.size() != local.size()) {
            return false;
        }
        List<JSONObject> remaining = registered.stream().map(CommandUploadBuilder::toJson).collect(Collectors.toList());
        for (CommandData data : local) {
            JSONObject json = toJson(data);
            if (!remaining.removeIf(json::similar)) {
                return false;
            }
        }
        return true;
    }

    private static JSONObject toJson(CommandData data) {
        return new JSONObject(data.toData().toString());
    }

    public static Map<String, List<CommandData>> buildCommands() {
        Map<String, List<CommandData>> commands = new HashMap<>();

        try (ScanResult result = ClassScan.scan()) {
            ClassInfoList classes = result.getClassesWithAnnotation(SlashCommand.class);
            for (ClassInfo info : classes) {
                // Superclasses outside of the scanned packages
                if (info.isExternalClass()) continue;
                Class<?> workerClass = info.loadClass();

                SlashCommand command = workerClass.getAnnotation(SlashCommand.class);

                commands.computeIfAbsent(command.guildId(), x -> new ArrayList<>());
                commands.get(command.guildId()).add(extractSlashCommand(command));
            }

            classes = result.getClassesWithAnnotation(ContextCommand.class);
            for (ClassInfo info : classes) {
                // Superclasses outside of the scanned packages
                if (info.isExternalClass()) continue;
                Class<?> workerClass = info.loadClass();

                ContextCommand command = workerClass.getAnnotation(ContextCommand.class);

                commands.computeIfAbsent(command.guildId(), x -> new ArrayList<>());
                commands.get(command.guildId()).add(extractContextCommand(command));
            }
        } catch (Exception e) {
            // A partial list would delete the missing commands at discord, so upload nothing
            LOGGER.error("Failed to build slash command configuration! No commands get uploaded", e);
            commands.clear();
        }

        return commands;
    }

    private static SlashCommandData extractSlashCommand(SlashCommand command) {
        validateNotBlank(command.name(), "Slash command name");
        validateNotBlank(command.description(), "Slash command description");

        SlashCommandData data = Commands.slash(command.name(), command.description());

        addLocalizations(data::setNameLocalization, command.localizedNames());
        addLocalizations(data::setDescriptionLocalization, command.localizedDescriptions());

        if (command.subCommands().length > 0 && command.options().length > 0)
            throw new IllegalArgumentException("Slash command /" + command.name() + " cannot have options and sub commands!");

        extractSubCommand(data, command.subCommands());
        data.addOptions(extractOptionData(command.options()));

        if (command.defaultMemberPermissions().length > 0)
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.defaultMemberPermissions()));

        data.setContexts(command.contextTypes());
        data.setIntegrationTypes(command.integrationTypes());

        data.setNSFW(command.nsfw());

        return data;
    }

    private static CommandData extractContextCommand(ContextCommand command) {
        validateNotBlank(command.name(), "Context command name");
        if (command.type() == null)
            throw new IllegalArgumentException("Context command type cannot be null!");

        CommandData data = Commands.context(command.type(), command.name());

        addLocalizations(data::setNameLocalization, command.localizedNames());

        if (command.defaultMemberPermissions().length > 0)
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.defaultMemberPermissions()));

        data.setContexts(command.contextTypes());
        data.setIntegrationTypes(command.integrationTypes());
        data.setNSFW(command.nsfw());

        return data;
    }

    private static void extractSubCommand(SlashCommandData data, SubCommand[] subCommands) {
        List<SubcommandData> subDataList = new ArrayList<>(subCommands.length);
        for (SubCommand subCommand : subCommands) {
            validateNotBlank(subCommand.name(), "Subcommand name");
            validateNotBlank(subCommand.description(), "Subcommand description");

            SubcommandData subData = new SubcommandData(subCommand.name(), subCommand.description());

            addLocalizations(subData::setNameLocalization, subCommand.localizedNames());
            addLocalizations(subData::setDescriptionLocalization, subCommand.localizedDescriptions());

            subData.addOptions(extractOptionData(subCommand.options()));
            subDataList.add(subData);
        }

        data.addSubcommands(subDataList);
    }

    private static List<OptionData> extractOptionData(CommandOption[] options) {
        List<OptionData> optionData = new ArrayList<>(options.length);

        for (CommandOption option : options) {
            validateNotBlank(option.name(), "Option name");
            validateNotBlank(option.description(), "Option description");
            if (option.type() == null)
                throw new IllegalArgumentException("Option type cannot be null!");

            OptionData data = new OptionData(option.type(), option.name(), option.description(), option.required());

            addLocalizations(data::setNameLocalization, option.localizedNames());
            addLocalizations(data::setDescriptionLocalization, option.localizedDescriptions());

            if (option.maxValue() != -1)
                data.setMaxValue(option.maxValue());
            if (option.minValue() != -1)
                data.setMinValue(option.minValue());
            if (option.maxLength() != -1)
                data.setMaxLength(option.maxLength());

            for (OptionChoice choice : option.choices()) {
                data.addChoice(choice.name(), choice.value());
            }

            if (option.autoComplete())
                data.setAutoComplete(true);

            optionData.add(data);
        }

        return optionData;
    }

    private static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null, blank, or empty!");
        }
    }

    private static void addLocalizations(BiConsumer<DiscordLocale, String> localizationSetter, LocalizedText[] localizations) {
        for (LocalizedText localizedText : localizations) {
            localizationSetter.accept(localizedText.locale(), localizedText.value());
        }
    }

}
