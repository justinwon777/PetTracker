package com.github.justinwon777.pettracker.item;

import com.github.justinwon777.pettracker.PetTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.checkerframework.checker.units.qual.C;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = PetTracker.MOD_ID)
public class EventHandler {

//    public static final String LOAD_CHUNK = "load_chunk";
//    public static final String PREV_CHUNK = "prev_chunk";
//    public static final String CHUNK_TIMER = "chunk_timer";

    @SubscribeEvent
    public static void trackerInteract(final PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity target) {
            Player player = event.getEntity();
            InteractionHand hand = event.getHand();
            ItemStack itemstack = player.getItemInHand(hand);

            if (!target.level.isClientSide) {
                if (itemstack.getItem() instanceof Tracker) {
                    itemstack.getItem().interactLivingEntity(itemstack, player, target, hand);
                    event.setCanceled(true);
                }
            }
        }
    }

//    @SubscribeEvent
//    public static void livingTick(final LivingEvent.LivingTickEvent event) {
//        LivingEntity entity = event.getEntity();
//        if (!(entity.level instanceof ServerLevel level)) return;
//        CompoundTag tag = entity.getPersistentData();
//        if (tag.contains(LOAD_CHUNK) && tag.getBoolean(LOAD_CHUNK)) {
//            ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
//            if (tag.contains(PREV_CHUNK)) {
//                long prevChunk = tag.getLong(PREV_CHUNK);
//                ChunkPos prevChunkPos = new ChunkPos(ChunkPos.getX(prevChunk), ChunkPos.getZ(prevChunk));
//                if (!chunkPos.equals(prevChunkPos)) {
////                    System.out.println("not prev");
//                    if (!isSpawnOrForced(level, prevChunkPos)) {
////                        System.out.println("not forced and not prev");
////                    level.setChunkForced(prevChunkPos.x, prevChunkPos.z, false);
//                    level.getChunkSource().updateChunkForced(prevChunkPos, false);
//                    }
//                } else {
//                    return;
//                }
//            }
////            System.out.println("no prev");
//            if (tag.contains(CHUNK_TIMER)) {
//                int timer = tag.getInt(CHUNK_TIMER);
//                if (timer > 0) {
//                    level.getChunkSource().updateChunkForced(chunkPos, true);
//                    tag.putInt(CHUNK_TIMER, timer - 1);
//                }
//            } else {
//                if (!isSpawnOrForced(level, chunkPos)) {
//                    level.getChunkSource().updateChunkForced(chunkPos, false);
//                }
//                tag.putBoolean(LOAD_CHUNK, false);
//            }
////            level.setChunkForced(chunkPos.x, chunkPos.z, true);
////            System.out.println(level.getForcedChunks());
//            tag.putLong(PREV_CHUNK, chunkPos.toLong());
//        }
//    }

//    public static boolean isSpawnOrForced(ServerLevel level, ChunkPos pos) {
//        LevelData levelData = level.getLevelData();
//        ChunkPos spawnChunk = new ChunkPos(new BlockPos(levelData.getXSpawn(), 0, levelData.getZSpawn()));
//        Stream<ChunkPos> spawnChunks = ChunkPos.rangeClosed(spawnChunk, 11);
//
//        for (long values : level.getForcedChunks()) {
//            if (pos.equals(new ChunkPos(ChunkPos.getX(values), ChunkPos.getZ(values)))) {
//                return true;
//            }
//        }
//
//        return spawnChunks.anyMatch(chunk -> chunk.equals(pos));
//    }
}
