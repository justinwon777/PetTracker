package com.github.justinwon777.pettracker.networking;

import com.github.justinwon777.pettracker.core.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenTrackerPacket {
    private final ItemStack itemstack;
    private final String hand;


    public OpenTrackerPacket(ItemStack itemstack, String hand) {
        this.itemstack = itemstack;
        this.hand = hand;
    }

    public static OpenTrackerPacket decode(FriendlyByteBuf buf) {
        return new OpenTrackerPacket(buf.readItem(), buf.readUtf());
    }

    public static void encode(OpenTrackerPacket msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.itemstack);
    }

    public ItemStack getItemStack() {
        return this.itemstack;
    }

    public String getHand() { return this.hand; }

    public static void handle(OpenTrackerPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            PacketHandler.openTracker(msg);
        });
        context.get().setPacketHandled(true);
    }
}
