package dev.mattware.youvegotmail.fabric;

import dev.mattware.youvegotmail.YouveGotMail;
import net.fabricmc.api.ModInitializer;

public class YouveGotMailFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        YouveGotMail.init();
    }
}