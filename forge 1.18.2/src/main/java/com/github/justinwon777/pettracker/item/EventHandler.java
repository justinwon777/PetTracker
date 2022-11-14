package com.github.justinwon777.pettracker.item;

import com.github.justinwon777.pettracker.PetTracker;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PetTracker.MOD_ID)
public class EventHandler {
    @SubscribeEvent
    public static void trackerInteract(final PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity target) {
            Player player = (Player) event.getEntity();
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
}
