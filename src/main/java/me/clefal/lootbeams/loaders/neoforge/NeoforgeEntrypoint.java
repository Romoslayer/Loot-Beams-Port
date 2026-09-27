//? if neoforge {

package me.clefal.lootbeams.loaders.neoforge;

import com.mojang.logging.LogUtils;
import me.clefal.lootbeams.LootBeamsRefork;
import me.clefal.lootbeams.config.configs.ConfigManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod("lootbeams")
public class NeoforgeEntrypoint {
    private static final Logger LOGGER = LogUtils.getLogger();

    public NeoforgeEntrypoint(Dist dist) {
        LootBeamsRefork.initialize();
        // Register every config up front, as the Fabric and Forge entrypoints already do. Otherwise
        // each one only registers when its class is first touched, so Light Config, Loot Information
        // and Custom Config stay missing from the config screen until a beam or tooltip renders.
        if (dist.isClient()) {
            ConfigManager.init();
        }
    }
}

//?}
