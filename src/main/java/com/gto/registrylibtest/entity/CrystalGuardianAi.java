package com.gto.registrylibtest.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Optional;

/**
 * 晶体矿守卫者的 Brain AI 配置（NeoForge 26.1 ActivityData API）。
 *
 * <ul>
 * <li><b>CORE</b> — 游泳、看向目标、走向目标
 * <li><b>IDLE</b> — 发现挖矿玩家后进入战斗；否则随机游荡、环视
 * <li><b>FIGHT</b> — 追踪并近战攻击挖矿玩家
 * </ul>
 */
public class CrystalGuardianAi {

    /** 创建 Brain.Provider，声明所需的额外 MemoryModuleType、SensorType 以及 ActivitySupplier。 */
    @SuppressWarnings("deprecation")
    public static Brain.Provider<CrystalGuardian> brainProvider() {
        return Brain.provider(
                List.of(
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.LOOK_TARGET,
                        MemoryModuleType.ATTACK_TARGET,
                        MemoryModuleType.ATTACK_COOLING_DOWN,
                        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                        MemoryModuleType.NEAREST_LIVING_ENTITIES,
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                        SimpleEntityExample.CRYSTAL_MINER_MEMORY),
                List.of(SensorType.NEAREST_LIVING_ENTITIES, SimpleEntityExample.CRYSTAL_MINER_SENSOR),
                CrystalGuardianAi::getActivities);
    }

    // ── ActivityData 列表 ────────────────────────────────────────────────────

    protected static List<ActivityData<CrystalGuardian>> getActivities(CrystalGuardian body) {
        return List.of(initCoreActivity(), initIdleActivity(), initFightActivity());
    }

    private static ActivityData<CrystalGuardian> initCoreActivity() {
        return ActivityData.<CrystalGuardian>create(
                Activity.CORE,
                0,
                ImmutableList.of(new Swim<>(0.8F), new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }

    private static ActivityData<CrystalGuardian> initIdleActivity() {
        return ActivityData.<CrystalGuardian>create(
                Activity.IDLE,
                0,
                ImmutableList.of(
                        StartAttacking.create(CrystalGuardianAi::findTarget),
                        new RunOne<>(
                                ImmutableList.of(
                                        Pair.of(RandomStroll.stroll(0.4F), 2),
                                        Pair.of(SetEntityLookTarget.create(8.0F), 1),
                                        Pair.of(new DoNothing(30, 60), 1)))));
    }

    private static ActivityData<CrystalGuardian> initFightActivity() {
        return ActivityData.<CrystalGuardian>create(
                Activity.FIGHT,
                10,
                ImmutableList.of(
                        StopAttackingIfTargetInvalid.create(),
                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                        MeleeAttack.create(20)),
                MemoryModuleType.ATTACK_TARGET);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Optional<? extends LivingEntity> findTarget(
                                                               ServerLevel level, CrystalGuardian guardian) {
        return guardian.getBrain().getMemory(SimpleEntityExample.CRYSTAL_MINER_MEMORY);
    }

    public static void updateActivity(CrystalGuardian guardian) {
        guardian
                .getBrain()
                .setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }
}
