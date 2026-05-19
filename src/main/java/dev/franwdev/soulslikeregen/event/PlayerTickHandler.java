package dev.franwdev.soulslikeregen.event;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import dev.franwdev.soulslikeregen.SoulslikeRegen;
import dev.franwdev.soulslikeregen.capability.IRegenCap;
import dev.franwdev.soulslikeregen.capability.RegenCapProvider;
import dev.franwdev.soulslikeregen.compat.FTBTeamsCompat;
import dev.franwdev.soulslikeregen.config.RegenConfig;
import dev.franwdev.soulslikeregen.data.InnData;
import dev.franwdev.soulslikeregen.data.InnEntry;
import dev.franwdev.soulslikeregen.data.NexusData;
import dev.franwdev.soulslikeregen.data.NexusEntry;
import dev.franwdev.soulslikeregen.feedback.FeedbackHelper;
import dev.franwdev.soulslikeregen.level.LevelUpHandler;

@Mod.EventBusSubscriber(modid = SoulslikeRegen.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.player instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            RegenCapProvider.get(player).ifPresent(cap -> {

                // 1. Initialize last damage tick if not set
                if (cap.getLastDamageTick() < 0) {
                    cap.setLastDamageTick(level.getGameTime());
                }

                // 2. Cooldown decrements
                if (cap.getExhaustedMessageCooldown() > 0) {
                    cap.setExhaustedMessageCooldown(cap.getExhaustedMessageCooldown() - 1);
                }

                // 3. Detect healing and apply fatigue
                float currentHealth = player.getHealth();
                float lastHealth = cap.getLastKnownHealth();
                if (lastHealth < 0) {
                    cap.setLastKnownHealth(currentHealth);
                } else if (currentHealth > lastHealth) {
                    float healed = currentHealth - lastHealth;

                    // Apply FTB Teams Proximity Discount
                    float discount = 0.0f;
                    if (FTBTeamsCompat.isLoaded()) {
                        int nearbyAllies = 0;
                        double radiusSq = RegenConfig.ALLY_SCAN_RADIUS * RegenConfig.ALLY_SCAN_RADIUS;
                        for (ServerPlayer other : level.players()) {
                            if (other != player && player.distanceToSqr(other) <= radiusSq) {
                                if (FTBTeamsCompat.arePlayersAllies(player, other)) {
                                    nearbyAllies++;
                                }
                            }
                        }
                        discount = Math.min(nearbyAllies * RegenConfig.ALLY_DISCOUNT_PER_PLAYER, RegenConfig.ALLY_DISCOUNT_MAX);
                    }

                    float fatigueToAdd = healed * (1.0f - discount);
                    cap.addFatigue(fatigueToAdd);

                    // Level up check
                    LevelUpHandler.checkLevelUp(player, cap);

                    cap.setLastKnownHealth(currentHealth);
                } else if (currentHealth < lastHealth) {
                    cap.setLastKnownHealth(currentHealth);
                }

                // 4. Zone checks (Nexus and Inns)
                boolean insideNexus = false;
                boolean insideInn = false;

                // Nexus check
                NexusData nexusData = NexusData.get(level);
                UUID playerTeam = FTBTeamsCompat.getPlayerTeamId(player);
                for (NexusEntry nexus : nexusData.getAllNexuses()) {
                    if (nexus.dimension().equals(level.dimension())) {
                        double distSq = player.distanceToSqr(nexus.x(), nexus.y(), nexus.z());
                        if (distSq <= nexus.radius() * nexus.radius()) {
                            if (!FTBTeamsCompat.isLoaded() || (playerTeam != null && playerTeam.equals(nexus.teamId()))) {
                                insideNexus = true;
                                break;
                            }
                        }
                    }
                }

                // Inn check (only if not inside a valid Nexus)
                if (!insideNexus) {
                    InnData innData = InnData.get(level);
                    for (InnEntry inn : innData.getAllInns()) {
                        if (inn.dimension().equals(level.dimension())) {
                            double distSq = player.distanceToSqr(inn.x(), inn.y(), inn.z());
                            if (distSq <= inn.radius() * inn.radius()) {
                                insideInn = true;
                                break;
                            }
                        }
                    }
                }

                // Apply Nexus ticking
                if (insideNexus) {
                    if (!cap.isNexoDrainActive()) {
                        cap.setNexoDrainActive(true);
                        FeedbackHelper.sendEnteredNexus(player);
                    }
                    if (player.tickCount % RegenConfig.NEXUS_DRAIN_INTERVAL_TICKS == 0) {
                        float drained = cap.drainFatigue(RegenConfig.NEXUS_DRAIN_RATE);
                        if (drained > 0 && cap.getCurrentFatigue() == 0) {
                            FeedbackHelper.sendFullyRested(player, dev.franwdev.soulslikeregen.feedback.ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.source.nexus"));
                        }
                    }
                } else {
                    if (cap.isNexoDrainActive()) {
                        cap.setNexoDrainActive(false);
                        FeedbackHelper.sendLeftNexus(player);
                    }
                }

                // Apply Inn ticking
                if (insideInn) {
                    if (!cap.isInnDrainActive()) {
                        if (cap.getInnWarmupTicks() == 0) {
                            FeedbackHelper.sendInnWarmupStarted(player, RegenConfig.INN_WARMUP_TICKS / 20);
                        }
                        cap.setInnWarmupTicks(cap.getInnWarmupTicks() + 1);
                        if (cap.getInnWarmupTicks() >= RegenConfig.INN_WARMUP_TICKS) {
                            cap.setInnDrainActive(true);
                            FeedbackHelper.sendInnDrainStarted(player);
                        }
                    } else {
                        if (player.tickCount % RegenConfig.INN_DRAIN_INTERVAL_TICKS == 0) {
                            float drained = cap.drainFatigue(RegenConfig.INN_DRAIN_RATE);
                            if (drained > 0 && cap.getCurrentFatigue() == 0) {
                                FeedbackHelper.sendFullyRested(player, dev.franwdev.soulslikeregen.feedback.ServerTranslationHelper.getComponent(player, "msg.soulslikeregen.source.inn"));
                            }
                        }
                    }
                } else {
                    if (cap.getInnWarmupTicks() > 0 || cap.isInnDrainActive()) {
                        cap.setInnWarmupTicks(0);
                        cap.setInnDrainActive(false);
                    }
                }

                // 5. Campfire rest check (throttled to every 20 ticks for performance)
                if (player.tickCount % 20 == 0) {
                    boolean nearCampfire = false;
                    BlockPos playerPos = player.blockPosition();

                    for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-4, -2, -4), playerPos.offset(4, 2, 4))) {
                        BlockState state = level.getBlockState(pos);
                        if (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                            if (state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
                                nearCampfire = true;
                                break;
                            }
                        }
                    }

                    boolean stationary = player.getDeltaMovement().lengthSqr() < 0.005;

                    if (nearCampfire && stationary) {
                        cap.setCampfireTicks(cap.getCampfireTicks() + 20);
                        if (cap.getCampfireTicks() >= RegenConfig.CAMPFIRE_REQUIRED_TICKS) {
                            long currentTime = level.getGameTime();
                            if (cap.getLastCampfireUseTick() < 0 || currentTime - cap.getLastCampfireUseTick() >= RegenConfig.CAMPFIRE_COOLDOWN_TICKS) {
                                cap.drainFatigue(RegenConfig.CAMPFIRE_REDUCTION);
                                cap.setLastCampfireUseTick(currentTime);
                                FeedbackHelper.sendCampfireRest(player, RegenConfig.CAMPFIRE_REDUCTION);
                            }
                            cap.setCampfireTicks(0);
                        }
                    } else {
                        cap.setCampfireTicks(0);
                    }
                }

                // 6. Day Survival Bonus check
                long elapsed = level.getGameTime() - cap.getLastDamageTick();
                if (cap.getLastDamageTick() >= 0 && elapsed >= 24000) {
                    cap.drainFatigue(RegenConfig.DAY_BONUS_REDUCTION);
                    FeedbackHelper.sendDayBonus(player, RegenConfig.DAY_BONUS_REDUCTION);
                    cap.setLastDamageTick(level.getGameTime());
                }

                // 7. Action Bar: Send persistent bar if admin enabled it, or subtle updates when exhausted
                if (cap.isActionBarEnabled()) {
                    // Persistent bar: send every 10 ticks (2 Hz) to keep it visible
                    if (player.tickCount % 10 == 0) {
                        Component bar = FeedbackHelper.buildStatusBar(player, cap);
                        FeedbackHelper.sendActionBar(player, bar);
                    }
                } else {
                    // Default: subtle updates only when exhausted, every 5 seconds
                    if (cap.isExhausted() && player.tickCount % 100 == 0) {
                        FeedbackHelper.sendStatusActionBar(player, cap);
                    }
                }
            });
        }
    }
}
