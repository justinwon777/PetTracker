package com.github.justinwon777.pettracker.networking;

import com.github.justinwon777.pettracker.core.PacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenTrackerPacket {
    private final ItemStack itemstack;
    private final String hand;
    private final double px;
    private final double py;
    private final double pz;

    public OpenTrackerPacket(ItemStack itemstack, String hand, double x, double y, double z) {
        this.itemstack = itemstack;
        this.hand = hand;
        this.px = x;
        this.py = y;
        this.pz = z;
    }

    public static OpenTrackerPacket decode(PacketBuffer buf) {
        return new OpenTrackerPacket(buf.readItemStack(), buf.readString(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void encode(OpenTrackerPacket msg, PacketBuffer buf) {
        buf.writeItemStack(msg.itemstack);
        buf.writeString(msg.hand);
        buf.writeDouble(msg.px);
        buf.writeDouble(msg.py);
        buf.writeDouble(msg.pz);
    }

    public ItemStack getItemStack() {
        return this.itemstack;
    }

    public String getHand() { return this.hand; }

    public double getX() { return this.px; }

    public double getY() { return this.py; }

    public double getZ() { return this.pz; }

    public static void handle(OpenTrackerPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            PacketHandler.openTracker(msg);
        });
        context.get().setPacketHandled(true);
    }
}
