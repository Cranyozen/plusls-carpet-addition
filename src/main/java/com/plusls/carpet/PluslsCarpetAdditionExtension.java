package com.plusls.carpet;

import com.mojang.brigadier.CommandDispatcher;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import com.plusls.carpet.util.rule.flippingTotemOfUndying.FlipCooldown;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import top.hendrixshen.magiclib.carpet.api.CarpetExtensionCompatApi;
import top.hendrixshen.magiclib.carpet.impl.WrappedSettingManager;

public class PluslsCarpetAdditionExtension implements CarpetExtensionCompatApi {
    private static final PluslsCarpetAdditionSettingManager SETTINGS_MANAGER = new PluslsCarpetAdditionSettingManager(
            PluslsCarpetAdditionReference.getModVersion(),
            PluslsCarpetAdditionReference.getModIdentifier(),
            PluslsCarpetAdditionReference.getModName());
    private static MinecraftServer server;

    public static PluslsCarpetAdditionSettingManager getSettingsManager() {
        return SETTINGS_MANAGER;
    }

    public static MinecraftServer getServer() {
        return PluslsCarpetAdditionExtension.server;
    }

    @Override
    public WrappedSettingManager getSettingsManagerCompat() {
        return SETTINGS_MANAGER;
    }

    @Override
    public void registerCommandCompat(CommandDispatcher<CommandSourceStack> dispatcher) {
    }

    @Override
    public void onGameStarted() {
        if (PluslsCarpetAdditionSettings.pcaDebug) {
            Configurator.setLevel(PluslsCarpetAdditionReference.getModIdentifier(), Level.DEBUG);
        }
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        PluslsCarpetAdditionExtension.server = server;
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayer player) {
        PcaSyncProtocol.clearPlayerWatchData(player);
        FlipCooldown.removePlayer(player);
    }
}
