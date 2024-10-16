package com.example.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

public class GearManager {

    private static final Random RANDOM = new Random();
    private static final String EQUIPMENT_MODIFIED_TAG = "dangerous_equipment_modified";
    private static final int SURFACE_Y_THRESHOLD = 64; // Ezen a magasságon felül számítjuk a felszínt

    public static void equipMobBasedOnDifficulty(Mob mob, ServerLevel world) {
        Difficulty difficulty = world.getDifficulty();
        CompoundTag entityData = mob.getPersistentData();

        if (entityData.getBoolean(EQUIPMENT_MODIFIED_TAG)) {
            return;
        }

        // Mob spawnolási magasságának ellenőrzése
        if (mob.getY() > SURFACE_Y_THRESHOLD) {
            // Felszíni mob
            giveSurfaceLevelGear(mob, difficulty);
        } else {
            // Mélyen lévő mob
            giveDeepLevelGear(mob, difficulty);
        }

        // Speciális logika zombik, skeletonok és creeper-ek számára
        if (mob instanceof Zombie) {
            giveZombieWeapon(mob);
        } else if (mob instanceof Skeleton) {
            giveSkeletonWeapon(mob);
        } else if (mob instanceof Creeper) {
            increaseCreeperSpeed((Creeper) mob);
        }

        entityData.putBoolean(EQUIPMENT_MODIFIED_TAG, true);
    }

    private static void increaseCreeperSpeed(Creeper creeper) {
        AttributeInstance speedAttribute = creeper.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            double speedMultiplier = DangerousConfig.COMMON.creeperSpeedMultiplier.get();
            speedAttribute.setBaseValue(speedAttribute.getBaseValue() * speedMultiplier);
        }
    }

    // Felszíni mobok felszerelése
    private static void giveSurfaceLevelGear(Mob mob, Difficulty difficulty) {
        equipArmorIfChance(mob, EquipmentSlot.HEAD, "helmet", getGearChanceForDifficulty(difficulty), DangerousConfig.COMMON.surfaceArmor.get());
        equipArmorIfChance(mob, EquipmentSlot.CHEST, "chestplate", getGearChanceForDifficulty(difficulty), DangerousConfig.COMMON.surfaceArmor.get());
        equipArmorIfChance(mob, EquipmentSlot.LEGS, "leggings", getGearChanceForDifficulty(difficulty), DangerousConfig.COMMON.surfaceArmor.get());
        equipArmorIfChance(mob, EquipmentSlot.FEET, "boots", getGearChanceForDifficulty(difficulty), DangerousConfig.COMMON.surfaceArmor.get());
        giveWeapon(mob, DangerousConfig.COMMON.surfaceWeapons.get());
    }

    // Mélyen lévő mobok felszerelése
    private static void giveDeepLevelGear(Mob mob, Difficulty difficulty) {
        equipArmorIfChance(mob, EquipmentSlot.HEAD, "helmet", getGearChanceForDifficulty(difficulty), DangerousConfig.COMMON.deepArmor.get());
        equipArmorIfChance(mob, EquipmentSlot.CHEST, "chestplate", getGearChanceForDifficulty(difficulty), DangerousConfig.COMMON.deepArmor.get());
        equipArmorIfChance(mob, EquipmentSlot.LEGS, "leggings", getGearChanceForDifficulty(difficulty), DangerousConfig.COMMON.deepArmor.get());
        equipArmorIfChance(mob, EquipmentSlot.FEET, "boots", getGearChanceForDifficulty(difficulty), DangerousConfig.COMMON.deepArmor.get());
        giveWeapon(mob, DangerousConfig.COMMON.deepWeapons.get());
    }

    private static double getGearChanceForDifficulty(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> DangerousConfig.COMMON.easyGearChance.get();
            case NORMAL -> DangerousConfig.COMMON.normalGearChance.get();
            case HARD -> DangerousConfig.COMMON.hardGearChance.get();
            default -> 0.0; // Default 0
        };
    }

    private static void equipArmorIfChance(Mob mob, EquipmentSlot slot, String armorType, double chance, List<String> availableArmor) {
        List<String> filteredArmor = availableArmor.stream()
                .filter(armor -> armor.contains(armorType))
                .toList();

        if (!filteredArmor.isEmpty() && RANDOM.nextDouble() < chance) {
            String selectedArmor = filteredArmor.get(RANDOM.nextInt(filteredArmor.size()));
            ItemStack armor = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(selectedArmor)));

            maybeEnchantItem(armor, DangerousConfig.COMMON.availableArmorEnchantments.get());
            giveNBT(armor);
            mob.setItemSlot(slot, armor);
        }
    }

    private static void giveWeapon(Mob mob, List<String> availableWeapons) {
        if (!availableWeapons.isEmpty()) {
            String selectedWeapon = availableWeapons.get(RANDOM.nextInt(availableWeapons.size()));
            ItemStack weapon = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(selectedWeapon)));
            maybeEnchantItem(weapon, DangerousConfig.COMMON.availableWeaponEnchantments.get());
            giveNBT(weapon);
            mob.setItemInHand(mob.getUsedItemHand(), weapon);
        }
    }

    private static void giveSkeletonWeapon(Mob mob) {
        ItemStack bow = new ItemStack(Items.BOW);
        maybeEnchantItem(bow, DangerousConfig.COMMON.availableBowEnchantments.get());
        mob.setItemSlot(EquipmentSlot.MAINHAND, bow);

        ItemStack meleeWeapon = getRandomMeleeWeapon();
        maybeEnchantItem(meleeWeapon, DangerousConfig.COMMON.availableWeaponEnchantments.get());
        mob.setItemSlot(EquipmentSlot.OFFHAND, meleeWeapon);
    }

    private static ItemStack getRandomMeleeWeapon() {
        List<String> availableWeapons = DangerousConfig.COMMON.surfaceWeapons.get();
        String selectedWeapon = availableWeapons.get(RANDOM.nextInt(availableWeapons.size()));
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(selectedWeapon)));
    }

    private static void giveZombieWeapon(Mob mob) {
        if (RANDOM.nextDouble() < DangerousConfig.COMMON.weaponChance.get()) {
            ItemStack weapon = getRandomMeleeWeapon();
            maybeEnchantItem(weapon, DangerousConfig.COMMON.availableWeaponEnchantments.get());
            mob.setItemInHand(mob.getUsedItemHand(), weapon);
        }
    }

    private static void maybeEnchantItem(ItemStack item, List<String> availableEnchantments) {
        double enchantmentChance = RANDOM.nextDouble();
        if (enchantmentChance < DangerousConfig.COMMON.enchantmentChance.get()) {
            String selectedEnchantment = availableEnchantments.get(RANDOM.nextInt(availableEnchantments.size()));
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(selectedEnchantment));
            item.enchant(enchantment, 1 + RANDOM.nextInt(3));
        }
    }

    private static void giveNBT (ItemStack item) {
        CompoundTag tag = item.getOrCreateTag();
        tag.putBoolean("dangerous_mod_item", true);
        item.setTag(tag);
    }
}