package dev.mattware.youvegotmail.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.mattware.youvegotmail.YouveGotMail;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(YouveGotMail.MOD_ID)
public class YouveGotMailForge {
    public YouveGotMailForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(YouveGotMail.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        YouveGotMail.init();
    }
}