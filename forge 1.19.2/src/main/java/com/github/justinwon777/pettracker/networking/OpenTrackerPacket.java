package com.github.justinwon777.pettracker.networking;

import com.github.justinwon777.pettracker.EntityLocation;
import com.github.justinwon777.pettracker.core.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class OpenTrackerPacket {
    private final ItemStack itemstack;
    private final String hand;
//    private final int[] idList;
//    private final List<EntityLocation> entityList;


    public OpenTrackerPacket(ItemStack itemstack, String hand) {
        this.itemstack = itemstack;
        this.hand = hand;
//        this.idList = idList;
//        this.entityList = collection;
    }

    public static OpenTrackerPacket decode(FriendlyByteBuf buf) {
//        return new OpenTrackerPacket(buf.readItem(), buf.readVarIntArray());
        return new OpenTrackerPacket(buf.readItem(), buf.readUtf());
//        return new OpenTrackerPacket(buf.readItem(), buf.readList((entity) -> {
//            String name = entity.readUtf();
//            int x = entity.readInt();
//            int y = entity.readInt();
//            int z = entity.readInt();
//            UUID uuid = entity.readUUID();
//            return new EntityLocation(name, x, y , z, uuid);
//        }));
    }

    public static void encode(OpenTrackerPacket msg, FriendlyByteBuf buf) {
        buf.writeItem(msg.itemstack);
        buf.writeUtf(msg.hand);
//        buf.writeVarIntArray(msg.idList);
//        buf.writeCollection(msg.entityList, (buffer, entity) -> {
//            buffer.writeUtf(entity.getName());
//            buffer.writeInt(entity.getX());
//            buffer.writeInt(entity.getY());
//            buffer.writeInt(entity.getZ());
//            buffer.writeUUID(entity.getUUID());
//        });
    }

    public ItemStack getItemStack() {
        return this.itemstack;
    }

    public String getHand() { return this.hand; }

//    public List<EntityLocation> getEntityList() { return this.entityList; }

//    public int[] getIdList() { return this.idList; }

    public static void handle(OpenTrackerPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            PacketHandler.openTracker(msg);
        });
        context.get().setPacketHandled(true);
    }
}
