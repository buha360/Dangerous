package com.example.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;

@Mod.EventBusSubscriber
public class MobDeathHandler {

    private static final String DANGEROUS_MOD_TAG = "dangerous_mod_item";

    @SubscribeEvent
    public static void onMobDeath(LivingDropsEvent event) {
        if (event.getEntity() instanceof Mob mob && mob instanceof Monster) {
            Iterator<ItemEntity> iterator = event.getDrops().iterator();

            while (iterator.hasNext()) {
                ItemStack item = iterator.next().getItem();
                if (!item.isEmpty()) {
                    CompoundTag tag = item.getTag();
                    if (tag != null && tag.getBoolean(DANGEROUS_MOD_TAG)) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}