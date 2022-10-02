package com.github.justinwon777.pettracker.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.StringTextComponent;

public class Tracker extends Item {
    public Tracker(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public net.minecraft.util.ActionResultType itemInteractionForEntity(ItemStack stack, net.minecraft.entity.player.PlayerEntity playerIn, LivingEntity entity, net.minecraft.util.Hand hand) {
        if (entity.world.isRemote) return net.minecraft.util.ActionResultType.PASS;
        playerIn.sendMessage(new StringTextComponent("Tracker used"), PlayerEntity.getUUID(playerIn.getGameProfile()));

        return ActionResultType.SUCCESS;
    }
}
