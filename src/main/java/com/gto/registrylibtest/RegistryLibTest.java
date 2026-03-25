package com.gto.registrylibtest;

import com.gto.registrylib.util.ColorUtil;
import com.gto.registrylib.util.ImageUtil;
import com.gto.registrylibtest.advancement.FullAdvancementExample;
import com.gto.registrylibtest.advancement.SimpleAdvancementExample;
import com.gto.registrylibtest.block.FullBlockExample;
import com.gto.registrylibtest.block.SimpleBlockExample;
import com.gto.registrylibtest.blockentity.FullBlockEntityExample;
import com.gto.registrylibtest.blockentity.SimpleBlockEntityExample;
import com.gto.registrylibtest.enchantment.FullEnchantmentExample;
import com.gto.registrylibtest.enchantment.SimpleEnchantmentExample;
import com.gto.registrylibtest.entity.FullEntityExample;
import com.gto.registrylibtest.entity.SimpleEntityExample;
import com.gto.registrylibtest.fluid.FullFluidExample;
import com.gto.registrylibtest.fluid.SimpleFluidExample;
import com.gto.registrylibtest.item.FullItemExample;
import com.gto.registrylibtest.item.SimpleItemExample;
import com.gto.registrylibtest.recipe.FullExtendCopyExample;
import com.gto.registrylibtest.recipe.FullRecipeExample;
import com.gto.registrylibtest.recipe.MultiInputRecipeExample;
import com.gto.registrylibtest.recipe.SimpleExtendCopyExample;
import com.gto.registrylibtest.recipe.SimpleIngredientTypeExample;
import com.gto.registrylibtest.recipe.SimpleRecipeExample;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;

/**
 * Mod 入口类，仅负责：
 *
 * <ol>
 * <li>创建共享的 {@link ModRegistryCore} 实例 {@link #REGISTRYLIB}；
 * <li>注册默认创造标签页；
 * <li>按顺序触发各示例类的静态初始化，使其注册调用在模组加载期间执行。
 * </ol>
 *
 * <p>
 * {@link ModRegistryCore} 继承 {@link com.gto.registrylib.RegistryCore}， 重写了三个 builder 工厂钩子，使
 * {@code .block()} / {@code .item()} / {@code .fluid()} 返回带有 {@code .langCn(String)} 方法的子类 builder。
 *
 * <p>
 * <b>示例文件索引：</b>
 *
 * <ul>
 * <li>{@link SimpleItemExample} / {@link FullItemExample} — 物品注册
 * <li>{@link SimpleBlockExample} / {@link FullBlockExample} — 方块注册
 * <li>{@link SimpleBlockEntityExample} / {@link FullBlockEntityExample} — BlockEntity 注册
 * <li>{@link SimpleFluidExample} / {@link FullFluidExample} — 流体注册
 * <li>{@link SimpleEntityExample} / {@link FullEntityExample} — 实体注册
 * <li>{@link SimpleAdvancementExample} / {@link FullAdvancementExample} — 成就进度
 * <li>{@link SimpleRecipeExample} / {@link FullRecipeExample} — 自定义配方
 * <li>{@link SimpleExtendCopyExample} / {@link FullExtendCopyExample} — 配方 extend / copy
 * <li>{@link SimpleEnchantmentExample} / {@link FullEnchantmentExample} — 附魔
 * </ul>
 */
@Mod(RegistryLibTest.MOD_ID)
public class RegistryLibTest {

    public static final String MOD_ID = "registrylibtest";
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 共享的注册核心。 使用 {@link ModRegistryCore} 而非普通 {@link com.gto.registrylib.RegistryCore}， 使得每个 builder
     * 链上可直接调用 {@code .langCn("中文名")}。
     */
    public static final ModRegistryCore REGISTRYLIB = ModRegistryCore.create(MOD_ID);

    // === Creative Tab ===
    static {
        var tab = REGISTRYLIB.creativeTab("test_tab").register();
        REGISTRYLIB.defaultCreativeTab(tab.getKey());
    }

    // === 触发各示例类静态初始化 ===
    // Java 的类静态字段在首次访问时才初始化；通过在此引用各类的任意字段，
    // 强制其 static 块在 RegistryLibTest 加载时同步执行。
    static {
        // 物品示例
        var _item1 = SimpleItemExample.COPPER_COIN;
        var _item2 = FullItemExample.MAGIC_WAND;

        // 方块示例
        var _block1 = SimpleBlockExample.DECORATIVE_STONE;
        var _block2 = FullBlockExample.MAGIC_ORE;

        // BlockEntity 示例
        var _be1 = SimpleBlockEntityExample.SIMPLE_TIMER_BE;
        var _be2 = FullBlockEntityExample.TIMER_BLOCK_ENTITY;

        // 流体示例
        var _fluid1 = SimpleFluidExample.ACID;
        var _fluid2 = FullFluidExample.MOLTEN_IRON;

        // 自定义配方
        var _recipe1 = SimpleRecipeExample.ALTAR;
        var _recipe2 = FullRecipeExample.INFUSER;
        var _recipe3 = MultiInputRecipeExample.SYNTHESIZER;
        var _recipe4 = SimpleExtendCopyExample.EXTRA_SMELTING;
        var _recipe5 = FullExtendCopyExample.EXTENDED_SMELTING;

        // 自定义 Ingredient 类型
        var _ingredientType1 = SimpleIngredientTypeExample.MIN_DURABILITY;

        // 附魔
        var _ench1 = SimpleEnchantmentExample.ORE_FORTUNE;
        var _ench2 = FullEnchantmentExample.AUTO_SMELT;

        // 实体示例
        var _entity1 = SimpleEntityExample.CRYSTAL_GUARDIAN;
        var _entity2 = FullEntityExample.OBSIDIAN_GOLEM;

        // 成就进度
        SimpleAdvancementExample.register();
        FullAdvancementExample.register();

        long time = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            REGISTRYLIB
                    .item("test_item_" + i)
                    .langCn("测试物品 " + i)
                    .lang("Test Item " + i)
                    .texture(
                            () -> ImageUtil.generateIcon(ColorUtil.generateRandomVibrantColor(), ImageUtil.STAR))
                    .register();
        }
        LOGGER.info("register 1000 items in {} ms", (System.nanoTime() - time) / 1000000.0);
    }

    public RegistryLibTest(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("registrylib test mod initializing...");
        LOGGER.info(
                "Registered {} items and {} blocks",
                REGISTRYLIB.getAll(Registries.ITEM).size(),
                REGISTRYLIB.getAll(Registries.BLOCK).size());
        com.gto.registrylib.util.DistExecutor.unsafeRunWhenOn(
                net.neoforged.api.distmarker.Dist.CLIENT,
                () -> () -> modEventBus.addListener(RegistryLibTest::onRegisterLayerDefinitions));
    }

    private static void onRegisterLayerDefinitions(
                                                   net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                com.gto.registrylibtest.client.CrystalGuardianModel.LAYER_LOCATION,
                com.gto.registrylibtest.client.CrystalGuardianModel::createBodyLayer);
    }
}
