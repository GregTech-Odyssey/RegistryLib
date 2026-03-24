---
sidebar_position: 6
title: Register Entities
description: How to register custom living entities (mobs) with AI, attributes, and renderers using RegistryLib.
---

# Register Entities

This guide covers registering custom entities (mobs) with RegistryLib, including AI configuration, attribute registration, client rendering, and spawn egg creation.

## Basic Entity Registration

```java
public static final EntityEntry<MyMob> MY_MOB = REGISTRYLIB
        .<MyMob>entity("my_mob", MyMob::new, MobCategory.MONSTER)
        .lang("My Mob")
        .sized(0.6F, 1.95F)
        .clientTrackingRange(8)
        .attributes(MyMob::createAttributes)
        .renderer(() -> MyMobRenderer::new)
        .spawnEgg()
        .register();
```

**Key parameters:**

| Parameter | Meaning |
| --- | --- |
| `"my_mob"` | Registry name (becomes `modid:my_mob`) |
| `MyMob::new` | Entity constructor reference (`EntityType.EntityFactory<T>`) |
| `MobCategory.MONSTER` | Spawn category: `MONSTER`, `CREATURE`, `AMBIENT`, `WATER_CREATURE`, `MISC` |

## Step 1: Create the Entity Class

Choose a base class based on your entity's needs:

| Base Class | Use Case |
| --- | --- |
| `PathfinderMob` | Basic movement + pathfinding |
| `Monster` | Hostile mob that attacks players |
| `Animal` | Breedable passive mob |
| `AgeableMob` | Mob that grows from baby to adult |

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
Attributes are **mandatory** for any `LivingEntity`. The game crashes if you spawn an entity without registered attributes. Always call `.attributes()` on the builder.
:::

## Step 2: Add AI — GoalSelector (Classic)

The simplest AI approach. Goals are priority-based: lower number = higher priority.

```java
@Override
protected void registerGoals() {
    // Self-behaviour goals
    goalSelector.addGoal(0, new FloatGoal(this));                          // Float in water
    goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));        // Melee attack
    goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));// Wander
    goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8f));// Look at player
    goalSelector.addGoal(4, new RandomLookAroundGoal(this));              // Random look

    // Target selection goals
    targetSelector.addGoal(1, new HurtByTargetGoal(this));                // Retaliate
    targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(          // Hunt players
            this, Player.class, true));
}
```

| Built-in Goal | Purpose |
| --- | --- |
| `FloatGoal` | Stay afloat in water |
| `MeleeAttackGoal` | Walk to target and attack |
| `RangedAttackGoal` | Ranged attack at target |
| `WaterAvoidingRandomStrollGoal` | Random wander (avoids water) |
| `LookAtPlayerGoal` | Stare at nearby player |
| `HurtByTargetGoal` | Counter-attack when hit |
| `NearestAttackableTargetGoal` | Actively hunt nearest entity of type |

## Step 2 (Alternative): Add AI — Brain System

For complex AI with Sensor → Memory → Behavior architecture. See `CrystalGuardianAi.java` in the test mod for a complete example.

```java
// In your entity class:
private static final Brain.Provider<MyMob> BRAIN_PROVIDER = MyMobAi.brainProvider();

@Override
protected Brain<MyMob> makeBrain(Brain.Packed packedBrain) {
    return BRAIN_PROVIDER.makeBrain(this, packedBrain);
}

// In a separate AI configuration class:
public static Brain.Provider<MyMob> brainProvider() {
    return Brain.provider(
            List.of(/* MemoryModuleTypes */),
            List.of(/* SensorTypes */),
            MyMobAi::getActivities);          // ActivitySupplier<MyMob>
}

static List<ActivityData<MyMob>> getActivities(MyMob body) {
    return List.of(
            ActivityData.create(Activity.CORE, 0, ImmutableList.of(/* core behaviours */)),
            ActivityData.create(Activity.IDLE, 0, ImmutableList.of(/* idle behaviours */)),
            ActivityData.create(Activity.FIGHT, 10, ImmutableList.of(/* fight behaviours */),
                    MemoryModuleType.ATTACK_TARGET)); // erase when stopped
}
```

