package com.poseidon.tacz_warborn_compat;

import com.mojang.logging.LogUtils;
import com.poseidon.tacz_warborn_compat.config.WarbornCompatConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TaczWarbornCompat.MOD_ID)
public class TaczWarbornCompat {
    public static final String MOD_ID = "tacz_warborn_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TaczWarbornCompat(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, WarbornCompatConfig.SPEC);
    }
}
