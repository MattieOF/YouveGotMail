package dev.mattware.youvegotmail;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YouveGotMail
{
	public static final String MOD_ID = "youvegotmail";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ResourceLocation MAIL_RECEIVED_PACKET = new ResourceLocation("youvegotmail", "mail_received");

	public static void init() {
		LOGGER.info("You've got mail!! Just kidding... but the mod is initialising :)");

		EnvExecutor.runInEnv(Env.CLIENT, () -> YouveGotMailClient::initClient);

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