:::tip
**GoalSelector vs Brain:** GoalSelector is simpler and suits most mobs. Brain is more powerful (used by Villagers, Wardens, Allays) but requires more boilerplate. Choose based on your AI complexity.
:::

## Step 3: SynchedEntityData (Optional)

Use `SynchedEntityData` to synchronize state from server to client (e.g., for rendering):

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

Common `EntityDataSerializers`: `BOOLEAN`, `INT`, `FLOAT`, `STRING`, `OPTIONAL_BLOCK_POS`, `COMPOUND_TAG`.

## Step 4: Client Renderer

### Option A: Block Model Renderer

Renders the entity as a scaled block model — no texture files needed:

```java
@OnlyIn(Dist.CLIENT)
public class MyMobRenderer extends EntityRenderer<MyMob, EntityRenderState> {
    @Override
    public void submit(EntityRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        // Scale, translate, then submit block model parts
        collector.submitBlockModel(poseStack, RenderTypes.solidMovingBlock(),
                parts, new int[]{-1}, state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
    }
}
```

### Option B: Custom Model Renderer (MobRenderer)

For entities with custom textures and bone animations, use `MobRenderer` + `EntityModel`:

```java
@OnlyIn(Dist.CLIENT)
public class MyMobRenderer extends MobRenderer<MyMob, MyMobRenderState, MyMobModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID,
            "textures/entity/my_mob.png");

    public MyMobRenderer(EntityRendererProvider.Context context) {
        super(context, new MyMobModel(context.bakeLayer(MyMobModel.LAYER_LOC)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(MyMobRenderState state) {
        return TEXTURE;
    }
}
```

:::note
When using `MobRenderer`, you also need to register the model layer via `EntityRenderersEvent.RegisterLayerDefinitions`. The RegistryLib `renderer()` method handles the renderer binding, but model layers must be registered separately in a client event handler.
:::

## Step 5: Register with EntityBuilder

Put it all together:

```java
public static final EntityEntry<MyMob> MY_MOB = REGISTRYLIB
        .<MyMob>entity("my_mob", MyMob::new, MobCategory.MONSTER)
        .lang("My Mob")
        .sized(0.6F, 1.95F)           // Collision box
        .clientTrackingRange(8)        // Render distance (chunks)
        .updateInterval(3)            // Sync interval (ticks)
        .fireImmune()                 // Fire/lava immunity
        .attributes(MyMob::createAttributes)
        .renderer(() -> MyMobRenderer::new)
        .spawnEgg(egg -> egg.lang("My Mob Spawn Egg"))
        .addTag(EntityTypeTags.FALL_DAMAGE_IMMUNE)
        .register();
```

## EntityBuilder Method Summary

| Method | Required | Description |
| --- | --- | --- |
| `sized()` | ✅ | Collision box — determines hitbox and rendering bounds |
| `attributes()` | ✅* | Entity attributes — crash without it (*required for LivingEntity) |
| `renderer()` | ✅ | Client rendering — invisible without it |
| `lang()` | Recommended | Display name for UI/death messages |
| `clientTrackingRange()` | Optional | Default varies by entity type |
| `updateInterval()` | Optional | Default 3 ticks |
| `fireImmune()` | Optional | Immunity to fire damage |
| `noSummon()` | Optional | Prevent `/summon` (for helper entities) |
| `noSave()` | Optional | Don't persist to disk (for transient entities) |
| `spawnEgg()` | Optional | Create spawn egg for creative/commands |
| `addTag()` | Optional | Add to entity type tags |

## See Also

- [Entry Types Reference](/reference/entry-types#entityentryt) — EntityEntry API
- [Builder Methods Reference](/reference/builder-methods#entitybuilder) — Complete method table
- [Recipes & Tags Tutorial](/tutorials/recipes-tags) — Using `addTag` with `ProviderType.ENTITY_TAGS`
