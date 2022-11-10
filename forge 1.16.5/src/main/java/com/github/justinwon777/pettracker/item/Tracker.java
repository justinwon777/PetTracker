package com.github.justinwon777.pettracker.item;

import com.github.justinwon777.pettracker.core.PacketHandler;
import com.github.justinwon777.pettracker.networking.OpenTrackerPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.UUID;

public class Tracker extends Item {

    public static final String TRACKING = "Tracking";

    public Tracker(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public ActionResultType itemInteractionForEntity(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pInteractionTarget,
                                                     Hand pUsedHand) {
        if (pInteractionTarget.world.isRemote) return ActionResultType.SUCCESS;
        if (pInteractionTarget instanceof TameableEntity) {
            if (((TameableEntity) pInteractionTarget).isTamed()) {
                if (pInteractionTarget.isOnSameTeam(pPlayer)) {
                    CompoundNBT tag = pStack.getOrCreateTag();
                    ListNBT listTag = getTrackingTag(tag);
                    if (isDuplicate(listTag, pInteractionTarget.getUniqueID())) {
                        pPlayer.sendMessage(new StringTextComponent("Entity already added"), pPlayer.getUniqueID());
                        return ActionResultType.SUCCESS;
                    }
                    CompoundNBT entityTag = new CompoundNBT();
                    entityTag.putUniqueId("uuid", pInteractionTarget.getUniqueID());
                    entityTag.putString("name", pInteractionTarget.getDisplayName().getString());
                    entityTag.putInt("x", (int) pInteractionTarget.getPosX());
                    entityTag.putInt("y", (int) pInteractionTarget.getPosY());
                    entityTag.putInt("z", (int) pInteractionTarget.getPosZ());
                    entityTag.putBoolean("active", true);
                    listTag.add(entityTag);
                    pPlayer.setHeldItem(pUsedHand, pStack);
                    pPlayer.sendMessage(new StringTextComponent("Entity added"), pPlayer.getUniqueID());
                } else {
                    pPlayer.sendMessage(new StringTextComponent("You don't own this mob"), pPlayer.getUniqueID());
                }
            } else {
                pPlayer.sendMessage(new StringTextComponent("This mob isn't tamed"), pPlayer.getUniqueID());
            }
        } else {
            pPlayer.sendMessage(new StringTextComponent("This mob isn't tameable"), pPlayer.getUniqueID());
        }

        return ActionResultType.SUCCESS;
    }

    public void inventoryTick(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
        if (!pLevel.isRemote) {
            CompoundNBT tag = pStack.getTag();
            if (tag != null && tag.contains(TRACKING)) {
                ListNBT listTag = tag.getList(TRACKING, 10);
                for (int i = 0; i < listTag.size(); ++i) {
                    CompoundNBT entityTag = listTag.getCompound(i);
                    Entity entity = getEntity((ServerWorld) pLevel, entityTag.getUniqueId("uuid"));
                    if (entity != null) {
                        entityTag.putInt("x", (int) entity.getPosX());
                        entityTag.putInt("y", (int) entity.getPosY());
                        entityTag.putInt("z", (int) entity.getPosZ());
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
    public ActionResult<ItemStack> onItemRightClick(World pLevel, PlayerEntity pPlayer, Hand pUsedHand) {
        if (pLevel.isRemote) return ActionResult.resultPass(pPlayer.getHeldItem(pUsedHand));
        String hand;
        if (pUsedHand == Hand.MAIN_HAND) {
            hand = "m";
        } else {
            hand = "o";
        }
        pPlayer.swing(pUsedHand, false);
        ItemStack itemstack = pPlayer.getHeldItem(pUsedHand);
        CompoundNBT tag = itemstack.getTag();
        if (tag != null && tag.contains(TRACKING)) {
            ListNBT listTag = tag.getList(TRACKING, 10);
            if (!listTag.isEmpty()) {
                OpenTrackerPacket packet = new OpenTrackerPacket(itemstack, hand);
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) pPlayer),
                        packet);
            } else {
                pPlayer.sendMessage(new StringTextComponent("No entities added"), pPlayer.getUniqueID());
            }
        } else {
            pPlayer.sendMessage(new StringTextComponent("No entities added"), pPlayer.getUniqueID());
        }

        return ActionResult.func_233538_a_(itemstack, pLevel.isRemote);
    }

    private Entity getEntity(ServerWorld world, UUID id) {
        return world.getEntityByUuid(id);
    }

    private boolean isDuplicate(ListNBT listTag, UUID uuid) {
        for(int i = 0; i < listTag.size(); ++i) {
            CompoundNBT entityTag = listTag.getCompound(i);
            UUID entityUUID = entityTag.getUniqueId("uuid");
            if (entityUUID.equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    private ListNBT getTrackingTag(CompoundNBT tag) {
        ListNBT listTag;
        if (tag.contains(TRACKING)) {
            listTag = tag.getList(TRACKING, 10);
        } else {
            listTag = new ListNBT();
            tag.put(TRACKING, listTag);
        }
        return listTag;
    }
}
