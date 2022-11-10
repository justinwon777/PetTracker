package com.github.justinwon777.pettracker.networking;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static com.github.justinwon777.pettracker.item.Tracker.TRACKING;

public class RemovePacket {
    private final UUID id;
    private final ItemStack tracker;
    private final String hand;


    public RemovePacket(UUID id, ItemStack tracker, String hand) {
        this.id = id;
        this.tracker = tracker;
        this.hand = hand;
    }

    public static RemovePacket decode(PacketBuffer buf) {
        return new RemovePacket(buf.readUniqueId(), buf.readItemStack(), buf.readString());
    }

    public static void encode(RemovePacket msg, PacketBuffer buf) {
        buf.writeUniqueId(msg.id);
        buf.writeItemStack(msg.tracker);
        buf.writeString(msg.hand);
    }

    public UUID getUUID() {
        return this.id;
    }

    public ItemStack getTracker() { return this.tracker; }

    public String getHand() {return this.hand; }

    public static void handle(RemovePacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if (player != null && player.world instanceof ServerWorld) {
                ItemStack tracker = msg.getTracker();
                Hand hand;
                if (Objects.equals(msg.getHand(), "m")) {
                    hand = Hand.MAIN_HAND;
                } else {
                    hand = Hand.OFF_HAND;
                }
                CompoundNBT tag = tracker.getTag();
                ListNBT listTag = tag.getList(TRACKING, 10);
                int i = 0;
                while (i < listTag.size()) {
                    CompoundNBT entityTag = listTag.getCompound(i);
                    if (entityTag.getUniqueId("uuid").equals(msg.getUUID())) {
                        listTag.remove(i);
                        player.setHeldItem(hand, tracker);
                        break;
                    }
                    i++;
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
