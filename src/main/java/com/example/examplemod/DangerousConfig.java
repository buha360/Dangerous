package com.example.examplemod;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public class DangerousConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.DoubleValue baseHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue maxHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue healthMultiplierIncrement;
        public final ForgeConfigSpec.IntValue daysPerIncrement;
        public final ForgeConfigSpec.DoubleValue weaponChance;
        public final ForgeConfigSpec.DoubleValue enchantmentChance;
        public final ForgeConfigSpec.ConfigValue<List<String>> availableBowEnchantments;
        public final ForgeConfigSpec.ConfigValue<List<String>> availableWeaponEnchantments;
        public final ForgeConfigSpec.ConfigValue<List<String>> availableArmorEnchantments;
        public final ForgeConfigSpec.DoubleValue easyGearChance;
        public final ForgeConfigSpec.DoubleValue normalGearChance;
        public final ForgeConfigSpec.DoubleValue hardGearChance;
        public final ForgeConfigSpec.DoubleValue creeperSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue spiderSpeedMultiplier;
        public final ForgeConfigSpec.ConfigValue<List<String>> surfaceArmor;
        public final ForgeConfigSpec.ConfigValue<List<String>> surfaceWeapons;
        public final ForgeConfigSpec.ConfigValue<List<String>> deepArmor;
        public final ForgeConfigSpec.ConfigValue<List<String>> deepWeapons;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("Health Multiplier Settings");

            builder.push("Base Health Multiplier");
            baseHealthMultiplier = builder
                    .comment(
                            "Base health multiplier for enemies.",
                            "This value determines the starting multiplier for enemy health."
                    )
                    .defineInRange("baseHealthMultiplier", 1.0, 0.1, 100.0);
            builder.pop();

            builder.push("Max Health Multiplier");
            maxHealthMultiplier = builder
                    .comment(
                            "Maximum health multiplier for enemies.",
                            "This is the maximum limit the multiplier can reach."
                    )
                    .defineInRange("maxHealthMultiplier", 2.0, 0.1, 100.0);
            builder.pop();

            builder.push("Health Multiplier Increment");
            healthMultiplierIncrement = builder
                    .comment(
                            "Increment to increase health multiplier after reaching certain amount of days.",
                            "This value specifies by how much the multiplier increases."
                    )
                    .defineInRange("healthMultiplierIncrement", 0.2, 0.1, 10.0);
            builder.pop();

            builder.push("Days Per Increment");
            daysPerIncrement = builder
                    .comment(
                            "How many in-game days it takes for the health multiplier to increment."
                    )
                    .defineInRange("daysPerIncrement", 12, 1, 100);
            builder.pop();

            builder.push("Gear Spawn Chances");
            surfaceArmor = builder
                    .comment("List of armor mobs can spawn with near the surface.")
                    .define("surfaceArmor", List.of("minecraft:iron_helmet", "minecraft:iron_chestplate", "minecraft:iron_leggings", "minecraft:iron_boots", "minecraft:chainmail_helmet", "minecraft:chainmail_chestplate", "minecraft:chainmail_leggings", "minecraft:chainmail_boots", "minecraft:leather_helmet", "minecraft:leather_chestplate", "minecraft:leather_leggings", "minecraft:leather_boots"));

            surfaceWeapons = builder
                    .comment("List of weapons mobs can spawn with near the surface.")
                    .define("surfaceWeapons", List.of("minecraft:stone_sword", "minecraft:stone_axe", "minecraft:iron_sword", "minecraft:iron_axe"));

            deepArmor = builder
                    .comment("List of armor mobs can spawn with in deep caves.")
                    .define("deepArmor", List.of("minecraft:diamond_helmet", "minecraft:diamond_chestplate", "minecraft:diamond_leggings", "minecraft:diamond_boots", "minecraft:gold_helmet", "minecraft:gold_chestplate", "minecraft:gold_leggings", "minecraft:gold_boots", "minecraft:iron_helmet", "minecraft:iron_chestplate", "minecraft:iron_leggings", "minecraft:iron_boots", "minecraft:chainmail_helmet", "minecraft:chainmail_chestplate", "minecraft:chainmail_leggings", "minecraft:chainmail_boots"));

            deepWeapons = builder
                    .comment("List of weapons mobs can spawn with in deep caves.")
                    .define("deepWeapons", List.of("minecraft:iron_sword", "minecraft:iron_axe", "minecraft:diamond_sword", "minecraft:diamond_axe"));

            easyGearChance = builder
                    .comment("Chance for mobs to spawn with gear in EASY mode.")
                    .defineInRange("easyGearChance", 0.10, 0.0, 1.0);

            normalGearChance = builder
                    .comment("Chance for mobs to spawn with gear in NORMAL mode.")
                    .defineInRange("normalGearChance", 0.20, 0.0, 1.0);

            hardGearChance = builder
                    .comment("Chance for mobs to spawn with gear in HARD mode.")
                    .defineInRange("hardGearChance", 0.40, 0.0, 1.0);
            builder.pop();

            builder.push("Equipment Settings");
            weaponChance = builder
                    .comment("Chance for mob to get a weapon.")
                    .defineInRange("weaponChance", 0.4, 0.0, 1.0);

            enchantmentChance = builder
                    .comment("Chance for item to get enchanted.")
                    .defineInRange("enchantmentChance", 0.2, 0.0, 1.0);

            availableBowEnchantments = builder
                    .comment("Enchantments available for bows.")
                    .define("availableBowEnchantments", List.of("minecraft:power", "minecraft:infinity", "minecraft:flame", "minecraft:punch"));

            availableWeaponEnchantments = builder
                    .comment("Enchantments available for melee weapons.")
                    .define("availableWeaponEnchantments", List.of("minecraft:sharpness", "minecraft:smite", "minecraft:fire_aspect", "minecraft:unbreaking"));

            availableArmorEnchantments = builder
                    .comment("Enchantments available for armor.")
                    .define("availableArmorEnchantments", List.of("minecraft:protection", "minecraft:unbreaking", "minecraft:fire_protection"));
            builder.pop();

            builder.push("Creeper Speed Settings");
            creeperSpeedMultiplier = builder
                    .comment("Multiplier to increase Creeper movement speed.",
                            "This value controls how fast Creepers can move.",
                            "Range: 1.0 (normal speed) to 5.0 (very fast)")
                    .defineInRange("creeperSpeedMultiplier", 1.5, 1.0, 5.0);

            builder.push("Spider Speed Settings");
            spiderSpeedMultiplier = builder
                    .comment("Multiplier to increase Spider movement speed.",
                            "This value controls how fast Spiders can move.",
                            "Range: 1.0 (normal speed) to 5.0 (very fast)")
                    .defineInRange("spiderSpeedMultiplier", 1.25, 1.0, 5.0);

            builder.pop();
        }
    }
}
