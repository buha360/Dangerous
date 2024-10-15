package com.example.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@Mod(Dangerous.MODID)
public class Dangerous {

    public static final String MODID = "dangerous";
    private static final Logger LOGGER = LoggerFactory.getLogger(Dangerous.class);
    private static final String HEALTH_MODIFIED_TAG = "dangerous_hp_modified";
    private float healthMultiplier = 1.0f;
    private double maxHealthMultiplier;
    private double healthMultiplierIncrement;
    private int daysPerIncrement;
    private static long lastCheckedDay = 0;
    private boolean finalFormReached = false;

    public Dangerous() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DangerousConfig.COMMON_SPEC);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new SkeletonWeaponManager());
        MinecraftForge.EVENT_BUS.register(new MobDeathHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Dangerous Mod setup complete.");
    }

    @SubscribeEvent
    public void onConfigLoad(final ModConfigEvent.Loading event) {
        loadConfigValues();
    }

    @SubscribeEvent
    public void onConfigReload(final ModConfigEvent.Reloading event) {
        loadConfigValues();
    }

    private void loadConfigValues() {
        healthMultiplier = DangerousConfig.COMMON.baseHealthMultiplier.get().floatValue();
        maxHealthMultiplier = DangerousConfig.COMMON.maxHealthMultiplier.get();
        healthMultiplierIncrement = DangerousConfig.COMMON.healthMultiplierIncrement.get();
        daysPerIncrement = DangerousConfig.COMMON.daysPerIncrement.get();
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel serverWorld) {
            MinecraftServer server = serverWorld.getServer();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendWorldInfoToPlayer(player, serverWorld);
                }
            }, 2500);
        }
    }

    private void sendWorldInfoToPlayer(ServerPlayer player, ServerLevel world) {
        long currentDay = world.getDayTime() / 24000L;
        double healthMultiplier = DangerousConfig.COMMON.baseHealthMultiplier.get().floatValue();
        int daysPerIncrement = DangerousConfig.COMMON.daysPerIncrement.get();

        long daysUntilNextIncrement = daysPerIncrement - (currentDay % daysPerIncrement);

        Component dangerousMessage = Component.literal("            === Dangerous Mod Stats ===")
                .withStyle(style -> style.withBold(true).withColor(0xFF5555));

        Component emptyLine = Component.literal(" ");

        Component combinedMessage = Component.literal("Day: ")
                .append(Component.literal("" + currentDay).withStyle(style -> style.withColor(0x00FF00)))
                .append(Component.literal(" | Mob HP Multiplier: "))
                .append(Component.literal("" + healthMultiplier).withStyle(style -> style.withColor(0xFFFF55)))
                .append(Component.literal(" | Days Until Next Increment: "))
                .append(Component.literal("" + daysUntilNextIncrement).withStyle(style -> style.withColor(0x55FFFF)));

        player.sendSystemMessage(dangerousMessage);
        player.sendSystemMessage(emptyLine);
        player.sendSystemMessage(combinedMessage);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Dangerous Mod is ready and loaded!");
        loadConfigValues();

        ServerLevel overworld = event.getServer().getLevel(ServerLevel.OVERWORLD);
        if (overworld != null) {
            long currentDay = overworld.getDayTime() / 24000L;
            initializeHealthMultiplier(currentDay);
        }
    }

    private void initializeHealthMultiplier(long currentDay) {
        long daysPassed = currentDay / daysPerIncrement;
        for (int i = 0; i < daysPassed; i++) {
            if (healthMultiplier < maxHealthMultiplier) {
                healthMultiplier += (float) healthMultiplierIncrement;
                if (healthMultiplier > maxHealthMultiplier) {
                    healthMultiplier = (float) maxHealthMultiplier;
                    finalFormReached = true;
                    break;
                }
            }
        }
        lastCheckedDay = currentDay;
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.level instanceof ServerLevel serverWorld && !serverWorld.isClientSide()) {
            ServerLevelData worldData = (ServerLevelData) serverWorld.getServer().getWorldData();
            long currentDay = worldData.getDayTime() / 24000;

            if (lastCheckedDay == 0) {
                lastCheckedDay = currentDay;
            }

            long daysPassed = currentDay - lastCheckedDay;

            if (daysPassed >= daysPerIncrement) {
                long increments = daysPassed / daysPerIncrement;
                for (long i = 0; i < increments; i++) {
                    increaseHealthMultiplier(serverWorld);
                }
                lastCheckedDay += increments * daysPerIncrement;
            }
        }
    }

    private void increaseHealthMultiplier(ServerLevel world) {
        if (healthMultiplier < maxHealthMultiplier) {
            healthMultiplier += (float) healthMultiplierIncrement;
            if (healthMultiplier > maxHealthMultiplier) {
                healthMultiplier = (float) maxHealthMultiplier;
            }

            Component message = Component.literal("The enemies become more DANGEROUS!")
                    .withStyle(style -> style.withBold(true).withColor(0xFF5555));

            world.getPlayers(player -> true).forEach(player -> player.sendSystemMessage(message));

            if (healthMultiplier == maxHealthMultiplier && !finalFormReached) {
                finalFormReached = true;
                Component finalMessage = Component.literal("The enemies have reached their FINAL FORM!")
                        .withStyle(style -> style.withBold(true).withColor(0xFFAA00));

                world.getPlayers(player -> true).forEach(player -> player.sendSystemMessage(finalMessage));

                LOGGER.info("The enemies have reached their FINAL FORM!");
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverWorld && event.getEntity() instanceof Mob mob) {
            if (mob instanceof Monster) {
                Difficulty difficulty = serverWorld.getDifficulty();
                adjustHealthBasedOnDifficulty(mob, difficulty, healthMultiplier);
                GearManager.equipMobBasedOnDifficulty(mob, serverWorld);
            }
        }
    }

    private void adjustHealthBasedOnDifficulty(LivingEntity entity, Difficulty difficulty, float multiplier) {
        CompoundTag entityData = entity.getPersistentData();
        if (!entityData.getBoolean(HEALTH_MODIFIED_TAG)) {
            float originalHealth = entity.getMaxHealth();
            float newHealth;

            switch (difficulty) {
                case EASY, NORMAL, HARD -> newHealth = originalHealth * multiplier;
                default -> newHealth = originalHealth;
            }

            Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(newHealth);
            entity.setHealth(newHealth);

            entityData.putBoolean(HEALTH_MODIFIED_TAG, true);
        }
    }

    @Mod.EventBusSubscriber(modid = Dangerous.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Dangerous Mod client setup complete.");
        }
    }
}