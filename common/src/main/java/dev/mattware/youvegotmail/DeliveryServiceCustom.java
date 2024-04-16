package dev.mattware.youvegotmail;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public interface DeliveryServiceCustom {
    default ServerPlayer youveGotMail$getMailboxOwner(UUID id) {
        return null;
    }

    default UUID youveGotMail$getMailboxOwnerUUID(UUID id) {
        return null;
    }

    default String youveGotMail$getMailboxName(UUID id) {
        return null;
    }
}
