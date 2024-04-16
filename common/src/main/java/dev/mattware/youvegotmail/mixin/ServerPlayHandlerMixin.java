package dev.mattware.youvegotmail.mixin;

import com.mrcrayfish.mightymail.mail.DeliveryService;
import com.mrcrayfish.mightymail.network.message.MessageSendPackage;
import com.mrcrayfish.mightymail.network.play.ServerPlayHandler;
import dev.architectury.networking.NetworkManager;
import dev.mattware.youvegotmail.DeliveryServiceCustom;
import dev.mattware.youvegotmail.YouveGotMail;
import dev.mattware.youvegotmail.YouveGotMailStorage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayHandler.class)
public class ServerPlayHandlerMixin {
    @Inject(method = "lambda$handleMessageSendPackage$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;clearContent()V"))
    private static void sendToast(Container container, MessageSendPackage message, ServerPlayer player, DeliveryService service, CallbackInfo ci) {
        ServerPlayer receiver = ((DeliveryServiceCustom)service).youveGotMail$getMailboxOwner(message.getMailboxId());
        String mailboxName = ((DeliveryServiceCustom)service).youveGotMail$getMailboxName(message.getMailboxId());

        if (receiver == null) {
            // Ok, the receiver is null. It's more than likely that they're just offline. So, add it to the persistent
            // queue so that the packet(s) can be sent once they log in again. Of course, if they don't log in again,
            // the toast will remain in the queue forever. TODO: Add an expiration time? For now, fuck it we bodge
            YouveGotMailStorage.getServerState(player.server).add(((DeliveryServiceCustom)service).
                    youveGotMail$getMailboxOwnerUUID(message.getMailboxId()), player.getGameProfile().getName(), mailboxName);
            return;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(player.getGameProfile().getName());
        buf.writeUtf(mailboxName);
        NetworkManager.sendToPlayer(receiver, YouveGotMail.MAIL_RECEIVED_PACKET, buf);
    }
}
