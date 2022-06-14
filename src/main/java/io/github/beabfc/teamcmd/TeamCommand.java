package io.github.beabfc.teamcmd;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TeamCommand implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> CommandBuilder.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(TeamUtil::tick);
    }
}
