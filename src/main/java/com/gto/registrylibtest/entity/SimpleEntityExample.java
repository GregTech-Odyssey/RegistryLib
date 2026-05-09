package com.gto.registrylibtest.entity;

import com.gto.registrylib.util.ImageUtil;
import com.gto.registrylib.util.entry.BlockEntry;
import com.gto.registrylib.util.entry.EntityEntry;
import com.gto.registrylibtest.client.CrystalGuardianRenderer;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.awt.Color;
import java.util.Optional;

import static com.gto.registrylibtest.RegistryLibTest.REGISTRYLIB;

/**
 * 晶体矿守卫者 — 完整的实体注册示例（Brain AI + MobRenderer 骨骼模型 + 战利品 + 自然生成）。
 *
 * <p>
 * 注册内容：
 *
 * <ul>
 * <li>{@link #CRYSTAL_ORE} — 晶体矿方块（记录挖掘玩家）
 * <li>{@link #CRYSTAL_MINER_MEMORY} — 自定义 MemoryModuleType（最近挖矿玩家）
 * <li>{@link #CRYSTAL_MINER_SENSOR} — 自定义 SensorType（探测挖矿玩家）
 * <li>{@link #CRYSTAL_GUARDIAN} — 晶体守卫者实体（Brain AI + MobRenderer 骨骼动画 + 战利品表 + 自然生成规则）
 * </ul>
 *
 * <p>
 * 本示例涵盖 EntityBuilder 的全部 API：
 *
 * <ul>
 * <li>{@code sized} — 碰撞箱
 * <li>{@code clientTrackingRange} — 追踪距离
 * <li>{@code attributes} — 属性（生命值、攻击力等）
 * <li>{@code renderer} — MobRenderer 骨骼动画渲染器
 * <li>{@code spawnEgg} — 刷怪蛋
 * <li>{@code loot} — 实体战利品表
 * <li>{@code spawnPlacement} — 自然生成规则
 * <li>{@code spawnBiomes} — 生物群系生成列表
 * </ul>
 */
public class SimpleEntityExample {

    // ── 方块 ─────────────────────────────────────────────────────────────────

    public static final BlockEntry<CrystalOreBlock> CRYSTAL_ORE = REGISTRYLIB
            .block(REGISTRYLIB, "crystal_ore", CrystalOreBlock::new)
            .langCn("晶体矿")
            .lang("Crystal Ore")
            .initialProperties(Blocks.DIAMOND_ORE)
            .texture(
                    () -> ImageUtil.generateIcon(
                            new Color(105, 240, 255), ImageUtil.CIRCLE, new Color(44, 84, 112)))
            .defaultLoot()
            .simpleItem()
            .register();

    // ── Brain 组件 ────────────────────────────────────────────────────────────

    public static final MemoryModuleType<Player> CRYSTAL_MINER_MEMORY = REGISTRYLIB.registry(
            "nearest_crystal_miner",
            new MemoryModuleType<>(Optional.empty()),
            Registries.MEMORY_MODULE_TYPE);

    public static final SensorType<CrystalMinerSensor> CRYSTAL_MINER_SENSOR = REGISTRYLIB.registry(
            "crystal_miner_sensor",
            new SensorType<>(CrystalMinerSensor::new),
            Registries.SENSOR_TYPE);

    // ── 实体 ─────────────────────────────────────────────────────────────────

    public static final EntityEntry<CrystalGuardian> CRYSTAL_GUARDIAN = REGISTRYLIB
            .<CrystalGuardian>entity("crystal_guardian", CrystalGuardian::new, MobCategory.MONSTER)
            .langCn("晶体守卫者")
            .lang("Crystal Guardian")
            // --- sized: 碰撞箱适配骨骼模型（宽 0.6 × 高 1.5 方块）---
            .sized(0.6F, 1.5F)
            .clientTrackingRange(10)
            .attributes(CrystalGuardian::createAttributes)
            // --- renderer: MobRenderer 骨骼动画渲染器 ---
            .renderer(() -> CrystalGuardianRenderer::new)
            .spawnEgg(
                    egg -> egg.texture(
                            () -> ImageUtil.generateIcon(
                                    new Color(149, 232, 255), ImageUtil.CIRCLE, new Color(87, 54, 168))))

            // --- loot: 实体战利品表（被玩家击杀时掉落钻石 1~2 + 经验）---
            .loot(
                    (loot, type) -> loot.add(
                            type,
                            LootTable.lootTable()
                                    .withPool(
                                            LootPool.lootPool()
                                                    .setRolls(ConstantValue.exactly(1.0F))
                                                    .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(1))
                                                    .when(LootItemKilledByPlayerCondition.killedByPlayer()))
                                    .withPool(
                                            LootPool.lootPool()
                                                    .setRolls(UniformGenerator.between(0.0F, 1.0F))
                                                    .add(LootItem.lootTableItem(Items.AMETHYST_SHARD)))))
            // --- spawnPlacement: 怪物类自然生成规则（地面生成 + 标准怪物亮度检查）---
            .spawnPlacement(
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules)
            // --- spawnBiomes: 添加到主世界生物群系的自然刷怪列表（权重 80，每次 1~3 只）---
            .spawnBiomes(BiomeTags.IS_OVERWORLD, 80, 1, 3)
            .register();
}
