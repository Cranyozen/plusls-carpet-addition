package com.plusls.carpet;

import com.plusls.carpet.impl.network.PcaSyncProtocol;
import com.plusls.carpet.util.rule.dispenserCollectXp.GlassBottleDispenserBehavior;
import com.plusls.carpet.util.rule.dispenserFixIronGolem.IronIngotDispenserBehavior;
import com.plusls.carpet.util.rule.flippingTotemOfUndying.FlipCooldown;
import com.plusls.carpet.util.rule.gravestone.GravestoneUtil;
import com.plusls.carpet.util.rule.gravestone.PendingGraveLoot;
import com.plusls.carpet.util.rule.potionRecycle.PotionDispenserBehavior;
import com.plusls.carpet.util.rule.sleepingDuringTheDay.SleepUtil;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import top.hendrixshen.magiclib.carpet.impl.WrappedSettingManager;
import top.hendrixshen.magiclib.util.MiscUtil;

@SuppressWarnings("removal")
public class PluslsCarpetAddition implements ModInitializer, DedicatedServerModInitializer {
    @Override
    public void onInitialize() {
        WrappedSettingManager.register(PluslsCarpetAdditionReference.getModIdentifier(),
                PluslsCarpetAdditionExtension.getSettingsManager(), new PluslsCarpetAdditionExtension());
        GravestoneUtil.init();
        ServerTickEvents.START_SERVER_TICK.register(server -> PendingGraveLoot.tick());
        SleepUtil.init();
        IronIngotDispenserBehavior.init();
        GlassBottleDispenserBehavior.init();
        PotionDispenserBehavior.init();
        PluslsCarpetAdditionExtension.getSettingsManager().parseSettingsClass(PluslsCarpetAdditionSettings.class);
        PluslsCarpetAdditionExtension.getSettingsManager().registerRuleCallback((source, rule, value) -> {
            if (rule.getName().equals("pcaSyncProtocol")) {
                if (rule.getRule().getBoolValue()) {
                    PcaSyncProtocol.enablePcaSyncProtocolGlobal();
                } else {
                    PcaSyncProtocol.disablePcaSyncProtocolGlobal();
                }
            } else if (rule.getName().equals("pcaDebug")) {
                Configurator.setLevel(PluslsCarpetAdditionReference.getModIdentifier(), MiscUtil.cast(rule.getValue()) ? Level.DEBUG : Level.INFO);
            }
        });
        FlipCooldown.init();
    }

    @Override
    public void onInitializeServer() {
        PcaSyncProtocol.init();
    }
}
