package com.gto.registrylibtest.entity;

import static com.gto.registrylibtest.RegistryLibTest.REGISTRYLIB;

import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.EntityEntry;
import com.gto.registrylib.util.entry.RegistryEntry;
import com.gto.registrylibtest.client.CrystalGuardianRenderer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

/**
 * 晶体矿守卫者 — 完整的实体注册示例。
 *
 * <p>注册内容：
 * <ul>
 *   <li>{@link #CRYSTAL_ORE} — 晶体矿方块（记录挖掘玩家）</li>
 *   <li>{@link #CRYSTAL_MINER_MEMORY} — 自定义 MemoryModuleType（最近挖矿玩家）</li>
 *   <li>{@link #CRYSTAL_MINER_SENSOR} — 自定义 SensorType（探测挖矿玩家）</li>
 *   <li>{@link #CRYSTAL_GUARDIAN} — 晶体矿守卫者实体（Brain AI + 方块模型渲染）</li>
 * </ul>
 */
public class SimpleEntityExample {

    // ── 方块 ─────────────────────────────────────────────────────────────────

    public static final BlockEntry<CrystalOreBlock> CRYSTAL_ORE = REGISTRYLIB
            .block(REGISTRYLIB, "crystal_ore", CrystalOreBlock::new)
            .langCn("晶体矿")
            .lang("Crystal Ore")
            .initialProperties(Blocks.DIAMOND_ORE)
            .simpleItem()
            .register();

    // ── Brain 组件 ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static final RegistryEntry<MemoryModuleType<?>, MemoryModuleType<Player>> CRYSTAL_MINER_MEMORY =
            (RegistryEntry<MemoryModuleType<?>, MemoryModuleType<Player>>) (RegistryEntry<?, ?>)
                    REGISTRYLIB.simple(
                            "nearest_crystal_miner",
                            Registries.MEMORY_MODULE_TYPE,
                            key -> new MemoryModuleType<>(Optional.empty()));

    @SuppressWarnings("unchecked")
    public static final RegistryEntry<SensorType<?>, SensorType<CrystalMinerSensor>> CRYSTAL_MINER_SENSOR =
            (RegistryEntry<SensorType<?>, SensorType<CrystalMinerSensor>>) (RegistryEntry<?, ?>)
                    REGISTRYLIB.simple(
                            "crystal_miner_sensor",
                            Registries.SENSOR_TYPE,
                            key -> new SensorType<>(CrystalMinerSensor::new));

    // ── 实体 ─────────────────────────────────────────────────────────────────

    public static final EntityEntry<CrystalGuardian> CRYSTAL_GUARDIAN = REGISTRYLIB
            .<CrystalGuardian>entity("crystal_guardian", CrystalGuardian::new, MobCategory.MONSTER)
            .langCn("晶体矿守卫者")
            .lang("Crystal Guardian")
            .sized(0.5F, 0.5F)
            .clientTrackingRange(10)
            .attributes(CrystalGuardian::createAttributes)
            .renderer(() -> CrystalGuardianRenderer::new)
            .spawnEgg()
            .register();
}
