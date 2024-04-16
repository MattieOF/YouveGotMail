package dev.mattware.youvegotmail;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class YouveGotMailStorage extends SavedData {
    private Map<UUID, HashSet<String>> queuedToasts = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        for (UUID id : queuedToasts.keySet()) {
            CompoundTag i = new CompoundTag();

            var queue = queuedToasts.get(id);
            ListTag list = new ListTag();
            list.addAll(queue.stream().map(StringTag::valueOf).collect(Collectors.toUnmodifiableSet()));
            i.put("Data", list);

            compoundTag.put(id.toString(), i);
        }

        return compoundTag;
    }

    public static YouveGotMailStorage createFromNbt(CompoundTag tag) {
        YouveGotMailStorage state = new YouveGotMailStorage();
        var keys = tag.getAllKeys();

        for (String s : keys) {
            CompoundTag cmp = tag.getCompound(s);
            ListTag data = cmp.getList("Data", Tag.TAG_STRING);
            UUID id = UUID.fromString(s);
            HashSet<String> processedData = data.stream().map(Tag::getAsString).collect(Collectors.toCollection(HashSet::new));
            state.queuedToasts.put(id, processedData);
        }

        return state;
    }

    public void add(UUID player, String sender, String mailboxName) {
        if (queuedToasts.containsKey(player)) {
            queuedToasts.get(player).add(sender + ";" + mailboxName);
        } else {
            HashSet<String> newList = new HashSet<>();
            newList.add(sender + ";" + mailboxName);
            queuedToasts.put(player, newList);
        }
    }

    public HashSet<String> getAndRemove(UUID player) {
        var retValue = queuedToasts.getOrDefault(player, null);
        if (retValue != null)
            queuedToasts.remove(player);
        return retValue;
    }

    public static YouveGotMailStorage getServerState(MinecraftServer server) {
        // Following comments are from the fabric docs. They use their own mappings, which are different to vanilla
        // minecraft, which I'm using here. So it's a bit fucked. The 3rd line is completely different to the docs.

        // (Note: arbitrary choice to use 'World.OVERWORLD' instead of 'World.END' or 'World.NETHER'.  Any work)
        DimensionDataStorage persistentStateManager = server.getLevel(Level.OVERWORLD).getDataStorage();

        // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateSaverAndLoader' and
        // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
        // 'StateSaverAndLoader' NBT on disk to our function 'StateSaverAndLoader::createFromNbt'.
        YouveGotMailStorage state = persistentStateManager.computeIfAbsent(YouveGotMailStorage::createFromNbt, YouveGotMailStorage::new, YouveGotMail.MOD_ID);

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.setDirty();

        return state;
    }
}
