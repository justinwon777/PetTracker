package com.github.justinwon777.pettracker.networking;

import com.github.justinwon777.pettracker.core.PacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenTrackerPacket {
    private final ItemStack itemstack;
    private final String hand;


    public OpenTrackerPacket(ItemStack itemstack, String hand) {
        this.itemstack = itemstack;
        this.hand = hand;
    }

    public static OpenTrackerPacket decode(PacketBuffer buf) {
        return new OpenTrackerPacket(buf.readItemStack(), buf.readString());
    }

    public static void encode(OpenTrackerPacket msg, PacketBuffer buf) {
        buf.writeItemStack(msg.itemstack);
        buf.writeString(msg.hand);
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
