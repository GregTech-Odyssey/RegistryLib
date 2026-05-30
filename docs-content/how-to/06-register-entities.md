---
sidebar_position: 6
title: Register Entities
description: How to register custom living entities (mobs) with AI, attributes, renderers, loot tables, and spawn rules using RegistryLib.
---

# Register Entities

本指南涵盖使用 RegistryLib 注册自定义实体（怪物/生物）的完整流程。

## 本章内容一览

| 章节 | 内容 |
| --- | --- |
| [基础注册](#basic-entity-registration) | `entity()` 入口与最精简注册链 |
| [Step 1: 实体类](#step-1-create-the-entity-class) | 基类选择、属性定义 |
| [Step 2: AI — GoalSelector](#step-2-add-ai--goalselector-classic) | 传统优先级目标系统 |
| [Step 2′: AI — Brain](#step-2-alternative-add-ai--brain-system) | Sensor → Memory → Behavior 架构 |
| [Step 3: SynchedEntityData](#step-3-synchedentitydata-optional) | 服务端→客户端数据同步 |
| [Step 4: 客户端渲染器](#step-4-client-renderer) | 方块模型渲染 vs MobRenderer 骨骼动画 |
| [Step 5: 战利品表 — loot()](#step-5-entity-loot-table) | 实体掉落物配置 |
| [Step 6: 自然生成规则 — spawnPlacement()](#step-6-spawn-placement) | 生成条件（地形、高度图、判定函数） |
| [Step 6.5: 生物群系生成 — spawnBiomes()](#step-65-biome-spawn-list) | 将实体添加到生物群系刷怪列表 |
| [Step 7: 完整注册链](#step-7-register-with-entitybuilder) | 所有方法串联 |
| [方法速查表](#entitybuilder-method-summary) | 全部 EntityBuilder 方法一览 |

---

## Basic Entity Registration

```java
public static final EntityEntry<MyMob> MY_MOB = REGISTRYLIB
        .<MyMob>entity("my_mob", MyMob::new, MobCategory.MONSTER)
        .lang("My Mob")
        .sized(0.6F, 1.95F)
        .clientTrackingRange(8)
        .attributes(MyMob::createAttributes)
        .renderer(() -> () -> MyMobRenderer::new)
        .spawnEgg()
        .register();
```

**入口参数：**

| Parameter | Meaning |
| --- | --- |
| `"my_mob"` | 注册名（最终变为 `modid:my_mob`） |
| `MyMob::new` | 实体构造器引用 (`EntityType.EntityFactory<T>`) |
| `MobCategory.MONSTER` | 生成类别：`MONSTER` / `CREATURE` / `AMBIENT` / `WATER_CREATURE` / `MISC` |

---

## Step 1: Create the Entity Class

根据需求选择基类：

| Base Class | Use Case |
| --- | --- |
| `PathfinderMob` | 基础移动 + 寻路 |
| `Monster` | 主动攻击玩家的敌对怪物 |
| `Animal` | 可繁殖的被动生物 |
| `AgeableMob` | 有幼年→成年阶段的生物 |

```java
public class MyMob extends Monster {
    public MyMob(EntityType<? extends MyMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }
}
```

:::warning
`LivingEntity` 的属性是**必须**的。未注册属性的实体在生成时会导致崩溃——必须在 builder 上调用 `.attributes()`。
:::

---

## Step 2: Add AI — GoalSelector (Classic)

最简单的 AI 方式。目标按优先级排列：数字越小 = 优先级越高。

```java
@Override
protected void registerGoals() {
    // 自身行为目标
    goalSelector.addGoal(0, new FloatGoal(this));                          // 在水中漂浮
    goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));        // 近战攻击
    goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));// 随机游荡
    goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8f));// 注视玩家
    goalSelector.addGoal(4, new RandomLookAroundGoal(this));              // 随机张望

    // 目标选择
    targetSelector.addGoal(1, new HurtByTargetGoal(this));                // 被攻击时反击
    targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(          // 主动搜索玩家
            this, Player.class, true));
}
```

| Built-in Goal | Purpose |
| --- | --- |
| `FloatGoal` | 在水中保持漂浮 |
| `MeleeAttackGoal` | 走向目标并攻击 |
| `RangedAttackGoal` | 远程攻击目标 |
| `WaterAvoidingRandomStrollGoal` | 随机游荡（避水） |
| `LookAtPlayerGoal` | 注视附近玩家 |
| `HurtByTargetGoal` | 被打时反击 |
| `NearestAttackableTargetGoal` | 主动寻找最近目标 |

---

## Step 2 (Alternative): Add AI — Brain System

适用于复杂 AI，采用 Sensor → Memory → Behavior 三层架构。完整示例参见测试模组的 `CrystalGuardianAi.java`。

```java
// 在实体类中：
private static final Brain.Provider<MyMob> BRAIN_PROVIDER = MyMobAi.brainProvider();

@Override
protected Brain<MyMob> makeBrain(Brain.Packed packedBrain) {
    return BRAIN_PROVIDER.makeBrain(this, packedBrain);
}

// 在单独的 AI 配置类中：
public static Brain.Provider<MyMob> brainProvider() {
    return Brain.provider(
            List.of(/* MemoryModuleTypes */),
            List.of(/* SensorTypes */),
            MyMobAi::getActivities);          // ActivitySupplier<MyMob>
}

static List<ActivityData<MyMob>> getActivities(MyMob body) {
    return List.of(
            ActivityData.create(Activity.CORE, 0, ImmutableList.of(/* 核心行为 */)),
            ActivityData.create(Activity.IDLE, 0, ImmutableList.of(/* 待机行为 */)),
            ActivityData.create(Activity.FIGHT, 10, ImmutableList.of(/* 战斗行为 */),
                    MemoryModuleType.ATTACK_TARGET)); // 停止时清除
}
```

:::tip
**GoalSelector vs Brain：** GoalSelector 简单直接，适合大多数怪物。Brain 更强大（Villager、Warden、Allay 使用），但样板代码更多。根据 AI 复杂度选择。
:::

---

## Step 3: SynchedEntityData (Optional)

使用 `SynchedEntityData` 将服务端状态同步到客户端（如用于渲染状态切换）：

```java
private static final EntityDataAccessor<Boolean> IS_ENRAGED =
        SynchedEntityData.defineId(MyMob.class, EntityDataSerializers.BOOLEAN);

@Override
protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(IS_ENRAGED, false);
}

public boolean isEnraged() { return entityData.get(IS_ENRAGED); }
public void setEnraged(boolean val) { entityData.set(IS_ENRAGED, val); }
```

常用 `EntityDataSerializers`：`BOOLEAN`、`INT`、`FLOAT`、`STRING`、`OPTIONAL_BLOCK_POS`、`COMPOUND_TAG`。

---

## Step 4: Client Renderer

### Option A: 方块模型渲染器

将实体渲染为缩放的方块模型——无需纹理文件：

```java
public class MyMobRenderer extends EntityRenderer<MyMob, EntityRenderState> {
    @Override
    public void submit(EntityRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitBlockModel(poseStack, RenderTypes.solidMovingBlock(),
                parts, new int[]{-1}, state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
    }
}
```

### Option B: MobRenderer + EntityModel（骨骼动画）

用于自定义纹理和骨骼动画的实体。完整流程分三步：

#### B-1. 定义骨骼模型

骨骼模型的构建管线：`MeshDefinition` → `PartDefinition` → `CubeListBuilder` → `LayerDefinition` → `ModelPart`。

```java
public class MyMobModel extends EntityModel<LivingEntityRenderState> {
    // 模型层位置——用于注册和 bakeLayer() 查找
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "my_mob"), "main");

    // 骨骼引用（用于动画）
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public MyMobModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
    }

    /**
     * 定义骨骼结构——在 RegisterLayerDefinitions 事件中调用。
     *
     * <p>每个 {@code addOrReplaceChild} 创建一个骨骼节点：
     * <ul>
     *   <li>String name — 骨骼名称（与构造器中 getChild() 匹配）</li>
     *   <li>CubeListBuilder — 此骨骼包含的立方体列表</li>
     *   <li>PartPose — 初始位置/旋转（resetPose 会还原到此状态）</li>
     * </ul>
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // head: 6×6×6，位于身体顶部 (y=16)
        PartDefinition headPart = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6),
                PartPose.offset(0.0F, 8.0F, 0.0F));   // pivot at neck

        // body: 6×8×4
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-3.0F, 0.0F, -2.0F, 6, 8, 4),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        // arms: 3×8×3
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(20, 12).addBox(-2.0F, 0.0F, -1.5F, 3, 8, 3),
                PartPose.offset(-4.5F, 8.0F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(20, 12).mirror().addBox(-1.0F, 0.0F, -1.5F, 3, 8, 3),
                PartPose.offset(4.5F, 8.0F, 0.0F));

        return LayerDefinition.create(mesh, 48, 36); // 纹理尺寸 48×36
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state); // 调用 resetPose()

        // 头部跟踪——yRot 左右转头，xRot 上下点头
        head.yRot = state.yRot * ((float) Math.PI / 180F);
        head.xRot = state.xRot * ((float) Math.PI / 180F);

        // 行走时手臂前后摆动
        float walkSpeed = state.walkAnimationSpeed;
        float walkPos = state.walkAnimationPos;
        rightArm.xRot = (float) Math.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;
        leftArm.xRot = (float) Math.cos(walkPos * 0.6662F + Math.PI) * 1.4F * walkSpeed;
    }
}
```

关键概念：
- **`texOffs(u, v)`** — 纹理 UV 偏移量（对应 png 文件中的像素坐标）
- **`addBox(x, y, z, w, h, d)`** — 从 (x,y,z) 开始的 w×h×d 方块
- **`PartPose.offset(x, y, z)`** — 骨骼枢轴点在父节点坐标系中的位置
- **`mirror()`** — 水平镜像（用于左右对称部件）
- **`setupAnim()`** — 每帧调用，通过设置 `ModelPart` 的 `xRot`/`yRot`/`zRot` 驱动动画

#### B-2. 创建渲染器

```java
public class MyMobRenderer extends MobRenderer<MyMob, LivingEntityRenderState, MyMobModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID,
            "textures/entity/my_mob.png");

    public MyMobRenderer(EntityRendererProvider.Context context) {
        // 参数: context, 骨骼模型实例, 阴影半径
        super(context, new MyMobModel(context.bakeLayer(MyMobModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
```

`MobRenderer` 内部自动处理：
- 调用 `setupAnim(state)` 驱动骨骼动画
- 姿势矩阵变换（缩放、旋转、受伤红闪等）
- 阴影渲染

#### B-3. 注册模型层

模型层必须通过客户端事件单独注册——RegistryLib 的 `renderer()` 只处理渲染器绑定：

```java
// 在 mod 主类构造函数中（客户端侧）：
DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
        modEventBus.addListener(MyMod::onRegisterLayerDefinitions));

// 事件处理器：
private static void onRegisterLayerDefinitions(
        EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.registerLayerDefinition(MyMobModel.LAYER_LOCATION, MyMobModel::createBodyLayer);
}
```

:::note
`renderer()` 方法内部通过 `Supplier` 惰性加载渲染器，确保仅在客户端执行。但模型层定义（`LayerDefinition`）需要在 `RegisterLayerDefinitions` 事件中手动注册。
:::

---

## Step 5: Entity Loot Table

使用 `.loot()` 为实体定义掉落物——与 `BlockBuilder.loot()` API 风格完全一致。

```java
.loot((tables, entityType) -> tables.add(entityType, LootTable.lootTable()
        .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.DIAMOND))
                .when(LootItemKilledByPlayerCondition.killedByPlayer()))
        .withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.AMETHYST_SHARD)))))
```

**`loot()` 参数解析：**

| Component | Type | Description |
| --- | --- | --- |
| `tables` | `RegistryLibEntityLootTables` | 战利品表构建器（继承 `EntityLootSubProvider`） |
| `entityType` | `EntityType<T>` | 当前注册的实体类型 |
| `LootTable.lootTable()` | builder | 创建战利品表 |
| `LootPool.lootPool()` | builder | 创建一个奖池（可叠加多个） |
| `setRolls()` | `NumberProvider` | 掷骰次数：`ConstantValue.exactly(n)` 或 `UniformGenerator.between(min, max)` |
| `add()` entry | `LootItem.lootTableItem(item)` | 添加战利品条目 |
| `when()` condition | `LootItemKilledByPlayerCondition` | 条件触发（此处：仅玩家击杀） |

:::tip
**多奖池 (withPool) vs 多条目 (add)：** 每个 `withPool` 独立掷骰；同一 pool 内的多个 `add` 共享掷骰次数（随机选一个或按权重选取）。上述例子中钻石仅在玩家击杀时掉落，紫水晶碎片无条件掉落。
:::

---

## Step 6: Spawn Placement

使用 `.spawnPlacement()` 为实体定义自然生成条件。

```java
.spawnPlacement(SpawnPlacementTypes.ON_GROUND,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
        Monster::checkMonsterSpawnRules)
```

**三个参数：**

| Parameter | Type | Description |
| --- | --- | --- |
| `placementType` | `SpawnPlacementType` | 放置位置类型 |
| `heightmap` | `Heightmap.Types` | 用于确定地面高度的高度图 |
| `predicate` | `SpawnPredicate<T>` | 判定函数 `(type, level, spawnType, pos, random) → boolean` |

**常用 `SpawnPlacementTypes`：**

| Type | Description |
| --- | --- |
| `ON_GROUND` | 地面实体（大多数陆地生物） |
| `IN_WATER` | 水中实体（鱼、海豚等） |
| `NO_RESTRICTIONS` | 无限制（蝙蝠、幽灵等可在任意位置生成） |

**常用内置判定函数：**

| Predicate | Description |
| --- | --- |
| `Monster::checkMonsterSpawnRules` | 标准怪物规则（亮度≤0、非和平模式等） |
| `Animal::checkAnimalSpawnRules` | 标准动物规则（草地上方、亮度充足） |
| `Mob::checkMobSpawnRules` | 最宽泛的默认规则 |

也可以传入自定义 lambda：

```java
.spawnPlacement(SpawnPlacementTypes.ON_GROUND,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
        (type, level, spawnType, pos, random) ->
                pos.getY() < 40 && level.canSeeSky(pos))
```

:::note
RegistryLib 内部将 spawn placement 注册为 `RegisterSpawnPlacementsEvent` 的回调。对于新注册的实体默认使用 `Operation.REPLACE`。
:::

---

## Step 6.5: Biome Spawn List

仅调用 `spawnPlacement()` 只会注册实体的生成**条件**（在什么地形可以生成），但不会让它实际出现在任何生物群系的刷怪列表中。要让实体自然生成，还需要调用 `.spawnBiomes()` 将其添加到指定生物群系的生成列表。

```java
.spawnBiomes(BiomeTags.IS_OVERWORLD, 80, 1, 3)
```

**四个参数：**

| Parameter | Type | Description |
| --- | --- | --- |
| `biomeTag` | `TagKey<Biome>` | 生物群系标签（决定在哪些生物群系中生成） |
| `weight` | `int` | 生成权重（值越大，在同分类中被选中的概率越高）|
| `minCount` | `int` | 每次生成的最小数量（最小群体大小）|
| `maxCount` | `int` | 每次生成的最大数量（最大群体大小）|

**常用 `BiomeTags`：**

| Tag | Description |
| --- | --- |
| `BiomeTags.IS_OVERWORLD` | 所有主世界生物群系 |
| `BiomeTags.IS_NETHER` | 所有下界生物群系 |
| `BiomeTags.IS_END` | 所有末地生物群系 |
| `BiomeTags.IS_FOREST` | 森林类生物群系 |
| `BiomeTags.IS_OCEAN` | 海洋类生物群系 |
| `BiomeTags.IS_MOUNTAIN` | 山地类生物群系 |

:::tip 两步才能自然生成
**`spawnPlacement()`** 定义 _在哪种地形 / 亮度下可以生成_（条件）。
**`spawnBiomes()`** 定义 _在哪些生物群系中会尝试生成_（列表）。
两者缺一不可。缺少 `spawnPlacement()` 时 NeoForge 会在日志中输出警告；缺少 `spawnBiomes()` 时实体根本不会出现在世界中。
:::

:::note
RegistryLib 内部通过 NeoForge 的 `AddSpawnsBiomeModifier` 数据包注册来实现此功能。运行 Data Generation 后会输出对应的 JSON 文件到 `data/<modid>/neoforge/biome_modifier/<name>_spawn.json`。
:::

---

## Step 7: Register with EntityBuilder

所有方法串联的完整注册链：

```java
public static final EntityEntry<MyMob> MY_MOB = REGISTRYLIB
        .<MyMob>entity("my_mob", MyMob::new, MobCategory.MONSTER)
        .lang("My Mob")
        .sized(0.6F, 1.95F)           // 碰撞箱
        .clientTrackingRange(8)        // 渲染距离（区块）
        .updateInterval(3)            // 同步间隔（tick）
        .fireImmune()                 // 火焰/岩浆免疫
        .attributes(MyMob::createAttributes)
        .renderer(() -> () -> MyMobRenderer::new)
        .spawnEgg(egg -> egg.lang("My Mob Spawn Egg"))
        .addTag(EntityTypeTags.FALL_DAMAGE_IMMUNE)
        .loot((tables, entityType) -> tables.add(entityType, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.DIAMOND))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer()))))
        .spawnPlacement(SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules)
        .spawnBiomes(BiomeTags.IS_OVERWORLD, 80, 1, 3)
        .register();
```

---

## EntityBuilder Method Summary

| Method | Required | Description |
| --- | --- | --- |
| `sized()` | ✅ | 碰撞箱——决定命中框和渲染范围 |
| `attributes()` | ✅* | 实体属性——LivingEntity 没有会崩溃 |
| `renderer()` | ✅ | 客户端渲染器——没有则不可见 |
| `lang()` | 推荐 | 显示名称（用于 UI / 死亡消息） |
| `clientTrackingRange()` | 可选 | 默认值因实体类型而异 |
| `updateInterval()` | 可选 | 默认 3 tick |
| `fireImmune()` | 可选 | 火焰伤害免疫 |
| `noSummon()` | 可选 | 禁止 `/summon`（用于辅助实体） |
| `noSave()` | 可选 | 不保存到世界（用于瞬态实体） |
| `spawnEgg()` | 可选 | 创建刷怪蛋 |
| `addTag()` | 可选 | 添加到实体类型标签 |
| `loot()` | 可选 | 实体战利品表（掉落物） |
| `spawnPlacement()` | 可选 | 自然生成规则（地形/高度图/判定） |
| `spawnBiomes()` | 可选 | 将实体添加到生物群系自然生成列表 |
| `properties()` | 可选 | 直接访问 `EntityType.Builder`（逃逸口） |

---

## See Also

- [Entry Types Reference](/reference/entry-types#entityentryt) — EntityEntry API
- [Builder Methods Reference](/reference/builder-methods#entitybuilder) — 完整方法表
- [Recipes & Tags Tutorial](/tutorials/recipes-tags) — 使用 `addTag` 和 `ProviderType.ENTITY_TAGS`
