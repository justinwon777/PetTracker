package com.github.justinwon777.pettracker.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Tracker extends Item {

    private static final String TRACKING = "Tracking";

    public Tracker(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity entity, Hand hand) {
        if (entity.world.isRemote) return ActionResultType.SUCCESS;
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putUniqueId(TRACKING, entity.getUniqueID());
        playerIn.setHeldItem(hand, stack);
        playerIn.sendMessage(new StringTextComponent("Entity added"),
                PlayerEntity.getUUID(playerIn.getGameProfile()));

        return ActionResultType.SUCCESS;
    }
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (worldIn.isRemote) return ActionResult.resultPass(playerIn.getHeldItem(handIn));
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        CompoundNBT tag = itemstack.getTag();
        if (tag != null && tag.contains(TRACKING)) {
            Entity entity = this.getEntity((ServerWorld) worldIn, itemstack);
            playerIn.sendMessage(new StringTextComponent(entity.getDisplayName().getString() + ": " + (int) entity.getPosX() + ", " + (int) entity.getPosY() + ", " + (int) entity.getPosZ() + " (" + (int) entity.getDistance(playerIn) + " blocks away)"),
                    PlayerEntity.getUUID(playerIn.getGameProfile()));
        } else {
            playerIn.sendMessage(new StringTextComponent("No entity added"),
                    PlayerEntity.getUUID(playerIn.getGameProfile()));
        }
        return ActionResult.resultPass(playerIn.getHeldItem(handIn));
    }

    private Entity getEntity(ServerWorld world, ItemStack stack) {
        return world.getEntityByUuid(NBTUtil.readUniqueId(stack.getTag().get(TRACKING)));
    }
}
