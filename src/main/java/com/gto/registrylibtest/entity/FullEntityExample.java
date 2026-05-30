package com.gto.registrylibtest.entity;

import com.gto.registrylib.util.ImageUtil;
import com.gto.registrylib.util.entry.EntityEntry;
import com.gto.registrylibtest.ModRegistryCore;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static com.gto.registrylibtest.RegistryLibTest.REGISTRYLIB;

/**
 * 使用 EntityBuilder 全部 API 的复杂实体示例。
 *
 * <p>
 * 与 {@link SimpleEntityExample} 的区别：
 *
 * <ul>
 * <li>使用传统 <b>GoalSelector</b> AI 而非 Brain AI
 * <li>使用 {@link SynchedEntityData} 进行服务端→客户端实时数据同步
 * <li>展示所有 EntityBuilder 方法（fireImmune、properties、spawnEgg 自定义等）
 * </ul>
 *
 * <p>
 * 涵盖 API：properties / sized / clientTrackingRange / updateInterval / fireImmune / attributes /
 * renderer / spawnEgg（带消费者）/ lang / langCn / addTag / loot / spawnPlacement / spawnBiomes
 *
 * @see SimpleEntityExample 使用 Brain AI 的简单示例
 */
public class FullEntityExample {

    // ── 完整实体注册链 ──────────────────────────────────────────────────────

    public static final EntityEntry<ObsidianGolem> OBSIDIAN_GOLEM = REGISTRYLIB
            // 创建 EntityBuilder：名称、工厂方法、生物分类（MONSTER = 敌对怪物）
            .<ObsidianGolem>entity("obsidian_golem", ObsidianGolem::new, MobCategory.MONSTER)
            // --- langCn: 简体中文显示名称 ---
            .langCn("黑曜石傀儡")
            // --- lang: 英文显示名称 ---
            .lang("Obsidian Golem")
            // --- sized: 碰撞箱尺寸（宽 × 高），单位：方块 ---
            .sized(0.7F, 1.8F)
            // --- clientTrackingRange: 客户端追踪距离（区块数），超出后客户端停止渲染 ---
            .clientTrackingRange(8)
            // --- updateInterval: 服务端→客户端位置同步间隔（tick），越小越流畅但网络开销越大 ---
            .updateInterval(3)
            // --- fireImmune: 免疫火焰/岩浆伤害（黑曜石来自岩浆，天然耐火）---
            .fireImmune()
            // --- attributes: 注册实体属性（生命值、攻击力、速度等）---
            // 必须在 EntityAttributeCreationEvent 之前调用，否则游戏崩溃
            .attributes(ObsidianGolem::createAttributes)
            // --- renderer: 客户端渲染器（supplier-of-supplier 惰性加载，仅在 Dist.CLIENT 链接/执行）---
            .renderer(() -> () -> ObsidianGolemRenderer::new)
            // --- spawnEgg: 刷怪蛋（Consumer 可自定义蛋的物品属性/名称）---
            .spawnEgg(
                    egg -> egg.lang("Obsidian Golem Spawn Egg")
                            .lang(ModRegistryCore.LANG_ZH_CN, "黑曜石傀儡刷怪蛋")
                            .texture(
                                    () -> ImageUtil.generateIcon(
                                            new Color(76, 72, 84),
                                            ImageUtil.CIRCLE,
                                            new Color(135, 103, 68))))
            // --- addTag: 将实体添加到标签（用于数据包条件判断）---
            .addTag(EntityTypeTags.FALL_DAMAGE_IMMUNE)
            // --- loot: 实体战利品表（掉落物品）---
            .loot(
                    (tables, entityType) -> tables.add(
                            entityType,
                            LootTable.lootTable()
                                    .withPool(
                                            LootPool.lootPool()
                                                    .setRolls(ConstantValue.exactly(1))
                                                    .add(LootItem.lootTableItem(Items.OBSIDIAN))
                                                    .when(LootItemKilledByPlayerCondition.killedByPlayer()))
                                    .withPool(
                                            LootPool.lootPool()
                                                    .setRolls(ConstantValue.exactly(1))
                                                    .add(LootItem.lootTableItem(Items.IRON_INGOT)))))
            // --- spawnPlacement: 刷新放置规则（生成条件）---
            .spawnPlacement(
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules)
            // --- spawnBiomes: 添加到主世界生物群系的自然刷怪列表（权重 60，每次 1~2 只）---
            .spawnBiomes(BiomeTags.IS_OVERWORLD, 60, 1, 2)
            .register();

    // ── 其他 EntityBuilder 方法说明 ─────────────────────────────────────────
    // .noSummon() — 阻止 /summon 命令召唤（用于拴绳连接点等辅助实体）
    // .noSave() — 不保存到世界数据（用于投射物、特效等短命实体）
    // .properties() — 直接访问 EntityType.Builder 的完整配置作为逃逸口
    // 上述方法通常用于非持久性实体，不适用于常规怪物，故此处不演示。

    // ═══════════════════════════════════════════════════════════════════════
    // 黑曜石傀儡 — 使用 GoalSelector 的怪物实体
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 黑曜石傀儡 — 使用传统 GoalSelector AI 的怪物实体。
     *
     * <p>
     * 演示要点：
     *
     * <ul>
     * <li><b>GoalSelector</b> — 经典的基于优先级的 AI 系统
     * <li><b>SynchedEntityData</b> — 自动将 {@code IS_ENRAGED} 从服务端同步到客户端
     * <li><b>hurtServer</b> — 受伤时根据血量百分比切换狂暴状态
     * </ul>
     *
     * <h3>GoalSelector vs Brain</h3>
     *
     * <p>
     * GoalSelector 基于优先级队列，每 tick 选择最高优先级可运行的 Goal。适合中低复杂度 AI。 Brain 系统使用 Sensor → Memory →
     * Behavior 分层，适合复杂行为编排（见 {@link CrystalGuardianAi}）。
     */
    public static class ObsidianGolem extends PathfinderMob {

