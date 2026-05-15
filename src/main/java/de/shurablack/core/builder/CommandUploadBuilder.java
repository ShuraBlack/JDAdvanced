package de.shurablack.core.builder;

import de.shurablack.core.event.annotation.command.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CommandUploadBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUploadBuilder.class);

    private CommandUploadBuilder() {
        // Private constructor to prevent instantiation
    }

    public static void uploadInteractionCommands(JDA jda) {
        Map<String, List<CommandData>> commands = buildCommands();

        List<CommandData> global = commands.remove("GLOBAL");
        if (global != null) {
            LOGGER.info("Registering {} global commands...", global.size());
            jda.updateCommands().addCommands(global).queue(
                    r -> LOGGER.info("Successfully registered {} global commands!", global.size()),
                    e -> LOGGER.error("Failed to register global commands!", e));
        }

        for (Map.Entry<String, List<CommandData>> entry : commands.entrySet()) {
            String guildId = entry.getKey();
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                LOGGER.warn("Guild with ID {} not found! Skipping command registration for this guild.", guildId);
                continue;
            }
            List<CommandData> guildCommands = entry.getValue();

            LOGGER.info("Registering {} commands for guild {}...", guildCommands.size(), guildId);
            guild.updateCommands().addCommands(guildCommands).queue(
                    r -> LOGGER.info("Successfully registered {} commands for guild {}!", guildCommands.size(), guildId),
                    e -> LOGGER.error("Failed to register commands for guild {}!", guildId, e));
        }
    }

    public static Map<String, List<CommandData>> buildCommands() {
        Map<String, List<CommandData>> commands = new HashMap<>();

        try (ScanResult result = new ClassGraph().enableAllInfo().acceptPackages("").scan()) {
            ClassInfoList classes = result.getClassesWithAnnotation(SlashCommand.class);
            for (ClassInfo info : classes) {
                Class<?> workerClass = info.loadClass();

                SlashCommand command = workerClass.getAnnotation(SlashCommand.class);

                commands.computeIfAbsent(command.guildId(), x -> new ArrayList<>());
                commands.get(command.guildId()).add(extractSlashCommand(command));
            }

            classes = result.getClassesWithAnnotation(ContextCommand.class);
            for (ClassInfo info : classes) {
                Class<?> workerClass = info.loadClass();

                ContextCommand command = workerClass.getAnnotation(ContextCommand.class);

                commands.computeIfAbsent(command.guildId(), x -> new ArrayList<>());
                commands.get(command.guildId()).add(extractContextCommand(command));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to build slash command configuration!", e);
        }

        return commands;
    }

    private static SlashCommandData extractSlashCommand(SlashCommand command) {
        validateNotBlank(command.name(), "Slash command name");
        validateNotBlank(command.description(), "Slash command description");

        SlashCommandData data = Commands.slash(command.name(), command.description());

        addLocalizations(data::setNameLocalization, command.localizedNames());
        addLocalizations(data::setDescriptionLocalization, command.localizedDescriptions());

        extractSubCommand(data, command.subCommands());

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
