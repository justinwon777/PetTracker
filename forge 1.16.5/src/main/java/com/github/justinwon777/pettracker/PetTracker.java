package com.github.justinwon777.pettracker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PetTracker.MOD_ID)
public class PetTracker
{
    public static final String MOD_ID = "pettracker";
    private static final Logger LOGGER = LogManager.getLogger();

    public PetTracker() {
        MinecraftForge.EVENT_BUS.register(this);
//        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

    }

}