        // ── SynchedEntityData ────────────────────────────────────
        // 需要在客户端渲染时读取的状态必须通过 SynchedEntityData 同步。
        // defineId() 创建唯一 ID；EntityDataSerializers 定义序列化类型。
        private static final EntityDataAccessor<Boolean> IS_ENRAGED = SynchedEntityData.defineId(ObsidianGolem.class, EntityDataSerializers.BOOLEAN);

        public ObsidianGolem(EntityType<? extends ObsidianGolem> type, Level level) {
            super(type, level);
        }

        /**
         * 注册属性 — 必须通过 EntityBuilder.attributes() 或 EntityAttributeCreationEvent 注册， 否则实体创建时将导致
         * NullPointerException。
         */
        public static AttributeSupplier.Builder createAttributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 40.0) // 20颗心
                    .add(Attributes.ATTACK_DAMAGE, 5.0) // 基础攻击力
                    .add(Attributes.MOVEMENT_SPEED, 0.28) // 移动速度
                    .add(Attributes.FOLLOW_RANGE, 24.0) // AI 追踪范围
                    .add(Attributes.ARMOR, 4.0) // 基础护甲
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.5); // 50% 击退抗性
        }

        // ── SynchedEntityData 注册 ──────────────────────────────
        // 必须调用 super 以注册父类字段，然后添加自定义字段及默认值。
        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
            super.defineSynchedData(builder);
            builder.define(IS_ENRAGED, false);
        }

        // ── GoalSelector AI ─────────────────────────────────────
        // goalSelector：控制自身行为（移动、攻击、闲逛）
        // targetSelector：决定攻击目标（谁是敌人）
        // 数字越小 = 优先级越高；相同 Flag 的 Goal 不会同时运行。
        @Override
        protected void registerGoals() {
            // --- goalSelector：自身行为 ---
            goalSelector.addGoal(0, new FloatGoal(this)); // 水中浮起
            goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true)); // 近战攻击
            goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8)); // 随机游荡
            goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F)); // 凝视玩家
            goalSelector.addGoal(4, new RandomLookAroundGoal(this)); // 随机环顾

            // --- targetSelector：目标选择 ---
            targetSelector.addGoal(1, new HurtByTargetGoal(this)); // 被攻击则反击
            targetSelector.addGoal(
                    2,
                    new NearestAttackableTargetGoal<>( // 主动寻找最近玩家
                            this, Player.class, true));
        }

        // ── SynchedEntityData 读写方法 ──────────────────────────
        public boolean isEnraged() {
            return entityData.get(IS_ENRAGED);
        }

        public void setEnraged(boolean enraged) {
            entityData.set(IS_ENRAGED, enraged);
        }

        // ── 受伤逻辑：血量低于 50% 时进入狂暴 ─────────────────
        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
            boolean hurt = super.hurtServer(level, source, damage);
            if (hurt && !isEnraged() && getHealth() < getMaxHealth() * 0.5F) {
                setEnraged(true);
            }
            return hurt;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 渲染器 — 使用方块模型渲染实体
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 将黑曜石傀儡渲染为缩小的黑曜石方块模型（2/3 大小）。
     *
     * <p>
     * 使用 {@link EntityRenderer} + {@link BlockStateModel} 实现方块模型复用， 无需创建自定义模型文件。适合方块形态的简单实体。
     *
     * <p>
     * 如需骨骼动画，应改用 {@code MobRenderer} + {@code EntityModel} + {@code ModelLayerLocation} 并在 {@code
     * EntityRenderersEvent.RegisterLayerDefinitions} 中注册模型层。
     */
    static class ObsidianGolemRenderer extends EntityRenderer<ObsidianGolem, EntityRenderState> {

        private static final float SCALE = 0.667F;
        private List<BlockStateModelPart> cachedParts;
        private final RandomSource random = RandomSource.create();

        ObsidianGolemRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.5F;
        }

        @Override
        public EntityRenderState createRenderState() {
            return new EntityRenderState();
        }

        @Override
        public void submit(
                           EntityRenderState state,
                           PoseStack poseStack,
                           SubmitNodeCollector collector,
                           CameraRenderState camera) {
            super.submit(state, poseStack, collector, camera);

            if (cachedParts == null) {
                cachedParts = collectBlockModelParts();
            }
            if (cachedParts.isEmpty()) return;

            poseStack.pushPose();
            poseStack.translate(0.0F, SCALE * 0.5F, 0.0F);
            poseStack.scale(SCALE, SCALE, SCALE);
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            collector.submitBlockModel(
                    poseStack,
                    RenderTypes.solidMovingBlock(),
                    cachedParts,
                    new int[] { -1 },
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    state.outlineColor);

            poseStack.popPose();
        }

        private List<BlockStateModelPart> collectBlockModelParts() {
            BlockState blockState = Blocks.OBSIDIAN.defaultBlockState();
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
            List<BlockStateModelPart> parts = new ArrayList<>();
            model.collectParts(Minecraft.getInstance().level, BlockPos.ZERO, blockState, random, parts);
            return parts;
        }
    }
}
