package com.github.justinwon777.pettracker.core;

import com.github.justinwon777.pettracker.PetTracker;
import com.github.justinwon777.pettracker.item.Tracker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            PetTracker.MOD_ID);

    public static final RegistryObject<Item> TRACKER = ITEMS.register("tracker",
            () -> new Tracker(new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
