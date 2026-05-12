package com.gto.registrylib.state;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.RegistryLib;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.JsonOps;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class StateDebugCommands {

    private static final DynamicCommandExceptionType INVALID_VALUE = new DynamicCommandExceptionType(
            value -> Component.literal(String.valueOf(value)));

    private StateDebugCommands() {}

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands
                        .literal("registrylib")
                        .then(Commands
                                .literal("state")
                                .then(Commands
                                        .literal("list")
                                        .requires(source -> hasPermission(source, 2))
                                        .executes(StateDebugCommands::listStates))
                                .then(Commands
                                        .literal("get")
                                        .then(Commands
                                                .argument("id", StateIdArgument.id())
                                                .suggests((ctx, builder) -> suggestStateIds(builder))
                                                .executes(StateDebugCommands::getWorldState)
                                                .then(Commands
                                                        .argument("chunkX", IntegerArgumentType.integer())
                                                        .then(Commands
                                                                .argument("chunkZ", IntegerArgumentType.integer())
                                                                .executes(StateDebugCommands::getChunkState)))))
                                .then(Commands
                                        .literal("set")
                                        .then(Commands
                                                .argument("id", StateIdArgument.id())
                                                .suggests((ctx, builder) -> suggestWritableStateIds(builder))
                                                .then(Commands
                                                        .argument("tail", StringArgumentType.greedyString())
                                                        .executes(StateDebugCommands::setState))))
                                .then(Commands
                                        .literal("debug")
                                        .then(Commands
                                                .argument("id", StateIdArgument.id())
                                                .suggests((ctx, builder) -> suggestStateIds(builder))
                                                .executes(StateDebugCommands::debugWorldState)
                                                .then(Commands
                                                        .argument("chunkX", IntegerArgumentType.integer())
                                                        .then(Commands
                                                                .argument("chunkZ", IntegerArgumentType.integer())
                                                                .executes(StateDebugCommands::debugChunkState)))))));
    }

    private static int listStates(CommandContext<CommandSourceStack> context) {
        StringBuilder builder = new StringBuilder("Registered RegistryLib states:");
        allStates().forEach(entry -> builder
                .append('\n')
                .append(entry.identifier())
                .append(" [")
                .append(entry.scope().id())
                .append(entry.debugConfig().enabled() ? ", debug" : "")
                .append(entry.debugConfig().writable() ? ", writable" : "")
                .append(']'));
        context.getSource().sendSuccess(() -> Component.literal(builder.toString()), false);
        return 1;
    }

    private static int getWorldState(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StateEntry<?> entry = requireState(context, StateScope.WORLD, false);
        requireRead(context, entry);
        ServerLevel level = context.getSource().getLevel();
        WorldStateEntry<?> world = (WorldStateEntry<?>) entry;
        Object value = world.getIfPresent(level).orElse(null);
        context.getSource().sendSuccess(() -> Component.literal(formatValue(entry, value)), false);
        return 1;
    }

    private static int getChunkState(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StateEntry<?> entry = requireState(context, StateScope.CHUNK, false);
        requireRead(context, entry);
        ChunkStateEntry<?> chunk = (ChunkStateEntry<?>) entry;
        ChunkPos pos = chunkPos(context);
        Object value = chunk.getIfLoaded(context.getSource().getLevel(), pos).orElse(null);
        context.getSource().sendSuccess(() -> Component.literal(formatValue(entry, value)), false);
        return 1;
    }

    private static <T> int setWorldState(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StateEntry<T> entry = requireTypedState(context, StateScope.WORLD);
        requireWrite(context, entry);
        T value = parseValue(StringArgumentType.getString(context, "tail"), entry);
        ((WorldStateEntry<T>) entry).set(context.getSource().getLevel(), value);
        context.getSource().sendSuccess(() -> Component.literal("Set " + entry.identifier()), true);
        return 1;
    }

    private static <T> int setChunkState(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StateEntry<T> entry = requireTypedState(context, StateScope.CHUNK);
        requireWrite(context, entry);
        ChunkSetInput input = parseChunkSetInput(context);
        T value = parseValue(input.value(), entry);
        ((ChunkStateEntry<T>) entry).set(context.getSource().getLevel(), input.pos(), value);
        context.getSource().sendSuccess(() -> Component.literal("Set " + entry.identifier()), true);
        return 1;
    }

    private static int setState(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = StateIdArgument.getId(context, "id");
        String raw = id.toString();
        String tail = StringArgumentType.getString(context, "tail");
        boolean hasChunkState = findState(id, StateScope.CHUNK).isPresent()
                || (!raw.contains(":") && findStateByPath(raw, StateScope.CHUNK).isPresent());
        if (hasChunkState && canParseChunkSetInput(tail)) {
            return setChunkState(context);
        }
        return setWorldState(context);
    }

    private static int debugWorldState(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StateEntry<?> entry = requireState(context, StateScope.WORLD, false);
        requireRead(context, entry);
        ServerLevel level = context.getSource().getLevel();
        boolean present = ((WorldStateEntry<?>) entry).getIfPresent(level).isPresent();
        context.getSource().sendSuccess(() -> Component.literal(debug(entry, present, null)), false);
        return 1;
    }

    private static int debugChunkState(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StateEntry<?> entry = requireState(context, StateScope.CHUNK, false);
        requireRead(context, entry);
        ChunkPos pos = chunkPos(context);
        boolean present = ((ChunkStateEntry<?>) entry).getIfLoaded(context.getSource().getLevel(), pos).isPresent();
        context.getSource().sendSuccess(() -> Component.literal(debug(entry, present, pos)), false);
        return 1;
    }

    private static String debug(StateEntry<?> entry, boolean present, ChunkPos pos) {
        return "State " + entry.identifier()
                + "\nscope=" + entry.scope().id()
                + "\npresent=" + present
                + (pos == null ? "" : "\nchunk=" + pos.x() + "," + pos.z())
                + "\ndebugWritable=" + entry.debugConfig().writable()
                + "\ncodec=" + entry.codec();
    }

    private static String formatValue(StateEntry<?> entry, Object value) {
        if (value == null) return entry.identifier() + " is not present";
        String encoded = encodeValue(entry, value);
        if (encoded.length() > 2048) {
            encoded = encoded.substring(0, 2048) + "...";
        }
        return entry.identifier() + " = " + encoded;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String encodeValue(StateEntry<?> entry, Object value) {
        Object encoded = ((StateEntry) entry)
                .codec()
                .encodeStart(JsonOps.INSTANCE, value)
                .result()
                .orElse(value);
        return String.valueOf(encoded);
    }

    private static <T> T parseValue(String value, StateEntry<T> entry) throws CommandSyntaxException {
        try {
            return entry
                    .codec()
                    .parse(JsonOps.INSTANCE, JsonParser.parseString(value))
                    .getOrThrow(message -> new IllegalArgumentException("Invalid state value: " + message));
        } catch (RuntimeException exception) {
            throw INVALID_VALUE.create(exception.getMessage());
        }
    }

    private static void requireRead(CommandContext<CommandSourceStack> context, StateEntry<?> entry) throws CommandSyntaxException {
        if (!entry.debugConfig().enabled()
                || !hasPermission(context.getSource(), entry.debugConfig().readPermission())) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("State debug read is disabled");
        }
    }

    private static void requireWrite(CommandContext<CommandSourceStack> context, StateEntry<?> entry) throws CommandSyntaxException {
        if (!entry.debugConfig().enabled()
                || !entry.debugConfig().writable()
                || !hasPermission(context.getSource(), entry.debugConfig().writePermission())) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("State debug write is disabled");
        }
    }

    private static boolean hasPermission(CommandSourceStack source, int permission) {
        try {
            return (Boolean) CommandSourceStack.class.getMethod("hasPermission", int.class).invoke(source, permission);
        } catch (ReflectiveOperationException exception) {
            return switch (permission) {
                case 0 -> true;
                case 1 -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR);
                case 2 -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
                case 3 -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
                default -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER);
            };
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> StateEntry<T> requireTypedState(
                                                       CommandContext<CommandSourceStack> context, StateScope scope) throws CommandSyntaxException {
        return (StateEntry<T>) requireState(context, scope, true);
    }

    private static StateEntry<?> requireState(
                                              CommandContext<CommandSourceStack> context,
                                              StateScope scope,
                                              boolean writable) throws CommandSyntaxException {
        Identifier id = StateIdArgument.getId(context, "id");
        String raw = id.toString();
        Optional<StateEntry<?>> entry = findState(id, scope);
        if (entry.isEmpty() && !raw.contains(":")) {
            entry = findStateByPath(raw, scope);
        }
        if (entry.isEmpty() || entry.get().scope() != scope || (writable && !entry.get().debugConfig().writable())) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Unknown state: " + raw);
        }
        return entry.get();
    }

    private static ChunkPos chunkPos(CommandContext<CommandSourceStack> context) {
        return new ChunkPos(
                IntegerArgumentType.getInteger(context, "chunkX"),
                IntegerArgumentType.getInteger(context, "chunkZ"));
    }

    private static Optional<StateEntry<?>> findState(Identifier id, StateScope scope) {
        return allStates().stream()
                .filter(e -> e.identifier().equals(id))
                .filter(e -> e.scope() == scope)
                .findFirst();
    }

    private static Optional<StateEntry<?>> findStateByPath(String path, StateScope scope) {
        return allStates().stream()
                .filter(e -> e.identifier().getPath().equals(path))
                .filter(e -> e.scope() == scope)
                .findFirst();
    }

    private static boolean canParseChunkSetInput(String tail) {
        try {
            parseChunkSetInput(tail);
            return true;
        } catch (CommandSyntaxException exception) {
            return false;
        }
    }

    private static ChunkSetInput parseChunkSetInput(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return parseChunkSetInput(StringArgumentType.getString(context, "tail"));
    }

    private static ChunkSetInput parseChunkSetInput(String tail) throws CommandSyntaxException {
        StringReader reader = new StringReader(tail);
        int x = reader.readInt();
        reader.skipWhitespace();
        int z = reader.readInt();
        reader.skipWhitespace();
        if (!reader.canRead()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Missing state value");
        }
        return new ChunkSetInput(new ChunkPos(x, z), reader.getString().substring(reader.getCursor()));
    }

    private static Collection<StateEntry<?>> allStates() {
        return RegistryCore
                .getRegistryCores()
                .stream()
                .flatMap(core -> core.getStateEntries().stream())
                .toList();
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestStateIds(
                                                                                                  com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        allStates()
                .stream()
                .filter(e -> e.debugConfig().enabled())
                .map(e -> e.identifier().toString())
                .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestWritableStateIds(
                                                                                                          com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        allStates()
                .stream()
                .filter(e -> e.debugConfig().enabled() && e.debugConfig().writable())
                .map(e -> e.identifier().toString())
                .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static final class StateIdArgument implements ArgumentType<Identifier> {

        private static final Collection<String> EXAMPLES = List.of("ambient_essence", "registrylibtest:ambient_essence");

        private StateIdArgument() {}

        private static StateIdArgument id() {
            return new StateIdArgument();
        }

        private static Identifier getId(CommandContext<CommandSourceStack> context, String name) {
            return context.getArgument(name, Identifier.class);
        }

        @Override
        public Identifier parse(StringReader reader) throws CommandSyntaxException {
            return Identifier.read(reader);
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }
    }

    private record ChunkSetInput(ChunkPos pos, String value) {}
}
