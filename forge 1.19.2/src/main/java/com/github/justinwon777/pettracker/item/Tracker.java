package com.github.justinwon777.pettracker.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Tracker extends Item {

    private static final String TRACKING = "Tracking";

    public Tracker(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity entity,
                                                  InteractionHand hand) {
        if (entity.level.isClientSide) return InteractionResult.PASS;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(TRACKING, entity.getUUID());
        playerIn.sendSystemMessage(Component.literal("Entity added"));

        return InteractionResult.SUCCESS;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (worldIn.isClientSide) return InteractionResultHolder.pass(playerIn.getItemInHand(handIn));
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        CompoundTag tag = itemstack.getTag();
        if (tag != null && tag.contains(TRACKING)) {
            Entity entity = this.getEntity((ServerLevel) worldIn, itemstack);
            playerIn.sendSystemMessage(Component.literal(entity.getX() + ", " + entity.getY() + ", " + entity.getZ()));
        } else {
            playerIn.sendSystemMessage(Component.literal("No entity added"));
        }
        return InteractionResultHolder.pass(playerIn.getItemInHand(handIn));
    }

    private Entity getEntity(ServerLevel world, ItemStack stack) {
        return world.getEntity(stack.getTag().getUUID(TRACKING));
    }
}
