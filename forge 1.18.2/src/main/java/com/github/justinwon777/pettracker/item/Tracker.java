package com.github.justinwon777.pettracker.item;

import com.github.justinwon777.pettracker.core.PacketHandler;
import com.github.justinwon777.pettracker.networking.OpenTrackerPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class Tracker extends Item {

    public static final String TRACKING = "Tracking";

    public Tracker(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget,
                                                  InteractionHand pUsedHand) {
        if (pInteractionTarget.level.isClientSide) return InteractionResult.SUCCESS;
        if (pInteractionTarget instanceof TamableAnimal) {
            if (((TamableAnimal) pInteractionTarget).isTame()) {
                if (pInteractionTarget.isAlliedTo(pPlayer)) {
                    CompoundTag tag = pStack.getOrCreateTag();
                    ListTag listTag = getTrackingTag(tag);
                    if (isDuplicate(listTag, pInteractionTarget.getUUID())) {
                        pPlayer.sendMessage(new TextComponent("Mob already added"), pPlayer.getUUID());
                        return InteractionResult.SUCCESS;
                    }
                    CompoundTag entityTag = new CompoundTag();
                    entityTag.putUUID("uuid", pInteractionTarget.getUUID());
                    entityTag.putString("name", pInteractionTarget.getDisplayName().getString());
                    entityTag.putInt("x", (int) pInteractionTarget.getX());
                    entityTag.putInt("y", (int) pInteractionTarget.getY());
                    entityTag.putInt("z", (int) pInteractionTarget.getZ());
                    entityTag.putBoolean("active", true);
                    listTag.add(entityTag);
                    pPlayer.setItemInHand(pUsedHand, pStack);
                    pPlayer.sendMessage(new TextComponent("Mob added"), pPlayer.getUUID());
                } else {
                    pPlayer.sendMessage(new TextComponent("You don't own this mob"), pPlayer.getUUID());
                }
            } else {
                pPlayer.sendMessage(new TextComponent("This mob isn't tamed"), pPlayer.getUUID());
            }
        } else {
            pPlayer.sendMessage(new TextComponent("This mob isn't tameable"), pPlayer.getUUID());
        }

        return InteractionResult.SUCCESS;
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
        if (!pLevel.isClientSide) {
            CompoundTag tag = pStack.getTag();
            if (tag != null && tag.contains(TRACKING)) {
                ListTag listTag = tag.getList(TRACKING, 10);
                for (int i = 0; i < listTag.size(); ++i) {
                    CompoundTag entityTag = listTag.getCompound(i);
                    Entity entity = getEntity((ServerLevel) pLevel, entityTag.getUUID("uuid"));
                    if (entity != null) {
                        entityTag.putInt("x", (int) entity.getX());
                        entityTag.putInt("y", (int) entity.getY());
                        entityTag.putInt("z", (int) entity.getZ());
                        entityTag.putBoolean("active", true);
                        entityTag.putString("name", entity.getDisplayName().getString());
                    } else {
                        entityTag.putBoolean("active", false);
                    }
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pLevel.isClientSide) return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
        String hand;
        if (pUsedHand == InteractionHand.MAIN_HAND) {
            hand = "m";
        } else {
            hand = "o";
        }
        pPlayer.swing(pUsedHand);
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        CompoundTag tag = itemstack.getTag();
        if (tag != null && tag.contains(TRACKING)) {
            ListTag listTag = tag.getList(TRACKING, 10);
            if (!listTag.isEmpty()) {
//                for (int i = 0; i < listTag.size(); ++i) {
//                    CompoundTag entityTag = listTag.getCompound(i);
//                    Entity entity = getEntity((ServerLevel) pLevel, entityTag.getUUID("uuid"));
//                    System.out.println(entity);
//                }
                OpenTrackerPacket packet = new OpenTrackerPacket(itemstack, hand, pPlayer.getX(), pPlayer.getY(),
                        pPlayer.getZ());
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) pPlayer),
                        packet);
//            pPlayer.sendSystemMessage(Component.literal(entity.getDisplayName().getString() + ": " + (int) entity.getX() + ", " + (int) entity.getY() +
//                    "," +
//                    " " + (int) entity.getZ() + " (" + (int) entity.distanceTo(pPlayer) + " blocks away)"));
            } else {
                pPlayer.sendMessage(new TextComponent("No mobs added"), pPlayer.getUUID());
            }
        } else {
            pPlayer.sendMessage(new TextComponent("No mobs added"), pPlayer.getUUID());
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    private Entity getEntity(ServerLevel world, UUID id) {
        return world.getEntity(id);
    }

    private boolean isDuplicate(ListTag listTag, UUID uuid) {
        for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag entityTag = listTag.getCompound(i);
            UUID entityUUID = entityTag.getUUID("uuid");
            if (entityUUID.equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    private ListTag getTrackingTag(CompoundTag tag) {
        ListTag listTag;
        if (tag.contains(TRACKING)) {
            listTag = tag.getList(TRACKING, 10);
        } else {
            listTag = new ListTag();
            tag.put(TRACKING, listTag);
        }
        return listTag;
    }
}
