package com.jason54.unpredictableblocks.command;

import dev.architectury.event.events.common.CommandRegistrationEvent;

public final class CommandRegistration {
    private CommandRegistration() {}

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
                UnpredictableBlocksCommands.register(dispatcher));
    }
}
