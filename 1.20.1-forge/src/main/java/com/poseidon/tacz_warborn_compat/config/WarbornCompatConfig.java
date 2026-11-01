package com.poseidon.tacz_warborn_compat.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WarbornCompatConfig {
    private WarbornCompatConfig() {
    }

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Master switch for the WARBORN backpack ammo bridge.",
                    "When true, TaCZ guns can see and consume ammo stored in WARBORN backpacks.")
            .define("enabled", true);

    public static final ForgeConfigSpec.BooleanValue WORN_BACKPACK = BUILDER
            .comment("Allow guns to use ammo from a WARBORN backpack worn on the player",
                    "(chest armor slot or a Curios slot).")
            .define("wornBackpack", true);

    public static final ForgeConfigSpec.BooleanValue INVENTORY_BACKPACKS = BUILDER
            .comment("Allow guns to use ammo from WARBORN backpacks carried inside the player inventory.")
            .define("inventoryBackpacks", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean isEnabled() {
        return !SPEC.isLoaded() || ENABLED.get();
    }

    public static boolean wornEnabled() {
        return !SPEC.isLoaded() || WORN_BACKPACK.get();
    }

    public static boolean inventoryEnabled() {
        return !SPEC.isLoaded() || INVENTORY_BACKPACKS.get();
    }
}
