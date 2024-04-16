package dev.mattware.youvegotmail.mixin;

import com.mojang.authlib.GameProfile;
import com.mrcrayfish.mightymail.mail.DeliveryService;
import com.mrcrayfish.mightymail.mail.Mailbox;
import dev.mattware.youvegotmail.DeliveryServiceCustom;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

@Mixin(DeliveryService.class)
public class DeliveryServiceMixin implements DeliveryServiceCustom {
    @Shadow @Final private Map<UUID, Mailbox> mailboxes;

    @Shadow @Final private MinecraftServer server;

    @Override
    public ServerPlayer youveGotMail$getMailboxOwner(UUID id) {
        Mailbox mailbox = mailboxes.get(id);
        if (mailbox != null) {
            var owner = mailbox.getOwner();
            return owner.map(gameProfile -> server.getPlayerList().getPlayer(gameProfile.getId())).orElse(null);
        } else return null;
    }

    @Override
    public UUID youveGotMail$getMailboxOwnerUUID(UUID id) {
        Mailbox mailbox = mailboxes.get(id);
        if (mailbox != null) {
            var owner = mailbox.getOwner();
            return owner.map(GameProfile::getId).orElse(null);
        } else return null;
    }

    @Override
    public String youveGotMail$getMailboxName(UUID id) {
        Mailbox mailbox = mailboxes.get(id);
        if (mailbox != null) {
            return mailbox.getCustomName().orElse("Mailbox");
        } else return "Mailbox";
    }
}
