package com.github.justinwon777.pettracker.item;

import com.github.justinwon777.pettracker.PetTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PetTracker.MOD_ID)
public class EventHandler {
    @SubscribeEvent
    public static void trackerInteract(final PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            LivingEntity target = (LivingEntity) event.getTarget();
            Hand hand = event.getHand();
            ItemStack itemstack = player.getHeldItem(hand);

            if (!target.world.isRemote()) {
                if (itemstack.getItem() instanceof Tracker) {
                    itemstack.getItem().itemInteractionForEntity(itemstack, player, target, hand);
                    event.setCanceled(true);
                }
            }
        }
    }
}
