package dev.mattware.youvegotmail;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YouveGotMail
{
	public static final String MOD_ID = "youvegotmail";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ResourceLocation MAIL_RECEIVED_PACKET = new ResourceLocation("youvegotmail", "mail_received");

	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(MOD_ID, Registries.SOUND_EVENT);
	public static final ResourceLocation YOUVE_GOT_MAYO_ID = new ResourceLocation(MOD_ID, "youvegotmayo");
	public static final RegistrySupplier<SoundEvent> YOUVE_GOT_MAYO = SOUND_EVENTS.register(YOUVE_GOT_MAYO_ID, () -> SoundEvent.createVariableRangeEvent(YOUVE_GOT_MAYO_ID));
	public static SimpleSoundInstance YOUVE_GOT_MAYO_INST;

	public static void init() {
		LOGGER.info("You've got mail!! Just kidding... but the mod is initialising :)");

		SOUND_EVENTS.register();

		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
			NetworkManager.registerReceiver(NetworkManager.Side.S2C, MAIL_RECEIVED_PACKET, (buf, context) -> {
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
		});

		PlayerEvent.PLAYER_JOIN.register(player -> {
			var queuedToasts = YouveGotMailStorage.getServerState(player.server).getAndRemove(player.getGameProfile().getId());
			if (queuedToasts != null) {
				LOGGER.info("Player " + player.getGameProfile().getName() + " has " + queuedToasts.size() + " queued toasts, sending them over...");
				for (String s : queuedToasts) {
					FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
					int i = s.lastIndexOf(';');
					String[] parts = {s.substring(0, i), s.substring(i+1)};
					buf.writeUtf(parts[0]); // sender
					buf.writeUtf(parts[1]); // mailbox name
					NetworkManager.sendToPlayer(player, YouveGotMail.MAIL_RECEIVED_PACKET, buf);
				}
			}
		});
	}
}
