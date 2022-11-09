package com.github.justinwon777.pettracker.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class GetEntityPacket {
    private final UUID id;
    public static Entity entity;


    public GetEntityPacket(UUID id) {
        this.id = id;
    }

    public static GetEntityPacket decode(FriendlyByteBuf buf) {
        return new GetEntityPacket(buf.readUUID());
    }

    public static void encode(GetEntityPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.id);
    }

    public UUID getUUID() {
        return this.id;
    }

    public static void handle(GetEntityPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.level instanceof ServerLevel level) {
                entity = level.getEntity(msg.getUUID());
            }
        });
        context.get().setPacketHandled(true);
    }
}
