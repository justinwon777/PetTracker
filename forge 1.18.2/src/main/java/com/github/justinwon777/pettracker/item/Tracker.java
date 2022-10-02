package com.github.justinwon777.pettracker.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
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
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget,
                                                  InteractionHand pUsedHand) {
        if (pInteractionTarget.level.isClientSide) return InteractionResult.PASS;
        CompoundTag tag = pStack.getOrCreateTag();
        tag.putUUID(TRACKING, pInteractionTarget.getUUID());
        System.out.println(tag);
        pPlayer.sendMessage(new TextComponent("Entity added"), pPlayer.getUUID());

        return InteractionResult.SUCCESS;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pLevel.isClientSide) return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        CompoundTag tag = itemstack.getTag();
        System.out.println(tag);
        if (tag != null && tag.contains(TRACKING)) {
            Entity entity = this.getEntity((ServerLevel) pLevel, itemstack);
            pPlayer.sendMessage(new TextComponent(entity.getX() + ", " + entity.getY() + ", " + entity.getZ()), pPlayer.getUUID());
        } else {
            pPlayer.sendMessage(new TextComponent("No entity added"), pPlayer.getUUID());
        }
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
    }

    private Entity getEntity(ServerLevel world, ItemStack stack) {
        return world.getEntity(stack.getTag().getUUID(TRACKING));
    }
}
