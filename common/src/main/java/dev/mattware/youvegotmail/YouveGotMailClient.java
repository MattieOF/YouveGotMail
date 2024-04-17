package dev.mattware.youvegotmail;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import static dev.mattware.youvegotmail.YouveGotMail.MOD_ID;

@Environment(EnvType.CLIENT)
public class YouveGotMailClient {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(MOD_ID, Registries.SOUND_EVENT);
    public static final ResourceLocation YOUVE_GOT_MAYO_ID = new ResourceLocation(MOD_ID, "youvegotmayo");
    public static final RegistrySupplier<SoundEvent> YOUVE_GOT_MAYO = SOUND_EVENTS.register(YOUVE_GOT_MAYO_ID, () -> SoundEvent.createVariableRangeEvent(YOUVE_GOT_MAYO_ID));
    public static SimpleSoundInstance YOUVE_GOT_MAYO_INST;

    public static void initClient() {
        SOUND_EVENTS.register();

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, YouveGotMail.MAIL_RECEIVED_PACKET, (buf, context) -> {
            String sender = buf.readUtf();
            String mailbox = buf.readUtf();
            SystemToast toast = new SystemToast(SystemToast.SystemToastIds.TUTORIAL_HINT,
                    Component.translatable("youvegotmail.toast.mail_received.title"),
                    Component.translatable("youvegotmail.toast.mail_received.desc", sender, mailbox));
            Minecraft.getInstance().getToasts().addToast(toast);

            // Sound stuff
            // Lazy init because it needs a registry entry
            if (YOUVE_GOT_MAYO_INST == null) {
                YOUVE_GOT_MAYO_INST = SimpleSoundInstance.forUI(YOUVE_GOT_MAYO.get(), 1f, 0.2f);
            }
            // Don't play over each other
            if (!Minecraft.getInstance().getSoundManager().isActive(YOUVE_GOT_MAYO_INST)) {
                Minecraft.getInstance().getSoundManager().play(YOUVE_GOT_MAYO_INST);
            }
        });
    }
}
