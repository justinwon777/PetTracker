package com.github.justinwon777.pettracker.networking;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TeleportPacket {
    private final UUID id;


    public TeleportPacket(UUID id) {
        this.id = id;
    }

    public static TeleportPacket decode(PacketBuffer buf) {
        return new TeleportPacket(buf.readUniqueId());
    }

    public static void encode(TeleportPacket msg, PacketBuffer buf) {
        buf.writeUniqueId(msg.id);
    }

    public UUID getUUID() {
        return this.id;
    }

    public static void handle(TeleportPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayerEntity player = context.get().getSender();
            if (player != null && player.world instanceof ServerWorld) {
                ServerWorld level = (ServerWorld) player.world;
                MobEntity entity = (MobEntity) level.getEntityByUuid(msg.getUUID());
                if (entity != null) {
                    BlockPos blockpos = player.getPosition();
                    if (msg.canTeleportTo(blockpos, (ServerWorld) player.world, entity)) {
                        entity.setLocationAndAngles(blockpos.getX(), blockpos.getY(), blockpos.getZ(),
                                entity.rotationYaw,
                                entity.rotationPitch);
                        entity.getNavigator().clearPath();
                    } else {
                        player.sendMessage(new StringTextComponent("Cannot teleport to current location"),
                                player.getUniqueID());
                    }
                } else {
                    player.sendMessage(new StringTextComponent("Mob doesn't exist"), player.getUniqueID());
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    private boolean canTeleportTo(BlockPos pPos, ServerWorld level, Entity entity) {
        PathNodeType blockpathtypes = WalkNodeProcessor.getFloorNodeType(level, pPos.toMutable());
        if (blockpathtypes != PathNodeType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = level.getBlockState(pPos.down());
            if (blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pPos.subtract(entity.getPosition());
                return level.hasNoCollisions(entity, entity.getBoundingBox().offset(blockpos));
            }
        }
    }
}
