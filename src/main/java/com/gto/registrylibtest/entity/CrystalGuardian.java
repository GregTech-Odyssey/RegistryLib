package com.gto.registrylibtest.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NonNull;

/**
 * 晶体矿守卫者 — 一个使用 Brain AI 的怪物实体。
 *
 * <ul>
 * <li>外形：0.5×0.5 方块，使用晶体矿方块模型在 1/3 缩放下渲染
 * <li>行为：自定义传感器发现正在挖掘晶体矿的玩家后，追踪并近战攻击
 * <li>空闲时在出生点附近随机游荡
 * </ul>
 */
public class CrystalGuardian extends PathfinderMob {

    private static final Brain.Provider<CrystalGuardian> BRAIN_PROVIDER = CrystalGuardianAi.brainProvider();

    public CrystalGuardian(EntityType<? extends CrystalGuardian> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2);
    }

    @Override
    protected @NonNull Brain<CrystalGuardian> makeBrain(Brain.Packed packedBrain) {
        return BRAIN_PROVIDER.makeBrain(this, packedBrain);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Brain<CrystalGuardian> getBrain() {
        return (Brain<CrystalGuardian>) super.getBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("crystalGuardianBrain");
        this.getBrain().tick(level, this);
        profiler.pop();
        profiler.push("crystalGuardianActivityUpdate");
        CrystalGuardianAi.updateActivity(this);
        profiler.pop();
        super.customServerAiStep(level);
    }
}
