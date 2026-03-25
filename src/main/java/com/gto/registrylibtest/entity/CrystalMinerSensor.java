package com.gto.registrylibtest.entity;

import com.google.common.collect.ImmutableSet;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

/** 自定义传感器：扫描附近 16 格内的玩家，检查是否有人最近挖掘了晶体矿。 如果发现，将最近的挖矿玩家写入 {@code CRYSTAL_MINER_MEMORY}。 */
public class CrystalMinerSensor extends Sensor<CrystalGuardian> {

    public CrystalMinerSensor() {
        super(20); // 每秒扫描一次
    }

    @Override
    protected void doTick(ServerLevel level, CrystalGuardian entity) {
        long gameTime = level.getGameTime();
        CrystalOreBlock.cleanStaleEntries(gameTime);

        Optional<Player> nearestMiner = level.players().stream()
                .filter(p -> p.distanceToSqr(entity) < 256.0) // 16 格以内
                .filter(p -> CrystalOreBlock.isRecentMiner(p, gameTime))
                .min(Comparator.comparingDouble(p -> p.distanceToSqr(entity)))
                .map(p -> (Player) p);

        if (nearestMiner.isPresent()) {
            entity
                    .getBrain()
                    .setMemory(SimpleEntityExample.CRYSTAL_MINER_MEMORY.get(), nearestMiner.get());
        } else {
            entity.getBrain().eraseMemory(SimpleEntityExample.CRYSTAL_MINER_MEMORY.get());
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(SimpleEntityExample.CRYSTAL_MINER_MEMORY.get());
    }
}
