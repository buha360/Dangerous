package com.example.examplemod;

import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SpiderSpeedEnhancement {

    private static final String SPIDER_BUFF_TAG = "dangerous_spider_buff";

    @SubscribeEvent
    public static void onSpiderSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Spider spider && event.getLevel() instanceof ServerLevel) {
            CompoundTag entityData = spider.getPersistentData();

            if (!entityData.getBoolean(SPIDER_BUFF_TAG)) {
                increaseSpiderSpeed(spider);
                entityData.putBoolean(SPIDER_BUFF_TAG, true);
            }
        }
    }

    private static void increaseSpiderSpeed(Spider spider) {
        // A spiderSpeedMultiplier érték lekérdezése a konfigurációból, amikor szükséges
        double speedMultiplier = DangerousConfig.COMMON.spiderSpeedMultiplier.get();

        AttributeInstance speedAttribute = spider.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(speedAttribute.getBaseValue() * speedMultiplier);
        }
    }
}
